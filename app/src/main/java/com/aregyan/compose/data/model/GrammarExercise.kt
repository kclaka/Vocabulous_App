package com.aregyan.compose.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.aregyan.compose.data.db.Converters

@Entity(
    tableName = "grammar_exercises",
    foreignKeys = [
        ForeignKey(
            entity = GrammarLesson::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lessonId")]
)
data class GrammarExercise(
    @PrimaryKey
    val id: String = "", // Default value for Firestore
    val lessonId: String = "", // Default value for Firestore
    val title: String = "", // Default value for Firestore
    val instruction: String = "", // Default value for Firestore
    val type: ExerciseType = ExerciseType.MULTIPLE_CHOICE, // Default value for Firestore
    @TypeConverters(Converters::class)
    val options: List<String> = emptyList(),
    val correctAnswer: String = "", // Default value for Firestore
    val explanation: String = "", // Default value for Firestore
    val difficulty: Int = 1, // Default value for Firestore
    val order: Int = 0, // Default value for Firestore
    val createdAt: Long = System.currentTimeMillis()
)

enum class ExerciseType {
    MULTIPLE_CHOICE,
    FILL_IN_BLANK,
    REORDER_SENTENCE,
    TRUE_FALSE,
    MATCHING
}
