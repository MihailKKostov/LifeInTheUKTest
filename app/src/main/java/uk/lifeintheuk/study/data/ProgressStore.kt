package uk.lifeintheuk.study.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class TestResult(val timestamp: Long, val score: Int, val total: Int) {
    val passed: Boolean get() = score * 4 >= total * 3 // 75% pass mark (18 of 24)
}

/**
 * All user progress, persisted in SharedPreferences and exposed as Compose state so screens
 * recompose when it changes.
 */
class ProgressStore(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(context.getSharedPreferences("progress", Context.MODE_PRIVATE))

    var readSections by mutableStateOf(prefs.getStringSet(KEY_READ, emptySet())!!.toSet())
        private set
    var knownCards by mutableStateOf(prefs.getStringSet(KEY_CARDS, emptySet())!!.toSet())
        private set
    var checkedFacts by mutableStateOf(prefs.getStringSet(KEY_FACTS, emptySet())!!.toSet())
        private set
    var missedQuestions by mutableStateOf(prefs.getStringSet(KEY_MISSED, emptySet())!!.toSet())
        private set
    var answeredQuestions by mutableStateOf(prefs.getStringSet(KEY_ANSWERED, emptySet())!!.toSet())
        private set
    var lastSection by mutableStateOf(prefs.getString(KEY_LAST, null))
        private set
    var testResults by mutableStateOf(parseResults(prefs.getString(KEY_RESULTS, "") ?: ""))
        private set
    var textScale by mutableFloatStateOf(prefs.getFloat(KEY_SCALE, 1f))
        private set

    fun markRead(sectionId: String, read: Boolean = true) {
        readSections = if (read) readSections + sectionId else readSections - sectionId
        prefs.edit().putStringSet(KEY_READ, readSections).apply()
    }

    fun openSection(sectionId: String) {
        lastSection = sectionId
        prefs.edit().putString(KEY_LAST, sectionId).apply()
    }

    fun setCardKnown(cardId: String, known: Boolean) {
        knownCards = if (known) knownCards + cardId else knownCards - cardId
        prefs.edit().putStringSet(KEY_CARDS, knownCards).apply()
    }

    fun toggleFact(factId: String) {
        checkedFacts = if (factId in checkedFacts) checkedFacts - factId else checkedFacts + factId
        prefs.edit().putStringSet(KEY_FACTS, checkedFacts).apply()
    }

    fun recordAnswer(questionId: String, correct: Boolean) {
        answeredQuestions = answeredQuestions + questionId
        missedQuestions = if (correct) missedQuestions - questionId else missedQuestions + questionId
        prefs.edit()
            .putStringSet(KEY_ANSWERED, answeredQuestions)
            .putStringSet(KEY_MISSED, missedQuestions)
            .apply()
    }

    fun addTestResult(score: Int, total: Int) {
        testResults = (testResults + TestResult(System.currentTimeMillis(), score, total)).takeLast(50)
        prefs.edit().putString(KEY_RESULTS, testResults.joinToString(";") { "${it.timestamp},${it.score},${it.total}" }).apply()
    }

    fun updateTextScale(scale: Float) {
        textScale = scale.coerceIn(0.85f, 1.4f)
        prefs.edit().putFloat(KEY_SCALE, textScale).apply()
    }

    fun resetAll() {
        prefs.edit().clear().apply()
        readSections = emptySet()
        knownCards = emptySet()
        checkedFacts = emptySet()
        missedQuestions = emptySet()
        answeredQuestions = emptySet()
        lastSection = null
        testResults = emptyList()
        textScale = 1f
    }

    private fun parseResults(raw: String): List<TestResult> =
        raw.split(';').filter { it.isNotBlank() }.mapNotNull { entry ->
            val p = entry.split(',')
            if (p.size == 3) TestResult(p[0].toLong(), p[1].toInt(), p[2].toInt()) else null
        }

    private companion object {
        const val KEY_READ = "read"
        const val KEY_CARDS = "cards"
        const val KEY_FACTS = "facts"
        const val KEY_MISSED = "missed"
        const val KEY_ANSWERED = "answered"
        const val KEY_LAST = "last"
        const val KEY_RESULTS = "results"
        const val KEY_SCALE = "scale"
    }
}
