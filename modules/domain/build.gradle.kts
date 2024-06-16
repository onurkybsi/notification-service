dependencies {
    implementation(project(":modules:common"))
    // TODO: Both were used temporarily.
    implementation(libs.spring.context)
    implementation(libs.spring.transaction)

    testImplementation(libs.junitjupiter)
    testImplementation(libs.kotlinxcoroutinestest)
    testImplementation(libs.mockk)
    testImplementation(libs.assertj)
}

tasks.withType<Test> {
    useJUnitPlatform()
}