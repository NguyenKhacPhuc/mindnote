package com.mindnote.server

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database as ExposedDatabase
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class AuthLogoutRouteTest {

    private val testJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun freshDb() {
        ExposedDatabase.connect(
            url = "jdbc:h2:mem:auth_logout_${System.nanoTime()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
        )
        transaction {
            SchemaUtils.create(AuthAccounts, AuthTokens)
            AuthTokens.deleteAll()
            AuthAccounts.deleteAll()
        }
    }

    @Test
    fun `204 deletes the token from the auth_tokens table`() = testApplication {
        freshDb()
        application {
            install(ServerContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { authRoutes() }
        }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        val registered: AuthSuccessDto = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(username = "alice_42", password = "Hunter2!aB"))
        }.body()
        val token = registered.token

        val logoutResp = client.post("/auth/logout") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NoContent, logoutResp.status)

        // token row is gone
        transaction {
            val count = AuthTokens.selectAll().where { AuthTokens.token eq token }.count()
            assertEquals(0, count)
        }
    }

    @Test
    fun `after logout the same token no longer resolves to its account`() = testApplication {
        freshDb()
        application {
            install(ServerContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { authRoutes() }
        }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        val registered: AuthSuccessDto = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(username = "alice_42", password = "Hunter2!aB"))
        }.body()
        val token = registered.token

        client.post("/auth/logout") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        transaction {
            assertNull(resolveAccountFromToken(token))
        }
    }

    @Test
    fun `401 invalid_token when Authorization header is missing`() = testApplication {
        freshDb()
        application {
            install(ServerContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { authRoutes() }
        }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        val resp = client.post("/auth/logout")
        assertEquals(HttpStatusCode.Unauthorized, resp.status)
        val env: ErrorEnvelope = resp.body()
        assertEquals("invalid_token", env.error.code)
    }

    @Test
    fun `401 invalid_token when token is unknown`() = testApplication {
        freshDb()
        application {
            install(ServerContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { authRoutes() }
        }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        val resp = client.post("/auth/logout") {
            header(HttpHeaders.Authorization, "Bearer never-issued-token")
        }
        assertEquals(HttpStatusCode.Unauthorized, resp.status)
        val env: ErrorEnvelope = resp.body()
        assertEquals("invalid_token", env.error.code)
    }

    @Test
    fun `logout twice with the same token returns 401 the second time`() = testApplication {
        freshDb()
        application {
            install(ServerContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
            routing { authRoutes() }
        }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        val registered: AuthSuccessDto = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(username = "alice_42", password = "Hunter2!aB"))
        }.body()
        val token = registered.token

        // first logout: 204
        val first = client.post("/auth/logout") { header(HttpHeaders.Authorization, "Bearer $token") }
        assertEquals(HttpStatusCode.NoContent, first.status)

        // second: 401 (no longer in table)
        val second = client.post("/auth/logout") { header(HttpHeaders.Authorization, "Bearer $token") }
        assertEquals(HttpStatusCode.Unauthorized, second.status)
    }
}
