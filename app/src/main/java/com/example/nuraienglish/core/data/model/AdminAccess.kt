package com.example.nuraienglish.core.data.model

object AdminAccess {
    val ownerEmails = setOf("shayne.f@mail.ru")

    fun isOwner(email: String): Boolean = ownerEmails.contains(email.trim().lowercase())

    fun isAdmin(user: User?): Boolean = user?.isAdmin == true || isOwner(user?.email.orEmpty())
}
