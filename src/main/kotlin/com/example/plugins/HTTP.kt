package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.hsts.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureHTTP() {
    install(HSTS) {
        includeSubDomains = true
    }
    routing {
        openAPI(path = "openapi")
        swaggerUI(path = "swagger")
    }
}
