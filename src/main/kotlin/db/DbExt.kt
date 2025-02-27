package db

import com.zaxxer.hikari.HikariDataSource
import java.sql.PreparedStatement

fun PreparedStatement.getGeneratedKey(): Long {
    return generatedKeys.use { keys ->
        if (keys.next()) {
            val genId = keys.getLong(1)
            genId
        } else {
            throw Exception("Ошибка: не удалось получить сгенерированный ID.")
        }
    }
}

fun HikariDataSource.initDb() {
    connection.createStatement().use { stmt ->
        stmt.execute("CREATE TABLE IF NOT EXISTS `languages` (`languageId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `code` TEXT NOT NULL)")
        stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_languages_code` ON `languages` (`code`)")
        stmt.execute("CREATE TABLE IF NOT EXISTS `translations` (`translationId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sourceWordId` INTEGER NOT NULL, `targetWordId` INTEGER NOT NULL, `wordType` TEXT NOT NULL, FOREIGN KEY(`sourceWordId`) REFERENCES `words`(`wordId`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`targetWordId`) REFERENCES `words`(`wordId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_translations_sourceWordId_targetWordId` ON `translations` (`sourceWordId`, `targetWordId`)")
        stmt.execute("CREATE TABLE IF NOT EXISTS `words` (`wordId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `word` TEXT NOT NULL, `languageId` INTEGER NOT NULL, FOREIGN KEY(`languageId`) REFERENCES `languages`(`languageId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_words_text` ON `words` (`word`)");
    }
}