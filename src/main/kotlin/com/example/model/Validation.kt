package com.example.model

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*


fun Application.configureValidation() {

    install(RequestValidation) {
        validate<Movie> { movie ->
            if (movie.imdbId.length <= 8)
                ValidationResult.Invalid("A imdb ID should be min 8 characters.")
            else ValidationResult.Valid
        }
    }

    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
        }
    }
}


