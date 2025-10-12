import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.androidGitVersion)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

android {
    namespace = "com.trueedu.spac"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.trueedu.spac"
        minSdk = 29
        targetSdk = 36
        versionCode = getVersionCodeProvider().get()
        versionName = androidGitVersion.name()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GOOGLE_OAUTH_CLIENT_ID",
            "\"${localProperties.getProperty("GOOGLE_OAUTH_CLIENT_ID") ?: ""}\""
        )
        resValue("string", "amplitude_api_key", "fbd90a5783ee1310d686c7d25a916a65")
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    hilt {
        enableAggregatingTask = false
    }
}

androidGitVersion {
    commitHashLength = 8
    format = "%tag%"
    prefix = ""
    untrackedIsDirty = true
}

fun getVersionCodeProvider(): Provider<Int> {
    return providers.exec {
        commandLine("git", "tag", "--list")
    }.standardOutput.asText.map { output ->
        val tagList = output.trim()
        if (tagList.isEmpty()) {
            1
        } else {
            tagList.split("\n").filter { it.isNotEmpty() }.size
        }
    }
}


// 버전 정보 확인을 위한 Gradle Task
tasks.register("printVersionInfo") {
    group = "versioning"
    description = "Print detailed version information"

    doLast {
        val tagListOutput = providers.exec {
            commandLine("git", "tag", "--list")
        }.standardOutput.asText.get()

        val tagList = tagListOutput.trim()
        val tagCount = if (tagList.isEmpty()) 1 else tagList.split("\n").filter { it.isNotEmpty() }.size

        val tagOutput = providers.exec {
            commandLine("git", "describe", "--tags", "--abbrev=0")
            isIgnoreExitValue = true
        }.standardOutput.asText.get()

        val tag = tagOutput.trim().removePrefix("v")
        val versionCode = getVersionCodeProvider().get()

        println("=== Version Information ===")
        println("Git tag: $tag")
        println("Tag count: $tagCount")
        println("Version code: $versionCode")
        println("Version name: ${androidGitVersion.name()}")
        println("========================")
    }
}

// 버전 정보 출력 태스크
tasks.register("printVersionName") {
    doLast {
        println(android.defaultConfig.versionName)
    }
}

tasks.register("printVersionCode") {
    doLast {
        println(android.defaultConfig.versionCode)
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.adaptive.navigation.suite)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.lifecycle.process)

    ksp(libs.symbol.processing.api)

    // Hilt
    implementation(libs.hilt)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.bundles.ksps)
    compileOnly(libs.ksp.gradle.plugin)

    implementation(libs.amplitude)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)

    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)

    // Credentials & Google Sign In
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.auth.ktx)
}
