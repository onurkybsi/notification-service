dependencies {
    implementation(libs.spring.context)
    implementation(project(":modules:common"))
    implementation(project(":modules:domain:common"))
    implementation(project(":modules:domain:model"))
    implementation(project(":modules:domain:port:emailstorage"))
}
