dependencies {
    implementation(project(":modules:common"))
    // TODO: Both were used temporarily.
    implementation(libs.spring.context)
    implementation(libs.spring.transaction)

    testImplementation(libs.junitjupiter)
    testImplementation(libs.kotlinxcoroutinestest)
    testImplementation(libs.mockk)
    testImplementation("org.assertj:assertj-core:3.26.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}