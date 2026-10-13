---
description: "Push the current branch to origin"
---

# Push the current branch

Push the current branch to `origin`, setting the upstream on the first push. Safe everywhere: it is
a no-op outside a Git repository, on a detached HEAD, or when no `origin` remote exists.

## User Input

```text
$ARGUMENTS
```

## Steps

1. Run the push script from the repository root:

   ```bash
   scripts/push.sh
   ```

2. Report the branch name, the remote, and whether it was already up to date.
