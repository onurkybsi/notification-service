import nu.studer.gradle.jooq.JooqEdition
import nu.studer.gradle.jooq.JooqGenerate
import org.flywaydb.gradle.task.FlywayMigrateTask
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
    alias(libs.plugins.studerjooqgenerator)
}

flyway {
    url = "jdbc:postgresql://$dbHost:$dbPort/notification_db"
    user = dbUser
    password = dbPassword
    locations = arrayOf("classpath:db/migration")
}

jooq {
    version = dependencyManagement.importedProperties["jooq.version"]
    edition = JooqEdition.OSS

    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://$dbHost:$dbPort/notification_db"
                    user = dbUser
                    password = dbPassword
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                    }
                    // Target package of the generated code
                    target.apply {
                        packageName = "org.kybprototyping.notificationservice.adapter.repository.notificationtemplate"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
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

    runtimeOnly(libs.postgresql.jdbc) // For Flyway task
    jooqGenerator(libs.postgresql.jdbc)

    testImplementation(libs.springmockk)
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.flyway.core)
    testImplementation(libs.kotlincoroutinestest)
    testImplementation(libs.kotestassertionsarrow)
}

configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JooqGenerate> {
    onlyIf { // Don't run this task when ktlint tasks are executed.
        !gradle.taskGraph.allTasks.any { it.name.contains("ktlint") }
    }

    dependsOn("flywayMigrate")
}

tasks.withType<FlywayMigrateTask> {
    onlyIf { // Don't run this task when ktlint tasks are executed.
        !gradle.taskGraph.allTasks.any { it.name.contains("ktlint") }
    }
}

tasks.withType<BootRun> {
    dependsOn("flywayMigrate")
    dependsOn("generateJooq")
}
