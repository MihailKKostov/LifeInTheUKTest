# Life in the UK – Study Companion (Android)

Concise and exact learning for the Life in the UK test.

An offline Android app that condenses *Life in the United Kingdom: A guide for new residents* (3rd edition) into a short, readable narrative that keeps only what is tested, with the facts that matter most highlighted.

**Android:** download [`apk/LifeInTheUK-Study.apk`](apk/LifeInTheUK-Study.apk) to your phone, open it and allow installs from this source when Android asks. Requires Android 8.0 or newer.

**iPhone or any browser:** the same app as a web page, hosted on GitHub Pages at `https://mihailkkostov.github.io/LifeInTheUKTest/`. In Safari, *Share → Add to Home Screen* gives it an app icon. Sign in (Google or email) to sync progress between devices through Firebase.

## Web hosting and sync

The web app is built from `web/template.html` and the shared content files by `tools/build_web.py`, and deployed to GitHub Pages by `.github/workflows/pages.yml` on every push to `main` that touches the web app or content.

Progress sync uses Firebase Authentication and Cloud Firestore (one document per user at `users/{uid}`). It switches on when `web/firebase-config.json` exists; without it the app saves progress in the browser only. One-time setup:

1. **Firebase console → Project settings → Your apps → Add app → Web.** Copy the `firebaseConfig` values into `web/firebase-config.json` (same shape as `web/firebase-config.example.json`). These values are public by design; access is controlled by the security rules and authorised domains below.
2. **Authentication → Sign-in method:** enable **Email/Password** and **Google**.
3. **Authentication → Settings → Authorised domains:** add `mihailkkostov.github.io`.
4. **Firestore Database → Create database** (production mode), then **Rules:** paste `firebase/firestore.rules` and publish (or run `firebase deploy --only firestore:rules`).
5. **GitHub repo → Settings → Pages → Source: GitHub Actions.** Private repositories need a paid GitHub plan for Pages.

## What's inside

| Tab | What it does |
| --- | --- |
| **Learn** | The 5 handbook chapters, rewritten as 36 short sections. History is told in chronological order. Key facts are highlighted like a marker pen, and people, tips, quotes and timelines get their own cards. Every section ends with its own **"Key facts to remember"** checklist you can tick off, and finishing a chapter asks whether you want to take the chapter test or skip to the next chapter. |
| **Cards** | 370 flip-cards. Mark each one "Got it" or "Still learning", then repeat the ones you missed. |
| **Practice** | 495 questions in the real test's formats: single answer, choose TWO, true/false, and "which statement is correct". You can take a chapter quiz with instant explanations, review the questions you got wrong, or sit a **mock test** (24 questions, 45-minute timer, pass mark 18/24). |

Progress (sections read, cards known, ticked facts, mistakes, test history) is stored on the device. The app needs no internet connection and no permissions.

> The practice questions were written for this app from the handbook. The real test uses its own questions, so learn the facts rather than memorising answers.

## Project layout

```
app/src/main/assets/content/   Study content (plain text, easy to edit)
  chapter1-5.txt               Narrative sections, each ending with its key facts
  cards.txt                    Flashcards  (front | back)
  questions1-5.txt             Question bank (Q:, + correct, - wrong, E: explanation)
app/src/main/java/...          Kotlin + Jetpack Compose app
web/template.html              Web version (HTML/CSS/JS); content is injected at build time
web/firebase-config.json       Firebase web app config (enables sign-in and sync)
firebase/firestore.rules       Firestore security rules: each user reads/writes only users/{uid}
.github/workflows/pages.yml    Builds and deploys the web app to GitHub Pages
tools/build_web.py             Builds web/index.html (and the Pages site) from the template and content
tools/validate_content.py      Checks the content files for mistakes
```

The content format is described at the top of `ContentParser.kt`. Wrap text in `**double asterisks**` to highlight it.

## Building

Requires JDK 17+ and the Android SDK (platform 35).

```bash
python3 tools/validate_content.py    # check the content
./gradlew testDebugUnitTest          # content + parser tests
./gradlew assembleRelease            # → app/build/outputs/apk/release/app-release.apk
./gradlew recordPaparazziDebug       # render screenshots of key screens
python3 tools/build_web.py           # → web/index.html (add --site _site for the full Pages site)
```

Release builds are signed with `keystore/release.jks` (password `lifeintheuk`), so newer builds install over older ones. The key is only meant for this personal app.
