# Quality checks

GitHub pull requests run through `Merge Gate`. Its `gate/merge` job fails unless
every job in its `needs:` list succeeds; `.github/workflows/merge-gate.yml` is the
list. The shared workflows behind those jobs come from `octopus-base`.

Two policies are not obvious from the workflow files:

- **Security findings are report-only.** CodeQL and Trivy findings do not fail the
  gate. A failure of the scanner job itself does.
- **Neither `build` nor `quality` runs the Docker-backed functional tests.** A green
  gate is not evidence that they passed. They run on TeamCity.

## Local validation and reports

```sh
./gradlew build qualityStatic --no-daemon -x test
git fetch --no-tags origin +main:refs/remotes/origin/main
./gradlew qualityCoverage --no-daemon
actionlint
```

Coverage requires no `-x` flags: test selection is configured in the build.
Keep existing detekt/ktlint baselines: increasing a
baseline requires reviewing the newly suppressed findings.

Reports are uploaded as run artifacts by the shared quality workflow; CodeQL and
Trivy results appear in GitHub code scanning.

## Dependency inventory

`Dependency Submission` feeds GitHub's dependency graph from `main`. See
[octopus-base: consumer quality workflows](https://github.com/octopusden/octopus-base/blob/main/docs/consumer-quality-workflows.md)
for what it does and why it is not a pull-request gate.

It supplies inventory for Dependabot alerts; it is not a vulnerability threshold.
Dependency-Check stays disabled until its data feed is stable enough to roll out.

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
