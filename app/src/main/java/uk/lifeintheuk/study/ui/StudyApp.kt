package uk.lifeintheuk.study.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import uk.lifeintheuk.study.data.ProgressStore
import uk.lifeintheuk.study.data.Repository

sealed interface Screen {
    data class Chapter(val number: Int) : Screen
    data class Reader(val sectionId: String) : Screen
    data class Remember(val chapter: Int?) : Screen
    data class ChapterComplete(val number: Int) : Screen
    data class Cards(val title: String, val cardIds: List<String>) : Screen
    data class Quiz(
        val title: String,
        val questionIds: List<String>,
        val mock: Boolean,
        /** Chapter to offer next on the results page, when the quiz ends a chapter. */
        val continueTo: Int? = null,
        val nonce: Long = System.nanoTime(),
    ) : Screen
}

/** Simple back stack on top of three bottom-navigation tabs. */
class Navigator {
    val stack = mutableStateListOf<Screen>()
    fun push(screen: Screen) { stack.add(screen) }
    fun replace(screen: Screen) {
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
        stack.add(screen)
    }
    fun pop() { if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex) }
}

@Composable
fun StudyApp(repo: Repository, progress: ProgressStore) {
    val nav = remember { Navigator() }
    var tab by rememberSaveable { mutableIntStateOf(0) }

    BackHandler(enabled = nav.stack.isNotEmpty()) { nav.pop() }
    BackHandler(enabled = nav.stack.isEmpty() && tab != 0) { tab = 0 }

    val top = nav.stack.lastOrNull()
    AnimatedContent(
        targetState = top to nav.stack.size,
        transitionSpec = {
            val forward = targetState.second >= initialState.second
            val enter = slideInHorizontally(tween(260)) { if (forward) it / 6 else -it / 6 } + fadeIn(tween(260))
            val exit = slideOutHorizontally(tween(200)) { if (forward) -it / 8 else it / 8 } + fadeOut(tween(160))
            enter togetherWith exit
        },
        label = "nav",
    ) { (screen, _) ->
        when (screen) {
            null -> Tabs(tab, onTab = { tab = it }, repo, progress, nav)
            is Screen.Chapter -> ChapterScreen(repo, progress, nav, screen.number)
            is Screen.Reader -> ReaderScreen(repo, progress, nav, screen.sectionId)
            is Screen.Remember -> RememberScreen(repo, progress, nav, screen.chapter)
            is Screen.ChapterComplete -> ChapterCompleteScreen(repo, nav, screen.number)
            is Screen.Cards -> CardSessionScreen(repo, progress, nav, screen)
            is Screen.Quiz -> QuizScreen(repo, progress, nav, screen)
        }
    }
}

@Composable
private fun Tabs(tab: Int, onTab: (Int) -> Unit, repo: Repository, progress: ProgressStore, nav: Navigator) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                val items = listOf(
                    Triple("Learn", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
                    Triple("Cards", Icons.Filled.Style, Icons.Outlined.Style),
                    Triple("Practice", Icons.Filled.Quiz, Icons.Outlined.Quiz),
                )
                items.forEachIndexed { i, (label, filled, outlined) ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { onTab(i) },
                        icon = { Icon(if (tab == i) filled else outlined, null) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> HomeScreen(repo, progress, nav)
                1 -> CardsHomeScreen(repo, progress, nav)
                else -> PracticeHomeScreen(repo, progress, nav)
            }
        }
    }
}
