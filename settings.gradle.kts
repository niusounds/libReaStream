pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    enableFeaturePreview("VERSION_CATALOGS")
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.5.31")
            version("compose", "1.0.5")
            version("ktor", "1.6.5")

            alias("annotation").to("androidx.annotation:annotation:1.2.0")
            alias("core").to("androidx.core:core-ktx:1.6.0")
            alias("appcompat").to("androidx.appcompat:appcompat:1.3.1")
            alias("constraintlayout").to("androidx.constraintlayout:constraintlayout:2.1.1")
            alias("material").to("com.google.android.material:material:1.4.0")
            alias("lifecycle-runtime").to("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
            alias("coroutines-core").to("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
            alias("coroutines-android").to("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.1")
            alias("ktor").to("io.ktor", "ktor-network").versionRef("ktor")
            // compose
            alias("compose-ui").to("androidx.compose.ui", "ui").versionRef("compose")
            alias("compose-ui-tooling").to("androidx.compose.ui", "ui-tooling")
                .versionRef("compose")
            alias("compose-ui-tooling-preview").to("androidx.compose.ui", "ui-tooling-preview")
                .versionRef("compose")
            alias("compose-ui-test").to("androidx.compose.ui", "ui-test-junit4")
                .versionRef("compose")
            alias("compose-material").to("androidx.compose.material", "material")
                .versionRef("compose")
            alias("compose-activity").to("androidx.activity:activity-compose:1.3.1")

            // test
            alias("junit").to("junit:junit:4.13.2")
            alias("mockk").to("io.mockk:mockk:1.12.0")
            alias("androidx-test").to("androidx.test.ext:junit:1.1.3")
            alias("espresso").to("androidx.test.espresso:espresso-core:3.4.0")
        }
    }
}
rootProject.name = "libReaStream"

include(":libreastream")
include(":samples:midisample")
include(":samples:flowsample")
include(":samples:sender")
