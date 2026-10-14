---
description: "Merge the finished feature branch into the base branch and push"
---

# Finish the feature

Merge the current feature branch into the base branch (`main`) and push it. It only runs when the
feature is actually finished: it is a no-op when not on a feature branch, when the working tree is
dirty, when no active feature is registered, or when the feature's `tasks.md` still has open tasks.

## User Input

```text
$ARGUMENTS
```

## Steps

1. Run the finish script from the repository root:

   ```bash
   scripts/finish.sh
   ```

2. Report the feature branch, the base branch, and whether a merge was performed.
