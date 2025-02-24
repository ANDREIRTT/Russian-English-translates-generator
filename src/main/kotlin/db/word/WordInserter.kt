package db.word

import db.getGeneratedKey
import java.sql.Connection
import java.sql.Statement

class WordInserter(
    private val connection: Connection
) {
    fun insertWord(langId: Long, word: String): Long {
        connection.prepareStatement(
            """
            INSERT OR IGNORE INTO words (word, languageId) VALUES (?, ?)
        """.trimIndent(), Statement.RETURN_GENERATED_KEYS
        ).use { statement ->
            statement.setString(1, word)
            statement.setLong(2, langId)
            val affectedRows = statement.executeUpdate()
            if (affectedRows > 0) {
                return statement.getGeneratedKey()
            } else {
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
    }
}