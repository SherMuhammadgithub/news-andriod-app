package com.example.quiz.screens.news

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.quiz.data.model.Article
import com.example.quiz.data.model.SUPPORTED_COUNTRIES
import com.example.quiz.viewmodel.NewsViewModel

/**
 * NewsHomeScreen — the main screen showing latest headlines.
 *
 * Layout (top to bottom):
 *   TopAppBar  (title + refresh button)
 *   SearchBar  (OutlinedTextField)
 *   CountryFilter (dropdown button)
 *   LazyColumn (list of NewsCard items)
 *
 * States handled:
 *   isLoading=true  → full-screen spinner
 *   errorMessage    → error icon + message + retry button
 *   articles empty  → "No articles found" message
 *   articles filled → scrollable list of cards
 *
 * @param onArticleClick Called with the article's index when user taps a card.
 *                       Index is passed to the Detail screen via nav args.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsHomeScreen(
    onArticleClick: (Int) -> Unit,
    viewModel: NewsViewModel = viewModel()
) {
    // collectAsState() converts the StateFlow into a Compose State.
    // Every time uiState changes, this composable automatically recomposes.
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "NewsFlash",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Refresh button — re-runs the current search or top headlines
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector        = Icons.Filled.Refresh,
                            contentDescription = "Refresh news"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            // ── Search Bar ────────────────────────────────────────────────────
            SearchBar(
                query    = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                onClear  = { viewModel.onSearchQueryChange("") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Country Filter ────────────────────────────────────────────────
            CountryFilterDropdown(
                selectedCountryName = uiState.selectedCountry.displayName,
                onCountrySelected   = { viewModel.onCountrySelected(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Content Area ──────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    // 1. Loading spinner
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    // 2. Error state
                    uiState.errorMessage != null -> {
                        ErrorState(
                            message   = uiState.errorMessage!!,
                            onRetry   = { viewModel.refresh() },
                            modifier  = Modifier.align(Alignment.Center)
                        )
                    }

                    // 3. Empty list
                    uiState.articles.isEmpty() -> {
                        Text(
                            text     = "No articles found",
                            modifier = Modifier.align(Alignment.Center),
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 4. Article list
                    else -> {
                        // LazyColumn = Compose equivalent of RecyclerView
                        // Only renders items currently visible on screen (efficient for long lists)
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // itemsIndexed gives both the index and the article object
                            // We need the index to pass to the Detail screen via navigation
                            itemsIndexed(uiState.articles) { index, article ->
                                NewsCard(
                                    article  = article,
                                    onClick  = { onArticleClick(index) }
                                )
                            }
                            // Add bottom padding so the last card isn't flush with the nav bar
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// ── Search Bar ────────────────────────────────────────────────────────────────

/**
 * SearchBar — the text input at the top of the home screen.
 *
 * Shows a search icon on the left and a clear (X) button on the right
 * when there is text in the field.
 */
@Composable
private fun SearchBar(
    query:         String,
    onQueryChange: (String) -> Unit,
    onClear:       () -> Unit
) {
    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        modifier      = Modifier.fillMaxWidth(),
        placeholder   = { Text("Search news...") },
        leadingIcon   = {
            Icon(Icons.Filled.Search, contentDescription = "Search")
        },
        trailingIcon  = {
            // Only show the X button when there is text to clear
            if (query.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                }
            }
        },
        singleLine    = true,
        shape         = RoundedCornerShape(12.dp)
    )
}

// ── Country Filter Dropdown ───────────────────────────────────────────────────

/**
 * CountryFilterDropdown — a button that opens a dropdown menu of countries.
 *
 * Uses a simple DropdownMenu instead of ExposedDropdownMenuBox
 * because the trigger is a custom Row, not a TextField.
 */
@Composable
private fun CountryFilterDropdown(
    selectedCountryName: String,
    onCountrySelected:   (com.example.quiz.data.model.NewsCountry) -> Unit
) {
    // Controls whether the dropdown is open or closed
    var expanded by remember { mutableStateOf(false) }

    Box {
        // The trigger button — tapping it toggles the dropdown
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text       = "Country: $selectedCountryName",
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Open country filter",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // The dropdown menu — appears below the trigger when expanded = true
        DropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }   // close when tapping outside
        ) {
            SUPPORTED_COUNTRIES.forEach { country ->
                DropdownMenuItem(
                    text    = { Text(country.displayName) },
                    onClick = {
                        onCountrySelected(country)
                        expanded = false      // close after selection
                    }
                )
            }
        }
    }
}

// ── News Card ─────────────────────────────────────────────────────────────────

/**
 * NewsCard — one item in the LazyColumn list.
 *
 * Layout:
 *   [  Image  ] [ Title (2 lines max)    ]
 *               [ Source • Date          ]
 *               [ Description (2 lines)  ]
 *
 * Tapping the card calls [onClick] which navigates to the Detail screen.
 */
@Composable
private fun NewsCard(article: Article, onClick: () -> Unit) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // ── Thumbnail image ───────────────────────────────────────────────
            // SubcomposeAsyncImage lets us show a placeholder while loading
            // and a fallback icon if the image URL fails
            SubcomposeAsyncImage(
                model             = article.image,
                contentDescription = article.title,
                modifier          = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale      = ContentScale.Crop,
                loading           = {
                    // Shown while the image is downloading
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier  = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = {
                    // Shown if image URL is null or fails to load
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.BrokenImage,
                            contentDescription = "Image not available",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // ── Text content ──────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {

                // Headline — bold, max 2 lines, ellipsize if longer
                Text(
                    text       = article.title,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Source name + published date on one line
                Text(
                    text     = "${article.source.name} • ${formatDate(article.publishedAt)}",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Short description preview
                Text(
                    text     = article.description,
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ── Error State ───────────────────────────────────────────────────────────────

/**
 * ErrorState — shown when a network request fails.
 * Shows an icon, the error message, and a retry button.
 */
@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = Icons.Filled.WifiOff,
            contentDescription = null,
            modifier           = Modifier.size(64.dp),
            tint               = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text      = message,
            color     = MaterialTheme.colorScheme.error,
            fontSize  = 14.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

// ── Date Formatter ────────────────────────────────────────────────────────────

/**
 * formatDate — converts ISO 8601 date string to a readable format.
 *
 * gnews.io returns: "2024-01-15T10:30:00Z"
 * We display:       "Jan 15, 2024"
 *
 * Uses simple string parsing to avoid requiring java.time API level 26+
 * (the app's minSdk is 24).
 */
fun formatDate(isoDate: String): String {
    return try {
        // "2024-01-15T10:30:00Z" → split on "T" → take date part "2024-01-15"
        val datePart = isoDate.split("T").firstOrNull() ?: return isoDate
        val parts    = datePart.split("-")
        if (parts.size < 3) return isoDate

        val year  = parts[0]
        val month = parts[1].toIntOrNull() ?: return isoDate
        val day   = parts[2]

        val monthName = listOf(
            "Jan","Feb","Mar","Apr","May","Jun",
            "Jul","Aug","Sep","Oct","Nov","Dec"
        ).getOrElse(month - 1) { "" }

        "$monthName $day, $year"  // e.g. "Jan 15, 2024"
    } catch (e: Exception) {
        isoDate  // return raw string if parsing fails
    }
}
