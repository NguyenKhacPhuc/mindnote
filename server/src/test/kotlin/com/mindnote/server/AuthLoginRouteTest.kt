package com.mindnote.server

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database as ExposedDatabase
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class AuthLoginRouteTest {

    private val testJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun freshDb() {
        ExposedDatabase.connect(
            url = "jdbc:h2:mem:auth_login_${System.nanoTime()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
        )
        transaction {
            SchemaUtils.create(AuthAccounts, AuthTokens)
            AuthTokens.deleteAll()
            AuthAccounts.deleteAll()
        }
    }

    @Test
    fun `200 returns a fresh token and the account when credentials are valid`() = testApplication {
        freshDb()
        application {
            install(ServerContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { authRoutes() }
        }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        // create the account first via /register
        val registered: AuthSuccessDto = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(username = "alice_42", password = "Hunter2!aB"))
        }.body()

        // log in
        val loginResp = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(username = "alice_42", password = "Hunter2!aB"))
        }
        assertEquals(HttpStatusCode.OK, loginResp.status)
        val loginBody: AuthSuccessDto = loginResp.body()
        assertEquals(registered.account.id, loginBody.account.id)
        assertEquals("alice_42", loginBody.account.username)
        assertNotNull(loginBody.token)
        assertTrue(loginBody.token.isNotBlank())
        // freshly minted (not the same token returned on register)
        assertNotEquals(registered.token, loginBody.token)

        // both tokens are persisted (multi-device support per D6)
        transaction {
            val count = AuthTokens.selectAll().where { AuthTokens.accountId eq registered.account.id }.count()
            assertEquals(2, count)
        }
    }

    @Test
    fun `401 invalid_credentials when password is wrong`() = testApplication {
        freshDb()
        application {
            install(ServerContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { authRoutes() }
        }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(username = "alice_42", password = "Hunter2!aB"))
        }

        val loginResp = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(username = "alice_42", password = "wrong-password"))
        }
        assertEquals(HttpStatusCode.Unauthorized, loginResp.status)
        val env: ErrorEnvelope = loginResp.body()
        assertEquals("invalid_credentials", env.error.code)
    }

    @Test
    fun `401 invalid_credentials when username is unknown (same body as wrong password)`() = testApplication {
        freshDb()
        application {
            install(ServerContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { authRoutes() }
        }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        // no account registered

        val loginResp = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(username = "ghost_user", password = "Anything9!aB"))
        }
        assertEquals(HttpStatusCode.Unauthorized, loginResp.status)
        val env: ErrorEnvelope = loginResp.body()
        assertEquals("invalid_credentials", env.error.code)
    }

    @Test
    fun `username lookup is case-insensitive`() = testApplication {
        freshDb()
        application {
            install(ServerContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { authRoutes() }
        }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(username = "alice_42", password = "Hunter2!aB"))
        }

        // input has uppercase — server normalizes to lowercase before lookup
        val loginResp = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(username = "ALICE_42", password = "Hunter2!aB"))
        }
        assertEquals(HttpStatusCode.OK, loginResp.status)
    }
}
