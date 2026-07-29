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
    // `publish` lifecycle task still exists as a no-op and the policy check below can read
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

// Regression guard: fails if the set of projects publishing to Maven Central drifts from the
// allowlist above. Needed here specifically because `maven-publish` is applied to every
// subproject unconditionally, so a new module would otherwise start publishing ~50 files to
// Central without anyone deciding to.
//
// allprojects, not subprojects: the root is a publishable project like any other, and a
// publication added there would otherwise be invisible.
fun centralPublicationPolicyProblems(): List<String> {
    val publishingProjects = allprojects.filter { candidate ->
        candidate.plugins.hasPlugin("maven-publish") &&
            candidate.extensions
                .getByType(PublishingExtension::class.java)
                .publications
                .isNotEmpty()
    }
    val actual = publishingProjects.map { it.path }.toSet()
    return if (actual != centralPublishedProjects) {
        listOf(
            "Maven Central publication set drifted.\n" +
                "  allowlisted: ${centralPublishedProjects.sorted()}\n" +
                "  publishing:  ${actual.sorted()}\n" +
                "Update centralPublishedProjects in the root build script only if the change is intentional.",
        )
    } else {
        emptyList()
    }
}

// A policy violation must fail its own gate, not every Gradle invocation: throwing at
// configuration time would break build, test, dependencies and IDE sync as well.
val verifyCentralPublicationPolicy =
    tasks.register("verifyCentralPublicationPolicy") {
        group = "verification"
        description = "Fails if the set of projects publishing to Maven Central drifts from the allowlist."
        doLast {
            val problems = centralPublicationPolicyProblems()
            if (problems.isNotEmpty()) {
                throw GradleException(problems.joinToString("\n\n"))
            }
            logger.lifecycle(
                "Maven Central publication set matches the allowlist (${centralPublishedProjects.size} projects).",
            )
        }
    }

// Hook the task TYPE, so a concrete task such as publishMavenPublicationToMavenLocal cannot
// bypass the guard; the aggregates are matched by name as well because `publish` is per-project
// and `publishToSonatype` only exists with -Pnexus, so neither can be forced into existence.
gradle.projectsEvaluated {
    allprojects {
        tasks.withType(AbstractPublishToMaven::class.java).configureEach {
            dependsOn(verifyCentralPublicationPolicy)
        }
        tasks
            .matching { it.name in setOf("publishToSonatype", "publish", "publishToMavenLocal") }
            .configureEach { dependsOn(verifyCentralPublicationPolicy) }
    }
}
