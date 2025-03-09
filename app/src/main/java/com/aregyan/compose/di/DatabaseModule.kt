package com.aregyan.compose.di

import android.content.Context
import androidx.room.Room
import com.aregyan.compose.data.db.*
import com.aregyan.compose.database.AppDatabase
import com.aregyan.compose.database.UsersDao
import com.aregyan.compose.repository.*
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    // Original database for Users
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "Users"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideChannelDao(appDatabase: AppDatabase): UsersDao {
        return appDatabase.usersDao
    }
    
    // Vocabulous Database
    @Provides
    @Singleton
    fun provideVocabulousDatabase(
        @ApplicationContext appContext: Context,
        firebaseAuth: FirebaseAuth
    ): VocabulousDatabase {
        // Get the current user ID, or null if not authenticated
        val userId = firebaseAuth.currentUser?.uid
        
        // Use the updated getDatabase method with user ID
        return VocabulousDatabase.getDatabase(appContext, userId)
    }
    
    // DAOs
    @Provides
    @Singleton
    fun provideVocabularyWordDao(database: VocabulousDatabase): VocabularyWordDao {
        return database.vocabularyWordDao()
    }
    
    @Provides
    @Singleton
    fun provideWordCategoryDao(database: VocabulousDatabase): WordCategoryDao {
        return database.wordCategoryDao()
    }
    
    @Provides
    @Singleton
    fun provideUserProgressDao(database: VocabulousDatabase): UserProgressDao {
        return database.userProgressDao()
    }
    
    @Provides
    @Singleton
    fun provideQuizResultDao(database: VocabulousDatabase): QuizResultDao {
        return database.quizResultDao()
    }
    
    @Provides
    @Singleton
    fun provideGrammarLessonDao(database: VocabulousDatabase): GrammarLessonDao {
        return database.grammarLessonDao()
    }
    
    @Provides
    @Singleton
    fun provideGrammarExerciseDao(database: VocabulousDatabase): GrammarExerciseDao {
        return database.grammarExerciseDao()
    }
    
    // Repositories
    // Note: VocabularyRepository is now provided by FirestoreModule
    /*
    @Provides
    @Singleton
    fun provideVocabularyRepository(
        vocabularyWordDao: VocabularyWordDao,
        wordCategoryDao: WordCategoryDao
    ): VocabularyRepository {
        return VocabularyRepositoryImpl(vocabularyWordDao, wordCategoryDao)
    }
    */
    
    // Note: UserProgressRepository is now provided by FirestoreModule
    /*
    @Provides
    @Singleton
    fun provideUserProgressRepository(
        userProgressDao: UserProgressDao,
        quizResultDao: QuizResultDao
    ): UserProgressRepository {
        return UserProgressRepositoryImpl(userProgressDao, quizResultDao)
    }
    */
    
    // Note: GrammarRepository is now provided by FirestoreModule
    /*
    @Provides
    @Singleton
    fun provideGrammarRepository(
        grammarLessonDao: GrammarLessonDao,
        grammarExerciseDao: GrammarExerciseDao
    ): IGrammarRepository {
        return GrammarRepository(grammarLessonDao, grammarExerciseDao)
    }
    */
}