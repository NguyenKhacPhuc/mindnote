package com.mindnote.server

import org.jetbrains.exposed.sql.Table

data class Account(
    val id: String,
    val username: String,
    val passwordHash: String,
    val createdAt: Long,
)

object AuthAccounts : Table("auth_accounts") {
    val id = varchar("id", 64)
    val username = varchar("username", 30).uniqueIndex()
    val passwordHash = varchar("password_hash", 256)
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}
