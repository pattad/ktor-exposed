package com.example.model

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


object UserRatingsTable : UUIDTable() {
    val value = long("value")
    val film = reference("film", MovieTable)
    val user = reference("user", UserTable)
}

class UserRatings(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserRatings>(UserRatingsTable)

    var value by UserRatingsTable.value
    var film by Movies referencedOn UserRatingsTable.film
    var users by Users referencedOn UserRatingsTable.user

    override fun toString(): String {
        return "UserRating(value=$value, film=$film, user=$users)"
    }
}

data class UserRating(
    val id: UUID,
    val value: Long,
    val film: Movie,
    val user: User
)

class UserRatingService {

    fun getAll(): List<UserRating> = transaction {
        UserRatings.all().map { toUserRatingDTO(it) }
    }

    fun get(id: UUID): UserRating? = transaction {
        UserRatings.findById(id)?.let { toUserRatingDTO(it) }
    }

    fun create(userRating: UserRating): UserRating = transaction {
        val users = Users.findById(userRating.user.id)
        val film = Movies.findById(userRating.film.id)
        if (users != null && film != null) {
            val userRatings = UserRatings.new(UUID.randomUUID()) {
                value = userRating.value
                this.film = film
                this.users = users
            }
            toUserRatingDTO(userRatings)
        } else {
            throw IllegalArgumentException("Invalid user or film.")
        }
    }

    fun findByRating(rating: Long): List<UserRating> = transaction {
        val rows = (UserRatingsTable leftJoin MovieTable leftJoin UserTable)
            .selectAll()
            .where { UserRatingsTable.value eq rating }

        UserRatings.wrapRows(rows).map { userRating -> toUserRatingDTO(userRating) }
    }

    fun update(id: UUID, updateUserRating: UserRating): UserRating? = transaction {
        val userRatings = UserRatings.findById(id)
        userRatings?.let {
            it.value = updateUserRating.value
            it.film = Movies.findById(updateUserRating.film.id)!!
            it.users = Users.findById(updateUserRating.user.id)!!
        }
        userRatings?.let { toUserRatingDTO(it) }
    }

    fun delete(id: UUID): Boolean = transaction {
        val userRatings = UserRatings.findById(id)
        userRatings?.delete() != null
    }

    fun deleteAll() = transaction {
        UserRatings.all().forEach { userRatings: UserRatings -> userRatings.delete() }
    }

    private fun toUserRatingDTO(it: UserRatings) = UserRating(
        id = it.id.value,
        value = it.value,
        film = Movie(it.film.id.value, it.film.imdbId, it.film.name, it.film.director),
        user = User(it.users.id.value, it.users.name, it.users.email)
    )

}