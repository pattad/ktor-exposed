package com.example.model

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object UserTable : UUIDTable() {
    val name = varchar("name", 50)
    val email = varchar("email", 50)
}

class Users(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Users>(UserTable)

    var name by UserTable.name
    var email by UserTable.email

    override fun toString(): String {
        return "User(id=$id, name='$name', email='$email')"
    }
}

data class User(
    val id: UUID,
    val name: String,
    val email: String
)

class UserService {

    fun getAll(): List<User> = transaction {
        Users.all().map { toUserDTO(it) }
    }

    private fun toUserDTO(it: Users) = User(it.id.value, it.name, it.email)

    fun get(id: UUID): User? = transaction {
        Users.findById(id)?.let { toUserDTO(it) }
    }

    fun create(user: User): User = transaction {
        val users = Users.new(UUID.randomUUID()) {
            name = user.name
            email = user.email
        }
        User(users.id.value, users.name, users.email)
    }

    fun update(id: UUID, updateUser: User): User? = transaction {
        val users = Users.find { UserTable.id eq id }.singleOrNull()
        if (users != null) {
            users.name = updateUser.name
            users.email = updateUser.email
        }
        users?.let { User(it.id.value, it.name, it.email) }
    }

    fun delete(id: UUID): Boolean = transaction {
        val users = Users.find { UserTable.id eq id }.singleOrNull()
        users?.delete() != null
    }
}



