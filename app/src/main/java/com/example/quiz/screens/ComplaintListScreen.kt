package com.example.quiz.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quiz.data.model.Complaint
import com.example.quiz.viewmodel.ComplaintViewModel

/**
 * ComplaintListScreen — shows all complaints in a scrollable list.
 *
 * Equivalent to a RecyclerView in XML-based Android development.
 * [LazyColumn] only renders items that are visible on screen (lazy = efficient).
 *
 * @param onComplaintClick called with the complaint's ID when user taps a card
 * @param onAddComplaint called when user taps the FloatingActionButton (+)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintListScreen(
    viewModel: ComplaintViewModel,
    onComplaintClick: (String) -> Unit,
    onAddComplaint: () -> Unit
) {
    // collectAsState() observes the StateFlow and recomposes when data changes
    val complaints by viewModel.complaints.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Complaints", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        // FAB = FloatingActionButton — the round "+" button at the bottom-right
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddComplaint,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Complaint", tint = Color.White)
            }
        }
    ) { paddingValues ->

        if (complaints.isEmpty()) {
            // ── Empty State ────────────────────────────────────────────────
            // Show a helpful message when there are no complaints yet
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No complaints yet.\nTap  +  to register your first complaint.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // ── Complaint List (LazyColumn = RecyclerView) ──────────────────
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),         // space at top and bottom
                verticalArrangement = Arrangement.spacedBy(12.dp) // gap between cards
            ) {
                // items() iterates over the complaints list and creates a Card for each
                items(
                    items = complaints,
                    key = { complaint -> complaint.id } // stable key improves animation
                ) { complaint ->
                    ComplaintCard(
                        complaint = complaint,
                        onClick = { onComplaintClick(complaint.id) }
                    )
                }
            }
        }
    }
}

// ── Complaint Card ────────────────────────────────────────────────────────────

/**
 * ComplaintCard — one item in the complaint list.
 *
 * Displays: Title, Student Name, Roll Number, Category chip, Priority badge.
 * Tapping the card navigates to the detail screen.
 */
@Composable
fun ComplaintCard(
    complaint: Complaint,
    onClick: () -> Unit
) {
    // Priority badge color — visual urgency indicator
    val priorityColor = when (complaint.priority) {
        "Urgent" -> Color(0xFFD32F2F)   // Red
        "High"   -> Color(0xFFFF5722)   // Deep Orange
        "Medium" -> Color(0xFFFFA000)   // Amber
        "Low"    -> Color(0xFF388E3C)   // Green
        else     -> MaterialTheme.colorScheme.primary
    }

    // Card is a clickable elevated container
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Row 1: Title + Priority Badge ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = complaint.complaintTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Priority badge — colored pill-shaped label
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = priorityColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = complaint.priority,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = priorityColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── Row 2: Student Name & Roll Number ─────────────────────────
            Text(
                text = complaint.studentName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = complaint.rollNumber,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Row 3: Category + Status Chips ────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // AssistChip = small informational tag (non-clickable here)
                AssistChip(
                    onClick = {},
                    label = { Text(complaint.category, style = MaterialTheme.typography.labelSmall) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(complaint.status, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}
