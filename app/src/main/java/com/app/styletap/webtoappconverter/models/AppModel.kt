package com.app.styletap.webtoappconverter.models

import com.google.firebase.Timestamp


data class AppModel(

    // IDs
    val id: String? = null,
    val userId: String? = null,
    val guestId: String? = null,

    // App Info
    val appName: String? = null,
    val appIconUrl: String? = null,
    val packageName: String? = null,
    val appVersion: String? = "1",
    val category: String? = null,
    val description: String? = null,
    val websiteUrl: String? = null,
    val apkUrl: String? = null,
    val bundleUrl: String? = null,
    val splashScreenUrl: String? = null,

    // Colors & Branding
    val primaryColor: String? = "#FFFFFF",
    val secondaryColor: String? = "#FFFFFF",
    val brandColor: String? = "#FFFFFF",

    // Ads
    val enableAds: Boolean = false,
    val admobAppId: String? = null,
    val bannerAdId: String? = null,
    val interstitialAdId: String? = null,
    val paidApp: Boolean = false,

    // Features
    val allowFileDownloads: Boolean = true,
    val enablePullToRefresh: Boolean = true,
    val enablePushNotifications: Boolean = false,
    val enableDesktopView: Boolean = false,
    val enableLoader: Boolean = true,
    val enableOfflineCache: Boolean = false,
    val darkModeEnabled: Boolean = false,
    val fullScreenMode: Boolean = false,
    val navigationButtons: Boolean = false,
    val openExternalInBrowser: Boolean = false,
    val showNoInternetAlert: Boolean = true,
    val urlValidationEnabled: Boolean = true,

    // Orientation & Platform
    val orientation: String? = null,

    // External Services
    val fcmToken: String? = null,
    val googleServicesUrl: String? = null,

    // Status & Errors
    val status: String? = "DRAFT",
    val errorMessage: String? = null,

    // Timestamps
    /*val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val processingStartedAt: Long = 0L*/

    val createdAt: Any? = null,
    val updatedAt: Any? = null,
    val processingStartedAt: Any? = null
)