plugins {
    id("java")
}

group = "io.github.gaming32"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.6")
}

tasks.test {
    useJUnitPlatform()
}
