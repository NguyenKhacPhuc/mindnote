package com.mindnote.server

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database as ExposedDatabase
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class BearerAuthTest {

    private fun freshDb() {
        ExposedDatabase.connect(
            url = "jdbc:h2:mem:bearer_${System.nanoTime()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
        )
        transaction {
            SchemaUtils.create(AuthAccounts, AuthTokens)
            AuthTokens.deleteAll()
            AuthAccounts.deleteAll()
        }
    }

    private fun seedAccountAndToken(token: String, accountId: String = "acct-test", username: String = "alice_42") {
        transaction {
            AuthAccounts.insert {
                it[id] = accountId
                it[AuthAccounts.username] = username
                it[passwordHash] = "hash"
                it[createdAt] = 1L
            }
            AuthTokens.insert {
                it[AuthTokens.token] = token
                it[AuthTokens.accountId] = accountId
                it[AuthTokens.createdAt] = 1L
            }
        }
    }

    private fun Application.testSetup() {
        install(ServerContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
        installBearerAuth()
        install(StatusPages) { bearerAuth401Envelope() }
        routing {
            authenticate(BEARER_AUTH) {
                get("/whoami") { call.respondText(call.account.username) }
            }
        }
    }

    @Test
    fun `valid bearer token resolves to call dot account`() = testApplication {
        freshDb()
        seedAccountAndToken("good-token")
        application { testSetup() }
        val client = createClient { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }

        val resp = client.get("/whoami") { header(HttpHeaders.Authorization, "Bearer good-token") }

        assertEquals(HttpStatusCode.OK, resp.status)
        assertEquals("alice_42", resp.bodyAsText())
    }

    @Test
    fun `missing Authorization header gives 401 unauthenticated`() = testApplication {
        freshDb()
        application { testSetup() }
        val client = createClient { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }

        val resp = client.get("/whoami")

        assertEquals(HttpStatusCode.Unauthorized, resp.status)
        val env: ErrorEnvelope = resp.body()
        assertEquals("unauthenticated", env.error.code)
    }

    @Test
    fun `unknown bearer token gives 401 invalid_token`() = testApplication {
        freshDb()
        seedAccountAndToken("good-token")
        application { testSetup() }
        val client = createClient { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }

        val resp = client.get("/whoami") { header(HttpHeaders.Authorization, "Bearer never-issued") }

        assertEquals(HttpStatusCode.Unauthorized, resp.status)
        val env: ErrorEnvelope = resp.body()
        assertEquals("invalid_token", env.error.code)
    }
}
