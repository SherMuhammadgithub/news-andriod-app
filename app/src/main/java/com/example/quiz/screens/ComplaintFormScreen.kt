package com.example.quiz.screens

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quiz.data.model.Complaint
import com.example.quiz.viewmodel.ComplaintViewModel
import com.example.quiz.viewmodel.SubmissionState
import kotlinx.coroutines.launch

/**
 * ComplaintFormScreen — the form where users submit a new complaint.
 *
 * Features:
 *  - Text fields: Student Name, Roll Number, Complaint Title, Description
 *  - Dropdowns (Spinners): Category, Priority
 *  - Validation: all fields are required
 *  - On success: shows Snackbar + clears the form
 *  - On error: shows Snackbar with error message
 */
@OptIn(ExperimentalMaterial3Api::class) // Required for ExposedDropdownMenuBox
@Composable
fun ComplaintFormScreen(
    viewModel: ComplaintViewModel,
    onNavigateBack: () -> Unit
) {
    // ── Form Field States ─────────────────────────────────────────────────────
    // Each remember{} keeps the value alive across recompositions
    var studentName by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var complaintTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // ── Validation Error States ───────────────────────────────────────────────
    // null = no error; non-null string = error message shown under the field
    var nameError by remember { mutableStateOf<String?>(null) }
    var rollError by remember { mutableStateOf<String?>(null) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var priorityError by remember { mutableStateOf<String?>(null) }
    var descError by remember { mutableStateOf<String?>(null) }

    // ── Dropdown Expanded States ──────────────────────────────────────────────
    var categoryExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    // ── Dropdown Options ──────────────────────────────────────────────────────
    val categories = listOf(
        "IT", "Library", "Transport", "Hostel",
        "Accounts", "Examination", "Cafeteria", "Administration"
    )
    val priorities = listOf("Low", "Medium", "High", "Urgent")

    // ── Snackbar ──────────────────────────────────────────────────────────────
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // ── Observe ViewModel State ───────────────────────────────────────────────
    val submissionState by viewModel.submissionState.collectAsState()

    // React to submission success or failure
    LaunchedEffect(submissionState) {
        when (val state = submissionState) {

            is SubmissionState.Success -> {
                // Show success Snackbar
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }

                // Clear all form fields
                studentName = ""; rollNumber = ""; complaintTitle = ""
                selectedCategory = ""; selectedPriority = ""; description = ""
                nameError = null; rollError = null; titleError = null
                categoryError = null; priorityError = null; descError = null

                viewModel.resetSubmissionState()
            }

            is SubmissionState.Error -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
                viewModel.resetSubmissionState()
            }

            else -> { /* Idle and Loading — do nothing */ }
        }
    }

    // ── Validation + Submit Logic ─────────────────────────────────────────────
    fun validateAndSubmit() {
        var valid = true

        // Check each field — set error message if blank
        if (studentName.isBlank())      { nameError = "Student name is required";    valid = false } else nameError = null
        if (rollNumber.isBlank())       { rollError = "Roll number is required";     valid = false } else rollError = null
        if (complaintTitle.isBlank())   { titleError = "Complaint title is required"; valid = false } else titleError = null
        if (selectedCategory.isBlank()) { categoryError = "Please select a category"; valid = false } else categoryError = null
        if (selectedPriority.isBlank()) { priorityError = "Please select a priority"; valid = false } else priorityError = null
        if (description.isBlank())      { descError = "Description is required";     valid = false } else descError = null

        if (valid) {
            // All fields filled — send to Firebase via ViewModel
            viewModel.submitComplaint(
                Complaint(
                    studentName = studentName.trim(),
                    rollNumber = rollNumber.trim(),
                    complaintTitle = complaintTitle.trim(),
                    category = selectedCategory,
                    priority = selectedPriority,
                    description = description.trim()
                    // status defaults to "Pending" (see Complaint.kt)
                    // timestamp is set by FirebaseRepository
                )
            )
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Complaint") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        // verticalScroll allows the form to scroll if content is taller than the screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Student Name ──────────────────────────────────────────────
            OutlinedTextField(
                value = studentName,
                onValueChange = { studentName = it; nameError = null },
                label = { Text("Student Name") },
                placeholder = { Text("e.g. Ali Ahmed") },
                isError = nameError != null,
                supportingText = nameError?.let { msg -> { Text(msg) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Roll Number ───────────────────────────────────────────────
            OutlinedTextField(
                value = rollNumber,
                onValueChange = { rollNumber = it; rollError = null },
                label = { Text("Roll Number") },
                placeholder = { Text("e.g. BSCS-101") },
                isError = rollError != null,
                supportingText = rollError?.let { msg -> { Text(msg) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Complaint Title ───────────────────────────────────────────
            OutlinedTextField(
                value = complaintTitle,
                onValueChange = { complaintTitle = it; titleError = null },
                label = { Text("Complaint Title") },
                placeholder = { Text("e.g. Wi-Fi Issue in Lab 2") },
                isError = titleError != null,
                supportingText = titleError?.let { msg -> { Text(msg) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Category Dropdown (Spinner equivalent) ────────────────────
            // ExposedDropdownMenuBox provides the dropdown behavior
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},          // readOnly — no manual typing allowed
                    readOnly = true,
                    label = { Text("Complaint Category") },
                    // trailingIcon shows the expand/collapse arrow
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    isError = categoryError != null,
                    supportingText = categoryError?.let { msg -> { Text(msg) } },
                    // menuAnchor() links this TextField to the dropdown menu
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                                categoryError = null
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Priority Dropdown (Spinner equivalent) ────────────────────
            ExposedDropdownMenuBox(
                expanded = priorityExpanded,
                onExpandedChange = { priorityExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedPriority,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Priority Level") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                    isError = priorityError != null,
                    supportingText = priorityError?.let { msg -> { Text(msg) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false }
                ) {
                    priorities.forEach { priority ->
                        DropdownMenuItem(
                            text = { Text(priority) },
                            onClick = {
                                selectedPriority = priority
                                priorityExpanded = false
                                priorityError = null
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Description (multi-line text area) ────────────────────────
            OutlinedTextField(
                value = description,
                onValueChange = { description = it; descError = null },
                label = { Text("Complaint Description") },
                placeholder = { Text("Describe your complaint in detail...") },
                isError = descError != null,
                supportingText = descError?.let { msg -> { Text(msg) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Submit Button ─────────────────────────────────────────────
            Button(
                onClick = ::validateAndSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                // Disable the button while submission is in progress
                enabled = submissionState !is SubmissionState.Loading
            ) {
                if (submissionState is SubmissionState.Loading) {
                    // Show spinner inside button during loading
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text("Submit Complaint", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
