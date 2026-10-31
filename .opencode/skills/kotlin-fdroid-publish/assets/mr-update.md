## Summary

| Field | Value |
|---|---|
| App | {{APP_NAME}} |
| Package ID | `{{PACKAGE_ID}}` |
| Previous version | `{{PREVIOUS_VERSION}}` (`{{PREVIOUS_VERSION_CODE}}`) |
| New version | `{{VERSION_NAME}}` (`{{VERSION_CODE}}`) |
| Commit | `{{COMMIT}}` |
| Tag | `{{TAG}}` |
| Category | {{CATEGORY}} |
| License | {{LICENSE}} |
| Source | {{SOURCE_CODE}} |
| Issue tracker | {{ISSUE_TRACKER}} |
| Fork | {{FORK_URL}} |
| Branch | `{{BRANCH}}` |
| Signing certificate SHA-256 | `{{SIGNING_KEY_SHA256}}` |
| Reproducible build | {{REPRODUCIBLE}} |

## What changed

{{UPDATE_SUMMARY}}

### Build recipe changes

- Gradle `{{GRADLE_VERSION}}`, AGP `{{AGP_VERSION}}`, JDK `{{JDK_VERSION}}`
- Module/subdir: {{MODULE}}
- Build file: `{{BUILD_FILE}}`
- Signing property: `-P{{SIGNING_PROPERTY}}`
- `SOURCE_DATE_EPOCH`: {{SOURCE_DATE_EPOCH}}
- Tag template: {{TAG_TEMPLATE}}
- Asset template: {{ASSET_TEMPLATE}}
- Other changes: {{BUILD_NOTES}}

## Validation evidence

| Check | Result | Evidence |
|---|---|---|
| `./gradlew assembleRelease -P{{SIGNING_PROPERTY}}` | {{GRADLE_RESULT}} | {{GRADLE_EVIDENCE}} |
| `fdroid readmeta` | {{READMETA_RESULT}} | {{READMETA_EVIDENCE}} |
| `fdroid rewritemeta` | {{REWRITEMETA_RESULT}} | {{REWRITEMETA_EVIDENCE}} |
| `fdroid lint -f` | {{LINT_RESULT}} | {{LINT_EVIDENCE}} |
| `fdroid checkupdates` | {{CHECKUPDATES_RESULT}} | {{CHECKUPDATES_EVIDENCE}} |
| Metadata checker | {{VALIDATOR_RESULT}} | {{VALIDATOR_EVIDENCE}} |
| APK URL and checksum | {{APK_RESULT}} | {{APK_EVIDENCE}} |
| Fork pipeline | {{PIPELINE_RESULT}} | {{PIPELINE_URL}} |

## Checklist

### Metadata

* [ ] Metadata is in `metadata/<applicationId>.yml`.
* [ ] Metadata is valid YAML and uses LF line endings.
* [ ] Only the latest build block is present; no disabled versions.
* [ ] `commit` is the full 40-character hash.
* [ ] `versionCode` is greater than the previous release.
* [ ] `CurrentVersion` and `CurrentVersionCode` match the new release.
* [ ] `UpdateCheckData` reads the version from `{{BUILD_FILE}}`.
* [ ] Descriptions, images, and changelogs remain in the upstream fastlane tree.
* [ ] No unrelated files are included in the MR.

### Build

* [ ] Releases are tagged and auto update is enabled.
* [ ] The Gradle build works without the release keystore.
* [ ] [Reproducible builds](https://f-droid.org/docs/Reproducible_Builds) still work, or the reason they do not is explained: {{REPRODUCIBILITY_NOTES}}
* [ ] The `binary` URL and signing fingerprint match the new release.
* [ ] The issue tracker and author contact are still valid.

### Pipeline

* [ ] All pipelines pass.
* [ ] All warnings and errors in the Reports tab are fixed or explained.

---------------------

{{RFP_ISSUE}}
