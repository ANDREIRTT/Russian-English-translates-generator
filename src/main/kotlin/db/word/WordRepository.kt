package db.word

import com.zaxxer.hikari.HikariDataSource
import db.getGeneratedKey
import java.sql.Connection
import java.sql.Statement

class WordRepository(
    private val dataSource: HikariDataSource
) {
    fun insertWord(langId: Long, word: String): Long {
        dataSource.connection.use { connection ->
            return insertWordWithConnection(connection, langId, word)
        }
    }

    fun insertWordWithConnection(connection: Connection, langId: Long, word: String): Long {
        connection.prepareStatement(
            """
            INSERT OR IGNORE INTO words (word, languageId) VALUES (?, ?)
        """.trimIndent(), Statement.RETURN_GENERATED_KEYS
        ).use { statement ->
            statement.setString(1, word)
            statement.setLong(2, langId)
            val affectedRows = statement.executeUpdate()
            return if (affectedRows > 0) {
                statement.getGeneratedKey()
            } else {
                findWordId(connection, word)
            }
        }
    }

    fun findWordId(connection: Connection, word: String): Long {
        connection.prepareStatement(
            """
                    SELECT wordId FROM words WHERE word= ?
                """.trimIndent()
        ).use { statementQuery ->
            statementQuery.setString(1, word)
            statementQuery.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getLong("wordId")
                } else {
                    throw Exception("something wrong, not found word")
                }
            }
        }
    }
}