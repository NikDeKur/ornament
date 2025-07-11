@file:OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.licenser)
    id("maven-publish")
}

group = "dev.nikdekur"
version = "1.2.0"

val authorId: String by project
val authorName: String by project

kotlin {
    explicitApi()

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
            freeCompilerArgs.addAll("-Xno-param-assertions", "-Xno-call-assertions")
        }
    }

    // iOS
    // iosX64()
    // iosArm64()
    // iosSimulatorArm64()

    // Desktop
    // mingwX64()
    // linuxX64()
    // linuxArm64()
    // macosX64()
    // macosArm64()

    // Web
    js {
        outputModuleName = project.name
        browser()
        nodejs()
    }

    wasmJs {
        outputModuleName = project.name + "Wasm"
        browser()
        nodejs()
    }

    sourceSets {

        commonMain.dependencies {

            // Kotlin require both api and compileOnly for some targets

            val dependencies = listOf(
                libs.kotlinx.coroutines.core,
                libs.kotlinx.coroutines.test,
                libs.kotlinx.serialization.core,
                libs.kotlinx.serialization.json,
                libs.kotlinx.serialization.properties,
                libs.kotlinx.datetime,
                libs.kotlinx.io.core,
                libs.kotlin.logging,
                libs.ndkore,
                libs.stately.concurrency,
                libs.stately.concurrent.collections,
                libs.bignum,
                libs.yamlkt,
                libs.koin
            ).forEach {
                compileOnly(it)
                api(it)
            }
        }

        jvmMain.dependencies {
            compileOnly(libs.slf4j.api)

            compileOnly(libs.kotlinx.serialization.barray)

            // MONGODB
            compileOnly(libs.mongodb)

            // CERTIFICATES
            compileOnly(libs.bouncycastle.prov)
            compileOnly(libs.bouncycastle.pkix)

            // SERIAL - GSON
            compileOnly(libs.gson)

            compileOnly(libs.google.guava)
        }

        commonTest.dependencies {
            implementation(libs.slf4j.api)
            implementation(libs.kotlin.logging)
            implementation(libs.bignum)


            implementation(libs.kotlinx.serialization.barray)

            implementation(kotlin("test"))

            // Logback is not supported on jdk-8
            implementation(libs.slf4j.simple)
            implementation(libs.koin)
            implementation(libs.google.guava)
            implementation(libs.gson)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.properties)


            // CERTIFICATES
            implementation(libs.bouncycastle.prov)
            implementation(libs.bouncycastle.pkix)
        }
    }
}


license {
    header(project.file("HEADER"))
    properties {
        set("year", "2024-present")
        set("name", authorName)
    }

    ignoreFailures = true
}


val repoUsernameProp = "NDK_REPO_USERNAME"
val repoPasswordProp = "NDK_REPO_PASSWORD"
val repoUsername: String? = System.getenv(repoUsernameProp)
val repoPassword: String? = System.getenv(repoPasswordProp)

if (repoUsername.isNullOrBlank() || repoPassword.isNullOrBlank()) {
    throw GradleException("Environment variables $repoUsernameProp and $repoPasswordProp must be set.")
}

publishing {

    repositories {
        maven {
            name = "ndk-repo"
            url = uri("https://repo.nikdekur.tech/releases")
            credentials {
                username = repoUsername
                password = repoPassword
            }
        }

        mavenLocal()
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            pom {
                developers {
                    developer {
                        id.set(authorId)
                        name.set(authorName)
                    }
                }
            }

            from(components["kotlin"])
        }
    }
}