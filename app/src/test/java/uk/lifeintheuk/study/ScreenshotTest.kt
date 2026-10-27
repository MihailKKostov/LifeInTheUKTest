package uk.lifeintheuk.study

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import org.junit.Rule
import org.junit.Test
import uk.lifeintheuk.study.data.ProgressStore
import uk.lifeintheuk.study.ui.CardSessionScreen
import uk.lifeintheuk.study.ui.CardsHomeScreen
import uk.lifeintheuk.study.ui.ChapterScreen
import uk.lifeintheuk.study.ui.HomeScreen
import uk.lifeintheuk.study.ui.Navigator
import uk.lifeintheuk.study.ui.PracticeHomeScreen
import uk.lifeintheuk.study.ui.QuizScreen
import uk.lifeintheuk.study.ui.ReaderScreen
import uk.lifeintheuk.study.ui.RememberScreen
import uk.lifeintheuk.study.ui.Screen
import uk.lifeintheuk.study.ui.StudyTheme

/** Renders key screens to PNGs (./gradlew recordPaparazziDebug) for visual review. */
class ScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6, maxPercentDifference = 0.1)

    private val repo = testRepository()
    private val nav = Navigator()

    private fun progress() = ProgressStore(FakePrefs()).apply {
        resetAll()
        repo.chapter(1).sections.forEach { markRead(it.id) }
        markRead("3.1")
        openSection("3.2")
        repo.cards.take(40).forEach { setCardKnown(it.id, true) }
        addTestResult(19, 24)
        addTestResult(16, 24)
    }

    private fun snap(content: @Composable () -> Unit) = paparazzi.snapshot {
        StudyTheme {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { content() }
        }
    }

    @Test fun home() { val p = progress(); snap { HomeScreen(repo, p, nav) } }
    @Test fun chapter() { val p = progress(); snap { ChapterScreen(repo, p, nav, 3) } }
    @Test fun readerHistory() { val p = progress(); snap { ReaderScreen(repo, p, nav, "3.3") } }
    @Test fun readerValues() { val p = progress(); snap { ReaderScreen(repo, p, nav, "1.1") } }
    @Test fun readerShortWithFacts() { val p = progress(); snap { ReaderScreen(repo, p, nav, "1.2") } }
    @Test fun readerChapterEnd() { val p = progress(); snap { ReaderScreen(repo, p, nav, "2.2") } }
    @Test fun readerInventions() { val p = progress(); snap { ReaderScreen(repo, p, nav, "3.10") } }
    @Test fun remember() { val p = progress(); snap { RememberScreen(repo, p, nav, 2) } }
    @Test fun cardsHome() { val p = progress(); snap { CardsHomeScreen(repo, p, nav) } }
    @Test fun cardSession() {
        val p = progress()
        snap { CardSessionScreen(repo, p, nav, Screen.Cards("Chapter 3", repo.cardsFor(3).map { it.id })) }
    }
    @Test fun practiceHome() { val p = progress(); snap { PracticeHomeScreen(repo, p, nav) } }
    @Test fun quizMulti() {
        val p = progress()
        val ids = repo.questions.filter { it.isMulti }.take(5).map { it.id }
        snap { QuizScreen(repo, p, nav, Screen.Quiz("Chapter 3 quiz", ids, mock = false)) }
    }
    @Test fun mockTest() {
        val p = progress()
        snap { QuizScreen(repo, p, nav, Screen.Quiz("Mock test", repo.mockTest().map { it.id }, mock = true)) }
    }
}

class DarkScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6.copy(nightMode = NightMode.NIGHT))

    private val repo = testRepository()

    @Test fun readerDark() {
        val p = ProgressStore(FakePrefs())
        paparazzi.snapshot { StudyTheme { ReaderScreen(repo, p, Navigator(), "4.2") } }
    }
    @Test fun homeDark() {
        val p = ProgressStore(FakePrefs())
        paparazzi.snapshot {
            StudyTheme {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { HomeScreen(repo, p, Navigator()) }
            }
        }
    }
}
