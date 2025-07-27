plugins {
    id("java-library")
    id("io.freefair.lombok") version "8.14"
    id("maven-publish")
}

group = "nl.mineburg.grafiq"
version = "v1.0.3"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    api("com.clickhouse:client-v2:0.9.0")
    implementation("org.reflections:reflections:0.10.2")
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
}

tasks.javadoc {
    isFailOnError = false
}

tasks.withType<Jar> {
    archiveBaseName.set("grafiq")
    archiveVersion.set(version.toString())
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.test {
    useJUnitPlatform()
}