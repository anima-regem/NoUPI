# Third-party notices

NoUPI's own code is MIT. Dependencies keep their original licenses.

| Component | Version | Source | License |
| --- | --- | --- | --- |
| ZXing Android Embedded | 4.3.0 | https://github.com/journeyapps/zxing-android-embedded/tree/v4.3.0 | Apache 2.0 |
| ZXing Core | 3.5.4 | https://github.com/zxing/zxing/tree/zxing-3.5.4 | Apache 2.0; upstream notices retained |
| Kotlin standard library | Resolved by AGP 9.1.1 built-in Kotlin; pinned in dependency locks | https://github.com/JetBrains/kotlin | Apache 2.0 |
| AndroidX test libraries | Resolved versions in dependency locks | https://android.googlesource.com/platform/frameworks/support/ | Apache 2.0 |
| JUnit, AndroidX Test/Espresso/UIAutomator | Pinned in app/build.gradle.kts | https://github.com/junit-team/junit4 / https://github.com/android/android-test | EPL 1.0 (JUnit 4), Apache 2.0 (AndroidX) — test only |
| Gradle wrapper / Android Gradle Plugin | 9.3.1 / 9.1.1 | https://github.com/gradle/gradle / https://android.googlesource.com/platform/tools/base/ | Apache 2.0 — build only |

The original JourneyApps license, ZXing license and upstream NOTICE are in `app/src/main/assets/licenses/` and are packaged with both apps. AndroidX and Kotlin use the same Apache 2.0 terms; AndroidX is used by the instrumentation tests, not the two app runtime classpaths. Source links and notices are also discoverable in the app's Help. No proprietary payment or hosted QR service is used. Transitive artifacts and their versions can be inspected with `./gradlew :app:dependencies --configuration intentDebugRuntimeClasspath` (or `ussdDebugRuntimeClasspath`).
