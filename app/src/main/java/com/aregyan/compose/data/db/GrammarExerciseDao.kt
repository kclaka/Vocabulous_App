package com.aregyan.compose.data.db

import androidx.room.*
import com.aregyan.compose.data.model.GrammarExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface GrammarExerciseDao {
    @Query("SELECT * FROM grammar_exercises WHERE lessonId = :lessonId ORDER BY `order` ASC")
    fun getExercisesByLessonId(lessonId: String): Flow<List<GrammarExercise>>
    
    @Query("SELECT * FROM grammar_exercises WHERE id = :exerciseId")
    fun getExerciseById(exerciseId: String): Flow<GrammarExercise?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: GrammarExercise)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<GrammarExercise>)
    
    @Update
    suspend fun updateExercise(exercise: GrammarExercise)
    
    @Delete
    suspend fun deleteExercise(exercise: GrammarExercise)
    
    @Query("DELETE FROM grammar_exercises WHERE lessonId = :lessonId")
    suspend fun deleteExercisesByLessonId(lessonId: String)
    
    @Query("DELETE FROM grammar_exercises")
    suspend fun deleteAllExercises()
}
