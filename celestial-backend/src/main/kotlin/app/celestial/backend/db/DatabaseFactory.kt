package app.celestial.backend.db

import app.celestial.backend.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

object DatabaseFactory {
    private var dataSource: HikariDataSource? = null

    fun init(config: AppConfig): Database {
        val ds = hikari(config)
        dataSource = ds
        Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/migration")
            .load()
            .migrate()
        return Database.connect(ds)
    }

    fun init(dataSource: DataSource): Database {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()
        return Database.connect(dataSource)
    }

    fun close() {
        dataSource?.close()
        dataSource = null
    }

    private fun hikari(config: AppConfig): HikariDataSource {
        val hc = HikariConfig().apply {
            jdbcUrl = config.databaseUrl
            username = config.databaseUser
            password = config.databasePassword
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(hc)
    }
}
