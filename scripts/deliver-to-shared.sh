#!/usr/bin/env bash
# Copies the newest personal release APK into the Android shared folder, ready to install.
set -euo pipefail

REPO="${REPO:-GiovanniDrago/wallhaven-client}"
DEST="${DEST:-/mnt/shared/debian/wallhaven-client}"
APP_ID="dev.giovannidrago.wallhaven"

mkdir -p "$DEST"

gh release download --repo "$REPO" --pattern '*.apk' --pattern '*.sha256' --clobber --dir "$DEST"

APK="$(find "$DEST" -maxdepth 1 -name '*.apk' -printf '%T@ %p\n' | sort -nr | head -n 1 | cut -d' ' -f 2-)"
APK_NAME="$(basename "$APK")"
CHECKSUM="$(sha256sum "$APK" | cut -d' ' -f 1)"

(cd "$DEST" && sha256sum "$APK_NAME" > "${APK_NAME}.sha256")
(cd "$DEST" && sha256sum "$APK_NAME" > SHA256.txt)

cat > "$DEST/LEGGIMI.txt" <<EOF
Wallhaven Client personal - istruzioni

1) INSTALLA L'APK
   - Apri ${APK_NAME} con l'app File del telefono.
   - Consenti "installa da origini sconosciute" se richiesto.
   - applicationId: ${APP_ID}
   - SHA-256 dell'APK: ${CHECKSUM}

2) API KEY (opzionale, serve per NSFW)
   - Apri https://wallhaven.cc/settings/account sul telefono e copia la API key.
   - Incollala in Impostazioni dentro l'app: viene cifrata con Android Keystore e non finisce
     mai nel database dei wallpaper.

3) SICUREZZA
   - Questa cartella e' visibile dal telefono: non lasciarci chiavi.
   - Il keystore di firma e la sua password restano solo su Debian (~/.android-keystores/).
EOF

echo "Delivered ${APK_NAME} to ${DEST}"
echo "SHA-256: ${CHECKSUM}"
