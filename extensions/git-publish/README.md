# Git Publish

Project-local Spec Kit extension for Wallhaven Client.

It publishes the current branch so the CI APK deploy always builds a pushed commit:

- Command: `speckit.git-publish.push`
- Hook `after_implement`: push the branch before the APK deploy (`priority 15`)
- Hook `after_converge`: commit + push the branch (`priority 10` then `20`)
- Script: `scripts/push.sh` (no-op outside a Git repo, on detached HEAD, or without `origin`)

Install (already registered in this repo):

```bash
specify extension add --dev extensions/git-publish
```
