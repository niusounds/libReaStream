// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("org.jetbrains.dokka") version "1.6.21" apply false
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}