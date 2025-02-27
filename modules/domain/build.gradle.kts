dependencies {
    implementation(project(":modules:common"))
    implementation(libs.bundles.arrow)
    implementation(libs.apache.log4j.kotlin)
    implementation(libs.spring.context)
    implementation(libs.jackson.core)

    testImplementation(libs.junit.engine)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlincoroutinestest)
    testImplementation(libs.kotestassertionsarrow)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
