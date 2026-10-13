# Wallhaven Client

Unofficial Android client for [wallhaven.cc](https://wallhaven.cc), built with Kotlin and Jetpack Compose.

Wallhaven Client is not affiliated with wallhaven.cc. The site and all the wallpapers shown or
downloaded through the app belong to their respective owners.

## Features

- Gallery of the latest wallpapers with infinite scrolling and pull to refresh.
- Search on the wallhaven API with its own persisted filters: relevance and other sortings,
  categories, independent SFW/Sketchy/NSFW purity, minimum and exact resolutions, orientation
  presets (landscape, portrait, square) with exact aspect ratios, and the wallhaven color palette.
  Results load immediately and the search works with filters only, no text required. A summary
  line always shows the filters currently applied.
- Left drawer with favorites, settings and content toggles (NSFW, sketchy, people, anime, general).
- Detail screen with zoomable image, full metadata, tag chips, download and favorite buttons.
- Downloads saved to `Pictures/Wallhaven` through MediaStore.
- Local Room/SQLite cache: every browsed wallpaper, gallery results and search results are stored,
  so they stay available offline and can be favorited.
- Favorites stored in the local database.
- Albums: create named collections from the side menu and add wallpapers to an existing or new album
  from the detail screen. Album images are cached locally so albums stay viewable offline, and each
  album can be exported to and imported from a JSON file that contains the identifiers of its
  wallpapers.
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

Wallhaven Client is released under the [GNU General Public License v3.0](LICENSE).
Copyright (C) 2026 Giovanni Drago.

The secure API key storage pattern is adapted from
[Wallhavend](https://github.com/Attacktive/Wallhavend-android) (MIT License).
