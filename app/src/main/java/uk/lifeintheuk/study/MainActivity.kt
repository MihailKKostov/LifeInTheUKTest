package uk.lifeintheuk.study

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import uk.lifeintheuk.study.data.ProgressStore
import uk.lifeintheuk.study.data.Repository
import uk.lifeintheuk.study.ui.StudyApp
import uk.lifeintheuk.study.ui.StudyTheme

/** Process-wide singletons; the content is static so there is no need for DI. */
object AppGraph {
    lateinit var repo: Repository
    lateinit var progress: ProgressStore

    fun init(activity: ComponentActivity) {
        if (!::repo.isInitialized) repo = Repository(activity.applicationContext)
        if (!::progress.isInitialized) progress = ProgressStore(activity.applicationContext)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.init(this)
        enableEdgeToEdge()
        setContent {
            StudyTheme {
                StudyApp(AppGraph.repo, AppGraph.progress)
            }
        }
    }
}
