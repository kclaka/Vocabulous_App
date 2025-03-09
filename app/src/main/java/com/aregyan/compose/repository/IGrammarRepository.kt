package com.aregyan.compose.repository

import com.aregyan.compose.data.model.GrammarExercise
import com.aregyan.compose.data.model.GrammarLesson
import kotlinx.coroutines.flow.Flow

/**
 * Interface for grammar repository operations
 */
interface IGrammarRepository {
    // Lesson operations
    fun getAllLessons(): Flow<List<GrammarLesson>>
    
    fun getLessonById(lessonId: String): Flow<GrammarLesson?>
    
    fun getLessonsByDifficulty(difficulty: Int): Flow<List<GrammarLesson>>
    
    suspend fun insertLesson(lesson: GrammarLesson)
    
    suspend fun updateLesson(lesson: GrammarLesson)
    
    suspend fun deleteLesson(lesson: GrammarLesson)
    
    suspend fun deleteLessonById(lessonId: String)
    
    // Exercise operations
    fun getExercisesByLessonId(lessonId: String): Flow<List<GrammarExercise>>
    
    fun getExerciseById(exerciseId: String): Flow<GrammarExercise?>
    
    suspend fun insertExercise(exercise: GrammarExercise)
    
    suspend fun insertExercises(exercises: List<GrammarExercise>)
    
    suspend fun updateExercise(exercise: GrammarExercise)
    
    suspend fun deleteExercise(exercise: GrammarExercise)
    
    suspend fun deleteExercisesByLessonId(lessonId: String)
}
