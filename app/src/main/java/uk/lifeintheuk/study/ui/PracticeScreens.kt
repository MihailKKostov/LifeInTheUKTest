package uk.lifeintheuk.study.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import uk.lifeintheuk.study.data.ProgressStore
import uk.lifeintheuk.study.data.Question
import uk.lifeintheuk.study.data.Repository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MOCK_SECONDS = 45 * 60

@Composable
fun PracticeHomeScreen(repo: Repository, progress: ProgressStore, nav: Navigator) {
    val results = progress.testResults
    val colors = LocalStudyColors.current

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text("Practice", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Test yourself in the same format as the real exam.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
        }
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primary) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    val onP = MaterialTheme.colorScheme.onPrimary
                    Text("Mock test", style = MaterialTheme.typography.titleLarge, color = onP)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "24 questions · 45 minutes · pass mark 18 (75%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onP.copy(alpha = 0.8f),
                    )
                    Spacer(Modifier.height(16.dp))
                    Row {
                        HeroNumber(results.size.toString(), "taken", onP, Modifier.weight(1f))
                        HeroNumber(results.count { it.passed }.toString(), "passed", onP, Modifier.weight(1f))
                        HeroNumber(results.maxOfOrNull { it.score }?.let { "$it/24" } ?: "–", "best", onP, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { nav.push(Screen.Quiz("Mock test", repo.mockTest().map { it.id }, mock = true)) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = onP, contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) { Text("Start mock test", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
        if (results.isNotEmpty()) {
            item { SectionLabel("Recent results") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(results.takeLast(10).reversed()) { r ->
                        val fmt = remember { SimpleDateFormat("d MMM", Locale.UK) }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (r.passed) colors.successContainer else colors.dangerContainer,
                        ) {
                            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Text(
                                    "${r.score}/${r.total}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (r.passed) colors.success else colors.danger,
                                )
                                Text(
                                    fmt.format(Date(r.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
        val missed = repo.questions.filter { it.id in progress.missedQuestions }
        if (missed.isNotEmpty()) {
            item { SectionLabel("Weak spots") }
            item {
                ListRow(
                    title = "Review mistakes",
                    subtitle = "${missed.size} questions you got wrong last time",
                    leading = { IconBadge(Icons.Outlined.ErrorOutline, colors.dangerContainer, colors.danger) },
                    onClick = {
                        nav.push(Screen.Quiz("Review mistakes", missed.shuffled().take(30).map { it.id }, mock = false))
                    },
                )
            }
        }
        item { SectionLabel("Quiz by chapter") }
        items(repo.chapters) { ch ->
            val qs = repo.questionsFor(ch.number)
            val answered = qs.count { it.id in progress.answeredQuestions }
            ListRow(
                title = ch.title,
                subtitle = "${qs.size} questions · $answered tried",
                leading = { NumberBadge(ch.number) },
                onClick = {
                    nav.push(Screen.Quiz("Chapter ${ch.number} quiz", qs.shuffled().take(20).map { it.id }, mock = false))
                },
            )
        }
        item {
            Text(
                "Practice questions are written from the handbook for this app. The real test uses its own questions, " +
                    "so focus on understanding the facts rather than memorising answers.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp, start = 4.dp, end = 4.dp),
            )
        }
    }
}

@Composable
private fun HeroNumber(value: String, label: String, color: Color, modifier: Modifier) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = color.copy(alpha = 0.75f))
    }
}

/** A question with its options in display order (shuffled once per quiz). */
private class QuizItem(val question: Question) {
    val order: List<Int> = if (question.isTrueFalse) question.options.indices.toList() else question.options.indices.shuffled()
    val options: List<String> = order.map { question.options[it] }
    val correct: Set<Int> = order.indices.filter { order[it] in question.correct }.toSet()
    val selected = mutableStateListOf<Int>()
    var checked by mutableStateOf(false)
    val isRight: Boolean get() = selected.toSet() == correct

    fun toggle(i: Int) {
        if (checked) return
        if (!question.isMulti) {
            selected.clear(); selected += i
        } else if (i in selected) {
            selected -= i
        } else {
            if (selected.size >= correct.size) selected.removeAt(0)
            selected += i
        }
    }
}

@Composable
fun QuizScreen(repo: Repository, progress: ProgressStore, nav: Navigator, screen: Screen.Quiz) {
    val items = remember {
        val byId = repo.questions.associateBy { it.id }
        screen.questionIds.mapNotNull { byId[it] }.map { QuizItem(it) }
    }
    var index by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(MOCK_SECONDS) }
    var confirmExit by remember { mutableStateOf(false) }
    var confirmFinish by remember { mutableStateOf(false) }

    fun finish() {
        if (finished) return
        if (screen.mock) {
            items.forEach { progress.recordAnswer(it.question.id, it.isRight) }
            progress.addTestResult(items.count { it.isRight }, items.size)
        }
        finished = true
    }

    if (screen.mock && !finished) {
        LaunchedEffect(Unit) {
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
            finish()
        }
    }

    androidx.activity.compose.BackHandler(enabled = !finished) { confirmExit = true }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BackTopBar(screen.title, onBack = { if (finished) nav.pop() else confirmExit = true }) {
                if (screen.mock && !finished) {
                    val low = secondsLeft < 5 * 60
                    Row(
                        Modifier.padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.Timer, null,
                            modifier = Modifier.size(18.dp),
                            tint = if (low) LocalStudyColors.current.danger else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "%d:%02d".format(secondsLeft / 60, secondsLeft % 60),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (low) LocalStudyColors.current.danger else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (items.isEmpty()) {
                Text("No questions here yet.", Modifier.align(Alignment.Center))
            } else if (finished) {
                Results(repo, items, screen, nav)
            } else {
                QuestionPage(
                    items = items,
                    index = index,
                    mock = screen.mock,
                    onJump = { index = it },
                    onCheck = {
                        items[index].checked = true
                        progress.recordAnswer(items[index].question.id, items[index].isRight)
                    },
                    onNext = { if (index < items.lastIndex) index++ else finish() },
                    onPrev = { if (index > 0) index-- },
                    onFinish = { confirmFinish = true },
                )
            }
        }
    }

    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text("Leave this ${if (screen.mock) "test" else "quiz"}?") },
            text = { Text("Your answers so far won't be scored.") },
            confirmButton = { TextButton(onClick = { confirmExit = false; nav.pop() }) { Text("Leave") } },
            dismissButton = { TextButton(onClick = { confirmExit = false }) { Text("Keep going") } },
        )
    }
    if (confirmFinish) {
        val unanswered = items.count { it.selected.size != it.correct.size }
        AlertDialog(
            onDismissRequest = { confirmFinish = false },
            title = { Text("Finish the test?") },
            text = {
                Text(
                    if (unanswered > 0) "You have $unanswered unanswered question${if (unanswered == 1) "" else "s"}. Unanswered questions count as wrong."
                    else "All questions answered. Ready to see your score?"
                )
            },
            confirmButton = { TextButton(onClick = { confirmFinish = false; finish() }) { Text("Finish") } },
            dismissButton = { TextButton(onClick = { confirmFinish = false }) { Text("Review") } },
        )
    }
}

@Composable
private fun QuestionPage(
    items: List<QuizItem>,
    index: Int,
    mock: Boolean,
    onJump: (Int) -> Unit,
    onCheck: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onFinish: () -> Unit,
) {
    val item = items[index]
    val q = item.question
    val colors = LocalStudyColors.current
    val scroll = rememberScrollState()
    LaunchedEffect(index) { scroll.scrollTo(0) }

    Column(Modifier.fillMaxSize().navigationBarsPadding()) {
        if (mock) {
            val rowState = rememberLazyListState()
            LaunchedEffect(index) { rowState.animateScrollToItem((index - 3).coerceAtLeast(0)) }
            LazyRow(
                state = rowState,
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                itemsIndexed(items) { i, it ->
                    val answered = it.selected.size == it.correct.size
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    i == index -> MaterialTheme.colorScheme.primary
                                    answered -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .clickable { onJump(i) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${i + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = when {
                                i == index -> MaterialTheme.colorScheme.onPrimary
                                answered -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        } else {
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                ThinProgress((index + if (item.checked) 1 else 0).toFloat() / items.size, Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                Text("${index + 1} / ${items.size}", style = MaterialTheme.typography.labelLarge)
            }
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                "QUESTION ${index + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            Text(q.text, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                when {
                    q.isMulti -> "Select TWO answers"
                    q.isTrueFalse -> "Is this statement true or false?"
                    else -> "Select ONE answer"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))

            item.options.forEachIndexed { i, text ->
                val selected = i in item.selected
                val isCorrect = i in item.correct
                val (border, bg) = when {
                    item.checked && isCorrect -> colors.success to colors.successContainer
                    item.checked && selected -> colors.danger to colors.dangerContainer
                    selected -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.outlineVariant to MaterialTheme.colorScheme.surfaceContainerLow
                }
                val shape = RoundedCornerShape(16.dp)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(shape)
                        .background(bg)
                        .border(if (selected || (item.checked && isCorrect)) 2.dp else 1.dp, border, shape)
                        .clickable(enabled = !item.checked) { item.toggle(i) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(if (q.isMulti) RoundedCornerShape(8.dp) else CircleShape)
                            .background(if (selected || (item.checked && isCorrect)) border else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            item.checked && isCorrect -> Icon(Icons.Filled.Check, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.surface)
                            item.checked && selected -> Icon(Icons.Filled.Close, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.surface)
                            else -> Text(
                                ('A' + i).toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(text, style = MaterialTheme.typography.bodyLarge)
                }
            }

            if (item.checked) {
                Spacer(Modifier.height(6.dp))
                Explanation(item)
            }
            Spacer(Modifier.height(16.dp))
        }

        val ready = item.selected.size == item.correct.size
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (mock) {
                OutlinedButton(
                    onClick = onPrev,
                    enabled = index > 0,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                ) { Text("Previous") }
                Button(
                    onClick = if (index == items.lastIndex) onFinish else onNext,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                ) { Text(if (index == items.lastIndex) "Finish test" else "Next") }
            } else {
                Button(
                    onClick = if (item.checked) onNext else onCheck,
                    enabled = item.checked || ready,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        when {
                            !item.checked -> "Check answer"
                            index == items.lastIndex -> "See results"
                            else -> "Next question"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Explanation(item: QuizItem) {
    val colors = LocalStudyColors.current
    val right = item.isRight
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (right) colors.successContainer else colors.dangerContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                if (right) "Correct" else "Not quite",
                style = MaterialTheme.typography.titleMedium,
                color = if (right) colors.success else colors.danger,
            )
            if (item.question.explanation.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                HighlightedText(item.question.explanation, MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun Results(repo: Repository, items: List<QuizItem>, screen: Screen.Quiz, nav: Navigator) {
    val score = items.count { it.isRight }
    val passed = score * 4 >= items.size * 3
    val colors = LocalStudyColors.current
    var showAll by remember { mutableStateOf(false) }
    val shown = if (showAll) items else items.filter { !it.isRight }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ProgressRing(
                    score.toFloat() / items.size,
                    size = 140.dp,
                    stroke = 12.dp,
                    color = if (passed) colors.success else colors.danger,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$score/${items.size}", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "${score * 100 / items.size}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    when {
                        screen.mock && passed -> "Pass"
                        screen.mock -> "Not a pass yet"
                        passed -> "Great work"
                        else -> "Keep practising"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (passed) colors.success else colors.danger,
                )
                Text(
                    if (screen.mock) "You need 18 out of 24 (75%) to pass the real test."
                    else "Wrong answers are saved to “Review mistakes”.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = nav::pop,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) { Text("Done") }
                Button(
                    onClick = {
                        nav.replace(
                            screen.copy(
                                questionIds = if (screen.mock) repo.mockTest().map { it.id } else screen.questionIds.shuffled(),
                                nonce = System.nanoTime(),
                            )
                        )
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) { Text(if (screen.mock) "New test" else "Try again") }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel(if (showAll) "All questions" else "Questions to review", Modifier.weight(1f))
                TextButton(onClick = { showAll = !showAll }) { Text(if (showAll) "Only mistakes" else "Show all") }
            }
        }
        if (shown.isEmpty()) {
            item {
                Text(
                    "No mistakes — every answer was correct.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(4.dp),
                )
            }
        }
        items(shown) { it ->
            ReviewCard(it, items.indexOf(it) + 1)
        }
    }
}

@Composable
private fun ReviewCard(item: QuizItem, number: Int) {
    val colors = LocalStudyColors.current
    StudyCard {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (item.isRight) colors.success else colors.danger),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (item.isRight) Icons.Filled.Check else Icons.Filled.Close, null,
                        Modifier.size(14.dp), tint = MaterialTheme.colorScheme.surface,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text("Question $number", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text(item.question.text, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            val yours = item.selected.map { item.options[it] }
            if (!item.isRight) {
                Text(
                    "Your answer: " + (yours.joinToString(" · ").ifEmpty { "not answered" }),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.danger,
                )
            }
            Text(
                "Correct: " + item.correct.sorted().joinToString(" · ") { item.options[it] },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.success,
                fontWeight = FontWeight.SemiBold,
            )
            if (item.question.explanation.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                HighlightedText(
                    item.question.explanation,
                    MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
