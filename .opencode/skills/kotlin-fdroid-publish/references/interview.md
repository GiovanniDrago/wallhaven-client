# Phase 0 — Interview and preflight

The interview is a single batch of questions, not a long back-and-forth. Ask only what
you cannot derive from the repository, and propose a default for every question.

## Contents

- Detect the local GitLab identity
- Questions
- Derive version and signing data
- Reproducible tag and asset templates
- Fork preflight
- Repository preflight
- Context file

## 1. Detect the local GitLab identity

Run before asking about the fork:

```bash
command -v glab && glab auth status
glab api user 2>/dev/null | jq -r '.username // empty'
git config --global --get user.name
git config --global --get user.email
```

Rules:

- One authenticated `gitlab.com` account: propose its username as the fork namespace.
- Several hosts/accounts: list them and ask.
- `GITLAB_TOKEN` without `glab`: resolve the token owner through the API, never print the
  token.
- No identity: ask the user to authenticate before creating a fork.

The fork is a fork of **`fdroid/fdroiddata`**, not of the app repository.

## 2. Questions

| Field | Default / source |
|---|---|
| Mode | `new` or `update`; check the upstream metadata URL |
| App repository | `git remote get-url origin` |
| Default branch | `git symbolic-ref refs/remotes/origin/HEAD` |
| Package ID | `namespace` / `applicationId` in the module `build.gradle.kts` |
| App name | `fastlane` title, strings resource, or README |
| Module | `app` unless the project uses another module |
| Build file | `app/build.gradle.kts` or `app/build.gradle` |
| Version name | `versionName = "..."` |
| Version code | `versionCode = N`; must increase on every release |
| Release tag | stable template, e.g. `personal-WallhavenClient-0.5.0-6` |
| Asset name | stable template, e.g. `WallhavenClient-0.5.0-6.apk` |
| Flavor | Gradle product flavor; empty if none |
| License | SPDX identifier from `LICENSE` |
| Author name | `git log`, fastlane, or the user |
| Issue tracker | `<source>/issues` |
| Changelog | `<source>/releases` |
| Website | Optional; do not reuse `SourceCode` |
| RFP issue | `rfp#<number>` for a first submission |
| Fork host | authenticated GitLab host, default `https://gitlab.com` |
| Fork namespace | authenticated username or a group |
| Fork protocol | `ssh` if `ssh -T git@<host>` works, otherwise `https` |
| Signing property | `fdroid` |
| Reproducible | `true` for this skill's default |
| MR language | `en` |

Do not ask for a keystore password. The signing certificate fingerprint is derived from
the release APK, not from the keystore. Do not copy `local.properties` or
`release.keystore` into the context.

## 3. Derive version and signing data

```bash
# package, version, and signing configuration
grep -nE 'namespace|applicationId|versionCode|versionName|signingConfig|signingConfigs' \
  app/build.gradle.kts app/build.gradle 2>/dev/null

# tags
git fetch --tags --force
git tag --sort=-v:refname | head -10
git describe --tags --exact-match 2>/dev/null || true
git rev-parse HEAD

# signing certificate from the release APK
keytool -printcert -jarfile WallhavenClient-0.5.0-6.apk \
  | sed -n 's/[[:space:]]*SHA256: //p' \
  | tr -d ':' | tr '[:upper:]' '[:lower:]'
```

If the app uses a version catalog and a `versionCode` computed from a property, find the
real value and record how F-Droid should extract it. `UpdateCheckData` is a regex, not a
Gradle evaluation: the value must be readable from a file at the tag.

## 4. Reproducible tag and asset templates

The tag and asset must be reconstructible from `%v` (versionName) and `%c` (versionCode).
Use both placeholders when the release changes independently:

```text
tag:   personal-WallhavenClient-{version}-{versionCode}
asset: WallhavenClient-{version}-{versionCode}.apk
```

Then the metadata uses:

```yaml
binary: https://github.com/<owner>/<repo>/releases/download/personal-WallhavenClient-%v-%c/WallhavenClient-%v-%c.apk
```

Do not include `GITHUB_RUN_NUMBER` or another CI-only value in the tag or asset: F-Droid
cannot substitute it. The version code itself must be unique per release, so it can play
the role of the run counter.

If the user wants to keep the current tag format with a run number, explain that
reproducible `binary:` cannot be built from it and ask whether to:
- change the tag/asset format; or
- fall back to an unsigned F-Droid build without `binary:`.

## 5. Fork preflight

```bash
encoded="$(python3 -c 'import sys, urllib.parse; print(urllib.parse.quote(sys.argv[1], safe=""))' "$namespace/fdroiddata")"

glab api "projects/$encoded" | jq '{path_with_namespace, visibility, default_branch}'
glab api "projects/$encoded/protected_branches" | jq -r '.[].name'
```

Required:

- fork `visibility` is `public`;
- `master` is not protected in the fork;
- the fork's `master` matches `fdroid/fdroiddata` before branching;
- the authenticated user can push.

Create the fork only after the namespace is confirmed:

```bash
glab repo fork fdroid/fdroiddata --clone --name fdroiddata
```

`glab repo fork` uses the authenticated user's namespace. For a group, use the GitLab UI
or API and clone the resulting URL.

## 6. Repository preflight

In the app checkout:

```bash
git status --porcelain
git remote -v
git tag --points-at HEAD
```

Check the Gradle signing setup:

```bash
grep -n "signingConfigs\|signingConfig\|release.keystore\|KEYSTORE_PASSWORD" \
  app/build.gradle.kts app/build.gradle 2>/dev/null
```

The F-Droid build must not need the release keystore. The expected pattern is a
`-Pfdroid` property that removes the signing config for the F-Droid build:

```kotlin
if (!project.hasProperty("fdroid")) {
    signingConfigs { /* release keystore */ }
}

buildTypes {
    release {
        signingConfig = if (project.hasProperty("fdroid")) {
            null
        } else {
            signingConfigs.getByName("release")
        }
    }
}
```

Stop when:

- the work tree is dirty;
- there is no release tag or release APK;
- `versionCode` did not increase;
- the release tag/asset name contains a CI-only run number;
- the release build cannot run without the keystore and `-Pfdroid` is missing;
- the license is missing or non-FOSS;
- the app depends on Firebase, Google Play Services, or another proprietary component
  without a free build.

## 7. Context file

Start from `assets/context.example.json`, replace every value, and validate:

```bash
scripts/validate_context.py --schema assets/context.schema.json fdroid-context.json
```

The context contains:

- `mode`, `app`, `upstream`, `release`, `android`, `fork`, `category`, `approvals`, `mr`,
  `paths`;
- the category's `sourceSha256`;
- `android.signingProperty`, `android.reproducible`, tag/asset templates;
- `approvals.neverReadyWithoutUser: true`;
- no token, password, keystore path, or secret.

Show the user a short summary and the planned upstream diff before launching the agents.
