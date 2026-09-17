import io.github.surpsg.deltacoverage.gradle.CoverageEntity
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
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
        // octopus-quality 3.0.0 cannot do this: TaskRegistrar registers the aggregate JaCoCo tasks
        // inside the lazy `qualityCoverage` config action and Gradle rejects that, and it has no
        // aggregated Kover task at all. Wired directly below; switch back to the DSL when fixed.
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

// The three `test` tasks the quality workflow excludes with -x: they drive real Gitea, Bitbucket and
// TeamCity servers through docker-compose. One definition, used for the task graph, for the exec
// files the gates read, and to keep a local run with docker from recording numbers CI can never
// reproduce (which would redden the baseline check permanently).
val dockerBoundTestTasks = listOf(
    ":gitea-test-client:test",
    ":bitbucket-test-client:test",
    ":teamcity-client:test",
)

// Every test task whose coverage the quality job actually measures.
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
                // silently into both the baseline and the check.
                exclude { (module.name to it.name) in dockerBoundExec }
            }
        },
    )
}

tasks.register<JacocoReport>("jacocoAggregatedReport") {
    group = "verification"
    dependsOn(measuredTestTasks)
    executionData.setFrom(jacocoExecutionData())
    sourceDirectories.setFrom(files(subprojects.map { it.the<SourceSetContainer>()["main"].allSource.srcDirs }))
    classDirectories.setFrom(files(subprojects.map { it.the<SourceSetContainer>()["main"].output }))
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// ---- Non-regression gate -------------------------------------------------------------------
// Fixed floors cannot express "coverage must not decrease": deleting a test drops the ratio while
// staying above any floor, and `deltaCoverage` sees no changed production lines and reports NaN.
// So the exact counters are committed and compared on every run, per module and in aggregate. This
// replaces the per-module and aggregate floors that stood here - two sources of truth for one number.
//
// The committed file is a BOOTSTRAP. `origin/main` carries no coverage wiring yet, so there is no
// base result to compare against. Once main produces coverage, compare against the PR's actual base
// SHA instead of this file: a moving "latest main" number would quietly accept a regression from one
// PR as the baseline for the next.
val coverageBaselineFile = layout.projectDirectory.file("coverage-baseline.properties").asFile

// LINE counters as covered/total, keyed by module name plus "aggregate". Classes are attributed to
// modules by which module's output directory holds the .class file: the report knows only packages,
// and test-client-commons and test-client-test-commons share one.
fun measuredCoverage(): Map<String, Pair<Int, Int>> {
    val classToModule = subprojects.flatMap { module ->
        module.the<SourceSetContainer>()["main"].output.classesDirs.files.flatMap { dir ->
            dir.walkTopDown()
                .filter { it.extension == "class" }
                .map { it.relativeTo(dir).path.removeSuffix(".class") to module.name }
                .toList()
        }
    }.toMap()

    val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    val reportTask = tasks.named<JacocoReport>("jacocoAggregatedReport").get()
    val document = factory.newDocumentBuilder().parse(reportTask.reports.xml.outputLocation.get().asFile)

    val counters = subprojects.associate { it.name to intArrayOf(0, 0) } + ("aggregate" to intArrayOf(0, 0))
    val classNodes = document.getElementsByTagName("class")
    for (i in 0 until classNodes.length) {
        val classNode = classNodes.item(i)
        val module = classToModule[classNode.attributes.getNamedItem("name").nodeValue] ?: continue
        val children = classNode.childNodes
        for (j in 0 until children.length) {
            val counter = children.item(j)
            if (counter.nodeName != "counter") continue
            if (counter.attributes.getNamedItem("type").nodeValue != "LINE") continue
            val covered = counter.attributes.getNamedItem("covered").nodeValue.toInt()
            val missed = counter.attributes.getNamedItem("missed").nodeValue.toInt()
            for (key in listOf(module, "aggregate")) {
                counters.getValue(key)[0] += covered
                counters.getValue(key)[1] += covered + missed
            }
        }
    }
    // The aggregate is taken from the report's own total so it matches the published XML and HTML.
    // The per-module numbers are sums of class counters, which can differ a little: JaCoCo totals by
    // source file, and one source file can produce several classes.
    val totals = document.documentElement.childNodes
    for (i in 0 until totals.length) {
        val counter = totals.item(i)
        if (counter.nodeName != "counter") continue
        if (counter.attributes.getNamedItem("type").nodeValue != "LINE") continue
        val covered = counter.attributes.getNamedItem("covered").nodeValue.toInt()
        val missed = counter.attributes.getNamedItem("missed").nodeValue.toInt()
        counters.getValue("aggregate")[0] = covered
        counters.getValue("aggregate")[1] = covered + missed
    }
    return counters.mapValues { (_, value) -> value[0] to value[1] }
}

fun formatBaseline(counters: Map<String, Pair<Int, Int>>): String =
    counters.toSortedMap().entries.joinToString(
        prefix = "# Generated by ./gradlew updateCoverageBaseline - do not edit by hand.\n" +
            "# LINE counters as covered/total, measured with the quality job's test exclusions.\n",
        separator = "",
    ) { (key, value) -> "$key=${value.first}/${value.second}\n" }

// A merge-conflict marker or a hand-edit should say where it is, not throw from a destructuring.
val baselineEntry = Regex("^([^=]+)=([0-9]+)/([0-9]+)$")

fun parseBaseline(): Map<String, Pair<Int, Int>> = coverageBaselineFile
    .readLines()
    .withIndex()
    .filterNot { (_, line) -> line.isBlank() || line.startsWith("#") }
    .associate { (index, line) ->
        val entry = baselineEntry.matchEntire(line.trim())
        requireNotNull(entry) {
            "${coverageBaselineFile.name}:${index + 1}: expected `module=covered/total`, got `$line`. " +
                "Regenerate it with ./gradlew updateCoverageBaseline."
        }
        entry.groupValues[1] to (entry.groupValues[2].toInt() to entry.groupValues[3].toInt())
    }

tasks.register("updateCoverageBaseline") {
    group = "verification"
    description = "Regenerates coverage-baseline.properties from the aggregated JaCoCo report"
    dependsOn("jacocoAggregatedReport")
    doLast {
        coverageBaselineFile.writeText(formatBaseline(measuredCoverage()))
        logger.lifecycle("Wrote ${coverageBaselineFile.name}. Commit it so the change is reviewed.")
    }
}

tasks.register("coverageBaselineCheck") {
    group = "verification"
    description = "Fails when any module's or the aggregate line coverage falls below the baseline"
    dependsOn("jacocoAggregatedReport")
    // With no execution data the report is SKIPPED and reading it throws FileNotFoundException, so
    // let coverageDataCheck report the real cause first.
    mustRunAfter("coverageDataCheck")
    doLast {
        val baseline = parseBaseline()
        // Ratios compared by cross-multiplication: exact, and still correct when the line count
        // moves. Zero tolerance - a drop is a drop, and the escape hatch is a reviewed commit.
        // Uncovered growth is a drop: 104/2786 < 104/2785. Covered growth is not: 105/2786 > 104/2785.
        val regressions = measuredCoverage().mapNotNull { (key, now) ->
            val before = baseline[key]
            when {
                before == null -> "$key is missing from the baseline"
                now.first.toLong() * before.second < before.first.toLong() * now.second ->
                    "$key: ${now.first}/${now.second} is below the baseline ${before.first}/${before.second}"
                else -> null
            }
        }
        check(regressions.isEmpty()) {
            "Test coverage decreased:\n" + regressions.joinToString("\n") { "  - $it" } +
                "\n\nAdd tests, or - if the drop is intended - run ./gradlew updateCoverageBaseline " +
                "and commit ${coverageBaselineFile.name} so the decision is reviewed."
        }
    }
}

// A gate that can skip itself is not a gate. Both JaCoCo tasks above are SKIPPED when no execution
// data exists, so losing the last test that runs in the quality job (renaming LocatorTest out of
// `unitTest`'s filter is enough) would silently turn this check green again with no report at all.
// That is the exact failure this PR exists to remove, so assert the data is there.
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

// New code must arrive with tests: the baseline gate above only watches covered lines, so uncovered
// additions are this gate's job. The base ref has to exist locally - the quality workflow fetches it
// (see quality.yml); -PdeltaCoverage.baseRef=<ref> and -PdeltaCoverage.minLineRatio=<0..1> override
// both defaults, so unblocking a change is a visible, reviewable argument rather than a code edit.
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
    dependsOn("coverageDataCheck", "jacocoAggregatedReport", "coverageBaselineCheck", "deltaCoverage")
}

gradle.projectsEvaluated {
    check("qualityCoverage" in tasks.names) {
        "octopus-quality no longer registers a 'qualityCoverage' task. The coverage gates are wired " +
            "to it by name and would silently stop running - rewire them before bumping the plugin."
    }
}
