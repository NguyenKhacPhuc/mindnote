package com.mindnote.server

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
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
import org.jetbrains.exposed.sql.transactions.transaction

class RouteAuthGatingTest {

    private val testJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun freshDb() {
        ExposedDatabase.connect(
            url = "jdbc:h2:mem:gating_${System.nanoTime()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
        )
        transaction {
            SchemaUtils.create(AuthAccounts, AuthTokens, Users, Notes, Topics, NoteTopics, Favorites)
            AuthTokens.deleteAll()
            AuthAccounts.deleteAll()
            Notes.deleteAll()
            Users.deleteAll()
        }
    }

    /**
     * Test app that mirrors Application.module()'s gating shape but skips routes/plugins
     * that need real environment (chat provider's API key, OCR provider, etc).
     * Includes the exempt /health and /auth/register, and the gated /notes group.
     */
    private fun Application.testModule() {
        install(ServerContentNegotiation) { json(testJson) }
        install(StatusPages) { bearerAuth401Envelope() }
        installBearerAuth()
        routing {
            get("/health") { call.respondText("ok") }
            authRoutes()
            authenticate(BEARER_AUTH) {
                notesRoutes()
            }
        }
    }

    @Test
    fun `health is exempt - works with no Authorization header`() = testApplication {
        freshDb()
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        val resp = client.get("/health")
        assertEquals(HttpStatusCode.OK, resp.status)
    }

    @Test
    fun `auth register is exempt - works with no Authorization header`() = testApplication {
        freshDb()
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        val resp = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(username = "alice_42", password = "Hunter2!aB"))
        }
        assertEquals(HttpStatusCode.Created, resp.status)
    }

    @Test
    fun `notes endpoints return 401 unauthenticated when no Authorization header`() = testApplication {
        freshDb()
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        val resp = client.get("/notes")
        assertEquals(HttpStatusCode.Unauthorized, resp.status)
        val env: ErrorEnvelope = resp.body()
        assertEquals("unauthenticated", env.error.code)
    }

    @Test
    fun `notes endpoints return 401 invalid_token when Bearer token is unknown`() = testApplication {
        freshDb()
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        val resp = client.get("/notes") { header(HttpHeaders.Authorization, "Bearer never-issued") }
        assertEquals(HttpStatusCode.Unauthorized, resp.status)
        val env: ErrorEnvelope = resp.body()
        assertEquals("invalid_token", env.error.code)
    }

    @Test
    fun `notes endpoints return 200 with a valid Bearer token`() = testApplication {
        freshDb()
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        val registered: AuthSuccessDto = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(username = "alice_42", password = "Hunter2!aB"))
        }.body()

        val resp = client.get("/notes") {
            header(HttpHeaders.Authorization, "Bearer ${registered.token}")
        }
        assertEquals(HttpStatusCode.OK, resp.status)
    }

    @Test
    fun `notes DELETE also returns 401 when no token`() = testApplication {
        freshDb()
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        val resp = client.delete("/notes/some-id")
        assertEquals(HttpStatusCode.Unauthorized, resp.status)
    }
}
