package com.example.plugins

import com.example.model.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureRouting(
    movieService: MovieService,
    userService: UserService,
    userRatingService: UserRatingService
) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        route("/movies") {
            get {
                call.respond(movieService.getAll())
            }
            get("/{id}") {
                val id = UUID.fromString(call.parameters["id"])
                val movie = movieService.get(id)
                if (movie == null) call.respond(HttpStatusCode.NotFound)
                else call.respond(movie)
            }
            get("/director/{searchString}") {
                val searchString = call.parameters["searchString"]
                searchString?.let {
                    val movies = movieService.findByDirector(it)
                    call.respond(movies)
                } ?: run {
                    call.respondText("Missing or malformed searchString", status = HttpStatusCode.BadRequest)
                }
            }

            post {
                val movie = call.receive<Movie>()
                call.respond(HttpStatusCode.Created, movieService.create(movie))
            }
            delete("/{id}") {
                val id = UUID.fromString(call.parameters["id"])
                if (movieService.delete(id)) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound)
            }
            put("/{id}") {
                val movie = call.receive<Movie>()
                val id = UUID.fromString(call.parameters["id"])
                val updated = movieService.update(id, movie)
                if (updated == null) call.respond(HttpStatusCode.NotFound)
                else call.respond(HttpStatusCode.OK, updated)
            }
        }

        route("/users") {
            get {
                call.respond(userService.getAll())
            }
            get("/{id}") {
                val id = call.parameters["id"].orEmpty()
                val user = userService.get(UUID.fromString(id))
                if (user == null) call.respond(HttpStatusCode.NotFound)
                else call.respond(user)
            }
            post {
                val user = call.receive<User>()
                call.respond(HttpStatusCode.Created, userService.create(user))
            }
            delete("/{id}") {
                val id = call.parameters["id"].orEmpty()
                if (userService.delete(UUID.fromString(id))) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound)
            }
            put("/{id}") {
                val user = call.receive<User>()
                val id = call.parameters["id"].orEmpty()
                val updated = userService.update(UUID.fromString(id), user)
                if (updated == null) call.respond(HttpStatusCode.NotFound)
                else call.respond(HttpStatusCode.OK, updated)
            }
        }
        route("/userRatings") {
            get {
                call.respond(userRatingService.getAll())
            }
            get("/{id}") {
                val id = UUID.fromString(call.parameters["id"])
                val userRating = userRatingService.get(id)
                if (userRating == null) call.respond(HttpStatusCode.NotFound)
                else call.respond(userRating)
            }
            post {
                val userRating = call.receive<UserRating>()
                val created = userRatingService.create(userRating)
                if (created == null) call.respond(HttpStatusCode.BadRequest, "Invalid film or user ID.")
                else call.respond(HttpStatusCode.Created, created)
            }
            delete("/{id}") {
                val id = UUID.fromString(call.parameters["id"])
                if (userRatingService.delete(id)) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound)
            }
            put("/{id}") {
                val userRating = call.receive<UserRating>()
                val id = UUID.fromString(call.parameters["id"])
                val updated = userRatingService.update(id, userRating)
                if (updated == null) call.respond(HttpStatusCode.NotFound)
                else call.respond(HttpStatusCode.OK, updated)
            }
            get("/byrating/{rating}") {
                val ratingString = call.parameters["rating"]
                ratingString?.let {
                    val rating = it.toLongOrNull()
                    if (rating != null) {
                        val userRatings = userRatingService.findByRating(rating)
                        call.respond(userRatings)
                    } else {
                        call.respondText("Rating must be a valid number.", status = HttpStatusCode.BadRequest)
                    }
                } ?: run {
                    call.respondText("Missing or malformed rating.", status = HttpStatusCode.BadRequest)
                }
            }
        }
    }
}
