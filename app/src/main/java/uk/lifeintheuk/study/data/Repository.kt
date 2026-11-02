package uk.lifeintheuk.study.data

import android.content.Context

/** Loads all study content once and keeps it in memory. [read] returns a content file's text. */
class Repository(read: (String) -> String) {

    constructor(context: Context) : this({ name ->
        context.assets.open(name).bufferedReader().use { it.readText() }
    })

    val chapters: List<Chapter>
    val cards: List<Flashcard>
    val questions: List<Question>

    init {
        chapters = (1..5).map { n -> ContentParser.parseChapter(n, read("content/chapter$n.txt")) }
        cards = ContentParser.parseCards(read("content/cards.txt"))
        questions = ContentParser.parseQuestions((1..5).joinToString("\n\n") { n -> read("content/questions$n.txt") })
    }

    val allSections: List<Section> by lazy { chapters.flatMap { it.sections } }

    fun chapter(number: Int): Chapter = chapters.first { it.number == number }

    fun chapterOfSection(sectionId: String): Chapter =
        chapters.first { ch -> ch.sections.any { it.id == sectionId } }

    fun section(id: String): Section = allSections.first { it.id == id }

    fun nextSection(id: String): Section? {
        val i = allSections.indexOfFirst { it.id == id }
        return allSections.getOrNull(i + 1)
    }

    fun cardsFor(chapter: Int?): List<Flashcard> =
        if (chapter == null) cards else cards.filter { it.chapter == chapter }

    fun questionsFor(chapter: Int?): List<Question> =
        if (chapter == null) questions else questions.filter { it.chapter == chapter }

    /**
     * Builds a 24-question mock test. The real test draws from the whole handbook, with most
     * questions coming from history, society and government, so we roughly mirror that split.
     */
    fun mockTest(): List<Question> {
        val quota = mapOf(1 to 2, 2 to 1, 3 to 8, 4 to 6, 5 to 7)
        val picked = quota.flatMap { (ch, n) -> questionsFor(ch).shuffled().take(n) }
        val shortfall = 24 - picked.size
        val extra = questions.filterNot { it in picked }.shuffled().take(shortfall)
        return (picked + extra).shuffled()
    }
}
