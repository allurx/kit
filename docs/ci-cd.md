# CI and releases

## IntelliJ IDEA

1. Set **File → Project Structure → Project → SDK** to **JDK 25**. In
   **Settings → Build Tools → Maven**, select your installed **Maven 3.9.x** or
   **Bundled Maven 3**, keeping your existing Maven user settings and credentials.
   Set **Maven → Runner → JRE** to **Project JDK** (JDK 25).
2. In the right-hand Maven tool window, click **Reload All Maven Projects**, then
   expand the root **kit → Lifecycle**. For daily work, leave the **release** profile
   unchecked and **Skip Tests** off.

| Phase | Purpose |
| --- | --- |
| **test** | Compile and run the existing tests |
| **verify** | Run the preceding phases, including tests and packaging, through verification |
| **install** | Complete `verify`, then install Kit's POMs and built artifacts into the local Maven repository |

Double-click the final phase you need; there is no need to run all three in sequence.
Run **clean** beforehand only when you need to discard previous build outputs.
These native actions use IDEA's Maven Runner and selected profiles. With `release`
unchecked, `verify` and `install` produce main JARs without sources/Javadoc or signing;
neither publishes remotely. GitHub CI performs the full sources/Javadoc check below.

## GitHub CI

`ci.yml` runs for pull requests targeting `dev` or `main`, pushes to either branch,
and manual runs. A newer run cancels an older CI run for the same ref. It uses
Temurin JDK 25 and the `mvn` preinstalled on `ubuntu-24.04`. The Maven version follows
the runner image; the workflow prints `mvn -version` so each run records its toolchain.

The workflow runs `clean verify` once with `release` and `gpg.skip=true`: it cleans,
builds, tests and generates sources/Javadoc without signing or installing Kit locally.
This explicitly checks the additional release artifacts that the default local
Lifecycle entries do not generate. Surefire reports are retained for 14 days,
including on failure. Tests use the module path; the real Chrome test skips unless
`kit.selenium.chromePath` is supplied. Normal CI does not verify real browser startup
or database compatibility. See [the workflow](../.github/workflows/ci.yml) for the invocation.

## Branches and merges

