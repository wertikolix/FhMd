plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.commonmark)

    testImplementation(kotlin("test-junit"))
    testImplementation(libs.junit4)
}

tasks.test {
    useJUnit()
}
