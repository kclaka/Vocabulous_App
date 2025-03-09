package com.aregyan.compose.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.aregyan.compose.data.db.Converters

@Entity(tableName = "pronunciation_exercises")
data class PronunciationExercise(
    @PrimaryKey
    val id: String = "", // Default value for Firestore
    val text: String = "", // Default value for Firestore
    val audioUrl: String? = null, // URL to native speaker pronunciation
    val difficulty: Int = 1, // 1-5 scale
    val category: String = "", // e.g., "Vowels", "Consonants", "Phrases"
    val tips: String = "", // Tips for correct pronunciation
    @TypeConverters(Converters::class)
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
