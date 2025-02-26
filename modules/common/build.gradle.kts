dependencies {
    implementation(libs.bundles.jackson)
    implementation(libs.arrow.core)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
