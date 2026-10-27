package uk.lifeintheuk.study.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.lifeintheuk.study.data.Block

/** Converts `**text**` markup into highlighted spans, like a highlighter pen on paper. */
@Composable
fun rememberHighlighted(text: String): AnnotatedString {
    val colors = LocalStudyColors.current
    return remember(text, colors) {
        buildAnnotatedString {
            val parts = text.split("**")
            parts.forEachIndexed { i, part ->
                if (i % 2 == 1) {
                    withStyle(
                        SpanStyle(
                            background = colors.highlight,
                            color = colors.onHighlight,
                            fontWeight = FontWeight.SemiBold,
                        )
                    ) { append(part) }
                } else append(part)
            }
        }
    }
}

/** Strips highlight markup for places where plain text is wanted. */
fun String.plain(): String = replace("**", "")

@Composable
fun HighlightedText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    Text(rememberHighlighted(text), style = style, modifier = modifier, color = color)
}

@Composable
fun BlockView(block: Block, scale: Float) {
    val reading = ReadingStyle.copy(
        fontSize = ReadingStyle.fontSize * scale,
        lineHeight = ReadingStyle.lineHeight * scale,
        color = MaterialTheme.colorScheme.onBackground,
    )
    val colors = LocalStudyColors.current
    when (block) {
        is Block.Paragraph -> HighlightedText(block.text, reading, Modifier.padding(bottom = 16.dp))

        is Block.Heading -> Text(
            block.text,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp * scale),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 12.dp, bottom = 10.dp),
        )

        is Block.Bullets -> Column(Modifier.padding(bottom = 16.dp)) {
            block.items.forEach { item ->
                Row(Modifier.padding(vertical = 3.dp)) {
                    Box(
                        Modifier
                            .padding(top = (12 * scale).dp, end = 12.dp, start = 4.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    HighlightedText(item, reading)
                }
            }
        }

        is Block.Person -> Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.Person, null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        block.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp * scale),
                    )
                }
                if (block.text.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    HighlightedText(
                        block.text,
                        reading.copy(fontSize = reading.fontSize * 0.92f, lineHeight = reading.lineHeight * 0.92f),
                    )
                }
            }
        }

        is Block.Tip -> Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.tip,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Row(Modifier.padding(16.dp)) {
                Icon(Icons.Outlined.Lightbulb, null, tint = colors.onTip, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                HighlightedText(
                    block.text,
                    MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp * scale, lineHeight = 22.sp * scale),
                    color = colors.onTip,
                )
            }
        }

        is Block.Quote -> Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(((block.lines.size + 1) * 26 * scale).dp)
                    .background(colors.accent.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
            )
            Column(Modifier.padding(start = 16.dp)) {
                block.lines.forEach {
                    Text(
                        it,
                        style = reading.copy(fontStyle = FontStyle.Italic, fontSize = reading.fontSize * 0.95f),
                    )
                }
                Text(
                    block.source,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        is Block.Table -> Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        ) {
            // Numbered rows get a narrow label column; word labels get a wider one.
            val narrow = block.rows.all { it.first.plain().length <= 3 }
            val labelWidth = if (narrow) 28.dp else (112 * scale).dp
            Column(Modifier.padding(vertical = 6.dp)) {
                block.rows.forEachIndexed { i, (left, right) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        HighlightedText(
                            left,
                            MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp * scale),
                            modifier = Modifier.width(labelWidth).padding(end = 10.dp),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        HighlightedText(
                            right,
                            MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp * scale, lineHeight = 22.sp * scale),
                        )
                    }
                    if (i < block.rows.lastIndex) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    stroke: Dp = 6.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    track: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable () -> Unit = {},
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), label = "ring")
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val s = stroke.toPx()
            val arcSize = Size(this.size.width - s, this.size.height - s)
            val topLeft = Offset(s / 2, s / 2)
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(s))
            drawArc(color, -90f, 360f * animated, false, topLeft, arcSize, style = Stroke(s, cap = StrokeCap.Round))
        }
        content()
    }
}

@Composable
fun ThinProgress(progress: Float, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), label = "bar")
    Box(
        modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
    }
}

/** Rounded card with a subtle outline, used for every list row in the app. */
@Composable
fun StudyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Surface(
        shape = shape,
        color = color,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) { content() }
}

@Composable
fun ListRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    StudyCard(onClick = onClick) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                leading != null -> leading()
                icon != null -> IconBadge(icon)
            }
            if (leading != null || icon != null) Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (trailing != null) trailing()
            else if (onClick != null) Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun IconBadge(icon: ImageVector, container: Color = MaterialTheme.colorScheme.primaryContainer, tint: Color = MaterialTheme.colorScheme.onPrimaryContainer) {
    Box(
        Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(container),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp)) }
}

@Composable
fun NumberBadge(number: Int, done: Boolean = false) {
    val colors = LocalStudyColors.current
    Box(
        Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (done) colors.successContainer else MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            number.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = if (done) colors.success else MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = 20.dp, bottom = 10.dp, start = 4.dp),
    )
}

@Composable
fun StatPill(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
