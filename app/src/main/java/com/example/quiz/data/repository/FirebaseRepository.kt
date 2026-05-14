package com.example.quiz.data.repository

import com.example.quiz.data.model.Complaint
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * FirebaseRepository — handles ALL Firestore database operations.
 *
 * This is the "Repository" layer in MVVM:
 *   ViewModel → Repository → Firebase
 *
 * Why a repository?
 *   It keeps Firebase-specific code out of the ViewModel and screens.
 *   If you ever switch databases (e.g. to Room), you only change this class.
 */
class FirebaseRepository {

    // Get a reference to the Firestore database (singleton managed by Firebase SDK)
    private val db = FirebaseFirestore.getInstance()

    // Reference to the "complaints" collection in Firestore
    // Think of it as a table in SQL — each document is a row
    private val complaintsRef = db.collection("complaints")

    /**
     * Save a new complaint to Firestore.
     *
     * [complaintsRef.add()] creates a new document with an auto-generated ID.
     * We use [.await()] to suspend (pause) the coroutine until Firestore responds,
     * then return the document reference.
     *
     * Returns [Result.success] with the new document ID, or [Result.failure] on error.
     */
    suspend fun saveComplaint(complaint: Complaint): Result<String> {
        return try {
            // Copy the complaint but set the timestamp to NOW before saving
            val complaintToSave = complaint.copy(timestamp = Timestamp.now())

            // .add() returns a Task<DocumentReference>; .await() suspends until done
            val docRef = complaintsRef.add(complaintToSave).await()

            Result.success(docRef.id) // Return the auto-generated document ID
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all complaints as a real-time Flow.
     *
     * [callbackFlow] converts Firestore's listener-based API into a Kotlin Flow.
     * Every time a complaint is added/edited/deleted in Firestore, this Flow
     * automatically emits the updated list — no manual refresh needed.
     *
     * The list is ordered by timestamp DESCENDING (newest first).
     */
    fun getComplaints(): Flow<List<Complaint>> = callbackFlow {

        // addSnapshotListener registers a real-time listener on the collection
        val listener = complaintsRef
            .orderBy("timestamp", Query.Direction.DESCENDING) // newest first
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    // Do NOT call close(error) — that crashes the app by throwing
                    // an uncaught exception in the coroutine.
                    // Instead, emit an empty list so the UI shows "No complaints".
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                // Convert each Firestore document into a Complaint object
                // toObject<Complaint>() uses the no-arg constructor + field names to map data
                val complaints = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Complaint::class.java)?.copy(id = doc.id)
                    // ↑ inject the Firestore document ID into the id field
                } ?: emptyList()

                trySend(complaints) // emit the new list to the Flow
            }

        // When the Flow is cancelled (e.g. screen leaves composition), remove the listener
        // This prevents memory leaks
        awaitClose { listener.remove() }
    }
}
