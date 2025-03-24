package db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import db.language.LanguageRepository
import db.preset.PresetRepository
import db.translate.TranslateInserter
import db.word.WordRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dbModule = module {
    single {
        val url = "jdbc:sqlite:C:\\Users\\Andrey\\AndroidStudioProjects\\WordsWallPaper\\app\\src\\main\\assets\\words.db"
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = url
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 10
        }
        HikariDataSource(hikariConfig)
    }

    factoryOf(::WordRepository)
    factoryOf(::TranslateInserter)
    factoryOf(::LanguageRepository)

    factoryOf(::PresetRepository)
}