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
        stmt.execute("CREATE TABLE IF NOT EXISTS `translations` (`sourceWordId` INTEGER NOT NULL, `targetWordId` INTEGER NOT NULL, PRIMARY KEY(`sourceWordId`, `targetWordId`), FOREIGN KEY(`sourceWordId`) REFERENCES `words`(`wordId`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`targetWordId`) REFERENCES `words`(`wordId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_translations_sourceWordId_targetWordId` ON `translations` (`sourceWordId`, `targetWordId`)")
        stmt.execute("CREATE INDEX IF NOT EXISTS `index_translations_sourceWordId` ON `translations` (`sourceWordId`)")
        stmt.execute("CREATE INDEX IF NOT EXISTS `index_translations_targetWordId` ON `translations` (`targetWordId`)")
        stmt.execute("CREATE TABLE IF NOT EXISTS `words` (`wordId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `word` TEXT NOT NULL, `languageId` INTEGER NOT NULL, FOREIGN KEY(`languageId`) REFERENCES `languages`(`languageId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_words_word` ON `words` (`word`)")
        stmt.execute("CREATE INDEX IF NOT EXISTS `index_words_languageId` ON `words` (`languageId`)")
        stmt.execute("CREATE TABLE IF NOT EXISTS `preset` (`presetId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `presetLevel` TEXT NOT NULL, `presetCategory` TEXT NOT NULL)")
        stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_preset_name_presetLevel_presetCategory` ON `preset` (`name`, `presetLevel`, `presetCategory`)")
        stmt.execute("CREATE TABLE IF NOT EXISTS `presetCrossRef` (`presetId` INTEGER NOT NULL, `wordId` INTEGER NOT NULL, `translationWordId` INTEGER NOT NULL, PRIMARY KEY(`presetId`, `wordId`), FOREIGN KEY(`presetId`) REFERENCES `preset`(`presetId`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`wordId`) REFERENCES `words`(`wordId`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`translationWordId`) REFERENCES `words`(`wordId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_presetCrossRef_presetId_wordId` ON `presetCrossRef` (`presetId`, `wordId`)")
        stmt.execute("CREATE INDEX IF NOT EXISTS `index_presetCrossRef_presetId` ON `presetCrossRef` (`presetId`)")
        stmt.execute("CREATE INDEX IF NOT EXISTS `index_presetCrossRef_wordId` ON `presetCrossRef` (`wordId`)")
        stmt.execute("CREATE INDEX IF NOT EXISTS `index_presetCrossRef_translationWordId` ON `presetCrossRef` (`translationWordId`)")
    }
}