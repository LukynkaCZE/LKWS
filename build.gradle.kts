plugins {
    `maven-publish`
    kotlin("jvm") version "1.9.22"
    application
}

group = "cz.lukynka"
version = "1.0"

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven {
        name = "devOS"
        url = uri("https://mvn.devos.one/releases")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("cz.lukynka:pretty-log:1.3")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}


tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}

application {
    mainClass.set("MainKt")
}

publishing {
    repositories {
        maven {
            url = uri("https://mvn.devos.one/releases")
            credentials {
                username = System.getenv()["MAVEN_USER"]
                password = System.getenv()["MAVEN_PASS"]
            }
        }
    }

    publications {
        register<MavenPublication>("maven") {
            groupId = "cz.lukynka"
            artifactId = "lkws"
            version = version
            from(components["java"])
        }
    }
}