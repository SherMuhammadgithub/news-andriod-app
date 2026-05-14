package com.example.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.quiz.navigation.NewsNavGraph
import com.example.quiz.ui.theme.QuizTheme

/**
 * MainActivity — the single Activity that hosts the entire app.
 *
 * Currently active: News Headlines App (Phase 4)
 * Commented out below: Complaint Registration App (previous project)
 *
 * Single Activity Architecture:
 *   - ONE Activity, all screens are @Composable functions
 *   - Navigation via NavController, NOT Android Intents
 *
 * Flow:  MainActivity → QuizTheme → NewsNavGraph → [Splash → Home → Detail]
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enableEdgeToEdge() lets the app draw behind the status bar and nav bar
        enableEdgeToEdge()

        setContent {
            // QuizTheme applies Material Design 3 colors/typography to all screens
            QuizTheme {

                // ── NEWS APP (active) ─────────────────────────────────────────
                // Splash (2s) → Home (headlines + search + country filter) → Detail
                NewsNavGraph()

                // ── COMPLAINT APP (commented out — previous project) ──────────
                // Uncomment the line below and comment out NewsNavGraph() above
                // to switch back to the Complaint Registration App.
                //
                // AppNavGraph()
            }
        }
    }
}
