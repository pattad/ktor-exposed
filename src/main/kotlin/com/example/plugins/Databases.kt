package com.example.plugins

import com.example.model.MovieTable
import com.example.model.UserRatingsTable
import com.example.model.UserTable
import io.ktor.server.application.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    connectToPostgres(embedded = true)

    transaction {
        SchemaUtils.create(MovieTable, UserTable, UserRatingsTable)

    }

}

/**
 * Makes a connection to a Postgres database.
 *
 * In order to connect to your running Postgres process,
 * please specify the following parameters in your configuration file:
 * - postgres.url -- Url of your running database process.
 * - postgres.user -- Username for database connection
 * - postgres.password -- Password for database connection
 *
 * If you don't have a database process running yet, you may need to [download]((https://www.postgresql.org/download/))
 * and install Postgres and follow the instructions [here](https://postgresapp.com/).
 * Then, you would be able to edit your url,  which is usually "jdbc:postgresql://host:port/database", as well as
 * user and password values.
 *
 *
 * @param embedded -- if [true] defaults to an embedded database for tests that runs locally in the same process.
 * In this case you don't have to provide any parameters in configuration file, and you don't have to run a process.
 *
 * */
fun Application.connectToPostgres(embedded: Boolean) {
    if (embedded) {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    } else {
        val url = environment.config.property("postgres.url").getString()
        val user = environment.config.property("postgres.user").getString()
        val password = environment.config.property("postgres.password").getString()

        Database.connect(url, driver = "org.postgresql.Driver", user, password)
    }
}
