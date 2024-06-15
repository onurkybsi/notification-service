import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    // From Spring initializer
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("plugin.spring") version "1.9.24"

    alias(libs.plugins.flywayplugin)
}

flyway {
    url = System.getenv("DB_URL")
    user = System.getenv("DB_USER")
    password = System.getenv("DB_PASSWORD")
    locations = arrayOf("classpath:db/migration")
}

dependencies {
    // From Spring initializer
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation(project(":modules:domain"))
    implementation(libs.postgresqlr2dbcdriver)
    implementation(libs.r2dbcpool)
    implementation(libs.spring.datar2dbc)
    implementation(libs.springdoc)

    // Needed for Flyway migration
    runtimeOnly(libs.postgresqljdbcdriver)

    // From Spring initializer
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation(libs.kotlinxcoroutinestest)
    testImplementation(libs.flywaycore)
    testImplementation(libs.testcontainers)
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)
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