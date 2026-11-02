plugins { id("com.android.application") }
android {
    namespace = "org.noupi.testhandler"
    compileSdk = 36
    defaultConfig { applicationId = "org.noupi.testhandler"; minSdk = 26; targetSdk = 36; versionCode = 1; versionName = "0.1.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
// Test fixture only: there is deliberately no release variant.
androidComponents { beforeVariants(selector().withBuildType("release")) { it.enable = false } }
