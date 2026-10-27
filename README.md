# Life in the UK – Study Companion (Android)

Concise and exact learning for the Life in the UK test.

An offline Android app that condenses *Life in the United Kingdom: A guide for new residents* (3rd edition) into a short, readable narrative that keeps only what is tested, with the facts that matter most highlighted.

**Install:** download [`apk/LifeInTheUK-Study.apk`](apk/LifeInTheUK-Study.apk) to your phone, open it and allow installs from this source when Android asks. Requires Android 8.0 or newer.

## What's inside

| Tab | What it does |
| --- | --- |
| **Learn** | The 5 handbook chapters, rewritten as 36 short sections. History is told in chronological order. Key facts are highlighted like a marker pen, and people, tips, quotes and timelines get their own cards. Each chapter ends with a **"Remember these"** checklist you can tick off. |
| **Cards** | 370 flip-cards. Mark each one "Got it" or "Still learning", then repeat the ones you missed. |
| **Practice** | 495 questions in the real test's formats: single answer, choose TWO, true/false, and "which statement is correct". You can take a chapter quiz with instant explanations, review the questions you got wrong, or sit a **mock test** (24 questions, 45-minute timer, pass mark 18/24). |

Progress (sections read, cards known, ticked facts, mistakes, test history) is stored on the device. The app needs no internet connection and no permissions.

> The practice questions were written for this app from the handbook. The real test uses its own questions, so learn the facts rather than memorising answers.

## Project layout

```
app/src/main/assets/content/   Study content (plain text, easy to edit)
  chapter1-5.txt               Narrative sections + "remember" lists
  cards.txt                    Flashcards  (front | back)
  questions1-5.txt             Question bank (Q:, + correct, - wrong, E: explanation)
app/src/main/java/...          Kotlin + Jetpack Compose app
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
```

Release builds are signed with `keystore/release.jks` (password `lifeintheuk`), so newer builds install over older ones. The key is only meant for this personal app.
