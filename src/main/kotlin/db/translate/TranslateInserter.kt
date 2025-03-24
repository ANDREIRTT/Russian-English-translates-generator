package db.translate

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

class TranslateInserter {
    fun insertTranslate(connection: Connection, sourceId: Long, translates: List<Long>) {
        connection.prepareStatement(
            """
            INSERT OR IGNORE INTO translations (sourceWordId, targetWordId) VALUES (?, ?)
        """.trimIndent()
        ).use { statement ->
            translates.forEach { translate ->
                statement.apply {
                    setLong(1, sourceId)
                    setLong(2, translate)
                    addBatch()
                }
                statement.apply {
                    setLong(1, translate)
                    setLong(2, sourceId)
                    addBatch()
                }
            }
            statement.executeBatch()
        }
    }
}