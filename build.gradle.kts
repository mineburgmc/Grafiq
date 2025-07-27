plugins {
    id("java")
    id("io.freefair.lombok") version "8.14"
}

group = "nl.mineburg.grafiq"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.clickhouse:client-v2:0.9.0")
    implementation("org.reflections:reflections:0.10.2")
}

tasks.withType<Jar> {
    archiveBaseName.set("grafiq")
    archiveVersion.set(version.toString())
}

tasks.test {
    useJUnitPlatform()
}