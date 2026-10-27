package uk.lifeintheuk.study.data

data class Chapter(
    val number: Int,
    val title: String,
    val subtitle: String,
    val intro: String,
    val sections: List<Section>,
    val remember: List<RememberGroup>,
)

data class Section(
    val id: String,
    val title: String,
    val blocks: List<Block>,
) {
    /** Rough reading time in minutes, used for the section list. */
    val minutes: Int by lazy {
        val words = blocks.sumOf { it.wordCount() }
        (words / 200).coerceAtLeast(1)
    }
}

/** A titled group of "must remember" facts shown at the end of a chapter. */
data class RememberGroup(val title: String, val items: List<RememberItem>)

data class RememberItem(val id: String, val text: String)

sealed interface Block {
    /** Narrative paragraph. Text may contain **highlighted** spans. */
    data class Paragraph(val text: String) : Block

    /** Small heading inside a section. */
    data class Heading(val text: String) : Block

    data class Bullets(val items: List<String>) : Block

    /** Profile card for a notable person. */
    data class Person(val name: String, val text: String) : Block

    /** Exam tip / memory aid. */
    data class Tip(val text: String) : Block

    /** Short quotation (poem, speech, pledge). */
    data class Quote(val lines: List<String>, val source: String) : Block

    /** Two-column table, e.g. year/event. */
    data class Table(val rows: List<Pair<String, String>>) : Block

    fun wordCount(): Int = when (this) {
        is Paragraph -> text.split(' ').size
        is Heading -> 0
        is Bullets -> items.sumOf { it.split(' ').size }
        is Person -> text.split(' ').size
        is Tip -> text.split(' ').size
        is Quote -> lines.sumOf { it.split(' ').size }
        is Table -> rows.size * 6
    }
}

data class Flashcard(
    val id: String,
    val chapter: Int,
    val front: String,
    val back: String,
)

data class Question(
    val id: String,
    val chapter: Int,
    val text: String,
    val options: List<String>,
    val correct: Set<Int>,
    val explanation: String,
) {
    val isMulti: Boolean get() = correct.size > 1
    val isTrueFalse: Boolean get() = options.size == 2 && options.toSet() == setOf("True", "False")
}
