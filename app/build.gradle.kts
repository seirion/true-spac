import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.io.FileInputStream
import java.util.Properties

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
        versionName = getVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GOOGLE_OAUTH_CLIENT_ID",
            "\"${localProperties.getProperty("GOOGLE_OAUTH_CLIENT_ID") ?: ""}\""
        )
        resValue("string", "amplitude_api_key", "fbd90a5783ee1310d686c7d25a916a65")

        resValue("string", "admob_id", "ca-app-pub-3613096182343800~6316364040")
        resValue("string", "native_ad_unit_id", "ca-app-pub-3613096182343800/9414134829")
    }

    signingConfigs {
        named("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        register("release") {
            val props = gradleLocalProperties(rootDir, providers)
            storeFile = file(props.getProperty("STORE_FILE") ?: System.getenv("RELEASE_STORE_FILE") ?: "release.keystore")
            storePassword = props.getProperty("STORE_PASSWORD") ?: System.getenv("RELEASE_STORE_PASSWORD") ?: ""
            keyAlias = props.getProperty("KEY_ALIAS") ?: System.getenv("RELEASE_KEY_ALIAS") ?: ""
            keyPassword = props.getProperty("KEY_PASSWORD") ?: System.getenv("RELEASE_KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        debug {
			versionNameSuffix = "-DEV"
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
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

fun getVersionName(): String {
    return try {
        val tagOutput = providers.exec {
            commandLine("git", "describe", "--tags", "--abbrev=0")
            isIgnoreExitValue = true
        }.standardOutput.asText.get()

        val tag = tagOutput.trim().removePrefix("v")
        val version = if (tag.isNotEmpty()) tag else "0.0.1"
        println("App versionName: $version")
        version
    } catch (e: Exception) {
        println("App versionName: 0.0.1 (fallback)")
        "0.0.1"
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
        println("Version name: ${getVersionName()}")
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

// KSP가 특정 환경에서 generated/ksp/<variant>/kotlin 디렉터리를 생성하지 못하는 경우가 있어,
// downstream 작업에서 NoSuchFileException이 발생할 수 있다. (빈 디렉터리라도 보장)
tasks.matching { it.name.startsWith("ksp") && it.name.endsWith("Kotlin") }.configureEach {
    doFirst {
        val variantName = name.removePrefix("ksp").removeSuffix("Kotlin")
        val variantDir = variantName.replaceFirstChar { it.lowercase() }
        file("$buildDir/generated/ksp/$variantDir/kotlin").mkdirs()
        file("$buildDir/generated/ksp/$variantDir/java").mkdirs()
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.adaptive.navigation.suite)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
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

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.kotlinx.coroutines.guava)

    // Credentials & Google Sign In
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.auth)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    implementation(platform(libs.coil.bom))
    implementation(libs.coil.compose)

    implementation(libs.markwon.core)
    implementation(libs.markwon.ext.latex)

    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    debugImplementation(libs.chucker.debug)
    releaseImplementation(libs.chucker.release)
    implementation(libs.okhttp.logging.interceptor)
    debugImplementation(libs.flipper)
    debugImplementation(libs.flipper.network.plugin)
    releaseImplementation(libs.flipper.noop)

    // Google Mobile Ads (AdMob)
    implementation(libs.play.services.ads)

    // ConstraintLayout
    implementation(libs.androidx.constraintlayout)

    // CardView
    implementation(libs.androidx.cardview)
}
