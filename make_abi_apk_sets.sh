#!/usr/bin/env bash
set -euo pipefail

AAB="${1:-app.aab}"

SDK_VERSION="${SDK_VERSION:-33}"
SCREEN_DENSITY="${SCREEN_DENSITY:-440}"
LOCALE="${LOCALE:-ru-RU}"

KS_PATH="${KS_PATH:-$HOME/.android/debug.keystore}"
KS_PASS="${KS_PASS:-android}"
KS_ALIAS="${KS_ALIAS:-androiddebugkey}"
KEY_PASS="${KEY_PASS:-android}"

mkdir -p specs out

make_spec() {
  local abi="$1"
  cat > "specs/device-${abi}.json" <<EOF
{
  "supportedAbis": ["$abi"],
  "supportedLocales": ["$LOCALE"],
  "screenDensity": $SCREEN_DENSITY,
  "sdkVersion": $SDK_VERSION
}
EOF
}

# shellcheck disable=SC2001
abi_tag() { echo "$1" | sed 's/-/_/g'; }

build_one() {
  local abi="$1"
  local tag; tag="$(abi_tag "$abi")"

  local spec="specs/device-${abi}.json"
  local apks="out/app_${abi}.apks"
  local unzip_dir="out/unz_${abi}"
  local out_dir="out/${abi}"

  make_spec "$abi"

  bundletool build-apks \
    --bundle="$AAB" \
    --output="$apks" \
    --device-spec="$spec" \
    --ks="$KS_PATH" \
    --ks-pass="pass:$KS_PASS" \
    --ks-key-alias="$KS_ALIAS" \
    --key-pass="pass:$KEY_PASS"

  rm -rf "$unzip_dir" "$out_dir"
  mkdir -p "$unzip_dir" "$out_dir"
  unzip -o "$apks" -d "$unzip_dir" >/dev/null

  # Сохраним весь набор splits (так точно установится)
  cp "$unzip_dir/splits/"*.apk "$out_dir/" 2>/dev/null || true

  # Дополнительно: покажем, что ABI split точно есть
  ls -1 "$out_dir"/*"$tag"*.apk >/dev/null 2>&1 || echo "⚠️ ABI split для $abi не найден по шаблону *$tag* (проверь содержимое out/${abi})"

  (cd out && zip -r "app_${abi}.zip" "$abi" >/dev/null)
  echo "OK: out/app_${abi}.apks + out/app_${abi}.zip"
}

for abi in arm64-v8a armeabi-v7a x86_64 x86; do
  build_one "$abi"
done