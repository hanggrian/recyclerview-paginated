plugins {
    android("application")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    compileSdkVersion(SDK_TARGET)
    buildToolsVersion(BUILD_TOOLS)
    defaultConfig {
        minSdkVersion(SDK_MIN)
        targetSdkVersion(SDK_TARGET)
        applicationId = "$RELEASE_GROUP.paginated.demo"
        versionCode = 1
        versionName = RELEASE_VERSION
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
    implementation(project(":$RELEASE_ARTIFACT"))
    implementation(kotlin("stdlib", VERSION_KOTLIN))

    implementation(support("appcompat-v7", VERSION_SUPPORT))
    implementation(support("recyclerview-v7", VERSION_SUPPORT))
    implementation(support("cardview-v7", VERSION_SUPPORT))
    implementation(support("design", VERSION_SUPPORT))

    implementation(square("retrofit2", "retrofit", VERSION_RETROFIT))
    implementation(square("retrofit2", "adapter-rxjava2", VERSION_RETROFIT))
    implementation(square("retrofit2", "converter-gson", VERSION_RETROFIT))
    implementation(square("okhttp3", "okhttp", VERSION_OKHTTP))
    implementation(square("okhttp3", "logging-interceptor", VERSION_OKHTTP))

    implementation(rx("java", VERSION_RXJAVA))
    implementation(rx("kotlin", VERSION_RXKOTLIN))
    implementation(rx("android", VERSION_RXANDROID))

    implementation(hendraanggrian("kota-support-v4", VERSION_KOTA))
    implementation(hendraanggrian("kota-recyclerview-v7", VERSION_KOTA))

    implementation("de.hdodenhof:circleimageview:$VERSION_CIRCLEIMAGEVIEW")
}
