java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(project(":client-commons"))
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.13.2.202306221912-r")
    implementation("org.slf4j:slf4j-simple:2.0.7")
}
