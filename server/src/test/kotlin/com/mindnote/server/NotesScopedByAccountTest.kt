package com.mindnote.server

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database as ExposedDatabase
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

class NotesScopedByAccountTest {

    private val testJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun freshDb() {
        ExposedDatabase.connect(
            url = "jdbc:h2:mem:scoped_${System.nanoTime()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
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

    private fun Application.testModule() {
        install(ServerContentNegotiation) { json(testJson) }
        install(StatusPages) { bearerAuth401Envelope() }
        installBearerAuth()
        routing {
            authRoutes()
            authenticate(BEARER_AUTH) {
                notesRoutes()
            }
        }
    }

    @Test
    fun `two accounts see disjoint notes lists`() = testApplication {
        freshDb()
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        // register account A and B
        val a: AuthSuccessDto = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(username = "alice_42", password = "Hunter2!aB"))
        }.body()
        val b: AuthSuccessDto = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(username = "bob_99", password = "Different9!bC"))
        }.body()

        // A creates a note
        val createResp = client.post("/notes") {
            header(HttpHeaders.Authorization, "Bearer ${a.token}")
            contentType(ContentType.Application.Json)
            setBody(NoteCreateDto(title = "alice's secret", body = "do not read"))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)

        // B's listing is empty
        val bListResp = client.get("/notes") { header(HttpHeaders.Authorization, "Bearer ${b.token}") }
        assertEquals(HttpStatusCode.OK, bListResp.status)
        val bList: List<NoteDto> = bListResp.body()
        assertEquals(0, bList.size)

        // A's listing contains A's note
        val aListResp = client.get("/notes") { header(HttpHeaders.Authorization, "Bearer ${a.token}") }
        assertEquals(HttpStatusCode.OK, aListResp.status)
        val aList: List<NoteDto> = aListResp.body()
        assertEquals(1, aList.size)
        assertEquals("alice's secret", aList.single().title)
    }

    @Test
    fun `cross-account read by id returns 404 (not 200)`() = testApplication {
        freshDb()
        application { testModule() }
        val client = createClient { install(ContentNegotiation) { json(testJson) } }

        val a: AuthSuccessDto = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(username = "alice_42", password = "Hunter2!aB"))
        }.body()
        val b: AuthSuccessDto = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(username = "bob_99", password = "Different9!bC"))
        }.body()

        // A creates a note
        val created: NoteDto = client.post("/notes") {
            header(HttpHeaders.Authorization, "Bearer ${a.token}")
            contentType(ContentType.Application.Json)
            setBody(NoteCreateDto(title = "private", body = "x"))
        }.body()
        assertTrue(created.id.isNotBlank())

        // B can't read it
        val bRead = client.get("/notes/${created.id}") {
            header(HttpHeaders.Authorization, "Bearer ${b.token}")
        }
        assertEquals(HttpStatusCode.NotFound, bRead.status)

        // A can
        val aRead = client.get("/notes/${created.id}") {
            header(HttpHeaders.Authorization, "Bearer ${a.token}")
        }
        assertEquals(HttpStatusCode.OK, aRead.status)
    }
}
