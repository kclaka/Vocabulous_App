package com.aregyan.compose.di

import com.aregyan.compose.data.db.VocabularyWordDao
import com.aregyan.compose.data.db.WordCategoryDao
import com.aregyan.compose.data.db.WordPackDao
import com.aregyan.compose.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FirestoreModule {
    
    @Provides
    @Singleton
    fun provideVocabularyRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): VocabularyRepository {
        return FirestoreVocabularyRepository(firestore, auth)
    }
    
    @Provides
    @Singleton
    fun provideUserProgressRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): UserProgressRepository {
        return FirestoreUserProgressRepository(firestore, auth)
    }
    
    @Provides
    @Singleton
    fun provideGrammarRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): IGrammarRepository {
        return FirestoreGrammarRepository(firestore, auth)
    }
    
    @Provides
    @Singleton
    fun provideWordPackRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        wordPackDao: WordPackDao,
        vocabularyWordDao: VocabularyWordDao,
        wordCategoryDao: WordCategoryDao
    ): WordPackRepository {
        return FirestoreWordPackRepository(firestore, auth, wordPackDao, vocabularyWordDao, wordCategoryDao)
    }
}
