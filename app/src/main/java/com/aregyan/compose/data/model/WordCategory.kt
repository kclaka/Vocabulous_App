package com.aregyan.compose.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_categories")
data class WordCategory(
    @PrimaryKey
    val id: String = "", // Default value for Firestore
    val name: String = "", // Default value for Firestore
    val description: String = "", // Default value for Firestore
    val iconUrl: String? = null,
    val difficulty: Int = 1, // Default value for Firestore (1-5 scale)
    val order: Int = 0, // Default value for Firestore
    val createdAt: Long = System.currentTimeMillis()
)
