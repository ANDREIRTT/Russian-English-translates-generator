plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
}

group = "com.mal.words.db"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.kotlin.coroutine)

    implementation(libs.koin.ktor)

    implementation(libs.csv.parser)

    implementation(libs.sqlite.hikari)
    implementation(libs.sqlite.jdbc)
    implementation(libs.slf4j)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}