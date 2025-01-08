import java.text.SimpleDateFormat
import java.util.Date

plugins {
    kotlin("jvm") version "2.0.20-RC2"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.github.goooler.shadow") version "8.1.8"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "com.ceraia"

// Get the current date
val date = Date()

val major = "0"
val minor = "0"

// Format the version string
version = "${major}.${minor}.${SimpleDateFormat("yyMMdd").format(date)}-build-${SimpleDateFormat("HHmm").format(date)}"


repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.incendo:cloud-paper:2.0.0-beta.8")
    implementation("dev.triumphteam:triumph-gui:3.1.11")
    implementation("com.mojang:brigadier:1.0.18")
}

tasks.shadowJar {
    relocate("dev.triumphteam.gui", "com.ceraia.gui")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.withType<JavaCompile> {
    options.release.set(targetJavaVersion)
    tasks.build {
        dependsOn("shadowJar")
    }
}


tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks {
    runServer {
        minecraftVersion("1.21.4")
    }
}
