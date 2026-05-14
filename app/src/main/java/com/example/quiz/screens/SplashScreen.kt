package com.example.quiz.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
 * SplashScreen — the first screen shown when the app launches.
 *
 * It fades in the app name, waits 2 seconds, then calls [onSplashFinished]
 * which navigates the user to the Complaint List screen.
 *
 * @param onSplashFinished callback invoked after the 2-second delay
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    // State variable that triggers the fade-in animation
    var startAnimation by remember { mutableStateOf(false) }

    // animateFloatAsState smoothly transitions alpha from 0f → 1f over 1000ms
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "splash_alpha"
    )

    // LaunchedEffect runs once when the composable enters composition
    LaunchedEffect(Unit) {
        startAnimation = true       // Start the fade-in animation
        delay(2000L)                // Wait 2 seconds total
        onSplashFinished()          // Then navigate to the next screen
    }

    // Full-screen box with primary color background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // App name — fades in with alpha animation
            Text(
                text = "Complaint App",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(alpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle — same fade-in effect
            Text(
                text = "Register & Track Your Complaints",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.alpha(alpha)
            )
        }
    }
}
