import com.android.build.gradle.internal.api.BaseVariantOutputImpl

val versionMajor = 1
val versionMinor = 1
val versionPatch = 0

android {
    defaultConfig {
        applicationId = "ch.hsr.ifs.gcs"
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"

        applicationVariants.all {
            outputs.filterIsInstance<BaseVariantOutputImpl>()
                    .forEach {
                        it.outputFileName = "${rootProject.name}-${it.baseName}-${versionName}.apk"
                    }
        }
    }
}

dependencies {
    // Kotlin
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version = "1.3.1")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.3.1")

    // Android
    implementation(group = "androidx.appcompat", name = "appcompat", version = "1.0.2")
    implementation(group = "androidx.preference", name = "preference", version = "1.0.0")
    implementation(group = "androidx.cardview", name = "cardview", version = "1.0.0")
    implementation(group = "androidx.legacy", name = "legacy-support-v4", version = "1.0.0")
    implementation(group = "androidx.recyclerview", name = "recyclerview", version = "1.0.0")
    implementation(group = "androidx.mediarouter", name = "mediarouter", version = "1.0.0")
    implementation(group = "androidx.constraintlayout", name = "constraintlayout", version = "1.1.3")
    implementation(group = "androidx.lifecycle", name = "lifecycle-extensions", version = "2.0.0")

    // External Libraries
    implementation(group = "org.osmdroid", name = "osmdroid-android", version = "6.0.2")
    implementation(group = "org.osmdroid", name = "osmdroid-third-party", version = "6.0.1")
    implementation(group = "com.google.code.gson", name = "gson", version = "2.8.4")
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "4.2.2")

    // Local Libraries
    implementation(project(":jMAVlib"))
    implementation(project(":usb-serial-for-android"))

    // Testing
    testImplementation(group = "org.robolectric", name = "robolectric", version = "4.2")
}
