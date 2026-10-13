# APK Delivery

Project-local Spec Kit extension for Wallhaven Client.

It adds an `after_implement` hook that builds the personal release APK in CI and copies it into the
Android shared folder (`/mnt/shared/debian/wallhaven-client`), so the APK can be installed from the
phone.

- Command: `speckit.apk-delivery.deliver`
- Hook: `after_implement` (optional, priority 20, runs after `speckit.git.commit`)
- Script: `scripts/build-and-deliver.sh`
- VM-gated: on machines without `/mnt/shared/debian` or `gh` it skips cleanly.

Install (already registered in this repo):

```bash
specify extension add --dev extensions/apk-delivery
```
