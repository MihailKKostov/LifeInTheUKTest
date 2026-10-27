package uk.lifeintheuk.study

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.lifeintheuk.study.data.Block
import uk.lifeintheuk.study.data.Repository
import java.io.File

fun testRepository() = Repository { name -> File("src/main/assets/$name").readText() }

class ContentTest {
    private val repo = testRepository()

    @Test
    fun everyChapterHasSectionsAndFacts() {
        assertEquals(5, repo.chapters.size)
        repo.chapters.forEach { ch ->
            assertTrue("chapter ${ch.number} title", ch.title.isNotBlank())
            assertTrue("chapter ${ch.number} sections", ch.sections.isNotEmpty())
            assertTrue("chapter ${ch.number} remember", ch.remember.all { it.items.isNotEmpty() })
            ch.sections.forEach { s -> assertTrue("empty section ${s.id}", s.blocks.isNotEmpty()) }
        }
    }

    @Test
    fun tablesAndPeopleParse() {
        val blocks = repo.allSections.flatMap { it.blocks }
        assertTrue(blocks.any { it is Block.Table })
        assertTrue(blocks.filterIsInstance<Block.Person>().all { it.text.isNotBlank() })
        assertTrue(blocks.filterIsInstance<Block.Quote>().all { it.lines.isNotEmpty() && it.source.isNotBlank() })
    }

    @Test
    fun questionsAreWellFormed() {
        assertTrue(repo.questions.size > 400)
        repo.questions.forEach { q ->
            val expected = if ("TWO" in q.text) 2 else 1
            assertEquals(q.text, expected, q.correct.size)
            assertTrue(q.text, q.explanation.isNotBlank())
        }
        (1..5).forEach { assertTrue(repo.questionsFor(it).isNotEmpty()) }
    }

    @Test
    fun mockTestHas24UniqueQuestions() {
        repeat(20) {
            val mock = repo.mockTest()
            assertEquals(24, mock.size)
            assertEquals(24, mock.map { it.id }.toSet().size)
        }
    }

    @Test
    fun cardsHaveUniqueIds() {
        assertEquals(repo.cards.size, repo.cards.map { it.id }.toSet().size)
        (1..5).forEach { assertTrue(repo.cardsFor(it).isNotEmpty()) }
    }
}
