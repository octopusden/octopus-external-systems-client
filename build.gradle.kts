import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.InetAddress
import java.time.Duration
import java.util.zip.CRC32

plugins {
    java
    idea
    id("org.octopusden.octopus.oc-template")
    id("org.jetbrains.kotlin.jvm")
    id("io.github.gradle-nexus.publish-plugin")
    id("io.gitlab.arturbosch.detekt") apply false
    id("org.jlleitschuh.gradle.ktlint") apply false
    id("org.octopusden.octopus-quality")
    signing
    `maven-publish`
}

octopusQuality {
    // Regression guard on what reaches Maven Central, from octopus-base v2.7.0. This repository
    // previously hand-rolled a task of the SAME name, deleted in this commit — two tasks with one
    // name fail configuration, so the bump and the deletion cannot be separated.
    //
    // The declared set is deliberately INDEPENDENT of `centralPublishedProjects` below, which
    // gates publication creation. The old local guard compared `centralPublishedProjects` against
    // the projects that publish — but that same list decides which projects publish, so both sides
    // moved together and an edit to it could never be reported as drift. Only an addition arriving
    // by some other route was catchable. Declaring the expected coordinates separately is what
    // makes editing the list a detectable change.
    publication {
        // Spelled out rather than derived from the module list: deriving it would recreate the
        // tautology described above. Only the groupId, a constant, is factored out.
        val group = "org.octopusden.octopus.octopus-external-systems-clients"
        enforceCentralPublications.set(true)
        centralPublications.set(
            setOf(
                ":artifactory-client|maven|${group}:artifactory-client|" +
                    "[jar, jar:javadoc, jar:sources]",
                ":bitbucket-client|maven|${group}:bitbucket-client|" +
                    "[jar, jar:javadoc, jar:sources]",
                ":bitbucket-test-client|maven|${group}:bitbucket-test-client|" +
                    "[jar, jar:javadoc, jar:sources]",
                ":client-commons|maven|${group}:client-commons|" +
                    "[jar, jar:javadoc, jar:sources]",
                ":confluence-client|maven|${group}:confluence-client|" +
                    "[jar, jar:javadoc, jar:sources]",
                ":gitea-client|maven|${group}:gitea-client|" +
                    "[jar, jar:javadoc, jar:sources]",
                ":gitea-test-client|maven|${group}:gitea-test-client|" +
                    "[jar, jar:javadoc, jar:sources]",
                ":jira-client|maven|${group}:jira-client|" +
                    "[jar, jar:javadoc, jar:sources]",
                ":sonarqube-client|maven|${group}:sonarqube-client|" +
                    "[jar, jar:javadoc, jar:sources]",
                ":teamcity-client|maven|${group}:teamcity-client|" +
                    "[jar, jar:javadoc, jar:sources]",
                ":test-client-commons|maven|${group}:test-client-commons|" +
                    "[jar, jar:javadoc, jar:sources]",
            ),
        )
    }
    kotlin {
        failOnViolation.set(true)
    }
    coverage {
        enabled.set(false)
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("MAVEN_USERNAME"))
            password.set(System.getenv("MAVEN_PASSWORD"))
        }
    }
    transitionCheckOptions {
        maxRetries.set(60)
        delayBetween.set(Duration.ofSeconds(30))
    }
}

val defaultVersion = "${
    with(CRC32()) {
        update(InetAddress.getLocalHost().hostName.toByteArray())
        value
    }
}-SNAPSHOT"

allprojects {
    group = "org.octopusden.octopus.octopus-external-systems-clients"
    if (version == "unspecified") {
        version = defaultVersion
    }
}

// Which projects publish to Maven Central. An ALLOWLIST, not a denylist: the block below
// applies `maven-publish` to every subproject, so without this a newly added module would start
// publishing 50 files to Central the moment it is created, silently. Adding a coordinate is now
// an explicit edit here.
//
// Paths, not names: a Set of names collapses two modules that share a simple name, and the root
// project's name is a repository-level string rather than a module name.
val centralPublishedProjects = setOf(
    ":artifactory-client",
    ":bitbucket-client",
    ":bitbucket-test-client",
    ":client-commons",
    ":confluence-client",
    ":gitea-client",
    ":gitea-test-client",
    ":jira-client",
    ":sonarqube-client",
    ":teamcity-client",
    ":test-client-commons",
)

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "idea")
    apply(plugin = "java")
    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    // Kotlin static analysis — configured by the octopus-quality convention plugin
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    repositories {
        mavenCentral()
    }

    val gitUrl = "https://github.com/octopusden/octopus-external-systems-client.git"

    // `maven-publish` stays applied above even for a project that is not allowlisted, so the
    // `publish` lifecycle task still exists as a no-op and the publication policy can read
    // `publishing` on every project. Only the publication itself is conditional.
    if (project.path in centralPublishedProjects) {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                pom {
                    name.set(project.name)
                    description.set("Octopus module: ${project.name}")
                    url.set(gitUrl)
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    scm {
                        url.set(gitUrl)
                        connection.set("scm:git://github.com/octopusden/octopus-external-systems-client.git")
                    }
                    developers {
                        developer {
                            id.set("octopus")
                            name.set("octopus")
                        }
                    }
                }
            }
        }
    }

    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        signingKey?.let {
            signingPassword?.let {
                sign(publishing.publications["maven"])
            }
        }
    }
    }

    idea.module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    java {
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            suppressWarnings = true
            jvmTarget = "1.8"
        }
    }

    ext {
        System.getenv().let {
            set("signingRequired", it.containsKey("ORG_GRADLE_PROJECT_signingKey") && it.containsKey("ORG_GRADLE_PROJECT_signingPassword"))
            set("testPlatform", it.getOrDefault("TEST_PLATFORM", properties["test.platform"]))
            set("dockerRegistry", it.getOrDefault("DOCKER_REGISTRY", properties["docker.registry"]))
            set("octopusGithubDockerRegistry", it.getOrDefault("OCTOPUS_GITHUB_DOCKER_REGISTRY", project.properties["octopus.github.docker.registry"]))
            set("okdActiveDeadlineSeconds", it.getOrDefault("OKD_ACTIVE_DEADLINE_SECONDS", properties["okd.active-deadline-seconds"]))
            set("okdProject", it.getOrDefault("OKD_PROJECT", properties["okd.project"]))
            set("okdClusterDomain", it.getOrDefault("OKD_CLUSTER_DOMAIN", properties["okd.cluster-domain"]))
            set("okdWebConsoleUrl", (it.getOrDefault("OKD_WEB_CONSOLE_URL", properties["okd.web-console-url"]) as String).trimEnd('/'))
            set("bitbucketLicense", it.getOrDefault("BITBUCKET_LICENSE", properties["bitbucket.license"]))
        }
    }

    val supportedTestPlatforms = listOf("docker", "okd")
    if (project.ext["testPlatform"] !in supportedTestPlatforms) {
        throw IllegalArgumentException("Test platform must be set to one of the following $supportedTestPlatforms. Start gradle build with -Ptest.platform=... or set env variable TEST_PLATFORM")
    }
    val mandatoryProperties = mutableListOf("dockerRegistry", "octopusGithubDockerRegistry")
    if (project.ext["testPlatform"] == "okd") {
        mandatoryProperties.add("okdActiveDeadlineSeconds")
        mandatoryProperties.add("okdProject")
        mandatoryProperties.add("okdClusterDomain")
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
    }
}
