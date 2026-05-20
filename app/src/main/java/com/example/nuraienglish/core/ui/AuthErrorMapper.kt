package com.example.nuraienglish.core.ui

import com.google.firebase.auth.FirebaseAuthException

/**
 * Maps a Firebase Auth throwable to a translated error string.
 * Call as `strings.authErrorMessage(throwable)` from a Composable that has UiStrings.
 */
fun UiStrings.authErrorMessage(e: Throwable): String {
    if (e is FirebaseAuthException) {
        return when (e.errorCode) {
            "ERROR_EMAIL_ALREADY_IN_USE"   -> errorEmailInUse
            // Newer Firebase SDK merges wrong-password + user-not-found into ERROR_INVALID_CREDENTIAL
            "ERROR_WRONG_PASSWORD",
            "ERROR_USER_NOT_FOUND",
            "ERROR_INVALID_CREDENTIAL"     -> errorWrongCredentials
            "ERROR_INVALID_EMAIL"          -> errorInvalidEmail
            "ERROR_WEAK_PASSWORD"          -> errorWeakPassword
            "ERROR_TOO_MANY_REQUESTS"      -> errorTooManyRequests
            "ERROR_NETWORK_REQUEST_FAILED" -> errorNetwork
            "ERROR_USER_DISABLED"          -> errorUserDisabled
            else                           -> errorUnknown
        }
    }
    return e.message ?: errorUnknown
}
