plugins {
    id("sample.conventions")
}

android {
    namespace = "com.niusounds.libreastream.midisample"
    defaultConfig {
        applicationId = "com.niusounds.libreastream.midisample"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.runtime)
    implementation(libs.coroutines.android)
}
