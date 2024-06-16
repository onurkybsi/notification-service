dependencies {
    testImplementation(libs.junitjupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.kotlinreflect)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
