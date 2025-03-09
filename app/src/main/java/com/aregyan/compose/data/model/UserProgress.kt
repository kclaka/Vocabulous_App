package com.aregyan.compose.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

@Entity(
    tableName = "user_progress",
    foreignKeys = [
        ForeignKey(
            entity = VocabularyWord::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wordId")]
)
data class UserProgress(
    @PrimaryKey(autoGenerate = true)
    @Exclude // Exclude from Firestore as we'll use a composite ID
    val id: Long = 0,
    val userId: String = "", // Default value for Firestore
    val wordId: String = "", // Default value for Firestore
    val proficiencyLevel: Int = 0, // 0-5 scale (0: Not started, 5: Mastered)
    val lastReviewedAt: Long? = null,
    val nextReviewDue: Long? = null,
    val reviewCount: Int = 0,
    val isBookmarked: Boolean = false,
    val notes: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
