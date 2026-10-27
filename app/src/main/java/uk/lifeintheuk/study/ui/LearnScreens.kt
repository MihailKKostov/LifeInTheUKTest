package uk.lifeintheuk.study.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.TextDecrease
import androidx.compose.material.icons.outlined.TextIncrease
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uk.lifeintheuk.study.data.ProgressStore
import uk.lifeintheuk.study.data.RememberItem
import uk.lifeintheuk.study.data.Repository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(title: String, onBack: () -> Unit, actions: @Composable () -> Unit = {}) {
    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
fun HomeScreen(repo: Repository, progress: ProgressStore, nav: Navigator) {
    val total = repo.allSections.size
    val read = repo.allSections.count { it.id in progress.readSections }
    val best = progress.testResults.maxOfOrNull { it.score }
    var menu by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Life in the UK", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Study companion",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Outlined.MoreVert, "More") }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Reset progress") }, onClick = { menu = false; confirmReset = true })
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        item {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primary) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProgressRing(
                            progress = if (total == 0) 0f else read.toFloat() / total,
                            size = 64.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            track = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                        ) {
                            Text(
                                "${if (total == 0) 0 else read * 100 / total}%",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                if (read == 0) "Let's get started" else if (read == total) "Handbook complete" else "Keep going",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Text(
                                "$read of $total sections read",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row {
                        val onP = MaterialTheme.colorScheme.onPrimary
                        HeroStat("${repo.cards.count { it.id in progress.knownCards }}/${repo.cards.size}", "cards mastered", onP, Modifier.weight(1f))
                        HeroStat(best?.let { "$it/24" } ?: "–", "best mock test", onP, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            val last = progress.lastSection?.let { id -> repo.allSections.firstOrNull { it.id == id } }
            val target = when {
                last != null && last.id !in progress.readSections -> last
                last != null -> repo.nextSection(last.id) ?: repo.allSections.firstOrNull { it.id !in progress.readSections }
                else -> repo.allSections.first()
            }
            if (target != null) {
                Spacer(Modifier.height(4.dp))
                StudyCard(onClick = { nav.push(Screen.Reader(target.id)) }) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.Outlined.AutoStories)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (progress.lastSection == null) "Start reading" else "Continue reading",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(target.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                repo.chapterOfSection(target.id).title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item { SectionLabel("Chapters") }

        items(repo.chapters) { ch ->
            val done = ch.sections.count { it.id in progress.readSections }
            StudyCard(onClick = { nav.push(Screen.Chapter(ch.number)) }) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NumberBadge(ch.number, done == ch.sections.size)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(ch.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                ch.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ThinProgress(done.toFloat() / ch.sections.size, Modifier.weight(1f))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "$done/${ch.sections.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item { SectionLabel("Revise") }
        item {
            val totalFacts = repo.chapters.sumOf { ch -> ch.remember.sumOf { it.items.size } }
            ListRow(
                title = "All key facts",
                subtitle = "${progress.checkedFacts.size} of $totalFacts ticked off",
                icon = Icons.Outlined.Checklist,
                onClick = { nav.push(Screen.Remember(null)) },
            )
        }
        item {
            Text(
                "Condensed from “Life in the United Kingdom: A guide for new residents” (3rd edition). " +
                    "Highlighted text marks facts that often come up in the test.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp, start = 4.dp, end = 4.dp),
            )
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset all progress?") },
            text = { Text("Reading progress, flashcards, ticked facts and test results will be cleared.") },
            confirmButton = {
                TextButton(onClick = { progress.resetAll(); confirmReset = false }) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun HeroStat(value: String, label: String, color: Color, modifier: Modifier) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = color.copy(alpha = 0.75f))
    }
}

@Composable
fun ChapterScreen(repo: Repository, progress: ProgressStore, nav: Navigator, number: Int) {
    val ch = repo.chapter(number)
    val colors = LocalStudyColors.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar("Chapter $number", nav::pop) },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(ch.title, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                HighlightedText(
                    ch.intro,
                    MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item { SectionLabel("Read") }
            items(ch.sections) { s ->
                val done = s.id in progress.readSections
                ListRow(
                    title = s.title,
                    subtitle = "${s.minutes} min read",
                    leading = {
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (done) colors.success else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (done) Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(18.dp))
                            else Text(
                                s.id.substringAfter('.'),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    onClick = { nav.push(Screen.Reader(s.id)) },
                )
            }
            item { SectionLabel("Revise") }
            item {
                val facts = ch.remember.flatMap { it.items }
                ListRow(
                    title = "Key facts to remember",
                    subtitle = "${facts.count { it.id in progress.checkedFacts }} of ${facts.size} ticked off",
                    icon = Icons.Outlined.Checklist,
                    onClick = { nav.push(Screen.Remember(number)) },
                )
            }
            item {
                val cards = repo.cardsFor(number)
                ListRow(
                    title = "Flashcards",
                    subtitle = "${cards.count { it.id in progress.knownCards }} of ${cards.size} mastered",
                    icon = Icons.Outlined.Style,
                    onClick = { nav.push(Screen.Cards("Chapter $number", cards.shuffled().map { it.id })) },
                )
            }
            item {
                val qs = repo.questionsFor(number)
                ListRow(
                    title = "Chapter quiz",
                    subtitle = "${qs.size} practice questions",
                    icon = Icons.Outlined.Quiz,
                    onClick = {
                        nav.push(Screen.Quiz("Chapter $number quiz", qs.shuffled().take(20).map { it.id }, mock = false))
                    },
                )
            }
        }
    }
}

@Composable
fun ReaderScreen(repo: Repository, progress: ProgressStore, nav: Navigator, sectionId: String) {
    val section = repo.section(sectionId)
    val chapter = repo.chapterOfSection(sectionId)
    val next = chapter.sections.getOrNull(chapter.sections.indexOf(section) + 1)
    val scroll = rememberScrollState()
    val scale = progress.textScale

    LaunchedEffect(sectionId) { progress.openSection(sectionId) }
    // maxValue is Int.MAX_VALUE until the content has been measured.
    val atEnd = scroll.maxValue != Int.MAX_VALUE && scroll.value >= scroll.maxValue - 24
    LaunchedEffect(atEnd) { if (atEnd) progress.markRead(sectionId) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                BackTopBar(chapter.title, nav::pop) {
                    IconButton(onClick = { progress.updateTextScale(scale - 0.1f) }) {
                        Icon(Icons.Outlined.TextDecrease, "Smaller text")
                    }
                    IconButton(onClick = { progress.updateTextScale(scale + 0.1f) }) {
                        Icon(Icons.Outlined.TextIncrease, "Larger text")
                    }
                }
                val frac = when (scroll.maxValue) {
                    0 -> 1f
                    Int.MAX_VALUE -> 0f
                    else -> scroll.value.toFloat() / scroll.maxValue
                }
                Box(Modifier.fillMaxWidth().height(2.dp).background(MaterialTheme.colorScheme.outlineVariant)) {
                    Box(Modifier.fillMaxWidth(frac).height(2.dp).background(MaterialTheme.colorScheme.primary))
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(horizontal = 22.dp)
                .navigationBarsPadding()
        ) {
            Spacer(Modifier.height(20.dp))
            Text(
                "SECTION ${section.id}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                section.title,
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif),
            )
            Spacer(Modifier.height(20.dp))
            section.blocks.forEach { BlockView(it, scale) }
            Spacer(Modifier.height(12.dp))

            if (section.remember.isNotEmpty()) {
                KeyFactsCard(section.remember, progress)
                Spacer(Modifier.height(24.dp))
            }

            if (next != null) {
                Button(
                    onClick = { progress.markRead(sectionId); nav.replace(Screen.Reader(next.id)) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Next section", style = MaterialTheme.typography.labelMedium)
                        Text(next.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            } else {
                ChapterEndActions(repo, nav, chapter.number, onContinue = { progress.markRead(sectionId) })
            }
            TextButton(
                onClick = { nav.pop() },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
            ) { Text("Back to chapter") }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun RememberScreen(repo: Repository, progress: ProgressStore, nav: Navigator, chapter: Int?) {
    val chapters = if (chapter == null) repo.chapters else listOf(repo.chapter(chapter))
    val all = chapters.flatMap { ch -> ch.remember.flatMap { it.items } }
    val ticked = all.count { it.id in progress.checkedFacts }
    val colors = LocalStudyColors.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(if (chapter == null) "All key facts" else "Chapter $chapter · Key facts", nav::pop) },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
        ) {
            item {
                Text("Remember these", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "The key facts from every section in one place. Tick each one you know.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ThinProgress(if (all.isEmpty()) 0f else ticked.toFloat() / all.size, Modifier.weight(1f), colors.success)
                    Spacer(Modifier.width(12.dp))
                    Text("$ticked/${all.size}", style = MaterialTheme.typography.labelLarge)
                }
            }
            chapters.forEach { ch ->
                if (chapter == null) {
                    item {
                        Text(
                            "${ch.number}. ${ch.title}",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 28.dp),
                        )
                    }
                }
                ch.remember.forEach { group ->
                    item { SectionLabel(group.title) }
                    item { StudyCard { FactRows(group.items, progress) } }
                }
            }
            if (chapter != null) {
                item { Spacer(Modifier.height(28.dp)); ChapterEndActions(repo, nav, chapter) }
            }
        }
    }
}

/** End-of-chapter choices: move on to the next chapter (or a mock test), or practise this one. */
@Composable
private fun ChapterEndActions(repo: Repository, nav: Navigator, chapter: Int, onContinue: () -> Unit = {}) {
    val next = repo.chapters.firstOrNull { it.number == chapter + 1 }
    Column {
        Button(
            onClick = {
                onContinue()
                if (next != null) nav.replace(Screen.Reader(next.sections.first().id))
                else nav.replace(Screen.Quiz("Mock test", repo.mockTest().map { it.id }, mock = true))
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (next != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Next: Chapter ${next.number}", style = MaterialTheme.typography.labelMedium)
                    Text(next.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            } else {
                Text("You've finished the handbook – take a mock test")
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { nav.push(Screen.Cards("Chapter $chapter", repo.cardsFor(chapter).shuffled().map { it.id })) },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Outlined.Style, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Flashcards")
            }
            OutlinedButton(
                onClick = {
                    nav.push(
                        Screen.Quiz("Chapter $chapter quiz", repo.questionsFor(chapter).shuffled().take(20).map { it.id }, mock = false)
                    )
                },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Outlined.Quiz, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Chapter quiz")
            }
        }
    }
}

/** Key facts at the end of a section, each of which can be ticked off once known. */
@Composable
private fun KeyFactsCard(facts: List<RememberItem>, progress: ProgressStore) {
    val ticked = facts.count { it.id in progress.checkedFacts }
    StudyCard {
        Column {
            Row(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconBadge(Icons.Outlined.Checklist)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Key facts to remember", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Tick each one you know",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("$ticked/${facts.size}", style = MaterialTheme.typography.labelLarge)
            }
            FactRows(facts, progress)
        }
    }
}

@Composable
private fun FactRows(facts: List<RememberItem>, progress: ProgressStore) {
    val colors = LocalStudyColors.current
    Column {
        facts.forEachIndexed { i, fact ->
            val checked = fact.id in progress.checkedFacts
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { progress.toggleFact(fact.id) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Box(
                    Modifier
                        .padding(top = 2.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (checked) colors.success else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    if (checked) Icon(
                        Icons.Filled.Check, null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(15.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                HighlightedText(
                    fact.text,
                    MaterialTheme.typography.bodyLarge,
                    color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
            }
            if (i < facts.lastIndex) {
                Box(
                    Modifier
                        .padding(start = 52.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
    }
}
