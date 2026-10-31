#!/usr/bin/env python3
"""Validate fdroid-context.json against assets/context.schema.json.

This is a deliberately small JSON Schema subset: it covers the keywords used by the
bundled schema and gives precise dotted paths in error messages.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any

PLACEHOLDER_PATTERNS = [
    re.compile(r"__[A-Z0-9_]+__"),
    re.compile(r"\{\{[^}]+\}\}"),
    re.compile(r"<[A-Z][A-Z0-9_]+>"),
    re.compile(r"\bTODO\b"),
    re.compile(r"\bCHANGEME\b"),
    re.compile(r"\bREPLACE_ME\b"),
]


def type_matches(value: Any, expected: str) -> bool:
    if expected == "object":
        return isinstance(value, dict)
    if expected == "array":
        return isinstance(value, list)
    if expected == "string":
        return isinstance(value, str)
    if expected == "integer":
        return isinstance(value, int) and not isinstance(value, bool)
    if expected == "number":
        return isinstance(value, (int, float)) and not isinstance(value, bool)
    if expected == "boolean":
        return isinstance(value, bool)
    if expected == "null":
        return value is None
    return True


def join(path: str, key: str | int) -> str:
    if isinstance(key, int):
        return f"{path}[{key}]"
    return f"{path}.{key}" if path else key


def check_placeholders(value: Any, path: str, errors: list[str]) -> None:
    if isinstance(value, str):
        for pattern in PLACEHOLDER_PATTERNS:
            if pattern.search(value):
                errors.append(f"{path}: unresolved placeholder {value!r}")
                return
    elif isinstance(value, dict):
        for key, item in value.items():
            check_placeholders(item, join(path, key), errors)
    elif isinstance(value, list):
        for index, item in enumerate(value):
            check_placeholders(item, join(path, index), errors)


def validate(instance: Any, schema: dict[str, Any], path: str, errors: list[str]) -> None:
    if not isinstance(schema, dict):
        return

    expected_type = schema.get("type")
    if expected_type and not type_matches(instance, expected_type):
        errors.append(f"{path or '$'}: expected {expected_type}")
        return

    if "const" in schema and instance != schema["const"]:
        errors.append(f"{path or '$'}: expected constant {schema['const']!r}")

    if "enum" in schema and instance not in schema["enum"]:
        errors.append(
            f"{path or '$'}: expected one of {schema['enum']!r}, got {instance!r}"
        )

    if isinstance(instance, str):
        if "minLength" in schema and len(instance) < schema["minLength"]:
            errors.append(
                f"{path}: shorter than {schema['minLength']} characters"
            )
        if "maxLength" in schema and len(instance) > schema["maxLength"]:
            errors.append(
                f"{path}: longer than {schema['maxLength']} characters"
            )
        if "pattern" in schema and not re.search(schema["pattern"], instance):
            errors.append(
                f"{path}: does not match pattern {schema['pattern']!r}"
            )

    if isinstance(instance, (int, float)) and not isinstance(instance, bool):
        if "minimum" in schema and instance < schema["minimum"]:
            errors.append(f"{path}: must be >= {schema['minimum']}")
        if "maximum" in schema and instance > schema["maximum"]:
            errors.append(f"{path}: must be <= {schema['maximum']}")

    if isinstance(instance, dict):
        required = schema.get("required", [])
        for key in required:
            if key not in instance:
                errors.append(f"{path or '$'}: missing required field {key!r}")

        properties = schema.get("properties", {})
        additional = schema.get("additionalProperties", True)
        for key, value in instance.items():
            if key in properties:
                validate(value, properties[key], join(path, key), errors)
            elif additional is False:
                errors.append(f"{join(path, key)}: unexpected field {key!r}")
            elif isinstance(additional, dict):
                validate(value, additional, join(path, key), errors)

    if isinstance(instance, list):
        if "minItems" in schema and len(instance) < schema["minItems"]:
            errors.append(f"{path}: fewer than {schema['minItems']} items")
        if "maxItems" in schema and len(instance) > schema["maxItems"]:
            errors.append(f"{path}: more than {schema['maxItems']} items")
        if schema.get("uniqueItems"):
            seen = set()
            for index, item in enumerate(instance):
                marker = json.dumps(item, sort_keys=True, ensure_ascii=False)
                if marker in seen:
                    errors.append(f"{join(path, index)}: duplicate item {item!r}")
                seen.add(marker)
        item_schema = schema.get("items")
        if isinstance(item_schema, dict):
            for index, item in enumerate(instance):
                validate(item, item_schema, join(path, index), errors)


def main() -> int:
    parser = argparse.ArgumentParser(description="Validate fdroid-context.json")
    parser.add_argument("context", help="path to fdroid-context.json")
    parser.add_argument(
        "--schema",
        default=str(
            Path(__file__).resolve().parent.parent / "assets" / "context.schema.json"
        ),
        help="path to context.schema.json",
    )
    parser.add_argument("--json", action="store_true", help="print JSON results")
    args = parser.parse_args()

    try:
        context = json.loads(Path(args.context).read_text(encoding="utf-8"))
    except OSError as exc:
        print(f"error: cannot read context: {exc}", file=sys.stderr)
        return 1
    except json.JSONDecodeError as exc:
        print(f"error: context is not valid JSON: {exc}", file=sys.stderr)
        return 1

    try:
        schema = json.loads(Path(args.schema).read_text(encoding="utf-8"))
    except OSError as exc:
        print(f"error: cannot read schema: {exc}", file=sys.stderr)
        return 1
    except json.JSONDecodeError as exc:
        print(f"error: schema is not valid JSON: {exc}", file=sys.stderr)
        return 1

    errors: list[str] = []
    validate(context, schema, "", errors)
    check_placeholders(context, "", errors)

    if args.json:
        print(
            json.dumps(
                {"ok": not errors, "errors": errors}, indent=2, ensure_ascii=False
            )
        )
    else:
        if errors:
            print(f"{len(errors)} error(s):", file=sys.stderr)
            for error in errors:
                print(f"  - {error}", file=sys.stderr)
        else:
            print("context OK")

    return 1 if errors else 0


if __name__ == "__main__":
    raise SystemExit(main())
