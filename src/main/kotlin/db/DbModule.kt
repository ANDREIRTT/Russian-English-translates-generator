package db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dbModule = module {
    single {
        val url = "jdbc:sqlite:translates.db"
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = url
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 10
        }
        HikariDataSource(hikariConfig)
    }

    factoryOf(::DatabaseCreator)
}