"""Builds the iPhone-friendly web version from the shared content files.

Writes:
  web/index.html        standalone page (open in any browser or host anywhere)
  <out>/study.html      body-only page for publishing as a claude.ai artifact (optional)
"""
import json
import pathlib
import sys

root = pathlib.Path(__file__).resolve().parent.parent
content_dir = root / "app/src/main/assets/content"
template = (root / "web/template.html").read_text()

content = {
    "chapters": [(content_dir / f"chapter{n}.txt").read_text() for n in range(1, 6)],
    "cards": (content_dir / "cards.txt").read_text(),
    "questions": [(content_dir / f"questions{n}.txt").read_text() for n in range(1, 6)],
}
blob = json.dumps(content, ensure_ascii=False).replace("</", "<\\/").replace("\u2028", "\\u2028").replace("\u2029", "\\u2029")
body = template.replace("/*__CONTENT__*/null", blob)

standalone = (
    "<!doctype html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n"
    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, viewport-fit=cover\">\n"
    "<meta name=\"apple-mobile-web-app-capable\" content=\"yes\">\n"
    "<meta name=\"apple-mobile-web-app-title\" content=\"Life in the UK\">\n"
    "<style>:root{padding-top:env(safe-area-inset-top,0px);padding-bottom:env(safe-area-inset-bottom,0px)}body{margin:0}[hidden]{display:none!important}</style>\n"
    "</head>\n<body>\n" + body + "\n</body>\n</html>\n"
)
(root / "web/index.html").write_text(standalone)
print(f"web/index.html: {len(standalone.encode()) // 1024} KB")

if len(sys.argv) > 1:
    out = pathlib.Path(sys.argv[1]) / "study.html"
    out.write_text(body)
    print(f"{out}: {len(body.encode()) // 1024} KB")
