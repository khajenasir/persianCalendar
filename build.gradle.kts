// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
        maven {
            url = uri("http://repo1.maven.org/maven2")
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.3")
        classpath(kotlin("gradle-plugin", version = "1.3.20"))
        classpath ("com.google.gms:google-services:4.3.3")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}




allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://dl.google.com/dl/android/maven2")
        }
        maven {
            url = uri("https://maven.google.com")
        }
        maven {
            url = uri("https://dl.bintray.com/tapsellorg/maven")
        }
        flatDir{
            dirs ("../app/libs")
        }
    }
}

task("clean") {
    delete(rootProject.buildDir)
}
