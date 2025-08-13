plugins {
    kotlin("jvm") version "2.2.0"
    application
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

// Custom tasks for different jobs
tasks.register<JavaExec>("syncStripeToFreeAgent") {
    group = "application"
    description = "Sync Stripe transactions to FreeAgent"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("io.shaka.StripeToFreeAgentSyncKt")
}

tasks.register<JavaExec>("syncZettleToFreeAgent") {
    group = "application"
    description = "Sync Zettle transactions to FreeAgent"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("io.shaka.ZettleToFreeAgentSyncKt")
}

tasks.register<JavaExec>("fetchZettleTransactions") {
    group = "application"
    description = "Fetch and display Zettle transactions"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("io.shaka.ZettleTransactionFetcherKt")
}

// Keep run task pointing to the main sync job for backward compatibility
tasks.named<JavaExec>("run") {
    mainClass.set("io.shaka.StripeToFreeAgentSyncKt")
}