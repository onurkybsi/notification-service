dependencies {
    implementation(project(":modules:common"))
    implementation(libs.bundles.arrow)
    implementation(libs.apache.log4j.kotlin)
    implementation(libs.spring.context)
}

tasks.withType<Test> {
    useJUnitPlatform()
}