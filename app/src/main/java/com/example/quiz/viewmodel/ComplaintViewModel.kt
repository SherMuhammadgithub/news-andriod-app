package com.example.quiz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.model.Complaint
import com.example.quiz.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ─────────────────────────────────────────────────────────────────

/**
 * SubmissionState — represents the state of a "submit complaint" operation.
 *
 * Using a sealed class means the UI can exhaustively handle every state:
 *   Idle (nothing happening) | Loading (in progress) | Success | Error
 */
sealed class SubmissionState {
    object Idle : SubmissionState()
    object Loading : SubmissionState()
    data class Success(val message: String) : SubmissionState()
    data class Error(val message: String) : SubmissionState()
}

// ── ViewModel ────────────────────────────────────────────────────────────────

/**
 * ComplaintViewModel — the "brain" between the UI and the database.
 *
 * MVVM Pattern:
 *   View (Composable screens) ← observes StateFlow ← ViewModel → Repository → Firebase
 *
 * ViewModel survives screen rotations. The UI just observes StateFlows and
 * calls functions — it never touches Firebase directly.
 *
 * [viewModelScope] is automatically cancelled when the ViewModel is destroyed.
 */
class ComplaintViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    // ── Complaints list state ─────────────────────────────────────────────────

    // Private mutable state — only the ViewModel can change this
    private val _complaints = MutableStateFlow<List<Complaint>>(emptyList())

    // Public read-only state — screens observe this
    val complaints: StateFlow<List<Complaint>> = _complaints.asStateFlow()

    // ── Form submission state ─────────────────────────────────────────────────

    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val submissionState: StateFlow<SubmissionState> = _submissionState.asStateFlow()

    // ── Init ─────────────────────────────────────────────────────────────────

    init {
        // Start listening to Firestore as soon as the ViewModel is created
        loadComplaints()
    }

    private fun loadComplaints() {
        viewModelScope.launch {
            try {
                // collect() suspends and processes each emitted list from the Flow
                repository.getComplaints().collect { list ->
                    _complaints.value = list
                }
            } catch (e: Exception) {
                // Catches PERMISSION_DENIED and other Firestore errors.
                // App stays open — list just stays empty.
                // Fix: set Firestore rules to allow read/write in Firebase Console.
                _complaints.value = emptyList()
            }
        }
    }

    // ── Public actions ───────────────────────────────────────────────────────

    /**
     * Submit a new complaint to Firebase.
     * The form screen calls this when the user taps "Submit Complaint".
     */
    fun submitComplaint(complaint: Complaint) {
        viewModelScope.launch {
            _submissionState.value = SubmissionState.Loading

            val result = repository.saveComplaint(complaint)

            _submissionState.value = if (result.isSuccess) {
                SubmissionState.Success("Complaint submitted successfully!")
            } else {
                SubmissionState.Error(result.exceptionOrNull()?.message ?: "Failed to submit complaint")
            }
        }
    }

    /** Call this after showing the success/error Snackbar to reset the state. */
    fun resetSubmissionState() {
        _submissionState.value = SubmissionState.Idle
    }

    /**
     * Find a complaint by its Firestore document ID.
     * Used by the Detail screen to look up the complaint that was clicked.
     */
    fun getComplaintById(id: String): Complaint? {
        return _complaints.value.find { it.id == id }
    }
}
