package com.mindnote.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class AuthApi(
    private val client: HttpClient,
    private val json: Json = DefaultJson,
) {

    suspend fun register(username: String, password: String): AuthResult<AuthSuccessDto> =
        callForBody("auth/register", RegisterRequest(username, password))

    private suspend inline fun <reified Req, reified Res> callForBody(
        path: String,
        body: Req,
    ): AuthResult<Res> = try {
        val response = client.post(path) { setBody(body) }
        AuthResult.Success(response.body<Res>())
    } catch (e: ClientRequestException) {
        e.toFailure()
    } catch (e: IOException) {
        AuthResult.NetworkError
    }

    private suspend fun ClientRequestException.toFailure(): AuthResult<Nothing> {
        val raw = response.bodyAsText()
        val envelope = try {
            json.decodeFromString(AuthErrorEnvelopeDto.serializer(), raw)
        } catch (_: SerializationException) {
            return AuthResult.Failure(code = "unknown", message = raw)
        }
        return AuthResult.Failure(code = envelope.error.code, message = envelope.error.message)
    }

    private companion object {
        val DefaultJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    }
}
