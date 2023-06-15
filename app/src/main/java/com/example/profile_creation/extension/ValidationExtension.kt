package com.example.profile_creation.extension

import android.util.Patterns
import java.util.regex.Pattern

fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    return Pattern.compile(".{6,}").matcher(this).matches()
}

fun String.isValidUrl(): Boolean = Patterns.WEB_URL.matcher(this).matches()

