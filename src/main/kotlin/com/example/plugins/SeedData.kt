package com.example.plugins

import com.example.model.Movies
import com.example.model.Users
import com.example.model.UserRatings
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.seedData() {

    transaction {

        val theNotebook = Movies.new {
            name = "The Notebook"
            imdbId = "tt0332280"
            director = "Nick Cassavetes"
        }
        println("id: " + theNotebook.id.value) //Reading the ID causes a flush

        val users1 = Users.new {
            name = "Mike Shields"
            email = "test@you.com"
        }

        UserRatings.new {
            value = 5
            film = theNotebook
            users = users1
        }
        commit()
    }


}