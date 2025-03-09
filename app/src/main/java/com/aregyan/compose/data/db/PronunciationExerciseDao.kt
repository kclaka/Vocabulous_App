package com.aregyan.compose.data.db

import androidx.room.*
import com.aregyan.compose.data.model.PronunciationExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface PronunciationExerciseDao {
    @Query("SELECT * FROM pronunciation_exercises ORDER BY difficulty ASC")
    fun getAllExercises(): Flow<List<PronunciationExercise>>
    
    @Query("SELECT * FROM pronunciation_exercises WHERE id = :exerciseId")
    fun getExerciseById(exerciseId: String): Flow<PronunciationExercise?>
    
    @Query("SELECT * FROM pronunciation_exercises WHERE difficulty = :difficulty ORDER BY createdAt ASC")
    fun getExercisesByDifficulty(difficulty: Int): Flow<List<PronunciationExercise>>
    
    @Query("SELECT * FROM pronunciation_exercises WHERE category = :category ORDER BY difficulty ASC")
    fun getExercisesByCategory(category: String): Flow<List<PronunciationExercise>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: PronunciationExercise)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<PronunciationExercise>)
    
    @Update
    suspend fun updateExercise(exercise: PronunciationExercise)
    
    @Delete
    suspend fun deleteExercise(exercise: PronunciationExercise)
    
    @Query("DELETE FROM pronunciation_exercises WHERE id = :exerciseId")
    suspend fun deleteExerciseById(exerciseId: String)
    
    @Query("DELETE FROM pronunciation_exercises")
    suspend fun deleteAllExercises()
}
