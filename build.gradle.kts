plugins {
    id("java")
    id("io.freefair.lombok") version "8.12.2.1"
}

group = "dev.brauw.mapper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(uri("https://repo.papermc.io/repository/maven-public/"))
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.16.1")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.test {
    useJUnitPlatform()
}