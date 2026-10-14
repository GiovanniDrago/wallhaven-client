# Git Finish

Project-local Spec Kit extension for Wallhaven Client.

It finishes a feature by merging its branch into the base branch (`main`) and pushing, but only when
it is safe:

- Command: `speckit.git-finish.merge`
- Hook `after_converge`: merge the converged feature into the base branch (`priority 30`)
- Script: `scripts/finish.sh`

It skips (no-op) when outside a Git repo, on a detached HEAD, already on `main`, with a dirty working
tree, with no active feature in `.specify/feature.json`, or when the feature's `tasks.md` still has
open tasks.

Install (already registered in this repo):

```bash
specify extension add --dev extensions/git-finish
```
