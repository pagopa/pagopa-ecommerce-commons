# pagopa eCommerce commons

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-ecommerce-commons&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=pagopa_pagopa-ecommerce-commons)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-ecommerce-commons&metric=coverage)](https://sonarcloud.io/summary/new_code?id=pagopa_pagopa-ecommerce-commons)

A utility library for the [eCommerce](https://github.com/topics/pagopa-ecommerce) project.

## Requirements

- Java 21 or higher
- Maven 3.6+ for building

## CI

Repo has Github workflow and actions that trigger Azure devops deploy pipeline once a PR is merged on main branch.

In order to properly set version bump parameters for call Azure devops deploy pipelines will be check for the following
tags presence during PR analysis:

| Tag                | Semantic versioning scope | Meaning                                                           |
|--------------------|---------------------------|-------------------------------------------------------------------|
| patch              | Application version       | Patch-bump application version into pom.xml and Chart app version |
| minor              | Application version       | Minor-bump application version into pom.xml and Chart app version |
| major              | Application version       | Major-bump application version into pom.xml and Chart app version |
| skip-release       | Any                       | The release will be skipped altogether                            |

For the check to be successfully passed only one of the `Application version` labels labels must be present for a given PR or the `skip-release` for skipping release step

# Local testing of GitHub Actions workflows

This repository supports local testing of GitHub Actions workflows using [act](https://github.com/nektos/act).
This allows validating workflow logic without triggering real executions on GitHub and without publishing artifacts.

---

## Prerequisites

### Install act

```bash
# macOS
brew install act

# Linux
curl -s https://raw.githubusercontent.com/nektos/act/master/install.sh | sudo bash

# Windows (PowerShell)
winget install nektos.act
```

### Initial configuration

Create a `.secrets` file in the repository root.
This is required because GitHub Actions always expects a `GITHUB_TOKEN` to be present.
A dummy value is enough for local execution since no real GitHub operation is performed.

```bash
echo "GITHUB_TOKEN=dummy" > .secrets
```

Create a `.actrc` file to configure the Docker image and secrets file used by act.
The `catthehacker/ubuntu:full-latest` image provides the closest match to GitHub Actions runners.

```bash
echo "-P ubuntu-latest=catthehacker/ubuntu:full-latest" > .actrc
echo "--secret-file .secrets" >> .actrc
```

Add both files to `.gitignore` so they are never committed:

```
.secrets
event-release.json
```

---

## How local isolation works

When running under `act`, the environment variable `ACT=true` is automatically set.
Each workflow uses this variable to redirect operations that would otherwise touch the real GitHub repository:

| Operation | On GitHub | Under act |
|---|---|---|
| `git push` | pushes to `origin` (GitHub) | pushes to `/tmp/fake_origin.git` (container) |
| Maven deploy | publishes to GitHub Packages | deploys to `/tmp/local-maven-repo` (container) |
| Create PR | opens a real PR on GitHub | prints a summary to the log |

# **Important:** `actions/checkout` reinitializes the git repository inside the Docker container at every run, wiping all remotes configured on the host. For this reason, `fake_origin` is **not** configured on the host machine — it is created inside the container by a dedicated workflow step.

---

## Workflows

### 1. Create develop branch (`create-develop.yml`)

Creates the `develop` branch from `main`, bumps the version to the next `-SNAPSHOT`, and pushes it.

The workflow includes a step that creates a local bare repository inside the container to safely replace the real `origin`:

```yaml
- name: Restore fake_origin for local act testing
  if: ${{ env.ACT == 'true' }}
  run: |
    git fetch --unshallow        # needed to avoid "shallow update not allowed"
    git init --bare /tmp/fake_origin.git
    git remote add fake_origin /tmp/fake_origin.git
```

> `git fetch --unshallow` is required because `actions/checkout` performs a shallow clone (`--depth=1`) by default, and a bare repository does not accept shallow pushes.

**Run locally:**

```bash
act workflow_dispatch -W .github/workflows/create-develop.yml
```

This will:
- create the `develop` branch inside the container
- bump the version in `pom.xml` to the next `-SNAPSHOT`
- commit and push to `/tmp/fake_origin.git` instead of GitHub
- never touch the real `origin` remote

---

### 2. Publish SNAPSHOT artifact (`publish-snapshot.yml`)

Builds and publishes the Maven SNAPSHOT artifact to GitHub Packages.

When running under `act`, the deploy is redirected to a local directory inside the container:

```yaml
- name: Deploy SNAPSHOT to GitHub Package Registry
  shell: bash
  run: |
    if [ "$ACT" = "true" ]; then
      mkdir -p /tmp/local-maven-repo
      REPO="local::file:///tmp/local-maven-repo"
    else
      REPO="github::https://maven.pkg.github.com/pagopa/pagopa-ecommerce-commons"
    fi
    mvn deploy \
      -DaltSnapshotDeploymentRepository="${REPO}" \
      -DskipTests \
      -Dspotless.check.skip=true
```

> The `pom.xml` must contain a `-SNAPSHOT` version before running this workflow locally.
> Since the `develop` branch does not exist on GitHub, the `pom.xml` in the working directory is used directly.
> The current `pom.xml` already contains `3.3.2-SNAPSHOT` so no manual changes are needed.

**Run locally:**

```bash
act workflow_dispatch -W .github/workflows/publish-snapshot.yml
```

This will:
- validate that the version in `pom.xml` ends with `-SNAPSHOT`
- deploy the artifact to `/tmp/local-maven-repo` inside the container
- never publish anything to GitHub Packages

---

### 3. Bump develop after release (`bump-develop.yml`)

Triggered when a release is published on GitHub. Computes the next SNAPSHOT version, updates `pom.xml` on a new branch, and opens a Pull Request toward `develop`.

When running under `act`:
- `git push` is redirected to `/tmp/fake_origin.git`
- the `peter-evans/create-pull-request` step is skipped
- a summary of the PR that would be created is printed to the log instead

**Create the event file** in the repository root:

```bash
# PowerShell
@'
{
  "action": "published",
  "release": {
    "tag_name": "3.3.1",
    "name": "3.3.1"
  }
}
'@ | Out-File -FilePath event-release.json -Encoding utf8

# macOS / Linux
cat > event-release.json << 'EOF'
{
  "action": "published",
  "release": {
    "tag_name": "3.3.1",
    "name": "3.3.1"
  }
}
EOF
```

**Run locally:**

```bash
act release -W .github/workflows/bump-develop.yml --eventpath event-release.json
```

This will:
- read the current version from `pom.xml` on `main`
- compute the next SNAPSHOT version
- update `pom.xml` on a new branch `bump-develop-<version>`
- push to `/tmp/fake_origin.git` instead of GitHub
- print the PR details that would be created, without opening a real PR

---

## Running all workflows in sequence

```bash
# Step 1 — create develop branch
act workflow_dispatch -W .github/workflows/create-develop.yml

# Step 2 — publish SNAPSHOT artifact
act workflow_dispatch -W .github/workflows/publish-snapshot.yml

# Step 3 — bump develop after release
act release -W .github/workflows/bump-develop.yml --eventpath event-release.json
```

When running locally, in all three cases:
- no SNAPSHOT is published to GitHub Packages
- no pull request is created on GitHub
- no push is performed to the real `origin` remote
