# Merge request body and draft state

F-Droid reviews merge requests, not pull requests. The official template is
`.gitlab/merge_request_templates/Default.md` in `fdroiddata`. It is written for a new app
inclusion; for updates, keep the structure and replace the inclusion-specific text.

Use `assets/mr-new.md` or `assets/mr-update.md`, fill every placeholder, and only check a
box when the MR contains evidence for it.

## Contents

- Title and labels
- Required body sections
- Checklist rules
- Draft lifecycle
- Update MRs

## Title and labels

| Mode | Title | Label |
|---|---|---|
| `new` | `New app: <AppName>` | `New App` |
| `update` | `Update <AppName> to <version>` | none, unless the user asks for an existing label |

Create the MR with `--draft`. GitLab adds the draft marker; do not also prefix the title
with `Draft:`.

For a first submission that closes an RFP issue, keep `Closes rfp#<number>`. Do not invent
an issue number.

## Required body sections

1. **Summary table**: app name, package ID, version/versionCode, commit, tag, category,
   license, source, issue tracker, fork, signing fingerprint, reproducible build,
   fastlane metadata.
2. **What the app does**: one short paragraph from the upstream description.
3. **Why this app**: the unique function over similar apps.
4. **Screenshots**: links to the upstream fastlane images, not uploads to `metadata/`.
5. **Build recipe notes**: Gradle/AGP/JDK versions, module/subdir, signing property,
   `SOURCE_DATE_EPOCH`, tag/asset template.
6. **Validation evidence**: command → result, including the pipeline status and URL.
7. **Official checklist**: every box with evidence or an explanation.

## Checklist rules

- Do not check the inclusion policy unless the user confirmed the license and the absence
  of non-free dependencies.
- Do not check author notification unless the user is the author or provided the link.
- Do not check the pipeline while it is pending or failed.
- Do not check reproducible builds if `binary:` was removed; explain why.
- If a requirement does not apply, leave it unchecked and add a one-line explanation.
- Never paste tokens, keystore contents, or private contact details.

## Draft lifecycle

1. Create the MR with `scripts/create_draft_mr.sh --execute`.
2. Report the URL and pipeline status.
3. Wait for the user's confirmation before marking it ready:

   ```bash
   glab -R fdroid/fdroiddata mr update <mr-id> --ready
   ```

4. After the user marks it ready, handle reviewer feedback in the same branch. Re-run the
   validation loop after every metadata change.

## Update MRs

An update MR should state:

- previous and new version/versionCode;
- what changed in the Gradle build recipe, if anything;
- the new commit and tag;
- whether AGP, Gradle, JDK, or compileSdk changed;
- that only the latest build block is present;
- that descriptions, images, and changelogs remain upstream.

Do not repeat the new-app policy checklist verbatim if it does not apply. Keep the
metadata, pipeline, and reproducible-build checks.
