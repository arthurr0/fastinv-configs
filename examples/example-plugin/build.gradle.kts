plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.2"
}

group = "dev.privatefinal.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://storehouse.okaeri.eu/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation(project(":"))
}

tasks.processResources {
    filteringCharset = "UTF-8"
}

tasks.shadowJar {
    archiveClassifier.set("")
    exclude("menus/shop.yml")
    relocate("fr.mrmicky.fastinv", "dev.privatefinal.example.libs.fastinv")
    relocate("eu.okaeri", "dev.privatefinal.example.libs.okaeri")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
