package com.aregyan.compose.util

import com.aregyan.compose.data.db.VocabulousDatabase
import com.aregyan.compose.data.model.GrammarExercise
import com.aregyan.compose.data.model.GrammarLesson
import com.aregyan.compose.data.model.QuizResult
import com.aregyan.compose.data.model.UserProgress
import com.aregyan.compose.data.model.VocabularyWord
import com.aregyan.compose.data.model.WordCategory
import com.aregyan.compose.repository.FirestoreGrammarRepository
import com.aregyan.compose.repository.FirestoreUserProgressRepository
import com.aregyan.compose.repository.FirestoreVocabularyRepository
import com.aregyan.compose.repository.IGrammarRepository
import com.aregyan.compose.repository.UserProgressRepository
import com.aregyan.compose.repository.VocabularyRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class to handle data migration between local Room database and Firestore
 */
@Singleton
class DataMigrationUtil @Inject constructor(
    private val database: VocabulousDatabase,
    private val vocabularyRepository: VocabularyRepository,
    private val userProgressRepository: UserProgressRepository,
    private val grammarRepository: IGrammarRepository,
    private val auth: FirebaseAuth
) {
    
    /**
     * Migrate data from local Room database to Firestore for the current user
     * This should be called when a user logs in
     */
    suspend fun migrateLocalDataToFirestore() = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext
            Timber.d("Starting data migration to Firestore for user: $userId")
            
            // Migrate vocabulary words
            migrateVocabularyData(userId)
            
            // Migrate user progress and quiz results
            migrateUserProgressData(userId)
            
            // Migrate grammar lessons and exercises
            migrateGrammarData()
            
            Timber.d("Data migration completed successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error during data migration to Firestore")
        }
    }
    
    private suspend fun migrateVocabularyData(userId: String) {
        // Migrate vocabulary words
        val words = database.vocabularyWordDao().getAllWords().first()
        if (words.isNotEmpty()) {
            Timber.d("Migrating ${words.size} vocabulary words to Firestore")
            vocabularyRepository.insertWords(words)
        }
        
        // Migrate word categories
        val categories = database.wordCategoryDao().getAllCategories().first()
        if (categories.isNotEmpty()) {
            Timber.d("Migrating ${categories.size} word categories to Firestore")
            vocabularyRepository.insertCategories(categories)
        }
    }
    
    private suspend fun migrateUserProgressData(userId: String) {
        // Migrate user progress
        val progress = database.userProgressDao().getUserProgressByUserId(userId).first()
        if (progress.isNotEmpty()) {
            Timber.d("Migrating ${progress.size} user progress entries to Firestore")
            progress.forEach { userProgressRepository.insertUserProgress(it) }
        }
        
        // Migrate quiz results
        val quizResults = database.quizResultDao().getQuizResultsByUserId(userId).first()
        if (quizResults.isNotEmpty()) {
            Timber.d("Migrating ${quizResults.size} quiz results to Firestore")
            quizResults.forEach { userProgressRepository.insertQuizResult(it) }
        }
    }
    
    private suspend fun migrateGrammarData() {
        // Migrate grammar lessons
        val lessons = database.grammarLessonDao().getAllLessons().first()
        if (lessons.isNotEmpty()) {
            Timber.d("Migrating ${lessons.size} grammar lessons to Firestore")
            lessons.forEach { grammarRepository.insertLesson(it) }
            
            // Migrate grammar exercises for each lesson
            lessons.forEach { lesson ->
                val exercises = database.grammarExerciseDao().getExercisesByLessonId(lesson.id).first()
                if (exercises.isNotEmpty()) {
                    Timber.d("Migrating ${exercises.size} grammar exercises for lesson ${lesson.id} to Firestore")
                    grammarRepository.insertExercises(exercises)
                }
            }
        }
    }
    
    /**
     * Check if the user has any data in Firestore
     * This can be used to determine if we need to migrate local data
     */
    suspend fun hasFirestoreData(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check for vocabulary words as an indicator
            val words = vocabularyRepository.getAllWords().first()
            if (words.isNotEmpty()) return@withContext true
            
            // If no vocabulary words, check for grammar lessons
            val lessons = grammarRepository.getAllLessons().first()
            if (lessons.isNotEmpty()) return@withContext true
            
            // If still no data found, check for user progress
            val userId = auth.currentUser?.uid ?: return@withContext false
            val progress = userProgressRepository.getUserProgressByUserId(userId).first()
            return@withContext progress.isNotEmpty()
        } catch (e: Exception) {
            Timber.e(e, "Error checking for Firestore data")
            return@withContext false
        }
    }
}
