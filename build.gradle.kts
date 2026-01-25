plugins {
    id("application")
    id("fabric-loom") version "1.15-SNAPSHOT"
    id("maven-publish")
    id("java")
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

repositories {
    maven { url = uri("https://maven.terraformersmc.com/") }
    maven { url = uri("https://maven.shedaniel.me/") }
    maven { url = uri("https://maven.terraformersmc.com/releases/") }
}

dependencies {
    // To change the versions see the libs.versions.toml file
    // PLEASE add dependencies that way too. See https://docs.gradle.org/current/userguide/version_catalogs.html for fancy documentation
    // (or just look at how I do it there)
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)

    modImplementation(libs.fabric.api)
    modImplementation(libs.modmenu)

    modApi(libs.cloth.config) {
        exclude(group = libs.fabric.api.get().group)
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(mapOf(
            "version" to project.version,
            "cloth_config_version" to libs.versions.cloth.config.get()
        ))
    }
}

val targetJavaVersion = 21
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

loom {
    accessWidenerPath.set(file("src/main/resources/texturesplusmod.classtweaker"))
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withSourcesJar()
}

tasks.jar {
    val archivesBaseName = project.property("archives_base_name") as String
    inputs.property("archivesName", archivesBaseName)
    from("LICENSE") {
        rename { "${it}_${archivesBaseName}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}
