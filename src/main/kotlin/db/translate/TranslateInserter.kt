package db.translate

import db.word.WordType
import java.sql.Connection
import java.sql.Statement

class TranslateInserter(
    private val connection: Connection
) {
    fun insertTranslate(sourceId: Long, translates: List<Long>, wordType: WordType) {
        connection.prepareStatement(
            """
            INSERT OR IGNORE INTO translations (sourceWordId, targetWordId, wordType) VALUES (?, ?, ?)
        """.trimIndent()
        ).use { statement ->
            translates.forEach { translate ->
                statement.apply {
                    setLong(1, sourceId)
                    setLong(2, translate)
                    setString(3, wordType.name)
                    addBatch()
                }
                statement.apply {
                    setLong(1, translate)
                    setLong(2, sourceId)
                    setString(3, wordType.name)
                    addBatch()
                }
            }
            statement.executeBatch()
        }
    }
}