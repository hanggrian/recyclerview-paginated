import org.gradle.kotlin.dsl.kotlin

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(targetSdk)
    buildToolsVersion(buildTools)
    defaultConfig {
        minSdkVersion(minSdk)
        targetSdkVersion(targetSdk)
        applicationId = "com.example.recyclerview.paginated"
        versionCode = 1
        versionName = "1.0"
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.srcDirs("src")
            res.srcDir("res")
            resources.srcDir("src")
        }
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(project(":recyclerview-paginated"))
    implementation(kotlin("stdlib", kotlinVersion))

    implementation(support("appcompat-v7", supportVersion))
    implementation(support("recyclerview-v7", supportVersion))
    implementation(support("cardview-v7", supportVersion))
    implementation(support("design", supportVersion))

    implementation(square("retrofit2", "retrofit", retrofitVersion))
    implementation(square("retrofit2", "adapter-rxjava2", retrofitVersion))
    implementation(square("retrofit2", "converter-gson", retrofitVersion))
    implementation(square("okhttp3", "okhttp", okhttpVersion))
    implementation(square("okhttp3", "logging-interceptor", okhttpVersion))

    implementation(rx("java", rxjavaVersion))
    implementation(rx("kotlin", rxkotlinVersion))
    implementation(rx("android", rxandroidVersion))

    implementation(hendraanggrian("kota-support-v4", kotaVersion))
    implementation(hendraanggrian("kota-recyclerview-v7", kotaVersion))

    implementation("de.hdodenhof:circleimageview:$circleimageviewVersion")
}
