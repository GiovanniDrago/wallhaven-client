---
description: "Build the personal release APK in CI and deliver it to the shared folder"
---

# Deliver APK to the shared folder

Build the personal release APK in CI and copy it into the Debian shared folder so it can be
installed from the phone. This only runs on the development VM; on any other machine it prints a
skip notice and exits successfully.

## User Input

```text
$ARGUMENTS
```

## Steps

1. Determine the current branch:

   ```bash
   git rev-parse --abbrev-ref HEAD
   ```

2. Run the delivery script from the repository root:

   ```bash
   ./scripts/build-and-deliver.sh
   ```

   - On the development VM it pushes the branch, dispatches the `Personal Release` workflow on that
     branch, waits for it to finish, and then delivers the newest release APK to the shared folder.
   - Off the VM (no `/mnt/shared/debian` or no `gh`) it prints a skip notice and exits 0.
   - If the working tree is dirty it stops: commit first.

3. Report the delivered APK name and its SHA-256 to the user.
