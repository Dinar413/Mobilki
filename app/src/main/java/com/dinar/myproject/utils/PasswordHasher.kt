package com.dinar.myproject.utils

import java.security.MessageDigest

object PasswordHasher {
    fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
