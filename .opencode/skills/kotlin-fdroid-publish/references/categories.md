# Categories — fetch, propose, confirm

F-Droid categories change over time. Never hardcode a list and never reuse an old
example such as `Games`: the current configuration uses granular names like
`Action Game`, `Puzzle Game`, `AI Chat`, `App Store & Updater`, and `Code & Forge`.

## 1. Fetch the official list

The source of truth is:

```text
https://gitlab.com/fdroid/fdroiddata/-/raw/master/config/categories.yml
```

Use the bundled script so the cache, SHA-256, and timestamp are recorded:

```bash
scripts/fetch_categories.py --output handoff/categories.json
jq -r '.sourceSha256, .fetchedAt' handoff/categories.json
jq -r '.categories[] | "\(.name): \(.description)"' handoff/categories.json
```

Use the cached copy only when the network is unavailable. Warn the user if the cache is
older than 30 days; the category may have been renamed.

## 2. Build a proposal

Read the app's own words, not only its name:

- `fastlane/metadata/android/en-US/short_description.txt`
- `fastlane/metadata/android/en-US/full_description.txt`
- `README.md`
- the Android launcher label
- existing screenshots or store listing

Then propose:

- **one primary category** with a one-sentence justification;
- **two alternatives** with the reason they are plausible;
- no more than three options, so the question stays easy to answer.

Example:

```text
Proposal: primary "Note" (the app is a local-first notes and checklists tool),
alternatives "Task" (it has recurring reminders) and "Text Editor" (it supports Markdown).
Which one is the best fit?
```

Do not choose a category by keyword matching alone. Explain the user-facing purpose of
the app and why the category fits that purpose.

## 3. Confirm with the user

Ask the user to confirm or change the primary category. Record the answer in
`fdroid-context.json`:

```json
"category": {
  "primary": "Note",
  "alternatives": ["Task", "Text Editor"],
  "sourceSha256": "<sha256 from categories.json>"
}
```

If the user wants two categories, keep the primary first and add the second to
`alternatives`; only add it to `Categories` in the metadata after confirming that the
second category is also valid.

## 4. Validate

`fdroid lint` validates `Categories` against the `config/categories.yml` of the checkout,
so run it inside the `fdroiddata` clone:

```bash
fdroid lint -f <package-id>
```

If the category is rejected:

1. re-fetch the list and check the exact spelling;
2. re-propose with the corrected name;
3. do not add `config/categories.yml` to an app MR; that is a separate proposal.

## 5. When nothing fits

If no existing category is a reasonable fit:

- choose the closest category and say explicitly that it is an approximation; or
- stop and propose a new category to F-Droid as a separate change.

Do not invent a category in `metadata/<package-id>.yml`. The client shows unknown
categories, but the F-Droid review process expects the official list.
