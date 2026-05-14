package com.example.quiz.data.model

import com.google.firebase.Timestamp

/**
 * Complaint — the core data model for this app.
 *
 * Each field has a default value so Firestore can call the no-argument constructor
 * when deserializing documents back into Kotlin objects (toObject<Complaint>()).
 *
 * The [id] field is NOT stored inside the Firestore document; it's the document ID
 * and is injected after reading with `.copy(id = documentSnapshot.id)`.
 */
data class Complaint(
    val id: String = "",                    // Firestore document ID (injected after fetch)
    val studentName: String = "",           // e.g. "Ali Ahmed"
    val rollNumber: String = "",            // e.g. "BSCS-101"
    val complaintTitle: String = "",        // Short title of the complaint
    val category: String = "",             // e.g. "IT", "Library", "Transport"
    val priority: String = "",             // "Low" | "Medium" | "High" | "Urgent"
    val description: String = "",          // Full complaint details
    val status: String = "Pending",        // Default status — always starts as Pending
    val timestamp: Timestamp? = null       // Auto-set by FirebaseRepository when saving
)
