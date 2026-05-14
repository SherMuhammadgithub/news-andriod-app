package com.example.quiz.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quiz.screens.ComplaintDetailScreen
import com.example.quiz.screens.ComplaintFormScreen
import com.example.quiz.screens.ComplaintListScreen
import com.example.quiz.screens.SplashScreen
import com.example.quiz.viewmodel.ComplaintViewModel

// ── Screen Routes ────────────────────────────────────────────────────────────

/**
 * Screen — sealed class that holds every navigation route in the app.
 *
 * A sealed class is perfect here because the compiler guarantees we've handled
 * every possible screen in when() expressions.
 *
 * Routes are just strings used by NavHost to identify destinations.
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object ComplaintList : Screen("complaint_list")
    object ComplaintForm : Screen("complaint_form")

    // Detail screen has a path argument: complaint_detail/{complaintId}
    // The {complaintId} placeholder is replaced with the real ID at runtime
    object ComplaintDetail : Screen("complaint_detail/{complaintId}") {
        // Helper to build the full route string with a real ID
        fun createRoute(complaintId: String) = "complaint_detail/$complaintId"
    }
}

// ── Navigation Graph ─────────────────────────────────────────────────────────

/**
 * AppNavGraph — sets up the full navigation structure for the app.
 *
 * NavHost works like a container that swaps Composables based on the current route.
 * NavController manages back-stack and navigate() calls.
 *
 * The ViewModel is created once here and passed to all screens so they share
 * the same complaints list and submission state.
 */
@Composable
fun AppNavGraph() {
    // rememberNavController() creates a NavController that survives recompositions
    val navController = rememberNavController()

    // One shared ViewModel for all screens — created at the NavGraph level
    val viewModel: ComplaintViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route  // First screen shown on app launch
    ) {

        // ── Splash Screen ───────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.ComplaintList.route) {
                        // popUpTo removes Splash from the back stack
                        // so pressing Back on the list screen exits the app
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Complaint List Screen ────────────────────────────────────────────
        composable(Screen.ComplaintList.route) {
            ComplaintListScreen(
                viewModel = viewModel,
                onComplaintClick = { complaintId ->
                    // Navigate to detail, passing the complaint ID in the route
                    navController.navigate(Screen.ComplaintDetail.createRoute(complaintId))
                },
                onAddComplaint = {
                    navController.navigate(Screen.ComplaintForm.route)
                }
            )
        }

        // ── Complaint Form Screen ────────────────────────────────────────────
        composable(Screen.ComplaintForm.route) {
            ComplaintFormScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Complaint Detail Screen ──────────────────────────────────────────
        composable(
            route = Screen.ComplaintDetail.route,
            // Declare the {complaintId} argument and its type
            arguments = listOf(
                navArgument("complaintId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Extract the complaintId from the route arguments
            val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""

            ComplaintDetailScreen(
                complaintId = complaintId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
