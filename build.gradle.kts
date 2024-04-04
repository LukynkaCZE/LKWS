import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    kotlin("jvm") version "1.9.22"
    application
}

group = "cz.lukynka"
version = "1.0"

val githubUser: String = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USER")
val githubPassword: String = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/LukynkaCZE/PrettyLog")
        credentials {username = githubUser; password = githubPassword}
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("cz.lukynka:pretty-log:1.2")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("MainKt")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/LukynkaCZE/LKWS")
            credentials {username = githubUser; password = githubPassword}
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            artifactId = "lkws"
            version = version
            from(components["java"])
        }
    }
}