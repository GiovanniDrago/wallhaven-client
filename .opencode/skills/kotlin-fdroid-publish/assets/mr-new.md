## Summary

| Field | Value |
|---|---|
| App | {{APP_NAME}} |
| Package ID | `{{PACKAGE_ID}}` |
| Version | `{{VERSION_NAME}}` (`{{VERSION_CODE}}`) |
| Commit | `{{COMMIT}}` |
| Tag | `{{TAG}}` |
| Category | {{CATEGORY}} |
| License | {{LICENSE}} |
| Author | {{AUTHOR_NAME}} |
| Source | {{SOURCE_CODE}} |
| Issue tracker | {{ISSUE_TRACKER}} |
| Fork | {{FORK_URL}} |
| Branch | `{{BRANCH}}` |
| Signing certificate SHA-256 | `{{SIGNING_KEY_SHA256}}` |
| Reproducible build | {{REPRODUCIBLE}} |
| Fastlane metadata | {{FASTLANE_STATUS}} |

## Abstract for new app inclusion

### What the app does

{{APP_SUMMARY}}

### Why this app

{{APP_DIFFERENTIATION}}

### Screenshots

{{SCREENSHOTS}}

## Build recipe

- Build system: Gradle (`{{GRADLE_VERSION}}`), AGP `{{AGP_VERSION}}`, JDK `{{JDK_VERSION}}`
- Module/subdir: {{MODULE}}
- Build file: `{{BUILD_FILE}}`
- Signing property: `-P{{SIGNING_PROPERTY}}`
- `SOURCE_DATE_EPOCH`: {{SOURCE_DATE_EPOCH}}
- Tag template: {{TAG_TEMPLATE}}
- Asset template: {{ASSET_TEMPLATE}}
- Build path: {{BUILD_PATH}}
- Notes: {{BUILD_NOTES}}

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

### Policy

* [ ] The app complies with the [inclusion policy](https://f-droid.org/docs/Inclusion_Policy). _Pay special attention to Quality point 6._
* [ ] The original app author has been notified (and does not oppose the inclusion). If you are not the author, please paste the link of the reply from the author: {{AUTHOR_NOTIFICATION}}
* [ ] The upstream app source code repo contains the app metadata in a [Fastlane](https://gitlab.com/snippets/1895688) or [Triple-T](https://gitlab.com/snippets/1901490) folder structure. The summary and description must be included and images, icon, and changelog should also be provided for better user experience. The `en-US` locale must be included.

### Docs

* [ ] Please read [the guide](https://gitlab.com/fdroid/fdroiddata/-/blob/master/CONTRIBUTING.md) first if this is your first contribution.
* [ ] Please make sure your metadata follows the best practice in [our templates](https://gitlab.com/fdroid/fdroiddata/tree/master/templates).
* [ ] Please read the [Build Metadata Reference](https://f-droid.org/docs/Build_Metadata_Reference/) and make sure your metadata is valid.
* [ ] Please read the [Quick Start Guide](https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/).

### Merge Request Setup

* [ ] The title of this merge request should follow "New app: app name" format.
* [ ] Please make sure your fdroiddata fork is public and your branch is not protected. See <https://docs.gitlab.com/user/project/repository/branches/protected/>.
* [ ] Please read [our Git guide](https://gitlab.com/fdroid/wiki/-/wikis/Tips-for-fdroiddata-contributors/Git-Usage) if you don't know how to rebase your branch. Don't rebase your branch if there is no conflict.
* [ ] All related [fdroiddata](https://gitlab.com/fdroid/fdroiddata/issues) and [RFP issues](https://gitlab.com/fdroid/rfp/issues) have been referenced in this merge request.
* [ ] Please only submit one app in one MR.

### Metadata

* [ ] Metadata must be put in `metadata/<applicationId>.yml`.
* [ ] Metadata must be a valid YAML file.
* [ ] Metadata must use LF as line ending.
* [ ] Don't add summary/description/changelog/images or anything that should be provided in upstream repo. Please check the Changes tab to make sure there is no other unrelated files added in the MR.
* [ ] Releases are tagged and auto update is enabled unless there is a special reason.
* [ ] There is an issue tracker and contact info of the author so that we can report bugs and contact the author.
* [ ] An AuthorName must be added. It doesn't need to be the real name.
* [ ] External repos are added as git submodules instead of srclibs. You can update git submodules without opening an MR in this repo and the submodule is covered by our scanner.
* [ ] Enable [Reproducible Builds](https://f-droid.org/docs/Reproducible_Builds). We'll use your signature for improved security/reliability, also allowing users to switch between different channels. Do note that if you don't enable reproducible build then the apk will be signed with our key so you can't enable it later. If you can't enable this, please add the reasons here: {{REPRODUCIBILITY_NOTES}}
* [ ] Setup abi split if the APK is large and the splitted ones can be much smaller.
* [ ] Only the latest versions should be kept in the metadata before it's merged. If you update the metadata, please replace the old versions with the new ones.
* [ ] Don't add any disabled versions in the metadata.
* [ ] The `commit` field should be the full hash. Please don't use tag or branch in `commit`.

### Pipeline

* [ ] All pipelines should pass.
* [ ] All warnings and errors in the Reports tab should be fixed or explained.
* [ ] F-Droid CI runners are under GitLab's FOSS program, so there's no need for you to pay for any CI time. If Gitlab starts asking for phone numbers or credit cards don't submit anything, just leave a note in the MR so we know we need to trigger the CI.

---------------------

Closes {{RFP_ISSUE}}

/label ~"New App"
