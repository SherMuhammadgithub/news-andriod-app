package com.example.quiz.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quiz.screens.news.NewsDetailScreen
import com.example.quiz.screens.news.NewsHomeScreen
import com.example.quiz.screens.news.NewsSplashScreen
import com.example.quiz.viewmodel.NewsViewModel

// ── Route constants ───────────────────────────────────────────────────────────

/**
 * NewsScreen — defines all navigation route strings for the News app.
 *
 * Using a sealed class prevents typos: instead of writing the string
 * "news_detail/3" everywhere, you call NewsScreen.Detail.createRoute(3).
 *
 * Route shapes:
 *   "news_splash"        — no arguments
 *   "news_home"          — no arguments
 *   "news_detail/{index}"— one Int argument: the article's position in the list
 */
sealed class NewsScreen(val route: String) {
    object Splash : NewsScreen("news_splash")
    object Home   : NewsScreen("news_home")

    // Detail route carries an Int argument so we know which article to show.
    // {index} is a placeholder — NavHost fills it in at runtime.
    object Detail : NewsScreen("news_detail/{index}") {
        // Helper to build the actual route string with the real index value.
        // e.g. createRoute(2) → "news_detail/2"
        fun createRoute(index: Int) = "news_detail/$index"
    }
}

// ── NavGraph ──────────────────────────────────────────────────────────────────

/**
 * NewsNavGraph — sets up all screens and navigation for the News app.
 *
 * Key concepts:
 *   [rememberNavController] — creates and remembers the NavController across recompositions.
 *   [NavHost]               — the container that swaps screens based on the current route.
 *   [composable("route")]   — registers a screen with NavHost.
 *   [navArgument]           — declares typed arguments that a route expects.
 *
 * The [viewModel] is created at this level and passed down so that both
 * NewsHomeScreen and NewsDetailScreen share the SAME ViewModel instance.
 * If each screen created its own viewModel(), they'd have separate data.
 */
@Composable
fun NewsNavGraph() {

    // rememberNavController() creates the NavController that controls which screen is shown.
    // It survives recomposition but is scoped to this NavGraph.
    val navController = rememberNavController()

    // Single shared ViewModel — both Home and Detail screens use this instance.
    // This is important: Detail calls viewModel.getArticleByIndex() to look up the
    // article that was loaded in Home. They must share the same list in memory.
    val newsViewModel: NewsViewModel = viewModel()

    // NavHost renders whichever screen matches the current back-stack route.
    // startDestination is the first screen shown when the app opens.
    NavHost(
        navController    = navController,
        startDestination = NewsScreen.Splash.route
    ) {

        // ── Splash Screen ─────────────────────────────────────────────────────
        composable(route = NewsScreen.Splash.route) {
            NewsSplashScreen(
                onSplashFinished = {
                    // Navigate to Home and remove Splash from the back stack.
                    // popUpTo + inclusive=true means pressing Back on Home exits the app
                    // instead of going back to the Splash screen.
                    navController.navigate(NewsScreen.Home.route) {
                        popUpTo(NewsScreen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Home Screen ───────────────────────────────────────────────────────
        composable(route = NewsScreen.Home.route) {
            NewsHomeScreen(
                viewModel      = newsViewModel,
                onArticleClick = { index ->
                    // Navigate to the Detail screen, embedding the article index in the URL.
                    // e.g. index=3 → navigate to "news_detail/3"
                    navController.navigate(NewsScreen.Detail.createRoute(index))
                }
            )
        }

        // ── Detail Screen ─────────────────────────────────────────────────────
        composable(
            route     = NewsScreen.Detail.route,         // "news_detail/{index}"
            arguments = listOf(
                // Declare "index" as an Int argument so NavHost extracts and type-checks it
                navArgument("index") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            // Extract the "index" argument from the route URL
            // e.g. route "news_detail/3" → index = 3
            val index = backStackEntry.arguments?.getInt("index") ?: 0

            NewsDetailScreen(
                articleIndex = index,
                viewModel    = newsViewModel,
                onBack       = {
                    // popBackStack removes Detail from the stack → returns to Home
                    navController.popBackStack()
                }
            )
        }
    }
}
