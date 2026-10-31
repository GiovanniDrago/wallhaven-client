#!/usr/bin/env python3
"""Fetch the official fdroiddata category list and cache it as JSON."""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import sys
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

SOURCE_URL = (
    "https://gitlab.com/fdroid/fdroiddata/-/raw/master/config/categories.yml"
)


def cache_path() -> Path:
    base = os.environ.get("XDG_CACHE_HOME") or os.path.expanduser("~/.cache")
    return Path(base) / "opencode" / "fdroid-categories.json"


def load_yaml(raw: bytes):
    try:
        import yaml
    except ImportError:
        sys.exit(
            "PyYAML is required. Run scripts/bootstrap.sh and use its Python binary."
        )
    return yaml.safe_load(raw.decode("utf-8"))


def parse_categories(data) -> list[dict[str, str]]:
    if not isinstance(data, dict):
        raise ValueError("categories.yml did not parse as a mapping")
    categories = []
    for key, value in data.items():
        if not isinstance(value, dict):
            continue
        categories.append(
            {
                "name": str(value.get("name") or key),
                "description": str(value.get("description") or ""),
            }
        )
    categories.sort(key=lambda item: item["name"].lower())
    return categories


def fetch(url: str, timeout: int = 30) -> bytes:
    request = urllib.request.Request(
        url,
        headers={"User-Agent": "opencode-kotlin-fdroid-publish/1.0"},
    )
    with urllib.request.urlopen(request, timeout=timeout) as response:
        return response.read()


def age_days(timestamp: str) -> float | None:
    try:
        parsed = datetime.fromisoformat(timestamp.replace("Z", "+00:00"))
    except (TypeError, ValueError):
        return None
    if parsed.tzinfo is None:
        parsed = parsed.replace(tzinfo=timezone.utc)
    return (datetime.now(timezone.utc) - parsed).total_seconds() / 86400


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Fetch and cache the fdroiddata categories.yml"
    )
    parser.add_argument("--output", help="write the JSON payload to this path")
    parser.add_argument(
        "--offline", action="store_true", help="use the cached payload only"
    )
    parser.add_argument(
        "--max-age-days",
        type=int,
        default=30,
        help="warn when the cache is older than this (default: 30)",
    )
    parser.add_argument("--search", help="filter by name or description")
    parser.add_argument("--json", action="store_true", help="print JSON to stdout")
    args = parser.parse_args()

    cache = cache_path()
    payload = None

    if not args.offline:
        try:
            raw = fetch(SOURCE_URL)
            payload = {
                "source": SOURCE_URL,
                "sourceSha256": hashlib.sha256(raw).hexdigest(),
                "fetchedAt": datetime.now(timezone.utc).isoformat(),
                "categories": parse_categories(load_yaml(raw)),
            }
            cache.parent.mkdir(parents=True, exist_ok=True)
            cache.write_text(
                json.dumps(payload, indent=2, ensure_ascii=False) + "\n",
                encoding="utf-8",
            )
        except Exception as exc:  # noqa: BLE001 - keep the cache fallback
            if not cache.exists():
                print(
                    f"error: cannot fetch categories and no cache exists: {exc}",
                    file=sys.stderr,
                )
                return 1
            print(
                f"warning: fetch failed ({exc}); using cached categories",
                file=sys.stderr,
            )

    if payload is None:
        if not cache.exists():
            print(f"error: no category cache at {cache}", file=sys.stderr)
            return 1
        try:
            payload = json.loads(cache.read_text(encoding="utf-8"))
        except json.JSONDecodeError as exc:
            print(f"error: invalid category cache: {exc}", file=sys.stderr)
            return 1

    days = age_days(payload.get("fetchedAt", ""))
    if days is not None and days > args.max_age_days:
        print(
            f"warning: category cache is {days:.1f} days old; "
            "re-fetch before choosing a category",
            file=sys.stderr,
        )

    categories = payload.get("categories", [])
    if args.search:
        needle = args.search.lower()
        categories = [
            item
            for item in categories
            if needle in item["name"].lower()
            or needle in item["description"].lower()
        ]

    result = {**payload, "categories": categories}

    if args.output:
        output = Path(args.output)
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(
            json.dumps(result, indent=2, ensure_ascii=False) + "\n",
            encoding="utf-8",
        )
        print(f"wrote {len(categories)} categories to {output}")

    if args.json or not args.output:
        print(json.dumps(result, indent=2, ensure_ascii=False))

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
