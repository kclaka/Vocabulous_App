package com.aregyan.compose.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true)
    @Exclude // Exclude from Firestore as we'll use a document ID
    val id: Long = 0,
    val userId: String = "", // Default value for Firestore
    val categoryId: String? = null,
    val score: Int = 0, // Default value for Firestore
    val totalQuestions: Int = 0, // Default value for Firestore
    val timeTakenMs: Long = 0, // Default value for Firestore
    val completedAt: Long = System.currentTimeMillis(),
    val wordIds: List<String> = emptyList() // Default value for Firestore
)
