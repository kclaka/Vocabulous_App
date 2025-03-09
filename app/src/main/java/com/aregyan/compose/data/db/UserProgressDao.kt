package com.aregyan.compose.data.db

import androidx.room.*
import com.aregyan.compose.data.model.UserProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getUserProgressByUserId(userId: String): Flow<List<UserProgress>>
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND wordId = :wordId")
    suspend fun getUserProgressForWord(userId: String, wordId: String): UserProgress?
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND proficiencyLevel >= :minLevel ORDER BY updatedAt DESC")
    fun getUserProgressByProficiency(userId: String, minLevel: Int): Flow<List<UserProgress>>
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND isBookmarked = 1 ORDER BY updatedAt DESC")
    fun getBookmarkedWords(userId: String): Flow<List<UserProgress>>
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND nextReviewDue <= :currentTime ORDER BY nextReviewDue ASC")
    fun getWordsForReview(userId: String, currentTime: Long): Flow<List<UserProgress>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(userProgress: UserProgress): Long
    
    @Update
    suspend fun updateUserProgress(userProgress: UserProgress)
    
    @Query("UPDATE user_progress SET proficiencyLevel = :level, updatedAt = :timestamp WHERE userId = :userId AND wordId = :wordId")
    suspend fun updateProficiencyLevel(userId: String, wordId: String, level: Int, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_progress SET isBookmarked = :isBookmarked, updatedAt = :timestamp WHERE userId = :userId AND wordId = :wordId")
    suspend fun updateBookmarkStatus(userId: String, wordId: String, isBookmarked: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM user_progress WHERE userId = :userId AND wordId = :wordId")
    suspend fun deleteUserProgressForWord(userId: String, wordId: String)
    
    @Query("DELETE FROM user_progress WHERE userId = :userId")
    suspend fun deleteAllUserProgress(userId: String)
    
    @Query("DELETE FROM user_progress")
    suspend fun deleteAllProgress()
}
