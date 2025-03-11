import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("io.freefair.lombok") version "8.12.2.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("maven-publish")
}

group = "dev.brauw.mapper"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.xenondevs.xyz/releases")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.16.1")
    implementation("xyz.xenondevs.invui:invui:1.44")
    implementation("org.incendo:cloud-core:2.0.0")
    implementation("org.incendo:cloud-annotations:2.0.0")
    implementation("org.incendo:cloud-paper:2.0.0-beta.10")

    testImplementation("com.github.seeseemelk:MockBukkit-v1.21:3.133.2")
    testImplementation("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
        }
    }
}

tasks.withType<ShadowJar> {
    archiveBaseName = "mapper-plugin"
    archiveVersion = ""
    archiveClassifier = ""
    from(sourceSets.main.get().output)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

publishing {
    publications {
        create<MavenPublication>("Mapper") {
            // Use the shadow JAR as the primary artifact
            artifact(tasks["shadowJar"])

            // Exclude the default JAR
            artifact(tasks["jar"]) {
                classifier = "original"
            }

            pom {
                name.set("Mapper")
                description.set("Mapper plugin")
            }
        }
    }
}

tasks.named<Copy>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.build {
    dependsOn("shadowJar")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.test {
    useJUnitPlatform()
}