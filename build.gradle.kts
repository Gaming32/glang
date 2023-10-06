plugins {
    java
    `java-library`
}

group = "io.github.gaming32"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.6")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    compileOnlyApi("org.jetbrains:annotations:24.0.1")
}

tasks.test {
    useJUnitPlatform()
}
