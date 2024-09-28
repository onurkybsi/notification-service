dependencies {
    implementation(libs.bundles.jackson)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
