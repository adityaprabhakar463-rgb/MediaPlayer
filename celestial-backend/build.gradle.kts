plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    application
}

group = "app.celestial"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val ktorVersion = "3.0.3"
val exposedVersion = "0.57.0"
val flywayVersion = "10.20.1"
val logbackVersion = "1.5.12"
val postgresVersion = "42.7.4"
val hikariVersion = "6.2.1"
val jwtVersion = "4.4.0"
val argon2Version = "2.11"

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")

    implementation("com.auth0:java-jwt:$jwtVersion")
    implementation("de.mkammerer:argon2-jvm:$argon2Version")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.1.0")
    testImplementation("org.testcontainers:postgresql:1.20.4")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
}

application {
    mainClass.set("app.celestial.backend.ApplicationKt")
}

// Avoid OneDrive/file-lock issues on Windows workspace builds
layout.buildDirectory.set(file("${System.getProperty("user.home")}/.celestial-build/celestial-backend"))


kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjdk-release=21")
    }
}

tasks.test {
    useJUnit() // kotlin-test-junit + JUnit4 style; Testcontainers can work; keep simple for M0
}
