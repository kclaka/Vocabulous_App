package com.aregyan.compose.data.db

import androidx.room.*
import com.aregyan.compose.data.model.QuizResult
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizResultDao {
    @Query("SELECT * FROM quiz_results WHERE userId = :userId ORDER BY completedAt DESC")
    fun getQuizResultsByUserId(userId: String): Flow<List<QuizResult>>
    
    @Query("SELECT * FROM quiz_results WHERE userId = :userId AND categoryId = :categoryId ORDER BY completedAt DESC")
    fun getQuizResultsByCategory(userId: String, categoryId: String): Flow<List<QuizResult>>
    
    @Query("SELECT * FROM quiz_results WHERE id = :quizId")
    suspend fun getQuizResultById(quizId: Long): QuizResult?
    
    @Query("SELECT AVG(score * 100.0 / totalQuestions) FROM quiz_results WHERE userId = :userId")
    fun getAverageScorePercentage(userId: String): Flow<Float?>
    
    @Query("SELECT COUNT(*) FROM quiz_results WHERE userId = :userId")
    fun getTotalQuizzesTaken(userId: String): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(quizResult: QuizResult): Long
    
    @Update
    suspend fun updateQuizResult(quizResult: QuizResult)
    
    @Delete
    suspend fun deleteQuizResult(quizResult: QuizResult)
    
    @Query("DELETE FROM quiz_results WHERE userId = :userId")
    suspend fun deleteAllUserQuizResults(userId: String)
    
    @Query("DELETE FROM quiz_results")
    suspend fun deleteAllResults()
}
