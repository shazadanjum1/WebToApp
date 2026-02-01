package com.app.styletap.webtoappconverter.models

data class User(
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var fcmToken: String = "",
    var signupDate: Any? = null,
    var lastLoginDate: Any? = null
)