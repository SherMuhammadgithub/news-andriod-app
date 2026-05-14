package com.example.quiz.screens.news

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.quiz.data.model.Article
import com.example.quiz.viewmodel.NewsViewModel

/**
 * NewsDetailScreen — shows the full details of a tapped article.
 *
 * How data gets here:
 *   User taps a card in NewsHomeScreen → onArticleClick(index) is called →
 *   NavGraph navigates to "news_detail/{index}" →
 *   This screen calls viewModel.getArticleByIndex(index) to retrieve the article.
 *
 * Layout (scrollable):
 *   TopAppBar (back button + source name as title)
 *   Full-width article image
 *   Title (large, bold)
 *   Source row (icon + name)
 *   Date row (icon + formatted date)
 *   Divider
 *   Description (full)
 *   Content preview (first ~200 chars from gnews.io)
 *   "Read Full Article" button (opens browser)
 *
 * @param articleIndex  The position of this article in the ViewModel's list.
 * @param onBack        Called when user taps the back arrow — NavController.popBackStack().
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    articleIndex: Int,
    onBack:       () -> Unit,
    viewModel:    NewsViewModel = viewModel()
) {
    // Retrieve the article object by index from the ViewModel's current list.
    // getArticleByIndex returns null if index is out of bounds (safety guard).
    val article = viewModel.getArticleByIndex(articleIndex)

    // context is needed to launch the browser Intent
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        // Show source name in the toolbar so user knows which outlet
                        text     = article?.source?.name ?: "Article",
                        maxLines = 1,
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    // Back arrow — pops this screen off the back stack
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.primary,
                    titleContentColor      = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->

        // If the article wasn't found (shouldn't happen but guard anyway)
        if (article == null) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Article not found.")
            }
            return@Scaffold
        }

        // verticalScroll makes the whole column scrollable — needed because
        // the content (image + text + button) may be taller than the screen.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero Image ────────────────────────────────────────────────────
            // AsyncImage is simpler than SubcomposeAsyncImage — just loads or shows nothing
            AsyncImage(
                model              = article.image,
                contentDescription = article.title,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale       = ContentScale.Crop    // crop to fill the box
            )

            // ── Text content ──────────────────────────────────────────────────
            Column(modifier = Modifier.padding(16.dp)) {

                // Article headline — large and bold
                Text(
                    text       = article.title,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Source row
                MetaRow(
                    icon  = Icons.Filled.Source,
                    label = article.source.name
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Published date row
                MetaRow(
                    icon  = Icons.Filled.CalendarToday,
                    label = formatDate(article.publishedAt)
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Full description — no maxLines limit here (user is on detail screen)
                if (article.description.isNotBlank()) {
                    Text(
                        text       = article.description,
                        fontSize   = 15.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Content preview — gnews.io provides the first ~300 chars of the article.
                // Often ends with "[N chars]" — we clean that up before showing it.
                val cleanContent = article.content
                    .substringBefore("[")   // remove the "[1234 chars]" trailer gnews appends
                    .trim()

                if (cleanContent.isNotBlank()) {
                    Text(
                        text       = cleanContent,
                        fontSize   = 14.sp,
                        lineHeight = 21.sp,
                        fontStyle  = FontStyle.Italic,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ── Read Full Article Button ───────────────────────────────────
                // Opens the original article URL in the device's default browser.
                // Intent.ACTION_VIEW with a Uri is the standard way to open a URL.
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector        = Icons.Filled.OpenInBrowser,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "Read Full Article", fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ── Meta Row ──────────────────────────────────────────────────────────────────

/**
 * MetaRow — a small icon + label row used for source and date.
 * Keeps the detail screen DRY (don't repeat yourself).
 */
@Composable
private fun MetaRow(
    icon:  androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            modifier           = Modifier.size(16.dp),
            tint               = MaterialTheme.colorScheme.primary
        )
        Text(
            text     = label,
            fontSize = 13.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
