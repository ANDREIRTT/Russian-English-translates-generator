package db.preset

import com.zaxxer.hikari.HikariDataSource
import db.getGeneratedKey
import db.language.Language
import db.language.LanguageRepository
import db.translate.TranslateInserter
import db.word.WordRepository
import preset.PresetData

class PresetRepository(
    private val dataSource: HikariDataSource,
    private val wordRepository: WordRepository,
    private val translateInserter: TranslateInserter,
    private val languageRepository: LanguageRepository
) {
    fun insertPreset(
        presetData: PresetData,
        onError: (String) -> Unit
    ) {
        val presetId = createTable(presetData.name, presetData.level.name, presetData.category)
        dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT OR IGNORE INTO presetCrossRef (presetId, wordId, translationWordId) VALUES (?, ?, ?)")
                .use { stmt ->
                    val languages = languageRepository.getOrCreateLanguages(connection)

                    presetData.words.forEach { word ->
                        val ruWordId = (languages[Language.RU]!! to word.wordRu).let { ru ->
                            wordRepository.insertWordWithConnection(
                                connection = connection,
                                langId = ru.first,
                                word = ru.second
                            )
                        }
                        val enWordId = (languages[Language.EN]!! to word.wordEn).let { en ->
                            wordRepository.insertWordWithConnection(
                                connection = connection,
                                langId = en.first,
                                word = en.second
                            )
                        }

                        translateInserter.insertTranslate(
                            connection = connection,
                            sourceId = ruWordId,
                            translates = listOf(enWordId)
                        )

                        stmt.setLong(1, presetId)
                        stmt.setLong(2, enWordId)
                        stmt.setLong(3, ruWordId)
                        stmt.addBatch()
                    }
                    stmt.executeBatch()
                }
        }
    }

    private fun createTable(
        name: String,
        presetLevel: String,
        presetCategory: String,
    ): Long {
        dataSource.connection.use {
            it.prepareStatement("INSERT OR IGNORE INTO preset (name, presetLevel, presetCategory) VALUES (?, ?, ?)")
                .use { stmt ->
                    stmt.setString(1, name)
                    stmt.setString(2, presetLevel)
                    stmt.setString(3, presetCategory)
                    val affectedRows = stmt.executeUpdate()
                    return if (affectedRows > 0) {
                        stmt.getGeneratedKey()
                    } else {
                        throw Exception("preset name $name already exists")
                    }
                }
        }
    }
}