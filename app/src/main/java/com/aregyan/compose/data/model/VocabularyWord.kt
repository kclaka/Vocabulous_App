package com.aregyan.compose.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary_words")
data class VocabularyWord(
    @PrimaryKey
    val id: String = "", // Default value for Firestore
    val word: String = "", // Default value for Firestore
    val definition: String = "", // Default value for Firestore
    val partOfSpeech: String = "", // Default value for Firestore
    val pronunciation: String = "", // Default value for Firestore
    val example: String = "", // Default value for Firestore
    val difficulty: Int = 1, // Default value for Firestore (1-5 scale)
    val imageUrl: String? = null,
    val categoryId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
