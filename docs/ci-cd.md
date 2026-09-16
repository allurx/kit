# CI and releases

Kit's root POM inherits build configuration from `io.allurx:maven-parent`.
Kit retains its four modules (`kit-base`, `kit-json`, `kit-mybatis`, `kit-selenium`),
business dependencies, JMH annotation processor and project metadata. Parent and
dependency versions are defined in the POMs.

## Shared workflows

Both callers follow `allurx/allurx-build` at `@main`:

| Kit caller | Shared workflow |
| --- | --- |
| [ci.yml](../.github/workflows/ci.yml) | `allurx/allurx-build/.github/workflows/maven-ci.yml@main` |
| [release.yml](../.github/workflows/release.yml) | `allurx/allurx-build/.github/workflows/maven-central-release.yml@main` |

Use the shared repository's documentation for all common setup and behavior:

- [Integration and GitHub setup](https://github.com/allurx/allurx-build#readme)
- [Maven contract](https://github.com/allurx/allurx-build/blob/main/docs/maven-contract.md)
- [Release procedure, evidence and failure handling](https://github.com/allurx/allurx-build/blob/main/docs/release-guide.md)

After migrating, update required checks to the actual check names emitted by the
reusable workflow; do not assume the former standalone `verify` check still applies.

## Local verification

Use JDK 25 and Maven 3.9.16 to match the current shared toolchain. In IDEA, select
that Maven installation and set **Maven → Runner → JRE** to **Project JDK**.
Run from the repository root:

```sh
mvn -version
mvn -pl kit-mybatis -am verify
mvn -Prelease "-Dgpg.skip=true" clean verify
```

The first build covers base, JSON and MyBatis; the second verifies the full reactor
and sources/Javadoc without signing or publishing. Read
[kit-selenium/AGENTS.md](../kit-selenium/AGENTS.md) before full-reactor tests.
The real Chrome test skips unless `kit.selenium.chromePath` is supplied; a skipped
test does not verify browser startup. These tests do not prove database compatibility.
JMH benchmarks remain separate from JUnit.

## Branches and merges

| Integration | Merge method |
| --- | --- |
| Short-lived feature/fix/dependency branch → `dev` | **Squash and merge** |
| Release from `dev` → `main` | **Create a merge commit** |
| Verified `main` release commit → `dev` | Fast-forward if possible; normal merge if diverged; no action if already included |

Check base/head before merging. After publication, synchronize the verified release
commit back into `dev`, preserve subsequent work, then push and verify CI if `dev`
advances. See [AGENTS.md](../AGENTS.md#分支与合并).

## Release

Update the root Kit project version and all four child POMs' Kit parent versions
together; the external `maven-parent` version is independent. Validate, integrate
`dev` into `main`, and follow the shared release guide above. Before pushing an
annotated release tag, confirm that the exact release SHA's latest `main` push run
of `.github/workflows/ci.yml` and its latest attempt succeeded. PR, `dev` and manual
CI runs do not satisfy this check.

An explicit request to release a specified version authorizes the necessary version
updates, validation, commits/pushes, integration into `main`, annotated tag creation
and push, publication checks, and synchronization back into `dev`. Source migration
alone does not authorize commits, pushes, releases or remote settings changes.
Report completion only after the shared procedure's public verification and GitHub
Release checks pass; review release notes and add migration guidance for breaking changes.
