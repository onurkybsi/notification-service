dependencies {
    implementation(libs.bundles.jackson)
    implementation(libs.arrow.core)

    testImplementation(libs.junit.engine)
    testImplementation(libs.assertj.core)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
