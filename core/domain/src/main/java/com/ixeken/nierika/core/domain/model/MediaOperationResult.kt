package com.ixeken.nierika.core.domain.model

import android.content.IntentSender

/**
 * Resultado sellado para operaciones sobre archivos y metadatos multimedia.
 * 
 * Permite manejar transparentemente tanto operaciones locales directas como flujos
 * que requieren el consentimiento del usuario (IntentSender) en Android 11+ / Scoped Storage.
 */
sealed interface MediaOperationResult {
    data object Success : MediaOperationResult
    data class RequiresConsent(val intentSender: IntentSender) : MediaOperationResult
    data class Failure(val error: Throwable) : MediaOperationResult
}
