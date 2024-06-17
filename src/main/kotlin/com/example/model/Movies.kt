package com.example.model

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


object MovieTable : UUIDTable() {
    val imdbId = varchar("imdb_id", 50).uniqueIndex()
    val name = varchar("name", 50)
    val director = varchar("director", 50)
}

class Movies(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, Movies>(MovieTable)

    var imdbId by MovieTable.imdbId
    var name by MovieTable.name
    var director by MovieTable.director

    override fun toString(): String {
        return "Movie(imdbId=$imdbId, name='$name', director='$director')"
    }

}

data class Movie(val id: UUID, val imdbId: String, val name: String, val director: String)

class MovieService {

    fun getAll(): List<Movie> = transaction {
        Movies.all().map { toMovieDTO(it) }
    }

    fun get(id: UUID): Movie? = transaction {
        Movies.findById(id)?.let { toMovieDTO(it) }
    }

    fun create(movie: Movie): Movie = transaction {
        val newMovies = Movies.new {
            imdbId = movie.imdbId
            name = movie.name
            director = movie.director
        }
        return@transaction Movie(newMovies.id.value, newMovies.imdbId, newMovies.name, newMovies.director)
    }

    // searchString e.g. %nick%
    fun findByDirector(searchString: String): List<Movie> = transaction {
        Movies.find { MovieTable.director.lowerCase() like "%" + searchString + "%" }
            .map { movie -> toMovieDTO(movie) }
    }

    fun delete(uuid: UUID): Boolean = transaction {
        Movies.findById(uuid)?.delete()?.let { true } ?: false
    }

    fun deleteAll() = transaction {
        Movies.all().forEach { movies: Movies ->
            movies.delete()
        }
        commit()
    }

    fun update(uuid: UUID, movie: Movie): Movie? = transaction {
        val movies = Movies.findById(uuid)
        movies?.imdbId = movie.imdbId
        movies?.name = movie.name
        movies?.director = movie.director
        return@transaction movies?.let { toMovieDTO(it) }
    }

    private fun toMovieDTO(it: Movies) = Movie(it.id.value, it.imdbId, it.name, it.director)

}

