package com.example

import com.example.model.MovieService
import com.example.model.UserRatingService
import com.example.model.UserService
import com.example.model.configureValidation
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureDatabase()
    configureRouting(MovieService(), UserService(), UserRatingService())
    configureValidation()

    seedData()
}


