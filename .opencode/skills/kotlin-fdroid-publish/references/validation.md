# Validation matrix and troubleshooting

Run everything from the `fdroiddata` clone unless stated otherwise. The `fdroid` command
must come from the virtual environment created by `scripts/bootstrap.sh`.

## Contents

- Bootstrap
- Static checks
- Update-check check
- Independent metadata check
- Gradle build test
- Fork pipeline
- Error table
- MR gate

## 1. Bootstrap

```bash
scripts/bootstrap.sh
# prints FDROID_BIN, PYTHON_BIN, and FDROID_VENV
```

The script installs `fdroidserver` (including `yamllint` and PyYAML) in
`$XDG_DATA_HOME/opencode/kotlin-fdroid-publish/venv`, checks `git`, `python3`,
`keytool`, `glab`, and Docker, and records versions. It does not install `glab`
automatically because package names differ across distributions.

## 2. Static checks

```bash
cd fdroiddata

"$FDROID_BIN" readmeta
"$FDROID_BIN" rewritemeta <package-id>
git diff -- metadata/<package-id>.yml
"$FDROID_BIN" lint -f <package-id>
git diff --check
file metadata/<package-id>.yml
```

`fdroid lint -f` also checks formatting. `rewritemeta` removes comments and normalizes
wrapping; apply its exact output.

## 3. Update-check check

```bash
"$FDROID_BIN" checkupdates --allow-dirty <package-id>
git diff -- metadata/<package-id>.yml
```

It may fill `AutoName` or adjust `CurrentVersion`. Review the diff. If it proposes a
different version, the tag, `UpdateCheckData`, or `versionCode` is wrong; fix the metadata
instead of accepting the change blindly.

## 4. Independent metadata check

```bash
scripts/validate_metadata.py \
  --context fdroid-context.json \
  --handoff handoff/upstream.json \
  --metadata fdroiddata/metadata/<package-id>.yml \
  --categories-json handoff/categories.json \
  --check-urls --check-apks --check-remote --check-diff
```

It checks placeholders, required fields, category membership, version-code consistency,
`subdir`, `gradleprops`, `UpdateCheckData`, tag/asset templates, signing fingerprint, line
endings, the git diff, APK URLs, and the local commit/tag. `--check-apks` downloads the APK
and verifies its SHA-256 and signing certificate; omit it only on a metered connection and
say so in the MR.

## 5. Gradle build test

First verify the upstream can build without the keystore:

```bash
export SOURCE_DATE_EPOCH="$(git log -1 --format=%ct)"
./gradlew assembleRelease -Pfdroid
```

The output should be an unsigned APK, normally:

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

`./gradlew` is only for reproducing the upstream build locally. The F-Droid recipe uses
the buildserver's `gradle` binary because the scanner removes the wrapper files.

Check the reported `versionName` and `versionCode`:

```bash
$ANDROID_HOME/build-tools/<version>/aapt dump badging \
  app/build/outputs/apk/release/app-release-unsigned.apk | head
```

Then run F-Droid's own buildserver for the real check:

```bash
git clone --depth=1 https://gitlab.com/fdroid/fdroidserver ~/fdroidserver
sudo docker run --rm -itu vagrant --entrypoint /bin/bash \
  -v "$PWD":/build:z \
  -v ~/fdroidserver:/home/vagrant/fdroidserver:Z \
  registry.gitlab.com/fdroid/fdroidserver:buildserver
```

Inside the container:

```bash
. /etc/profile
export PATH="$HOME/fdroidserver:$PATH" PYTHONPATH="$HOME/fdroidserver"
export JAVA_HOME=$(java -XshowSettings:properties -version 2>&1 > /dev/null \
  | grep 'java.home' | awk -F'=' '{print $2}' | tr -d ' ')
cd /build
fdroid readmeta
fdroid rewritemeta <package-id>
fdroid lint -f <package-id>
fdroid build -t -l <package-id>
```

`-t` keeps output in `tmp/` and forces a build; `-l` builds only the latest version. The
container needs network access and several GB of disk. If the local environment cannot
support it, rely on the fork pipeline and state that in the MR.

## 6. Fork pipeline

Push the branch to the fork. GitLab CI runs the F-Droid checks:

```bash
git -C fdroiddata push -u origin <package-id>
glab -R <namespace>/fdroiddata ci status --branch <package-id>
glab -R fdroid/fdroiddata mr view <mr-id>
```

Do not claim the pipeline is green until it is. A pending pipeline is reported as pending
in the MR body.

## 7. Error table

| Symptom | Cause | Fix |
|---|---|---|
| `rewritemeta` produces a diff | Wrong wrapping, trailing space, key order | Apply the exact `rewritemeta` output |
| `Categories 'X' is not valid` | Old or misspelled category | Re-fetch `categories.yml`, re-propose, confirm |
| `Categories are not set` | Missing list | Add one valid category |
| `versionCode` warning | Not higher than the previous release | Bump it in `app/build.gradle.kts` |
| `CurrentVersionCode` warning | Wrong value | Set it to the release `versionCode` |
| Gradle build needs `release.keystore` | No `-Pfdroid` branch | Add the conditional signing config |
| `app-release-unsigned.apk` not found | AGP output changed | Inspect the build log and set `output` only with a manual build |
| `Dependency metadata` scanner error | Gradle dependency metadata | Add `dependenciesInfo { includeInApk = false; includeInBundle = false }` |
| `unknown maven repo` | Non-default repository | Use a trusted repository or document the exception; do not hide non-free code |
| `NonFree` / `NonFreeDep` | Proprietary dependency | Stop; F-Droid inclusion policy is not satisfied |
| `binary:` URL 404 | Tag/asset name does not match `%v`/`%c` | Fix the workflow templates |
| APK checksum mismatch | Build not reproducible or wrong asset | Fix Gradle/SDK/path/timestamps; do not fake it |
| `AllowedAPKSigningKeys` mismatch | Wrong certificate or re-signed APK | Re-derive the SHA-256 from the referenced APK |
| SDK/platform not found | AGP/compileSdk newer than buildserver | Check the build log; adjust the SDK or document the blocker |
| Pipeline fails on a protected branch | Branch protection in the fork | Unprotect the branch or use another branch |

## 8. MR gate

Before creating the draft MR:

- [ ] only `metadata/<package-id>.yml` changed in `fdroiddata`;
- [ ] `versionCode` increased and the tag points to the release commit;
- [ ] `./gradlew assembleRelease -Pfdroid` produces an unsigned APK;
- [ ] `fdroid readmeta`, `rewritemeta`, `lint -f`, `checkupdates` pass;
- [ ] independent script exits 0;
- [ ] APK URL and checksum match;
- [ ] signing fingerprint matches;
- [ ] fork is public and the branch is not protected;
- [ ] MR body has evidence for every checked box;
- [ ] no placeholder remains;
- [ ] title uses the official format;
- [ ] the MR is draft.

Report the pipeline as `pending` until it finishes. Reviewer feedback is the final
authority; update the metadata and re-run the loop.
