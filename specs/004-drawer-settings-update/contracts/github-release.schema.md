# External Contract: GitHub Latest Release

The update checker consumes GitHub's public "latest release" endpoint for the app repository:

```text
GET https://api.github.com/repos/GiovanniDrago/wallhaven-client/releases/latest
Accept: application/vnd.github+json
User-Agent: wallhaven-client/<installedVersion>
```

No authentication token and no API key are sent.

## Fields consumed

| GitHub field | Used as | Notes |
|--------------|---------|-------|
| `tag_name` | release version | A leading `v` is stripped; build metadata (`+...`) is ignored when comparing. |
| `html_url` | release page | Fallback download target when no APK asset exists. |
| `assets[].name` | asset selection | An asset is an APK when its name ends with `.apk` (case-insensitive). |
| `assets[].browser_download_url` | download URL | Opened in the browser. |

## Selection rules

1. Among APK assets, prefer the asset named `WallhavenClient-latest.apk`.
2. Otherwise use the first APK asset.
3. Otherwise use `html_url`.

## Example (abridged)

```json
{
  "tag_name": "personal-WallhavenClient-0.5.0-16",
  "html_url": "https://github.com/GiovanniDrago/wallhaven-client/releases/tag/personal-WallhavenClient-0.5.0-16",
  "assets": [
    { "name": "WallhavenClient-0.5.0-16.apk", "browser_download_url": "https://github.com/.../WallhavenClient-0.5.0-16.apk" },
    { "name": "WallhavenClient-latest.apk", "browser_download_url": "https://github.com/.../WallhavenClient-latest.apk" },
    { "name": "WallhavenClient-latest.apk.sha256", "browser_download_url": "https://github.com/.../WallhavenClient-latest.apk.sha256" }
  ]
}
```

Note: the real releases use tags such as `personal-WallhavenClient-0.5.0-16`; the version used for the
comparison is extracted from `tag_name` by taking the numeric version when present (see the
repository's parsing decision), so a non-numeric tag falls back safely to "no update".

## Error handling

- Non-200 responses, timeouts, malformed JSON, or a missing `tag_name` produce `UpdateCheckResult.Error`
  (silent for the automatic check, a localized message for the manual check).
- The checker never crashes the app and never logs secrets.
