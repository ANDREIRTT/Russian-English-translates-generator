package db

import com.zaxxer.hikari.HikariDataSource
import csv.CsvData
import csv.CsvReader
import db.language.Language
import db.language.LanguageInserter
import db.translate.TranslateInserter
import db.word.WordInserter
import db.word.WordType

class DatabaseCreator(
    private val dataSource: HikariDataSource
) {
    fun create(onMessage: (String) -> Unit) {
        dataSource.connection.use { connection ->
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
                                onMessage("insert word ${csvData.sourceWord} error: ${e.message}")
                                throw e
                            },
                            translates = csvData.translates.map { translate ->
                                try {
                                    wordInserter.insertWord(
                                        langId = languages[Language.EN]!!,
                                        word = translate
                                    )
                                } catch (e: Exception) {
                                    onMessage(" insert word $translate error: ${e.message}")
                                    throw e
                                }
                            },
                            wordType = csvData.wordType
                        )
                    } catch (e: Exception) {
                        onMessage(" insert $csvData error: ${e.message}")
                    }
                }
            }
            listOf(
                WordType.NOUN,
                WordType.ADJECTIVE,
                WordType.VERB,
                WordType.ADVERB
            ).forEach { wordType ->
                onMessage("start reading ${wordType.name}")
                CsvReader().read(wordType, insertion)
                onMessage("end reading ${wordType.name}")
            }
        }
    }
}