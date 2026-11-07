import java.net.Socket

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.celestial.player"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.celestial.player"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        // Android emulator host alias (10.0.2.2 points to host machine localhost). Override for physical devices.
        val apiBaseUrl = (project.findProperty("celestialApiBaseUrl") as String?)
            ?: "http://10.0.2.2:8080"
        buildConfigField("String", "CELESTIAL_API_BASE_URL", "\"$apiBaseUrl\"")

        val audiusApiKey = (project.findProperty("audiusApiKey") as String?)
            ?: System.getenv("AUDIUS_API_KEY")
            ?: ""
        buildConfigField("String", "AUDIUS_API_KEY", "\"$audiusApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

val adbExecutable: String = run {
    val sdkRoots = listOfNotNull(
        System.getenv("ANDROID_HOME"),
        System.getenv("ANDROID_SDK_ROOT"),
        "${System.getProperty("user.home")}/Library/Android/sdk",
    )
    sdkRoots
        .map { file("$it/platform-tools/adb") }
        .firstOrNull { it.exists() }
        ?.absolutePath
        ?: "adb"
}

fun isBackendRunning(): Boolean {
    return try {
        val socket = Socket("127.0.0.1", 8080)
        socket.close()
        true
    } catch (_: Exception) {
        false
    }
}

val startBackendServer = tasks.register("startBackendServer") {
    group = "celestial"
    description = "Starts the backend server in the background if not already running."
    doLast {
        if (!isBackendRunning()) {
            println("[Celestial] Backend not running on port 8080. Starting backend server in background...")
            val gradlew = rootProject.file("gradlew").absolutePath
            val process = ProcessBuilder(gradlew, ":backend:run")
                .directory(rootProject.projectDir)
                .redirectOutput(ProcessBuilder.Redirect.appendTo(rootProject.file("backend.log")))
                .redirectError(ProcessBuilder.Redirect.appendTo(rootProject.file("backend.log")))
                .start()
            println("[Celestial] Backend server started (PID: ${process.pid()}). Logging to backend.log")
            repeat(12) {
                Thread.sleep(500)
                if (isBackendRunning()) {
                    println("[Celestial] Backend server successfully bound to port 8080.")
                    return@doLast
                }
            }
        } else {
            println("[Celestial] Backend server already running on port 8080.")
        }
    }
}

val adbReverse = tasks.register<Exec>("adbReverse") {
    group = "celestial"
    description = "Maps the device's localhost:8080 to the host backend over ADB."
    commandLine(adbExecutable, "reverse", "tcp:8080", "tcp:8080")
    isIgnoreExitValue = true
}

tasks.matching { it.name == "installDebug" || it.name == "installRelease" }.configureEach {
    dependsOn(startBackendServer, adbReverse)
}

tasks.withType<Test>().configureEach {
    maxHeapSize = "2g"
    jvmArgs(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED",
        "--add-opens=java.base/java.nio=ALL-UNNAMED",
        "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
        "--add-opens=java.base/java.net=ALL-UNNAMED",
    )
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.androidx.media3.exoplayer)
}
