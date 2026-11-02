package uk.lifeintheuk.study.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import uk.lifeintheuk.study.data.ProgressStore
import uk.lifeintheuk.study.data.Repository

@Composable
fun CardsHomeScreen(repo: Repository, progress: ProgressStore, nav: Navigator) {
    val known = repo.cards.count { it.id in progress.knownCards }
    val learning = repo.cards.filter { it.id !in progress.knownCards }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text("Flashcards", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Quick-fire recall of the facts most likely to come up.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
        }
        item {
            StudyCard {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProgressRing(known.toFloat() / repo.cards.size, size = 56.dp, color = LocalStudyColors.current.success) {
                            Text("${known * 100 / repo.cards.size}%", style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("$known of ${repo.cards.size} mastered", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${learning.size} still to learn",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val deck = (if (learning.isNotEmpty()) learning else repo.cards).shuffled().take(30)
                            nav.push(Screen.Cards("Study session", deck.map { it.id }))
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text(if (learning.isNotEmpty()) "Study 30 unlearned cards" else "Review 30 random cards") }
                }
            }
        }
        item {
            ListRow(
                title = "Shuffle everything",
                subtitle = "All ${repo.cards.size} cards in random order",
                icon = Icons.Outlined.Shuffle,
                onClick = { nav.push(Screen.Cards("All cards", repo.cards.shuffled().map { it.id })) },
            )
        }
        item { SectionLabel("By chapter") }
        items(repo.chapters) { ch ->
            val cards = repo.cardsFor(ch.number)
            val k = cards.count { it.id in progress.knownCards }
            ListRow(
                title = ch.title,
                subtitle = "$k of ${cards.size} mastered",
                leading = { NumberBadge(ch.number, k == cards.size && cards.isNotEmpty()) },
                onClick = { nav.push(Screen.Cards("Chapter ${ch.number}", cards.shuffled().map { it.id })) },
            )
        }
    }
}

@Composable
fun CardSessionScreen(repo: Repository, progress: ProgressStore, nav: Navigator, screen: Screen.Cards) {
    val byId = remember { repo.cards.associateBy { it.id } }
    var deck by remember { mutableStateOf(screen.cardIds) }
    var index by remember { mutableIntStateOf(0) }
    var flipped by remember { mutableStateOf(false) }
    val missed = remember { mutableStateListOf<String>() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(screen.title, nav::pop) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            if (index >= deck.size) {
                SessionSummary(
                    total = deck.size,
                    missed = missed.size,
                    onRetry = {
                        deck = missed.toList().shuffled(); missed.clear(); index = 0; flipped = false
                    },
                    onDone = nav::pop,
                )
                return@Column
            }

            val card = byId.getValue(deck[index])
            Row(verticalAlignment = Alignment.CenterVertically) {
                ThinProgress(index.toFloat() / deck.size, Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                Text("${index + 1} / ${deck.size}", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(20.dp))

            val rotation by animateFloatAsState(if (flipped) 180f else 0f, tween(380), label = "flip")
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 14f * density
                    }
                    .shadow(6.dp, RoundedCornerShape(28.dp))
                    .clickable { flipped = !flipped },
            ) {
                val showBack = rotation > 90f
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = if (showBack) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLowest,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { if (showBack) rotationY = 180f },
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(28.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            if (showBack) "ANSWER" else "CHAPTER ${card.chapter}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (showBack) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (showBack) card.back else card.front,
                            style = if (showBack) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineSmall,
                            color = if (showBack) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        if (!showBack) {
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Tap to reveal",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            val colors = LocalStudyColors.current
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        progress.setCardKnown(card.id, false)
                        missed += card.id
                        index++; flipped = false
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) { Text("Still learning") }
                Button(
                    onClick = {
                        progress.setCardKnown(card.id, true)
                        index++; flipped = false
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.success, contentColor = MaterialTheme.colorScheme.surface),
                ) { Text("Got it") }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SessionSummary(total: Int, missed: Int, onRetry: () -> Unit, onDone: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(bottom = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconBadge(
            Icons.Outlined.TaskAlt,
            container = LocalStudyColors.current.successContainer,
            tint = LocalStudyColors.current.success,
        )
        Spacer(Modifier.height(16.dp))
        Text("Session complete", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(6.dp))
        Text(
            "You knew ${total - missed} of $total cards.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))
        if (missed > 0) {
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                androidx.compose.material3.Icon(Icons.Outlined.Replay, null)
                Spacer(Modifier.width(8.dp))
                Text("Go again with the $missed you missed")
            }
        }
        TextButton(onClick = onDone, modifier = Modifier.padding(top = 8.dp)) { Text("Done") }
    }
}
