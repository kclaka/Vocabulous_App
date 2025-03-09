package com.aregyan.compose.repository

import com.aregyan.compose.data.db.QuizResultDao
import com.aregyan.compose.data.db.UserProgressDao
import com.aregyan.compose.data.model.QuizResult
import com.aregyan.compose.data.model.UserProgress
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface UserProgressRepository {
    fun getUserProgressByUserId(userId: String): Flow<List<UserProgress>>
    suspend fun getUserProgressForWord(userId: String, wordId: String): UserProgress?
    fun getUserProgressByProficiency(userId: String, minLevel: Int): Flow<List<UserProgress>>
    fun getBookmarkedWords(userId: String): Flow<List<UserProgress>>
    fun getWordsForReview(userId: String, currentTime: Long = System.currentTimeMillis()): Flow<List<UserProgress>>
    suspend fun insertUserProgress(userProgress: UserProgress): Long
    suspend fun updateUserProgress(userProgress: UserProgress)
    suspend fun updateProficiencyLevel(userId: String, wordId: String, level: Int)
    suspend fun updateBookmarkStatus(userId: String, wordId: String, isBookmarked: Boolean)
    suspend fun deleteUserProgressForWord(userId: String, wordId: String)
    suspend fun deleteAllUserProgress(userId: String)
    
    fun getQuizResultsByUserId(userId: String): Flow<List<QuizResult>>
    fun getQuizResultsByCategory(userId: String, categoryId: String): Flow<List<QuizResult>>
    suspend fun getQuizResultById(quizId: Long): QuizResult?
    fun getAverageScorePercentage(userId: String): Flow<Float?>
    fun getTotalQuizzesTaken(userId: String): Flow<Int>
    suspend fun insertQuizResult(quizResult: QuizResult): Long
    suspend fun updateQuizResult(quizResult: QuizResult)
    suspend fun deleteQuizResult(quizResult: QuizResult)
    suspend fun deleteAllUserQuizResults(userId: String)
}

class UserProgressRepositoryImpl @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val quizResultDao: QuizResultDao
) : UserProgressRepository {
    
    override fun getUserProgressByUserId(userId: String): Flow<List<UserProgress>> =
        userProgressDao.getUserProgressByUserId(userId)
    
    override suspend fun getUserProgressForWord(userId: String, wordId: String): UserProgress? =
        userProgressDao.getUserProgressForWord(userId, wordId)
    
    override fun getUserProgressByProficiency(userId: String, minLevel: Int): Flow<List<UserProgress>> =
        userProgressDao.getUserProgressByProficiency(userId, minLevel)
    
    override fun getBookmarkedWords(userId: String): Flow<List<UserProgress>> =
        userProgressDao.getBookmarkedWords(userId)
    
    override fun getWordsForReview(userId: String, currentTime: Long): Flow<List<UserProgress>> =
        userProgressDao.getWordsForReview(userId, currentTime)
    
    override suspend fun insertUserProgress(userProgress: UserProgress): Long =
        userProgressDao.insertUserProgress(userProgress)
    
    override suspend fun updateUserProgress(userProgress: UserProgress) =
        userProgressDao.updateUserProgress(userProgress)
    
    override suspend fun updateProficiencyLevel(userId: String, wordId: String, level: Int) =
        userProgressDao.updateProficiencyLevel(userId, wordId, level)
    
    override suspend fun updateBookmarkStatus(userId: String, wordId: String, isBookmarked: Boolean) =
        userProgressDao.updateBookmarkStatus(userId, wordId, isBookmarked)
    
    override suspend fun deleteUserProgressForWord(userId: String, wordId: String) =
        userProgressDao.deleteUserProgressForWord(userId, wordId)
    
    override suspend fun deleteAllUserProgress(userId: String) =
        userProgressDao.deleteAllUserProgress(userId)
    
    override fun getQuizResultsByUserId(userId: String): Flow<List<QuizResult>> =
        quizResultDao.getQuizResultsByUserId(userId)
    
    override fun getQuizResultsByCategory(userId: String, categoryId: String): Flow<List<QuizResult>> =
        quizResultDao.getQuizResultsByCategory(userId, categoryId)
    
    override suspend fun getQuizResultById(quizId: Long): QuizResult? =
        quizResultDao.getQuizResultById(quizId)
    
    override fun getAverageScorePercentage(userId: String): Flow<Float?> =
        quizResultDao.getAverageScorePercentage(userId)
    
    override fun getTotalQuizzesTaken(userId: String): Flow<Int> =
        quizResultDao.getTotalQuizzesTaken(userId)
    
    override suspend fun insertQuizResult(quizResult: QuizResult): Long =
        quizResultDao.insertQuizResult(quizResult)
    
    override suspend fun updateQuizResult(quizResult: QuizResult) =
        quizResultDao.updateQuizResult(quizResult)
    
    override suspend fun deleteQuizResult(quizResult: QuizResult) =
        quizResultDao.deleteQuizResult(quizResult)
    
    override suspend fun deleteAllUserQuizResults(userId: String) =
        quizResultDao.deleteAllUserQuizResults(userId)
}
