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
//	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.springframework.data:spring-data-r2dbc")
	implementation("io.r2dbc:r2dbc-pool:1.0.1.RELEASE")

	implementation(libs.postgresql.r2dbc)
	runtimeOnly(libs.postgresql.jdbc) // For Flyway task
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

tasks.withType<BootRun> {
	dependsOn("flywayMigrate")
}