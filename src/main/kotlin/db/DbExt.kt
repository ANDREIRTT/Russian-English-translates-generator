package db

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