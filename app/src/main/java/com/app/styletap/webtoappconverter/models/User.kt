package com.app.styletap.webtoappconverter.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val fcmToken: String = "",
    val signupDate: Any? = null,
    val lastLoginDate: Any? = null
)