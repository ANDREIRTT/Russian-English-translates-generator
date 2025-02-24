import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import csv.CsvData
import csv.CsvReader
import db.language.Language
import db.language.LanguageInserter
import db.translate.TranslateInserter
import db.word.WordInserter
import db.word.WordType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.sql.SQLException
import kotlin.time.Duration.Companion.milliseconds

fun main() = runBlocking(Dispatchers.IO) {
    val url = "jdbc:sqlite:translates.db"
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = url
        driverClassName = "org.sqlite.JDBC"
        maximumPoolSize = 40
    }

    val dataSource = HikariDataSource(hikariConfig)
    try {
        println("Соединение с базой данных установлено.")
        val startTime = System.currentTimeMillis()

        dataSource.connection.use { connection ->
            connection.createStatement().use { stmt ->
//            stmt.execute
                stmt.execute("CREATE TABLE IF NOT EXISTS `languages` (`languageId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `code` TEXT NOT NULL)");
                stmt.execute("CREATE TABLE IF NOT EXISTS `translations` (`translationId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sourceWordId` INTEGER NOT NULL, `targetWordId` INTEGER NOT NULL, `wordType` TEXT NOT NULL, FOREIGN KEY(`sourceWordId`) REFERENCES `words`(`wordId`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`targetWordId`) REFERENCES `words`(`wordId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
                stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_translations_sourceWordId_targetWordId` ON `translations` (`sourceWordId`, `targetWordId`)");
                stmt.execute("CREATE TABLE IF NOT EXISTS `words` (`wordId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `word` TEXT NOT NULL, `languageId` INTEGER NOT NULL, FOREIGN KEY(`languageId`) REFERENCES `languages`(`languageId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
                stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_words_text` ON `words` (`word`)");
            }
            val languages = LanguageInserter(connection).generateLanguages()


            val insertion: (CsvData) -> Unit = { csvData ->
                dataSource.connection.use { connection ->
                    val wordInserter = WordInserter(connection)
                    val translateInserter = TranslateInserter(connection)
                    try {
                        translateInserter.insertTranslate(
                            sourceId = try {
                                wordInserter.insertWord(
                                    langId = languages[Language.RU]!!,
                                    word = csvData.sourceWord
                                )
                            } catch (e: Exception) {
                                println(" insert word ${csvData.sourceWord} error: ${e.message}")
                                throw e
                            },
                            translates = csvData.translates.map { translate ->
                                try {
                                    wordInserter.insertWord(
                                        langId = languages[Language.EN]!!,
                                        word = translate
                                    )
                                } catch (e: Exception) {
                                    println(" insert word $translate error: ${e.message}")
                                    throw e
                                }
                            },
                            wordType = csvData.wordType
                        )
                    } catch (e: Exception) {
                        println(" insert $csvData error: ${e.message}")
                    }
                }

            }
            CsvReader().read(WordType.NOUN, insertion)
            CsvReader().read(WordType.ADJECTIVE, insertion)
            CsvReader().read(WordType.VERB, insertion)
            CsvReader().read(WordType.ADVERB, insertion)
        }

        println("Данные успешно импортированы в SQLite файл. time s:${(System.currentTimeMillis() - startTime).milliseconds.inWholeSeconds}")
    } catch (e: SQLException) {
        println("Ошибка работы с базой данных: ${e.message}")
    } catch (e: Exception) {
        println("Общая ошибка: ${e.message}")
    } finally {
        dataSource.connection.close()
    }
}
