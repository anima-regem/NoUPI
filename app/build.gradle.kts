plugins { id("com.android.application") }
android {
    namespace = "org.noupi"
    compileSdk = 36
    defaultConfig {
        applicationId = "org.noupi"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    flavorDimensions += "handoff"
    productFlavors {
        create("intent") { dimension = "handoff"; applicationIdSuffix = ".intent"; resValue("string", "app_name", "NoUPI Intent") }
        create("ussd") { dimension = "handoff"; applicationIdSuffix = ".ussd"; resValue("string", "app_name", "NoUPI USSD") }
    }
    buildFeatures { buildConfig = true; resValues = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }

    lint { abortOnError = true; checkReleaseBuilds = true }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
dependencies {
    "intentImplementation"("com.journeyapps:zxing-android-embedded:4.3.0")
    "intentImplementation"("com.google.zxing:core:3.5.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.7.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.4.0")
}
