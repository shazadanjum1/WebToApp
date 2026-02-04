import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-kapt")
}

android {
    namespace = "com.app.styletap.webtoappconverter"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.webtoapp.converter"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            resValue("string", "appId", "ca-app-pub-3940256099942544~3347511713")
            resValue("string", "appOpenId", "ca-app-pub-3940256099942544/9257395921")
            resValue("string", "homeBannerId", "ca-app-pub-3940256099942544/9214589741")
            resValue("string", "createappBannerId", "ca-app-pub-3940256099942544/9214589741")

            resValue("string", "interstitialId", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "nativeId", "ca-app-pub-3940256099942544/2247696110")


            resValue("string", "splashInterstitialId", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "buildAppInterstitialId", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "downloadApkInterstitialId", "ca-app-pub-3940256099942544/1033173712")

            resValue("string", "languageNativeId", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "onboardingNativeId", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "createAppScreenNativeId", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "myAppScreenNativeId", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "generateAppScreenNativeId", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "servicesScreenNativeId", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "turtorialsScreenNativeId", "ca-app-pub-3940256099942544/2247696110")


            isShrinkResources = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {

            resValue("string", "appId", "ca-app-pub-5471933816484694~8077984612")
            resValue("string", "appOpenId", "ca-app-pub-5471933816484694/6127008072")
            resValue("string", "homeBannerId", "ca-app-pub-5471933816484694/4322971812")
            resValue("string", "createappBannerId", "ca-app-pub-5471933816484694/4322971812")

            resValue("string", "splashInterstitialId", "ca-app-pub-5471933816484694/4138739605")
            resValue("string", "buildAppInterstitialId", "ca-app-pub-5471933816484694/2658536357")
            resValue("string", "downloadApkInterstitialId", "ca-app-pub-5471933816484694/1881415382")

            resValue("string", "languageNativeId", "ca-app-pub-5471933816484694/9199494596")
            resValue("string", "onboardingNativeId", "ca-app-pub-5471933816484694/2658136971")
            resValue("string", "createAppScreenNativeId", "ca-app-pub-5471933816484694/9874681391")
            resValue("string", "myAppScreenNativeId", "ca-app-pub-5471933816484694/6645882824")
            resValue("string", "generateAppScreenNativeId", "ca-app-pub-5471933816484694/5260249585")
            resValue("string", "servicesScreenNativeId", "ca-app-pub-5471933816484694/7133742061")
            resValue("string", "turtorialsScreenNativeId", "ca-app-pub-5471933816484694/5820660395")


            isShrinkResources = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.intuit.ssp:ssp-android:1.1.1")

    // firebase
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-storage")

    implementation("com.github.bumptech.glide:glide:5.0.5")
    kapt ("com.github.bumptech.glide:compiler:4.15.1")

    //Color Picker
    implementation ("com.github.QuadFlask:colorpicker:0.0.15")

    // billing
    implementation("com.android.billingclient:billing:8.1.0")
    implementation("com.google.guava:guava:31.1-android")

    // ads
    implementation("com.google.android.gms:play-services-ads:24.4.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")

    // lifecycle
    implementation ("androidx.lifecycle:lifecycle-process:2.8.3")
    implementation ("androidx.lifecycle:lifecycle-common-java8:2.8.3")

    //shimmar
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // Lottie Animation
    implementation("com.airbnb.android:lottie:6.5.0")
}