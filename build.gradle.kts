import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    kotlin("jvm") version "1.9.22"
    application
}

group = "cz.lukynka"
version = project.property("version").toString()

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = "LKWS"
            version = version

            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "test"
            url = layout.buildDirectory.dir("/pom/").get().asFile.toURI()
        }
    }
}

tasks.jar {
    val pomFile = File("./build/pom/cz/lukynka/LKWS/0.4/LKWS-0.4.pom")
    val dotFile = File("./LKWS-0.4.pom")
    val xmlFile = File("./pom.xml")
    if(pomFile.exists()) {
        dotFile.createNewFile()
        xmlFile.createNewFile()
        dotFile.writeText(pomFile.readText())
        xmlFile.writeText(pomFile.readText())
    }
    project.logger.lifecycle(dotFile.exists().toString())
    project.logger.lifecycle(dotFile.path)
    from(dotFile.path) {
        into("META-INF/maven/cz.lukynka/LKWS-0.4")
    }
    from(dotFile.path) {
        into("/")
    }
}