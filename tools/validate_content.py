"""Sanity checks for the study content in app/src/main/assets/content."""
import pathlib
import re
import sys

root = pathlib.Path(__file__).resolve().parent.parent / "app/src/main/assets/content"
errors = []


def check_marks(name, lineno, text):
    if text.count("**") % 2:
        errors.append(f"{name}:{lineno}: unbalanced ** highlight")


for n in range(1, 6):
    path = root / f"chapter{n}.txt"
    sections = 0
    for i, line in enumerate(path.read_text().splitlines(), 1):
        check_marks(path.name, i, line)
        sections += line.startswith("@section ")
    print(f"chapter{n}: {sections} sections")

cards = 0
for i, line in enumerate((root / "cards.txt").read_text().splitlines(), 1):
    line = line.strip()
    if not line or line.startswith("# "):
        continue
    if line.count(" | ") != 1:
        errors.append(f"cards.txt:{i}: expected exactly one ' | ' separator")
    cards += 1
print(f"cards: {cards}")

total = 0
for n in range(1, 6):
    path = root / f"questions{n}.txt"
    blocks = re.split(r"\n\s*\n", path.read_text().strip())
    count = 0
    seen = set()
    for block in blocks:
        lines = [l.strip() for l in block.splitlines() if l.strip()]
        if lines and lines[0].startswith("# "):
            lines = lines[1:]
        if not lines:
            continue
        q = next((l[3:] for l in lines if l.startswith("Q: ")), None)
        opts = [l for l in lines if l[:2] in ("+ ", "- ")]
        right = [l for l in opts if l.startswith("+ ")]
        expl = next((l for l in lines if l.startswith("E: ")), None)
        where = f"{path.name}: {q!r}"
        if q is None:
            errors.append(f"{path.name}: block without Q: {lines[:1]}")
            continue
        key = (q, tuple(sorted(opts)))
        if key in seen:
            errors.append(f"{where}: duplicate question")
        seen.add(key)
        if expl is None:
            errors.append(f"{where}: missing explanation")
        else:
            check_marks(path.name, 0, expl)
        texts = [o[2:] for o in opts]
        if len(set(texts)) != len(texts):
            errors.append(f"{where}: duplicate options")
        is_tf = sorted(texts) == ["False", "True"]
        if "TWO" in q:
            if len(right) != 2 or len(opts) != 4:
                errors.append(f"{where}: TWO-answer question needs 4 options with 2 correct")
        elif len(right) != 1:
            errors.append(f"{where}: needs exactly one correct answer")
        if not is_tf and q.startswith("Which statement") and len(opts) != 2:
            errors.append(f"{where}: statement question needs 2 options")
        if not is_tf and not q.startswith("Which statement") and len(opts) != 4:
            errors.append(f"{where}: expected 4 options")
        count += 1
    total += count
    print(f"questions{n}: {count}")
print(f"questions total: {total}")

if errors:
    print("\n".join(errors))
    sys.exit(1)
print("OK")
