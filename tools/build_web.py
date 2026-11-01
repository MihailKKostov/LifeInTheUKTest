"""Builds the web version of the study app from the shared content files.

  python3 tools/build_web.py                  -> web/index.html (standalone page)
  python3 tools/build_web.py --site _site     -> GitHub Pages site: index.html, icons, manifest
  python3 tools/build_web.py --artifact DIR   -> DIR/study.html, body-only page for a claude.ai artifact

Firebase sync is switched on when web/firebase-config.json exists (the web app config from the
Firebase console). It is left out of the artifact build, which syncs through claude.ai instead.
"""
import argparse
import json
import pathlib
import shutil

root = pathlib.Path(__file__).resolve().parent.parent
web = root / "web"
content_dir = root / "app/src/main/assets/content"


def js_literal(value):
    text = json.dumps(value, ensure_ascii=False)
    return text.replace("</", "<\\/").replace("\u2028", "\\u2028").replace("\u2029", "\\u2029")


def body(firebase):
    content = {
        "chapters": [(content_dir / f"chapter{n}.txt").read_text() for n in range(1, 6)],
        "cards": (content_dir / "cards.txt").read_text(),
        "questions": [(content_dir / f"questions{n}.txt").read_text() for n in range(1, 6)],
    }
    config_path = web / "firebase-config.json"
    config = json.loads(config_path.read_text()) if firebase and config_path.exists() else None
    page = (web / "template.html").read_text()
    page = page.replace("/*__CONTENT__*/null", js_literal(content))
    page = page.replace("/*__FIREBASE__*/null", js_literal(config))
    return page, config is not None


def standalone(page):
    return (
        "<!doctype html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n"
        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, viewport-fit=cover\">\n"
        "<meta name=\"apple-mobile-web-app-capable\" content=\"yes\">\n"
        "<meta name=\"mobile-web-app-capable\" content=\"yes\">\n"
        "<meta name=\"apple-mobile-web-app-title\" content=\"Life in the UK\">\n"
        "<meta name=\"apple-mobile-web-app-status-bar-style\" content=\"default\">\n"
        "<meta name=\"theme-color\" content=\"#f8f7f3\" media=\"(prefers-color-scheme: light)\">\n"
        "<meta name=\"theme-color\" content=\"#121316\" media=\"(prefers-color-scheme: dark)\">\n"
        "<link rel=\"apple-touch-icon\" href=\"icon-180.png\">\n"
        "<link rel=\"icon\" type=\"image/png\" href=\"icon-512.png\">\n"
        "<link rel=\"manifest\" href=\"manifest.webmanifest\">\n"
        "<style>:root{padding-top:env(safe-area-inset-top,0px);padding-bottom:env(safe-area-inset-bottom,0px)}"
        "body{margin:0}[hidden]{display:none!important}</style>\n"
        "</head>\n<body>\n" + page + "\n</body>\n</html>\n"
    )


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--site", help="write a complete static site into this folder")
    parser.add_argument("--artifact", help="write study.html for a claude.ai artifact into this folder")
    args = parser.parse_args()

    page, has_firebase = body(firebase=True)
    html = standalone(page)
    (web / "index.html").write_text(html)
    print(f"web/index.html: {len(html.encode()) // 1024} KB, Firebase sync {'on' if has_firebase else 'off'}")

    if args.site:
        site = pathlib.Path(args.site)
        site.mkdir(parents=True, exist_ok=True)
        (site / "index.html").write_text(html)
        for name in ("icon-180.png", "icon-512.png", "manifest.webmanifest"):
            shutil.copy(web / name, site / name)
        (site / ".nojekyll").write_text("")
        print(f"site written to {site}")

    if args.artifact:
        artifact_page, _ = body(firebase=False)
        out = pathlib.Path(args.artifact) / "study.html"
        out.write_text(artifact_page)
        print(f"{out}: {len(artifact_page.encode()) // 1024} KB")


if __name__ == "__main__":
    main()
