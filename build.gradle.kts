import io.github.surpsg.deltacoverage.gradle.CoverageEntity
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.octopusden.octopus.quality.CoverageExtension
import java.math.BigDecimal
import java.net.InetAddress
import java.time.Duration
import java.util.zip.CRC32

plugins {
    java
    idea
    jacoco
    id("org.octopusden.octopus.oc-template")
    id("org.jetbrains.kotlin.jvm")
    id("io.github.gradle-nexus.publish-plugin")
    id("io.gitlab.arturbosch.detekt") apply false
    id("org.jlleitschuh.gradle.ktlint") apply false
    id("org.octopusden.octopus-quality")
    id("io.github.surpsg.delta-coverage")
    signing
    `maven-publish`
}

// The `test` tasks that drive real Gitea, Bitbucket and TeamCity servers through docker-compose,
// with the JaCoCo tasks that depend on them - excluding only `test` would let those two pull it
// back into the graph. Their modules' classes stay in the denominator either way.
val dockerBoundTestTasks = listOf(
    ":gitea-test-client",
    ":bitbucket-test-client",
    ":teamcity-client",
).flatMap { module ->
    listOf("$module:test", "$module:jacocoTestReport", "$module:jacocoTestCoverageVerification")
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
    // Without this a plain `./gradlew qualityCoverage` starts docker: the plugin adds every
    // `:test` task to the aggregate itself. The gate must not depend on anyone remembering flags.
    excludedTasks.addAll(dockerBoundTestTasks)
    coverage {
        enabled.set(true)
        // AUTO resolves to Kover for a Kotlin-only repo, and the plugin has no aggregated Kover
        // task; JaCoCo is the one with `jacocoOverallCoverage*`.
        tool.set(CoverageExtension.Tool.JACOCO)
        // teamcity-client's `test` is docker-bound and excluded above, so its docker-free
        // `unitTest` is what contributes that module's coverage.
        additionalTestTasks.add("unitTest")
        // Per-module floor off. Ten of twelve modules have no tests at all, so the 0.10 default
        // fails them on the first run; a per-module floor is worth setting when they have tests.
        minimumLineCoverage.set(BigDecimal.ZERO)
        // A floor, NOT a ratchet: it catches a collapse, it does not notice a small slide. Set
        // just under the measured 6.4% so ordinary line-count drift does not trip it. Raise it
        // when coverage grows.
        overallMinimum.set(BigDecimal("0.06"))
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

// `allprojects`, not `subprojects`: the aggregate JaCoCo tasks run on the root project and need to
// resolve org.jacoco:org.jacoco.ant there too.
allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "idea")
    apply(plugin = "java")
    apply(plugin = "signing")
    apply(plugin = "jacoco")
    apply(plugin = "maven-publish")
    // Kotlin static analysis — configured by the octopus-quality convention plugin
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

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

// The test tasks the quality job actually runs, which is what the vacuity guard below waits for.
val measuredTestTasks = subprojects.map { "${it.path}:test" } - dockerBoundTestTasks.toSet() +
    ":teamcity-client:unitTest"

fun jacocoExecutionData(): FileCollection {
    val dockerBoundExec = dockerBoundTestTasks
        .map { it.removeSuffix(":test").removePrefix(":") to "test.exec" }
        .toSet()
    return files(
        subprojects.map { module ->
            module.fileTree(module.layout.buildDirectory) {
                include("jacoco/*.exec")
                // A leftover test.exec from an earlier full local run would otherwise merge
                // silently into the delta gate's view.
                exclude { (module.name to it.name) in dockerBoundExec }
            }
        },
    )
}

// A gate that can skip itself is not a gate. With no execution data JaCoCo skips its own tasks and
// `deltaCoverage` reports NaN%, so losing the last test that runs here - renaming LocatorTest out
// of `unitTest`'s filter is enough - would turn the whole check green and empty.
tasks.register("coverageDataCheck") {
    group = "verification"
    description = "Fails when no test produced coverage data, which would make the gate vacuous"
    dependsOn(measuredTestTasks)
    val executionData = jacocoExecutionData()
    doLast {
        check(!executionData.isEmpty) {
            "No JaCoCo execution data: no test ran in this build, so the coverage gate would pass " +
                "vacuously. Check that :teamcity-client:unitTest still matches at least one test."
        }
    }
}

// New code must arrive with tests. The plugin's aggregate floor only catches a collapse, so this is
// the gate that looks at the change itself. The base ref has to exist locally - the quality workflow
// fetches it (see quality.yml); -PdeltaCoverage.baseRef=<ref> and -PdeltaCoverage.minLineRatio=<0..1>
// override both defaults, so unblocking a change is a reviewable argument rather than a code edit.
configure<DeltaCoverageConfiguration> {
    diffSource.byGit {
        compareWith(providers.gradleProperty("deltaCoverage.baseRef").getOrElse("origin/main"))
        useNativeGit.set(true)
    }
    coverageBinaryFiles = jacocoExecutionData()
    classesDirs = files(subprojects.map { it.the<SourceSetContainer>()["main"].output })
    srcDirs = files(subprojects.map { it.the<SourceSetContainer>()["main"].allSource.srcDirs })
    violationRules {
        failOnViolation.set(true)
        // LINE only. `failIfCoverageLessThan` would set the same ratio for BRANCH and INSTRUCTION
        // too, and every other number in this build is a line count.
        rule(CoverageEntity.LINE) {
            minCoverageRatio.set(
                providers.gradleProperty("deltaCoverage.minLineRatio").getOrElse("0.5").toDouble(),
            )
        }
    }
}

tasks.named("deltaCoverage") {
    dependsOn(measuredTestTasks)
}

// delta-coverage 2.5.0 leaves `gitDiff` UP-TO-DATE when HEAD moves, so an incremental local run
// checks the previous diff and passes on code that fails after `clean`. Regenerating it is cheap.
tasks.named("gitDiff") {
    outputs.upToDateWhen { false }
}

// `qualityCoverage` is registered by the convention plugin in `projectsEvaluated`; matching by name
// avoids depending on listener ordering. Matching by name also fails OPEN, so assert the task is
// really there: a rename upstream would otherwise detach every gate and leave CI green and empty.
tasks.matching { it.name == "qualityCoverage" }.configureEach {
    dependsOn("coverageDataCheck", "deltaCoverage")
}

gradle.projectsEvaluated {
    check("qualityCoverage" in tasks.names) {
        "octopus-quality no longer registers a 'qualityCoverage' task. The coverage gates are wired " +
            "to it by name and would silently stop running - rewire them before bumping the plugin."
    }
}
