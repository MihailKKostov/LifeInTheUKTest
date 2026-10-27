package uk.lifeintheuk.study.data

/**
 * Parses the plain-text content files in assets/.
 *
 * Chapter files use line directives:
 *   @title, @subtitle, @intro      chapter metadata
 *   @section <title>               starts a new section
 *   ## text                        heading
 *   - text                         bullet (consecutive bullets form one list)
 *   > Name :: text                 person card
 *   ! text                         exam tip
 *   " line                         quote line;  "— source  ends the quote
 *   | left | right                 table row
 *   @remember                      ends the section text; following "- " lines are its key facts
 *   anything else                  paragraph text (blank line separates paragraphs)
 */
object ContentParser {

    fun parseChapter(number: Int, raw: String): Chapter {
        var title = ""
        var subtitle = ""
        var intro = ""
        val sections = mutableListOf<Section>()

        var sectionTitle: String? = null
        var blocks = mutableListOf<Block>()
        var para = StringBuilder()
        var bullets = mutableListOf<String>()
        var quote = mutableListOf<String>()
        var table = mutableListOf<Pair<String, String>>()
        var inRemember = false
        var rememberItems = mutableListOf<RememberItem>()

        fun flushPara() {
            if (para.isNotBlank()) blocks += Block.Paragraph(para.toString().trim())
            para = StringBuilder()
        }
        fun flushBullets() {
            if (bullets.isNotEmpty()) blocks += Block.Bullets(bullets.toList())
            bullets = mutableListOf()
        }
        fun flushTable() {
            if (table.isNotEmpty()) blocks += Block.Table(table.toList())
            table = mutableListOf()
        }
        fun flushInline() {
            flushPara(); flushBullets(); flushTable()
        }
        fun flushSection() {
            flushInline()
            sectionTitle?.let {
                sections += Section("$number.${sections.size + 1}", it, blocks.toList(), rememberItems.toList())
            }
            blocks = mutableListOf()
            sectionTitle = null
            inRemember = false
            rememberItems = mutableListOf()
        }

        for (rawLine in raw.lines()) {
            val line = rawLine.trim()
            when {
                line.startsWith("@title ") -> title = line.removePrefix("@title ").trim()
                line.startsWith("@subtitle ") -> subtitle = line.removePrefix("@subtitle ").trim()
                line.startsWith("@intro ") -> intro = line.removePrefix("@intro ").trim()
                line.startsWith("@section ") -> {
                    flushSection()
                    sectionTitle = line.removePrefix("@section ").trim()
                }
                line == "@remember" -> {
                    flushInline()
                    inRemember = true
                }
                inRemember -> {
                    if (line.startsWith("- ")) {
                        val id = "f$number.${sections.size + 1}.${rememberItems.size}"
                        rememberItems += RememberItem(id, line.removePrefix("- ").trim())
                    }
                }
                line.isEmpty() -> flushInline()
                line.startsWith("## ") -> {
                    flushInline(); blocks += Block.Heading(line.removePrefix("## ").trim())
                }
                line.startsWith("- ") -> {
                    flushPara(); flushTable(); bullets += line.removePrefix("- ").trim()
                }
                line.startsWith("> ") -> {
                    flushInline()
                    val body = line.removePrefix("> ")
                    val idx = body.indexOf("::")
                    blocks += if (idx > 0) {
                        Block.Person(body.substring(0, idx).trim(), body.substring(idx + 2).trim())
                    } else Block.Person(body.trim(), "")
                }
                line.startsWith("! ") -> {
                    flushInline(); blocks += Block.Tip(line.removePrefix("! ").trim())
                }
                line.startsWith("\"— ") -> {
                    flushInline()
                    blocks += Block.Quote(quote.toList(), line.removePrefix("\"— ").trim())
                    quote = mutableListOf()
                }
                line.startsWith("\" ") -> {
                    flushInline(); quote += line.removePrefix("\" ").trim()
                }
                line.startsWith("| ") -> {
                    flushPara(); flushBullets()
                    val parts = line.trim('|').split('|').map { it.trim() }
                    table += parts[0] to parts.getOrElse(1) { "" }
                }
                else -> {
                    flushBullets(); flushTable()
                    if (para.isNotEmpty()) para.append(' ')
                    para.append(line)
                }
            }
        }
        flushSection()
        return Chapter(number, title, subtitle, intro, sections)
    }

    /** Cards file: "# <chapter>" headers followed by "front | back" lines. */
    fun parseCards(raw: String): List<Flashcard> {
        val cards = mutableListOf<Flashcard>()
        var chapter = 0
        var index = 0
        for (rawLine in raw.lines()) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue
            if (line.startsWith("# ")) {
                chapter = line.removePrefix("# ").trim().toInt()
                index = 0
                continue
            }
            val sep = line.indexOf(" | ")
            if (sep < 0) continue
            cards += Flashcard(
                id = "c$chapter.$index",
                chapter = chapter,
                front = line.substring(0, sep).trim(),
                back = line.substring(sep + 3).trim(),
            )
            index++
        }
        return cards
    }

    /**
     * Questions file: "# <chapter>" headers, then blocks separated by blank lines:
     *   Q: question text
     *   + correct option
     *   - wrong option
     *   E: explanation
     */
    fun parseQuestions(raw: String): List<Question> {
        val questions = mutableListOf<Question>()
        var chapter = 0
        var index = 0
        var text = ""
        var options = mutableListOf<String>()
        var correct = mutableSetOf<Int>()
        var explanation = ""

        fun flush() {
            if (text.isNotEmpty() && options.isNotEmpty()) {
                questions += Question("q$chapter.$index", chapter, text, options.toList(), correct.toSet(), explanation)
                index++
            }
            text = ""; options = mutableListOf(); correct = mutableSetOf(); explanation = ""
        }

        for (rawLine in raw.lines()) {
            val line = rawLine.trim()
            when {
                line.startsWith("# ") -> {
                    flush(); chapter = line.removePrefix("# ").trim().toInt(); index = 0
                }
                line.isEmpty() -> flush()
                line.startsWith("Q: ") -> text = line.removePrefix("Q: ").trim()
                line.startsWith("+ ") -> {
                    correct += options.size; options += line.removePrefix("+ ").trim()
                }
                line.startsWith("- ") -> options += line.removePrefix("- ").trim()
                line.startsWith("E: ") -> explanation = line.removePrefix("E: ").trim()
            }
        }
        flush()
        return questions
    }
}
