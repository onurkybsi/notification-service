dependencies {
    implementation(project(":modules:common"))
    implementation(libs.bundles.arrow)
    implementation(libs.apache.log4j.kotlin)
    implementation(libs.spring.context)
    implementation(libs.jackson.kotlin)
    implementation(libs.apache.commons.text)
    implementation(libs.jackson.datatype.jsr310)

    testImplementation(libs.junit.engine)
    testImplementation(libs.junit.params)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlincoroutinestest)
    testImplementation(libs.kotestassertionsarrow)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
