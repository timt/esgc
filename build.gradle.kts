plugins {
    kotlin("jvm") version "2.2.0"
}

group = "io.shaka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.stripe:stripe-java:29.3.0")
    implementation("org.http4k:http4k-core:5.14.4.0")
    implementation("org.http4k:http4k-client-okhttp:5.14.4.0")
    implementation("org.http4k:http4k-format-jackson:5.14.4.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}