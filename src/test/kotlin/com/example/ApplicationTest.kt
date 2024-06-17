package com.example

import com.example.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.test.*

class ApplicationTest {

    @BeforeTest
    fun beforeTest() {

        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.drop(UserRatingsTable, MovieTable, UserTable)
            SchemaUtils.create(MovieTable, UserTable, UserRatingsTable)
        }

    }

    @Test
    fun testRoot() = testApplication {
        val response = client.get("/")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello World!", response.bodyAsText())

    }

    @Test
    fun testGetMovieById() = testApplication {

        // Presuming a movie with this id exists
        var id: UUID? = null

        transaction {
            val movie = Movies.new {
                name = "Oceans Eleven"
                imdbId = "tt0054135"
                director = "Lewis Milestone"
            }

            id = movie.id.value
        }


        val response = client.get("/movies/${id}")

        assertEquals(HttpStatusCode.OK, response.status)
        val movie: Movie? = Gson().fromJson(response.bodyAsText(), Movie::class.java)
        assertEquals("Oceans Eleven", movie?.name)

    }

    @Test
    fun testCreateMovie() = testApplication {
        val movie =
            Movie(id = UUID.randomUUID(), imdbId = "tt0249462", name = "Billy Eillot", director = "Stephen Daldry")

        val response = client.post("/movies") {
            contentType(ContentType.Application.Json)
            setBody(Gson().toJson(movie))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val result: Movie? = Gson().fromJson(response.bodyAsText(), Movie::class.java)
        assertNotNull(result)
        assertContains("Billy Eillot", result.name)

    }

    @Test
    fun testFindMovieByDirector() = testApplication {
        transaction {
            Movies.new {
                name = "Top Gun Maverick"
                imdbId = "tt1745960"
                director = "Joseph Kosinski"
            }
            commit()
        }
        val response = client.get("/movies/director/kosi")

        assertEquals(HttpStatusCode.OK, response.status)
        val userListType = object : TypeToken<List<Movie>>() {}.type
        val movieList: List<Movie> = Gson().fromJson(response.bodyAsText(), userListType)
        assertEquals(1, movieList.size)

    }
}
