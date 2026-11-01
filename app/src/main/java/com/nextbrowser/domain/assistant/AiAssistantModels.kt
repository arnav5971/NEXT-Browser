package com.nextbrowser.domain.assistant

/**
 * User-approved context for an AI request.
 *
 * Callers must never place cookies, authentication headers, form values,
 * password fields, or browser storage in this model.
 */
data class AiPageContext(
    val url: String,
    val title: String,
    val text: String,
    val locale: String = "en"
)

data class AiAssistantRequest(
    val question: String,
    val page: AiPageContext
)

data class AiAssistantReply(
    val answer: String,
    val requestId: String? = null
)

sealed interface AiAssistantError {
    data object NotConfigured : AiAssistantError
    data object NetworkUnavailable : AiAssistantError
    data object Unauthorized : AiAssistantError
    data object RateLimited : AiAssistantError
    data class ServerFailure(val message: String) : AiAssistantError
    data class InvalidResponse(val message: String) : AiAssistantError
}

/**
 * Backend boundary for AI features. The implementation must keep provider
 * credentials server-side and must only receive page text after user consent.
 */
interface AiAssistantRepository {
    suspend fun ask(request: AiAssistantRequest): Result<AiAssistantReply>
}
