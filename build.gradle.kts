import com.android.build.gradle.BaseExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.util.profile

buildscript {
    extra.apply {
        // Kotlin
        set("kotlinCoroutinesVersion", "1.3.1")
    }

    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath(group = "com.android.tools.build", name = "gradle", version = "4.0.0")
        classpath(kotlin("gradle-plugin", version = "1.3.50"))
    }
}

plugins {
    id("io.gitlab.arturbosch.detekt") version "1.1.1" apply false
}

subprojects {
    repositories {
        google()
        jcenter()
    }

    if (name == "app") {
        apply(plugin = "com.android.application")
    } else {
        apply(plugin = "com.android.library")
    }

    apply(plugin = "kotlin-android")
    apply(plugin = "kotlin-android-extensions")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<DetektExtension> {
        profile("main") {
            input = files(
                    "src/main/kotlin"
            )
            config = files(
                    "detekt.yml"
            )
        }
    }

    configure<BaseExtension> {
        compileSdkVersion(29)

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        defaultConfig {
            minSdkVersion(26)
            targetSdkVersion(29)
        }

        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
                proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            }
        }

        sourceSets {
            val main by getting
            val test by getting

            main.java.srcDirs("src/main/kotlin")
            test.java.srcDirs("src/test/kotlin")
        }

        lintOptions {
            isAbortOnError = false
        }

        testOptions {
            unitTests.isIncludeAndroidResources = true
        }
    }

    dependencies {
        "implementation"(kotlin("stdlib"))
        "implementation"(kotlin("reflect"))

        "testImplementation"(group = "junit", name = "junit", version = "4.12")
        "testImplementation"(group = "org.hamcrest", name = "hamcrest-library", version = "1.3")
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions.freeCompilerArgs += listOf(
                    "-Xuse-experimental=kotlin.time.ExperimentalTime"
            )
        }
    }
}
