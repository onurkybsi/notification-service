import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

val dbUrl = System.getenv("DB_URL")
val dbUser = System.getenv("DB_USER")
val dbPassword = System.getenv("DB_PASSWORD")

plugins {
    // From Spring initializer
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("plugin.spring") version "1.9.24"

    alias(libs.plugins.flywayplugin)
}

flyway {
    url = dbUrl
    user = dbUser
    password = dbPassword
    locations = arrayOf("classpath:db/migration")
}

dependencies {
    // From Spring initializer
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.data:spring-data-r2dbc:3.3.0")

    implementation(project(":modules:domain"))
    implementation(libs.postgresqlr2dbcdriver)
    implementation(libs.r2dbcpool)
    implementation("org.springframework.data:spring-data-relational:3.3.0")

    // Needed for Flyway migration
    runtimeOnly(libs.postgresqljdbcdriver)

    // From Spring initializer
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation(libs.testcontainers)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation(libs.flywaycore)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<BootRun> {
    dependsOn("flywayMigrate")
}