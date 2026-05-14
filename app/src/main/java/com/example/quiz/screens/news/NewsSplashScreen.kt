package com.example.quiz.screens.news

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * NewsSplashScreen — displayed for 2 seconds when the app opens.
 *
 * Features:
 *   - Fade-in animation for the logo and title
 *   - After 2 seconds, automatically calls [onSplashFinished] to navigate to Home
 *
 * @param onSplashFinished Lambda called after the 2-second delay.
 *                         Navigation is handled by the caller (NavGraph), not here.
 */
@Composable
fun NewsSplashScreen(onSplashFinished: () -> Unit) {

    // Controls whether the fade-in animation has started
    // Starts false → set to true after the first frame → triggers the animation
    var startAnimation by remember { mutableStateOf(false) }

    // animateFloatAsState smoothly animates between 0f (invisible) and 1f (fully visible)
    // tween(1000) means the animation takes 1000ms (1 second) to complete
    val alpha by animateFloatAsState(
        targetValue     = if (startAnimation) 1f else 0f,
        animationSpec   = tween(durationMillis = 1000),
        label           = "splash_alpha"
    )

    // LaunchedEffect runs this block once when the composable first appears.
    // It starts the fade-in, waits 2 seconds, then calls onSplashFinished.
    LaunchedEffect(key1 = true) {
        startAnimation = true      // kick off the fade-in animation
        delay(2000L)               // wait 2 seconds total
        onSplashFinished()         // tell NavGraph to navigate to Home
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)  // solid primary color background
            .alpha(alpha),                                   // apply the fade-in animation
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App icon — using a built-in Material icon for the newspaper
        Icon(
            imageVector        = Icons.Filled.Newspaper,
            contentDescription = "News App Logo",
            tint               = Color.White,
            modifier           = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // App name
        Text(
            text       = "NewsFlash",
            color      = Color.White,
            fontSize   = 36.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tagline
        Text(
            text     = "Stay informed, stay ahead",
            color    = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp
        )
    }
}