`dev` and `main` are long-lived branches. Use the merge policy recorded in
[AGENTS.md](../AGENTS.md#分支与合并):

| Integration | Merge method |
| --- | --- |
| Short-lived feature, fix or dependency-update branch → `dev` | **Squash and merge**; keep one logical change per PR |
| Release from `dev` → `main` | **Create a merge commit**; preserve shared ancestry between releases |
| Synchronize the verified `main` release commit → `dev` | Fast-forward when possible; use a normal merge commit if the branches have diverged; do nothing if already included |

Squashing a long-lived branch can make later PRs include already-squashed commits
and repeat conflicts. See [GitHub's guidance on long-running branches](https://docs.github.com/en/pull-requests/reference/pull-request-merges#squashing-and-merging-a-long-running-branch).
Check the PR's base/head and explicitly select the appropriate merge method.

## GitHub setup

Create the `maven-central` Environment with:

| Secret | Value |
| --- | --- |
| `CENTRAL_USERNAME` | Central Portal user-token username |
| `CENTRAL_PASSWORD` | Central Portal user-token password |
| `GPG_PRIVATE_KEY` | ASCII-armored signing private key |
| `GPG_PASSPHRASE` | Signing key passphrase |

The token must be authorized for `io.allurx.kit`; publish the corresponding public
GPG key as required by Central. `setup-java` creates Maven settings with server ID
`central`, matching the POM, and imports the signing key for the publishing job.

Limit the Environment to tags matching `v*`. Require a pull request and a successful
`verify` check from GitHub Actions before merging into `main`; no additional human
approval or strict branch-update requirement is needed for this workflow. Protect
`v*` release tags against updates and deletion while allowing new tags to be created.
Repository settings and secrets are configured separately from source changes.

In **Settings → General → Pull Requests**, enable **Allow squash merging** and
**Allow merge commits**, and disable **Allow rebase merging**. These repository-wide
options make both methods available; the merger follows the policy above for each PR.

Actions are pinned to commit SHAs. Dependabot proposes weekly Maven and Actions
version updates to `dev`. Maven plugin versions, including the Help plugin used for
release version checks, are managed in the root POM; workflows invoke the goals
without duplicating version numbers. Only the GitHub Release job has `contents: write`.

## Release

The same procedure applies whether you release through IDEA or delegate the work
to an agent. Complete [GitHub setup](#github-setup) before the first release, and
include the workflow files in the release commit.

### Shared release procedure

1. Review the changes on `dev`, identify the release scope, and choose a stable
   `MAJOR.MINOR.PATCH` version based on compatibility. Confirm that the version has
   not been published and its tag is unused locally and on GitHub.
2. Update the root POM's project version and all four child POMs' parent versions
   together. Run root `verify` with `release` unchecked and fix relevant failures.
   `install` is only needed by other local projects; it is not a release prerequisite.
3. Commit and push the release changes, wait for CI, then
   merge the `dev` → `main` release PR with **Create a merge commit**. Update local
   `main` with `git pull --ff-only` and confirm a clean working tree.
4. Record the final release commit SHA. Query the `CI` workflow run for that exact
   commit on `main` and wait for successful completion. A missing, pending, failed,
   cancelled or skipped run does not satisfy this check. A green result for another
   commit, including the earlier `dev` or pull request commit, is insufficient.
   Resolve unsuccessful CI before continuing; recheck if the release commit changes.
5. Create the annotated `vMAJOR.MINOR.PATCH` tag on that verified commit and push
   that exact tag to trigger the `Release` workflow.
6. Follow both release jobs through completion. Confirm that the public Central
   artifact checks pass and the GitHub Release exists. Review the generated notes
   against this release's changes, correct unrelated history, and add migration
   guidance for breaking changes.
7. Update local `dev` from `origin/dev` with `git pull --ff-only`, then synchronize
   the verified `main` release commit into it. If already included, no merge is
   needed. Otherwise, fast-forward when possible or use a normal merge commit to
   preserve subsequent work on `dev`; do not squash. If `dev` advances, push it and
   wait for its CI to pass. Then report the version, release commit SHA, tag and
   release links.

The person or agent performing the release owns step 4. Git tag operations and the
current release workflow do not query earlier CI results. The release workflow
performs its own build and tests after the tag is pushed.

### Delegating a release

A request such as “帮我把当前工程发布为 vX.Y.Z” authorizes the necessary version
updates, verification, commits, pushes, integration into `main`, tag creation and
push, publication checks, and synchronization of the verified release commit back
into `dev`. The agent follows the shared
procedure, queries and waits for CI itself, and completes the authorized steps
without requesting confirmation for each one. If the version or release scope is
materially unclear, establish it before the dependent actions.

The agent follows GitHub Actions through publication and reports any unresolved
blocker. A successful local tag push alone is not a completed release.

### Create and push the tag in IDEA

Complete steps 1–4 first, then use IDEA's Git tools:

1. Open **Git → New Tag**. Select this repository under **Git Root**, enter the
   release name `vMAJOR.MINOR.PATCH`, and paste the verified release SHA into **Commit**.
2. Enter a non-empty **Message**, such as `Release vMAJOR.MINOR.PATCH`, then click
   **Create Tag**. The message makes this an annotated tag; an empty message creates
   a lightweight tag that the release workflow rejects.
3. In the **Git** tool window (`Alt+9`), open **Tags**, select only this release tag,
   and choose **Push to origin** from its context menu. This pushes the selected tag.
   The **Push tags** groups in **Push Commits** can include additional tags.

Git credentials must permit the push. Creating the tag is local; **pushing it starts
the GitHub release workflow**. Follow **GitHub → Actions → Release** through completion.
An entrusted agent can perform the same annotated-tag creation and exact-tag push
with Git, using the verified SHA and the shared release procedure.

### Git tags and Maven deploy

| Operation | Responsibility |
| --- | --- |
| Push the annotated Git tag | Publish the release marker to GitHub and trigger the `Release` workflow |
| Maven `deploy` | Run the build lifecycle through deployment and upload artifacts to a remote Maven repository |

`deploy` includes tests, packaging, verification and local installation; it does not
create Git tags. GitHub Actions runs it with the `release` profile and Central/GPG
credentials. Running `deploy` locally executes deployment on your machine; it does
not delegate the work to GitHub Actions.

### GitHub Actions

The workflow has two jobs:

1. **`publish`** checks the annotated tag, all module versions and `main` ancestry.
   One `clean deploy` builds, tests, generates sources/Javadoc, signs and publishes
   through the Central plugin, waiting for `PUBLISHED`. Logs and Surefire reports
   are retained for 90 days when report upload succeeds.
2. **`github-release`** downloads the five POMs and twelve main, sources and Javadoc
   JARs from public Maven Central, then creates the GitHub Release with generated
   notes. An existing release is preserved. Rerunning only this job repeats the
   public checks and GitHub Release creation without rebuilding or deploying.

The [release workflow](../.github/workflows/release.yml) owns the publishing command
and its options.

## Failure handling

- If local tag creation succeeds but its push fails, fix Git access and use IDEA's
  Git tools or Git to push only that existing tag. Preserve its verified commit;
  do not recreate or move the tag as a push retry.
- If compilation, tests, documentation or signing fail before upload, fix the cause
  and rerun the failed job when appropriate. A source change needs a new reviewed tag.
- If deploy times out or fails after uploading may have started, inspect the saved
  Maven log and deployment status in Central Portal first. Follow an in-progress
  deployment there; do not blindly rerun the entire release or upload the version again.
- If the `publish` job failed but Central reports `PUBLISHED`, manually confirm that
  the five POMs and twelve main, sources and Javadoc JARs can be downloaded from
  public Maven Central. Then, if the GitHub Release is missing, finish with
  `gh release create <tag> --repo allurx/kit --verify-tag --generate-notes`.
  Review the generated notes using the same release procedure.
- If only the final GitHub Release job failed, rerunning that failed job is sufficient.

Published Central coordinates are immutable. Fix faulty public content in a new version.

## References

- [Maven build lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)
- [IntelliJ IDEA Maven goals](https://www.jetbrains.com/help/idea/work-with-maven-goals.html)
- [IntelliJ IDEA Git tags](https://www.jetbrains.com/help/idea/use-tags-to-mark-specific-commits.html)
- [Central Maven plugin](https://central.sonatype.org/publish/publish-portal-maven/)
- [GPG signing and skip option](https://maven.apache.org/plugins/maven-gpg-plugin/sign-mojo.html)
- [GitHub Release creation](https://cli.github.com/manual/gh_release_create)
