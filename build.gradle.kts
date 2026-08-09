plugins {
    kotlin("jvm") version "1.9.22"
    jacoco
    application
}

group = "com.pdfgen"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

kotlin {
    jvmToolchain(17)
}

application {
    // The distribution runs the HTTP server; the CLI remains available
    // via the fat jar (Main-Class: pdf.CliKt).
    mainClass.set("pdf.ServerMainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "pdf.CliKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
