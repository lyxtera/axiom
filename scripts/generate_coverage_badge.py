#!/usr/bin/env python3
"""Generate shields-style SVG coverage badge from JaCoCo jacoco.xml (INSTRUCTION counter)."""
import os
import sys
import xml.etree.ElementTree as ET


def main() -> None:
    if len(sys.argv) != 3:
        print(
            "Usage: generate_coverage_badge.py <path/to/jacoco.xml> <path/to/coverage.svg>",
            file=sys.stderr,
        )
        sys.exit(2)

    jacoco_path, out_path = sys.argv[1], sys.argv[2]
    tree = ET.parse(jacoco_path)
    root = tree.getroot()

    missed, covered = 0, 0
    for counter in root.findall("counter"):
        if counter.get("type") == "INSTRUCTION":
            missed = int(counter.get("missed", 0))
            covered = int(counter.get("covered", 0))
            break

    total = missed + covered
    pct = round(covered / total * 100) if total else 0

    if pct >= 80:
        color = "#4c1"
    elif pct >= 60:
        color = "#a4a61d"
    elif pct >= 40:
        color = "#e05d44"
    else:
        color = "#e05d44"

    label = "coverage"
    message = f"{pct}%"
    lw = 63
    mw = 34 + len(message) * 6
    tw = lw + mw

    svg = f"""<svg xmlns="http://www.w3.org/2000/svg" width="{tw}" height="20" role="img" aria-label="{label}: {message}">
  <title>{label}: {message}</title>
  <linearGradient id="s" x2="0" y2="100%">
    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
    <stop offset="1" stop-opacity=".1"/>
  </linearGradient>
  <clipPath id="r"><rect width="{tw}" height="20" rx="3" fill="#fff"/></clipPath>
  <g clip-path="url(#r)">
    <rect width="{lw}" height="20" fill="#555"/>
    <rect x="{lw}" width="{mw}" height="20" fill="{color}"/>
    <rect width="{tw}" height="20" fill="url(#s)"/>
  </g>
  <g fill="#fff" text-anchor="middle" font-family="Verdana,Geneva,DejaVu Sans,sans-serif" font-size="110" text-rendering="geometricPrecision">
    <text x="{lw // 2 * 10}" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)" textLength="{(lw - 10) * 10}">{label}</text>
    <text x="{lw // 2 * 10}" y="140" transform="scale(.1)" fill="#fff" textLength="{(lw - 10) * 10}">{label}</text>
    <text x="{(lw + mw // 2) * 10}" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)" textLength="{(mw - 10) * 10}">{message}</text>
    <text x="{(lw + mw // 2) * 10}" y="140" transform="scale(.1)" fill="#fff" textLength="{(mw - 10) * 10}">{message}</text>
  </g>
</svg>
"""

    os.makedirs(os.path.dirname(os.path.abspath(out_path)) or ".", exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        f.write(svg)

    print(f"Coverage: {pct}% — badge written to {out_path}")


if __name__ == "__main__":
    main()
