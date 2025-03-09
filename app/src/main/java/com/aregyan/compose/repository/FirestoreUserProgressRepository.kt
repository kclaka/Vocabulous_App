package com.aregyan.compose.repository

import com.aregyan.compose.data.model.QuizResult
import com.aregyan.compose.data.model.UserProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserProgressRepository that stores data in Firestore
 * Each user has their own collection of progress data and quiz results
 */
@Singleton
class FirestoreUserProgressRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserProgressRepository {
    
    private val progressCollection = "user_progress"
    private val quizCollection = "quiz_results"
    
    // User Progress methods
    override fun getUserProgressByUserId(userId: String): Flow<List<UserProgress>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null || currentUserId != userId) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val collection = firestore.collection("users")
            .document(currentUserId)
            .collection(progressCollection)
        
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to user progress")
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val progress = snapshot?.documents?.mapNotNull {
                it.toObject(UserProgress::class.java)
            } ?: emptyList()
            
            trySend(progress)
        }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun getUserProgressForWord(userId: String, wordId: String): UserProgress? {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null || currentUserId != userId) return null
        
        // Create a composite ID to ensure uniqueness
        val progressId = "$userId-$wordId"
        
        try {
            val document = firestore.collection("users")
                .document(currentUserId)
                .collection(progressCollection)
                .document(progressId)
                .get()
                .await()
            
            return document.toObject(UserProgress::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error getting user progress for word")
            return null
        }
    }
    
    override fun getUserProgressByProficiency(userId: String, minLevel: Int): Flow<List<UserProgress>> = 
        getUserProgressByUserId(userId).map { progressList ->
            progressList.filter { it.proficiencyLevel >= minLevel }
        }
    
    override fun getBookmarkedWords(userId: String): Flow<List<UserProgress>> = 
        getUserProgressByUserId(userId).map { progressList ->
            progressList.filter { it.isBookmarked }
        }
    
    override fun getWordsForReview(userId: String, currentTime: Long): Flow<List<UserProgress>> =
        getUserProgressByUserId(userId).map { progressList ->
            progressList.filter { it.nextReviewDue != null && it.nextReviewDue <= currentTime }
        }
    
    override suspend fun insertUserProgress(userProgress: UserProgress): Long {
        val userId = auth.currentUser?.uid ?: return -1L
        
        // Create a composite ID to ensure uniqueness
        val progressId = "${userProgress.userId}-${userProgress.wordId}"
        
        try {
            firestore.collection("users")
                .document(userId)
                .collection(progressCollection)
                .document(progressId)
                .set(userProgress)
                .await()
            return System.currentTimeMillis() // Return current time as a pseudo-ID
        } catch (e: Exception) {
            Timber.e(e, "Error inserting user progress")
            return -1L
        }
    }
    
    override suspend fun updateUserProgress(userProgress: UserProgress) {
        val userId = auth.currentUser?.uid ?: return
        
        // Create a composite ID to ensure uniqueness
        val progressId = "${userProgress.userId}-${userProgress.wordId}"
        
        try {
            firestore.collection("users")
                .document(userId)
                .collection(progressCollection)
                .document(progressId)
                .set(userProgress)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error updating user progress")
        }
    }
    
    override suspend fun updateProficiencyLevel(userId: String, wordId: String, level: Int) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId != userId) return
        
        val progressId = "$userId-$wordId"
        
        try {
            // Get the current progress first
            val progress = getUserProgressForWord(userId, wordId)
            
            if (progress != null) {
                // Update the proficiency level
                val updatedProgress = progress.copy(
                    proficiencyLevel = level,
                    updatedAt = System.currentTimeMillis()
                )
                updateUserProgress(updatedProgress)
            } else {
                // Create a new progress entry if it doesn't exist
                val newProgress = UserProgress(
                    userId = userId,
                    wordId = wordId,
                    proficiencyLevel = level,
                    lastReviewedAt = System.currentTimeMillis(),
                    nextReviewDue = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 1 day later
                    updatedAt = System.currentTimeMillis()
                )
                insertUserProgress(newProgress)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating proficiency level")
        }
    }
    
    override suspend fun updateBookmarkStatus(userId: String, wordId: String, isBookmarked: Boolean) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId != userId) return
        
        try {
            // Get the current progress first
            val progress = getUserProgressForWord(userId, wordId)
            
            if (progress != null) {
                // Update the bookmark status
                val updatedProgress = progress.copy(
                    isBookmarked = isBookmarked,
                    updatedAt = System.currentTimeMillis()
                )
                updateUserProgress(updatedProgress)
            } else {
                // Create a new progress entry if it doesn't exist
                val newProgress = UserProgress(
                    userId = userId,
                    wordId = wordId,
                    isBookmarked = isBookmarked,
                    proficiencyLevel = 0, // Default to level 0 (not started)
                    lastReviewedAt = System.currentTimeMillis(),
                    nextReviewDue = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 1 day later
                    updatedAt = System.currentTimeMillis()
                )
                insertUserProgress(newProgress)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating bookmark status")
        }
    }
    
    override suspend fun deleteUserProgressForWord(userId: String, wordId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId != userId) return
        
        val progressId = "$userId-$wordId"
        
        try {
            firestore.collection("users")
                .document(currentUserId)
                .collection(progressCollection)
                .document(progressId)
                .delete()
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error deleting user progress for word")
        }
    }
    
    override suspend fun deleteAllUserProgress(userId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId != userId) return
        
        try {
            // Get all progress documents
            val documents = firestore.collection("users")
                .document(currentUserId)
                .collection(progressCollection)
                .get()
                .await()
                .documents
            
            // Batch delete all documents
            if (documents.isNotEmpty()) {
                val batch = firestore.batch()
                documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting all user progress")
        }
    }
    
    // Quiz Result methods
    override fun getQuizResultsByUserId(userId: String): Flow<List<QuizResult>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null || currentUserId != userId) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val collection = firestore.collection("users")
            .document(currentUserId)
            .collection(quizCollection)
            .orderBy("timestamp")
        
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to quiz results")
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val results = snapshot?.documents?.mapNotNull {
                it.toObject(QuizResult::class.java)
            } ?: emptyList()
            
            trySend(results)
        }
        
        awaitClose { listener.remove() }
    }
    
    override fun getQuizResultsByCategory(userId: String, categoryId: String): Flow<List<QuizResult>> =
        getQuizResultsByUserId(userId).map { results ->
            results.filter { it.categoryId == categoryId }
        }
    
    override suspend fun getQuizResultById(quizId: Long): QuizResult? {
        val userId = auth.currentUser?.uid ?: return null
        
        try {
            val document = firestore.collection("users")
                .document(userId)
                .collection(quizCollection)
                .document(quizId.toString())
                .get()
                .await()
            
            return document.toObject(QuizResult::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error getting quiz result by ID")
            return null
        }
    }
    
    override fun getAverageScorePercentage(userId: String): Flow<Float?> =
        getQuizResultsByUserId(userId).map { results ->
            if (results.isEmpty()) {
                null
            } else {
                val totalScore = results.sumOf { it.score }
                val totalPossible = results.sumOf { it.totalQuestions }
                if (totalPossible == 0) 0f else (totalScore.toFloat() / totalPossible) * 100
            }
        }
    
    override fun getTotalQuizzesTaken(userId: String): Flow<Int> =
        getQuizResultsByUserId(userId).map { it.size }
    
    override suspend fun insertQuizResult(quizResult: QuizResult): Long {
        val userId = auth.currentUser?.uid ?: return -1L
        
        try {
            // If the quiz result doesn't have an ID, generate one
            val resultId = quizResult.id ?: System.currentTimeMillis()
            val resultWithId = quizResult.copy(id = resultId)
            
            firestore.collection("users")
                .document(userId)
                .collection(quizCollection)
                .document(resultId.toString())
                .set(resultWithId)
                .await()
                
            return resultId
        } catch (e: Exception) {
            Timber.e(e, "Error inserting quiz result")
            return -1L
        }
    }
    
    override suspend fun updateQuizResult(quizResult: QuizResult) {
        val userId = auth.currentUser?.uid ?: return
        val quizId = quizResult.id ?: return
        
        try {
            firestore.collection("users")
                .document(userId)
                .collection(quizCollection)
                .document(quizId.toString())
                .set(quizResult)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error updating quiz result")
        }
    }
    
    override suspend fun deleteQuizResult(quizResult: QuizResult) {
        val userId = auth.currentUser?.uid ?: return
        val quizId = quizResult.id ?: return
        
        try {
            firestore.collection("users")
                .document(userId)
                .collection(quizCollection)
                .document(quizId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error deleting quiz result")
        }
    }
    
    override suspend fun deleteAllUserQuizResults(userId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId != userId) return
        
        try {
            // Get all quiz result documents
            val documents = firestore.collection("users")
                .document(currentUserId)
                .collection(quizCollection)
                .get()
                .await()
                .documents
            
            // Batch delete all documents
            if (documents.isNotEmpty()) {
                val batch = firestore.batch()
                documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting all user quiz results")
        }
    }
}
