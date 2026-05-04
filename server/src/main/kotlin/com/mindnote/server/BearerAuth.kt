package com.mindnote.server

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer
import io.ktor.server.auth.principal
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond
import io.ktor.util.AttributeKey
import org.jetbrains.exposed.sql.transactions.transaction

/** Name of the Bearer auth provider — used in `authenticate(BEARER_AUTH) { ... }` blocks. */
const val BEARER_AUTH = "auth-bearer"

/**
 * Install Ktor's Authentication plugin with a single Bearer provider that validates the
 * incoming token via [resolveAccountFromToken]. The resolved [Account] becomes the call's
 * principal, accessible through [account].
 */
fun Application.installBearerAuth() {
    install(Authentication) {
        bearer(BEARER_AUTH) {
            realm = "MindNote"
            authenticate { credential ->
                transaction { resolveAccountFromToken(credential.token) }
            }
        }
    }
}

/**
 * Convert the framework's empty 401 (from a failed Bearer challenge) into the project's
 * error envelope, distinguishing missing-header (`unauthenticated`) from
 * present-but-bad-token (`invalid_token`).
 *
 * Call from inside an `install(StatusPages) { ... }` block.
 *
 * Note: only fires when no response body has been written yet, so endpoints that respond
 * with their own 401 (e.g. `POST /auth/login` returning `invalid_credentials`) are NOT
 * overridden.
 */
fun StatusPagesConfig.bearerAuth401Envelope() {
    status(HttpStatusCode.Unauthorized) { call, _ ->
        val hadBearer = call.request.headers[HttpHeaders.Authorization]
            ?.startsWith("Bearer ", ignoreCase = true) == true
        val (code, message) = if (hadBearer) {
            "invalid_token" to "token is unknown or revoked"
        } else {
            "unauthenticated" to "auth required"
        }
        call.respond(HttpStatusCode.Unauthorized, ErrorEnvelope(ErrorBody(code, message)))
    }
}

/** Account resolved by the Bearer auth provider on the current call. */
val ApplicationCall.account: Account
    get() = principal<Account>()
        ?: error("call.account accessed outside an authenticate(BEARER_AUTH) block")

internal val AccountAttributeKey = AttributeKey<Account>("mindnote.account")
