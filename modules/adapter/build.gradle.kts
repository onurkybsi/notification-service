import org.flywaydb.gradle.task.FlywayMigrateTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Property
import org.jooq.meta.jaxb.Target
import org.springframework.boot.gradle.tasks.run.BootRun

val dbHost = System.getenv("DB_HOST") ?: "localhost"
val dbPort = System.getenv("DB_PORT") ?: 5432
val dbUser = System.getenv("DB_USER") ?: "user"
val dbPassword = System.getenv("DB_PASSWORD") ?: "password"

plugins {
    // From Spring Initializr
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"

    alias(libs.plugins.flyway.plugin)
    alias(libs.plugins.jooq.codegengradle)
}

dependencyManagement {
    imports {
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.10.0")
    }
}

flyway {
    url = "jdbc:postgresql://$dbHost:$dbPort/notification_db"
    user = dbUser
    password = dbPassword
    locations = arrayOf("classpath:db/migration")
}

dependencies {
    // From Spring Initializr
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito", module = "mockito-core")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.springdoc)
    implementation(libs.bundles.arrow)
    implementation(project(":modules:common"))
    implementation(project(":modules:domain"))
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation(libs.apache.log4j.kotlin)
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(libs.postgresql.r2dbc)
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
    implementation(libs.micrometer.otlpregistry)
    implementation(libs.angusmail)
    implementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test") {
        exclude(group = "ch.qos.logback", module = "logback-core")
    }

    runtimeOnly(libs.postgresql.jdbc) // For Flyway task

    jooqCodegen(libs.jooq.metaextensions)
    jooqCodegen("com.h2database:h2") // Needed for jOOQ generation!

    testImplementation(libs.springmockk)
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.flyway.core)
    testImplementation(libs.kotlincoroutinestest)
    testImplementation(libs.kotestassertionsarrow)
}

jooq {
    configuration {
        withGenerator(
            Generator()
                .withDatabase(
                    Database()
                        .withName("org.jooq.meta.extensions.ddl.DDLDatabase")
                        .withProperties(
                            Property()
                                .withKey("scripts")
                                .withValue("src/main/resources/db/migration/*.sql"),
                            Property()
                                .withKey("sort")
                                .withValue("semantic"),
                            Property()
                                .withKey("unqualifiedSchema")
                                .withValue("none"),
                            Property()
                                .withKey("defaultNameCase")
                                .withValue("lower"),
                        ),
                )
                .withTarget(
                    Target()
                        .withPackageName("org.kybprototyping.notificationservice.adapter.repository.notificationtemplate"),
                ),
        )
    }
}

configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("compileKotlin") {
    dependsOn("jooqCodegen")
}

tasks.withType<KtLintCheckTask> {
    dependsOn("jooqCodegen")
}

tasks.withType<FlywayMigrateTask> {
    onlyIf { // Don't run this task when ktlint tasks are executed.
        !gradle.startParameter.taskNames.any { it.contains("ktlint") }
    }
}

tasks.withType<BootRun> {
    dependsOn("flywayMigrate")
    dependsOn("jooqCodegen")
}
