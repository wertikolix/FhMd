plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.commonmark)
    implementation(libs.commonmark.ext.gfm.tables)

    testImplementation(kotlin("test-junit"))
    testImplementation(libs.junit4)
}

tasks.test {
    useJUnit()
}
