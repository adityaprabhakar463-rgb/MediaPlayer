package com.celestial.backend.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    @Volatile
    private var database: Database? = null

    @Synchronized
    fun init(url: String, user: String, pass: String): Database {
        database?.let { return it }

        val dataSource = try {
            val config = HikariConfig().apply {
                jdbcUrl = url
                driverClassName = "org.postgresql.Driver"
                username = user
                password = pass
                maximumPoolSize = 10
                connectionTimeout = 2000
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            }
            HikariDataSource(config).also { ds ->
                ds.connection.use { } // test connection
            }
        } catch (e: Exception) {
            println("[Backend DB] PostgreSQL connection failed (Docker not running). Falling back to in-memory H2 DB.")
            val h2Config = HikariConfig().apply {
                jdbcUrl = "jdbc:h2:mem:celestial;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
                driverClassName = "org.h2.Driver"
                username = "sa"
                password = ""
                maximumPoolSize = 5
                validate()
            }
            HikariDataSource(h2Config)
        }

        try {
            val flyway = Flyway.configure().dataSource(dataSource).load()
            flyway.migrate()
        } catch (e: Exception) {
            println("[Backend DB] Flyway migration note: ${e.message}")
        }

        val db = Database.connect(dataSource)
        database = db
        return db
    }
}
