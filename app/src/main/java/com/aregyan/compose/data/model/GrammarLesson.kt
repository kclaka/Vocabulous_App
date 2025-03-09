package com.aregyan.compose.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.aregyan.compose.data.db.Converters

@Entity(tableName = "grammar_lessons")
data class GrammarLesson(
    @PrimaryKey
    val id: String = "", // Default value for Firestore
    val title: String = "", // Default value for Firestore
    val description: String = "", // Default value for Firestore
    val content: String = "", // Markdown or HTML content
    val difficulty: Int = 1, // 1-5 scale
    val order: Int = 0, // Default value for Firestore
    @TypeConverters(Converters::class)
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
