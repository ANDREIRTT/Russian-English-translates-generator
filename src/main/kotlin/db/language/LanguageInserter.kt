package db.language

import db.getGeneratedKey
import java.sql.Connection
import java.sql.Statement

class LanguageInserter(
    private val connection: Connection
) {
    fun generateLanguages(): Map<Language, Long> {
        return mapOf(
            Language.RU to create("Russian", "RU"),
            Language.EN to create("English", "EN")
        )
    }

    private fun create(name: String, code: String): Long {
        connection.prepareStatement(
            """
            INSERT INTO languages (name, code) VALUES (?, ?) 
        """.trimIndent(), Statement.RETURN_GENERATED_KEYS
        ).use { statement ->
            statement.setString(1, name)
            statement.setString(2, code)

            statement.execute()

            return statement.getGeneratedKey()
        }
    }
}