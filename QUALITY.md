# Quality checks

GitHub PRs run through `Merge Gate`. Its `gate/merge` job requires all four
dependencies to succeed:

| Job | Checks | Policy |
| --- | --- | --- |
| `build` | Compile/package on JDK 21; publication metadata validation | Blocking |
| `quality` | Gradle wrapper validation, detekt, ktlint, configured unit/coverage tasks | Blocking |
| `workflow-lint` | actionlint and syntax checks for `.github/**/*.sh` | Blocking |
| `security` | CodeQL (`java-kotlin`, `actions`) and Trivy filesystem scan | Findings are report-only; scanner job failures fail the gate |

The shared workflows come from `octopus-base`. Published workflows and the
`octopus-quality` Gradle plugin use 3.0.2. The two new reusable workflows are
pinned separately until they are included in an upstream release.
The wrapper also verifies the Gradle 8.6 distribution ZIP against its
[published SHA-256](https://gradle.org/release-checksums/#8.6).

Coverage policy and test selection are maintained separately in
[PR #160](https://github.com/octopusden/octopus-external-systems-client/pull/160).
This infrastructure update changes neither tests nor coverage thresholds.
The Docker-backed functional tests remain excluded from the GitHub coverage
command. `build` also excludes tests; it must not be treated as evidence that
functional tests ran. Full functional tests use the existing TeamCity setup.

## Local validation and reports

```sh
./gradlew build qualityStatic --no-daemon -x test
actionlint
```

Use the exact `coverage-command` from `.github/workflows/quality.yml` for the
current coverage policy. Keep existing detekt/ktlint baselines: increasing a
baseline requires reviewing the newly suppressed findings.

GitHub publishes `static-analysis-reports` and `coverage-reports` artifacts from the shared
quality workflow. Static reports include `**/build/reports/detekt/**` and
`**/build/reports/ktlint/**`; coverage includes test results and JaCoCo/Kover
reports. CodeQL and Trivy results appear in GitHub code scanning. Artifact paths
do not by themselves enable additional analyzers.

## Dependency inventory

`Dependency Submission` resolves the Gradle dependency graph on pushes to `main`
and manual runs on `main`, validates Gradle wrappers, submits the graph to GitHub
and uploads a graph artifact. It does not execute tests. It uses `contents: write`
in a separate workflow and is not a PR gate.

After its first successful run, verify that GitHub's dependency graph contains
the JVM libraries and enable Dependabot alerts if necessary. This supplies
dependency inventory for alerts; it is not a vulnerability threshold. Dependency-
Check remains disabled until its data-feed/runtime behavior is stable enough for
an explicit rollout.

## Enforcing merge protection

The existence of a green `gate/merge` check does not make it mandatory. A
repository administrator must require `gate/merge` for `main`, restrict its
source to GitHub Actions, and enable the rule (not evaluation-only).
Keep the existing GitGuardian policy if already managed at organization level.

`.github/rulesets/quality-gates.json` is the proposed active rule (GitHub Actions
app ID 15368). It is a configuration artifact, not an automatically applied
rule. An administrator can incorporate it into existing protection or create
a ruleset if no equivalent rule exists:

```sh
gh api --method POST repos/octopusden/octopus-external-systems-client/rulesets --input .github/rulesets/quality-gates.json
```

To validate enforcement, check repository/organization rules and confirm that a
PR with a failed `gate/merge` cannot merge. The credentials used to prepare this
change have push permission but no administration permission, so repository
rules and Dependabot alert settings were not changed.
