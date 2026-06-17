plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

group = "dev.privatefinal"
version = providers.gradleProperty("releaseVersion")
    .orElse(providers.gradleProperty("version"))
    .getOrElse("1.0.0-SNAPSHOT")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://maven.minecodes.pl/releases")
    maven("https://storehouse.okaeri.eu/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")

    api("fr.mrmicky:fastinv:3.1.2")
    api("eu.okaeri:okaeri-configs-yaml-bukkit:5.0.13")
    api("eu.okaeri:okaeri-configs-serdes-bukkit:5.0.13")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("FastInvConfigs")
                description.set("Config-driven inventory GUI library for Paper combining FastInv and okaeri-configs.")
            }
        }
    }
    repositories {
        maven {
            name = "mineCodes"
            val isSnapshot = version.toString().endsWith("SNAPSHOT")
            url = uri(
                if (isSnapshot) "https://maven.minecodes.pl/snapshots"
                else "https://maven.minecodes.pl/releases"
            )
            credentials {
                username = System.getenv("MINECODES_USERNAME")
                    ?: providers.gradleProperty("minecodesUsername").orNull
                password = System.getenv("MINECODES_PASSWORD")
                    ?: providers.gradleProperty("minecodesPassword").orNull
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}
