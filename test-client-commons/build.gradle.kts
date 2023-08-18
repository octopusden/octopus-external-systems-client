java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(project(":client-commons"))
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.6.0.202305301015-r")
}
