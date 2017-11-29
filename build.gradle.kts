buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.0.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.novoda:bintray-release:0.7.0")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

/**
./gradlew :recyclerview-paginated:clean :recyclerview-paginated:build
./gradlew :recyclerview-paginated:bintrayUpload -PbintrayUser=hendraanggrian -PdryRun=false -PbintrayKey=
 */