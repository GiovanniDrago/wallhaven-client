# Wallhaven Client

Private Android client for [wallhaven.cc](https://wallhaven.cc), built with Kotlin and Jetpack Compose.

## Features

- Gallery of the latest wallpapers with infinite scrolling and pull to refresh.
- Search on the wallhaven API with its own persisted filters: relevance and other sortings,
  categories, independent SFW/Sketchy/NSFW purity, minimum and exact resolutions, orientation
  presets (landscape, portrait, square) with exact aspect ratios, and the wallhaven color palette.
  A summary line always shows the filters currently applied.
- Left drawer with favorites, settings and content toggles (NSFW, sketchy, people, anime, general).
- Detail screen with zoomable image, full metadata, tag chips, download and favorite buttons.
- Downloads saved to `Pictures/Wallhaven` through MediaStore.
- Local Room/SQLite cache: every browsed wallpaper, gallery results and search results are stored,
  so they stay available offline and can be favorited.
- Favorites stored in the local database.
- Settings show the account purity allowed by the API key and a diagnostics card with the last
  search request and response purity breakdown.
- The wallhaven API key is encrypted with the Android Keystore in a dedicated preferences file,
  excluded from backups and never stored in the wallpaper database or in a request URL.

## Building

The APK is built by GitHub Actions:

- `Test` runs `./gradlew test assembleDebug` on every push and uploads an installable debug APK.
- `Personal Release` (manual dispatch) builds a signed release APK and attaches it to a GitHub
  release; `scripts/deliver-to-shared.sh` copies it into the Android shared folder.

## License

The secure API key storage pattern is adapted from
[Wallhavend](https://github.com/Attacktive/Wallhavend-android) (MIT License).
