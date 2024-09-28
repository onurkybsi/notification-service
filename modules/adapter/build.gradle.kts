plugins {
	// From Spring Initializr
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
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
	testImplementation(libs.springmockk)
}

configurations {
	all {
		exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
