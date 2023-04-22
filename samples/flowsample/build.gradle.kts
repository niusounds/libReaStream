plugins {
    id("sample.conventions")
}

android {
    namespace = "com.niusounds.flowsample"
    defaultConfig {
        applicationId = "com.niusounds.flowsample"
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
