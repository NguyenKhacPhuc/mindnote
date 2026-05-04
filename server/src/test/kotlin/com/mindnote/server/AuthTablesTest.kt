package com.mindnote.server

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database as ExposedDatabase
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class AuthTablesTest {

    private fun withDb(block: () -> Unit) {
        ExposedDatabase.connect(
            url = "jdbc:h2:mem:test_${System.nanoTime()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
        )
        transaction {
            SchemaUtils.create(AuthAccounts)
            block()
        }
    }

    @Test
    fun `insert + select round-trips an account`() = withDb {
        AuthAccounts.insert {
            it[id] = "acct-1"
            it[username] = "alice_42"
            it[passwordHash] = "argon2id\$v=19\$m=65536,t=3,p=4\$abc\$def"
            it[createdAt] = 1_700_000_000L
        }

        val row = AuthAccounts.selectAll().where { AuthAccounts.id eq "acct-1" }.single()
        assertEquals("acct-1", row[AuthAccounts.id])
        assertEquals("alice_42", row[AuthAccounts.username])
        assertEquals("argon2id\$v=19\$m=65536,t=3,p=4\$abc\$def", row[AuthAccounts.passwordHash])
        assertEquals(1_700_000_000L, row[AuthAccounts.createdAt])
    }

    @Test
    fun `unique username constraint rejects duplicates`() = withDb {
        AuthAccounts.insert {
            it[id] = "acct-1"
            it[username] = "alice_42"
            it[passwordHash] = "hash1"
            it[createdAt] = 1L
        }
        assertFailsWith<ExposedSQLException> {
            AuthAccounts.insert {
                it[id] = "acct-2"
                it[username] = "alice_42"
                it[passwordHash] = "hash2"
                it[createdAt] = 2L
            }
        }
    }

    @Test
    fun `password_hash column accepts argon2id-encoded strings up to 256 chars`() = withDb {
        val longHash = "argon2id\$v=19\$m=65536,t=3,p=4\$" + "a".repeat(220)
        assertEquals(true, longHash.length >= 250)

        AuthAccounts.insert {
            it[id] = "acct-1"
            it[username] = "bob"
            it[passwordHash] = longHash
            it[createdAt] = 1L
        }
        val row = AuthAccounts.selectAll().where { AuthAccounts.id eq "acct-1" }.single()
        assertNotNull(row[AuthAccounts.passwordHash])
        assertEquals(longHash, row[AuthAccounts.passwordHash])
    }
}
