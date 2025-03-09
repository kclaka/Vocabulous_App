package com.aregyan.compose.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aregyan.compose.data.model.GrammarExercise
import com.aregyan.compose.data.model.GrammarLesson
import com.aregyan.compose.data.model.PronunciationExercise
import com.aregyan.compose.data.model.QuizResult
import com.aregyan.compose.data.model.UserProgress
import com.aregyan.compose.data.model.VocabularyWord
import com.aregyan.compose.data.model.WordCategory
import com.aregyan.compose.data.model.WordPack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [
        VocabularyWord::class,
        WordCategory::class,
        UserProgress::class,
        QuizResult::class,
        GrammarLesson::class,
        GrammarExercise::class,
        PronunciationExercise::class,
        WordPack::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VocabulousDatabase : RoomDatabase() {
    
    abstract fun vocabularyWordDao(): VocabularyWordDao
    abstract fun wordCategoryDao(): WordCategoryDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun grammarLessonDao(): GrammarLessonDao
    abstract fun grammarExerciseDao(): GrammarExerciseDao
    abstract fun pronunciationExerciseDao(): PronunciationExerciseDao
    abstract fun wordPackDao(): WordPackDao
    
    /**
     * Clears all data from the database
     */
    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            vocabularyWordDao().deleteAllWords()
            wordCategoryDao().deleteAllCategories()
            userProgressDao().deleteAllProgress()
            quizResultDao().deleteAllResults()
            grammarLessonDao().deleteAllLessons()
            grammarExerciseDao().deleteAllExercises()
            pronunciationExerciseDao().deleteAllExercises()
            wordPackDao().deleteAllWordPacks()
        }
    }
    
    companion object {
        private const val DEFAULT_DB_NAME = "vocabulous_database_default"
        private val INSTANCES = mutableMapOf<String, VocabulousDatabase>()
        
        /**
         * Get a database instance for the specified user
         * @param context Application context
         * @param userId Firebase user ID, or null for a default database (when not authenticated)
         * @return Database instance specific to the user
         */
        fun getDatabase(context: Context, userId: String? = null): VocabulousDatabase {
            // Generate a database name based on the user ID, or use default for unauthenticated users
            val dbName = if (userId != null) {
                "vocabulous_database_${userId}"
            } else {
                DEFAULT_DB_NAME
            }
            
            // Return existing instance if available
            return INSTANCES[dbName] ?: synchronized(this) {
                // Check again inside synchronized block
                INSTANCES[dbName]?.let { return it }
                
                // Create new instance
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VocabulousDatabase::class.java,
                    dbName
                )
                .fallbackToDestructiveMigration()
                .build()
                
                // Store in instances map
                INSTANCES[dbName] = instance
                instance
            }
        }
        
        /**
         * Clear the database instance for a specific user when they log out
         * @param userId Firebase user ID to clear
         */
        fun clearUserDatabase(userId: String) {
            synchronized(this) {
                val dbName = "vocabulous_database_${userId}"
                INSTANCES.remove(dbName)
            }
        }
    }
}
