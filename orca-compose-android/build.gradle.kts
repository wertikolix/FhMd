import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
    signing
}

android {
    namespace = "ru.wertik.orca.compose.android"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    api(project(":orca-core"))

    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.coil3.compose)
    implementation(libs.coil3.network.okhttp)

    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit4)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui)
}

val releaseJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    val placeholder = layout.buildDirectory.file("tmp/javadoc/placeholder.txt")
    from(placeholder)
    doFirst {
        val file = placeholder.get().asFile
        file.parentFile.mkdirs()
        file.writeText("javadoc placeholder for alpha builds")
    }
}

publishing {
    publications {
        fun MavenPublication.configureComposePublication() {
            artifactId = "orca-compose"

            afterEvaluate {
                from(components["release"])
            }
            artifact(releaseJavadocJar)

            pom {
                name.set("Orca Compose Android")
                description.set("Android Compose renderer for Orca")
                url.set("https://github.com/wertikolix/Orca")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("wertikolix")
                        name.set("Wertik")
                        email.set("wertikolix@users.noreply.github.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/wertikolix/Orca.git")
                    developerConnection.set("scm:git:ssh://git@github.com/wertikolix/Orca.git")
                    url.set("https://github.com/wertikolix/Orca")
                }
            }
        }

        register<MavenPublication>("release") {
            configureComposePublication()
        }
    }

    repositories {
        maven {
            name = "github"
            url = uri(
                providers.gradleProperty("orcaMavenRepoUrl")
                    .orElse("https://maven.pkg.github.com/wertikolix/Orca")
                    .get(),
            )
            credentials {
                username = providers.gradleProperty("orcaMavenUsername").orNull
                password = providers.gradleProperty("orcaMavenPassword").orNull
            }
        }
        maven {
            name = "centralStaging"
            url = uri(
                providers.gradleProperty("centralStagingRepoUrl")
                    .orElse("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                    .get(),
            )
            credentials {
                username = providers.gradleProperty("centralTokenUsername").orNull
                password = providers.gradleProperty("centralTokenPassword").orNull
            }
        }
    }
}
