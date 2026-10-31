# Reproducible Gradle builds for F-Droid

F-Droid builds the app from source and compares the result with the developer's signed
APK when `binary:` is present. A mismatch fails the build. Reproducibility is a gate, not
a decoration.

## Contents

- Why it matters
- Stable release tags and assets
- Deterministic Gradle output
- Signing without the release keystore
- Build path and toolchain
- Verify the match
- When reproducibility cannot be achieved

## Why it matters

- The developer's signature stays on the APK, so users can switch between the developer's
  channel and F-Droid without reinstalling.
- F-Droid verifies that the published APK matches the published source.
- If reproducibility cannot be achieved, F-Droid signs with its own key and the app cannot
  switch to developer signing later without a reinstall.

## Stable release tags and assets

F-Droid substitutes only `%v` (versionName) and `%c` (versionCode) in `binary:`. The tag
and asset name must therefore be functions of those two values:

```text
tag:   personal-WallhavenClient-0.5.0-6
asset: WallhavenClient-0.5.0-6.apk
```

```yaml
binary: https://github.com/GiovanniDrago/wallhaven-client/releases/download/personal-WallhavenClient-%v-%c/WallhavenClient-%v-%c.apk
```

Do not include `GITHUB_RUN_NUMBER` or any other CI-only value. The version code must be
bumped for every release, so it is the unique counter.

Example workflow change:

```yaml
      - name: 'Resolve release metadata'
        id: meta
        run: |
          VERSION=$(grep -oP 'versionName = "\K[^"]+' app/build.gradle.kts)
          VERSION_CODE=$(grep -oP 'versionCode = \K\d+' app/build.gradle.kts)
          NAME="WallhavenClient-${VERSION}-${VERSION_CODE}"
          echo "NAME=${NAME}" >> "$GITHUB_OUTPUT"
          echo "VERSION=${VERSION}" >> "$GITHUB_OUTPUT"
          echo "VERSION_CODE=${VERSION_CODE}" >> "$GITHUB_OUTPUT"

      - name: 'Rename and checksum'
        run: |
          NAME="${{ steps.meta.outputs.NAME }}"
          mv app/build/outputs/apk/release/app-release.apk \
             "app/build/outputs/apk/release/${NAME}.apk"
          sha256sum "app/build/outputs/apk/release/${NAME}.apk" \
            | tee "app/build/outputs/apk/release/${NAME}.apk.sha256"

      - name: 'Create release'
        uses: 'softprops/action-gh-release@v2'
        with:
          tag_name: 'personal-${{ steps.meta.outputs.NAME }}'
          target_commitish: ${{ github.sha }}
          files: |
            app/build/outputs/apk/release/${{ steps.meta.outputs.NAME }}.apk
            app/build/outputs/apk/release/${{ steps.meta.outputs.NAME }}.apk.sha256
```

The tag must point at the commit whose build file contains the same `versionCode`.

## Deterministic Gradle output

Add to the root `build.gradle.kts`:

```kotlin
import org.gradle.api.tasks.bundling.AbstractArchiveTask

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
```

Add to the module `android { ... }` block:

```kotlin
dependenciesInfo {
    includeInApk = false
    includeInBundle = false
}
```

Set `SOURCE_DATE_EPOCH` before the build:

```bash
export SOURCE_DATE_EPOCH="$(git log -1 --format=%ct)"
```

Keep the release build free of dynamic version names, build timestamps, hostnames, and
random output directories. Do not enable `-Pandroid.injected.build.*` values that change
between machines.

## Signing without the release keystore

F-Droid must build an unsigned release APK. Add a `-Pfdroid` branch as described in
`references/metadata.md`, and pass it through the metadata:

```yaml
    gradleprops:
      - fdroid
```

If the project uses a manual build instead of the `gradle:` shortcut, pass the same
property directly: `build: - gradle assembleRelease -Pfdroid`.

The upstream release workflow still signs with the real keystore. F-Droid builds
unsigned, compares the unsigned result with the signed reference APK after signature
handling, and then uses the developer's signature when the comparison succeeds.

Never commit `release.keystore`, `local.properties`, or passwords.

## Build path and toolchain

The workflow and the F-Droid build must use the same:

- JDK major version;
- Gradle wrapper / AGP version;
- Android compile SDK and build tools;
- version name and version code;
- product flavor and build type;
- `SOURCE_DATE_EPOCH`.

If the APK embeds absolute paths, build from the same path. The F-Droid checkout is
normally `build/<package-id>`; the workflow can create and build in
`/home/vagrant/build/<package-id>` and the metadata can use `sudo`/`mv` to reproduce it.
Only add this relocation when the build actually needs it; verify with a checksum.

The project uses a recent AGP and compile SDK. If the F-Droid buildserver does not have
them, the build log will say so; do not hide the error with `novcheck` or by removing the
SDK requirement.

## Verify the match

Build the same way and compare:

```bash
sha256sum app/build/outputs/apk/release/WallhavenClient-0.5.0-6.apk
```

Then let F-Droid CI compare the built APK with `binary:`. If it fails, read the diff in
the build log. Common causes:

- different `versionCode` or `versionName`;
- different Gradle/AGP/SDK versions;
- timestamps or file order in the APK;
- different build path;
- `dependenciesInfo` still enabled upstream;
- the release asset is a different build than the tagged commit.

## When reproducibility cannot be achieved

Do not add `binary:` and do not add `AllowedAPKSigningKeys`. F-Droid will build and sign
the APK with its own key. Explain in the MR what was tried. Do not fake a `binary:` URL to
make the checklist look complete.

Because this skill defaults to reproducible mode, stop and ask the user before switching
to the unsigned fallback.
