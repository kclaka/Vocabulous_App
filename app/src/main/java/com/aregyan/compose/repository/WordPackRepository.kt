package com.aregyan.compose.repository

import com.aregyan.compose.data.model.WordPack
import kotlinx.coroutines.flow.Flow

interface WordPackRepository {
    fun getAllWordPacks(): Flow<List<WordPack>>
    fun getDownloadedWordPacks(): Flow<List<WordPack>>
    fun getWordPacksByLanguage(language: String): Flow<List<WordPack>>
    fun getWordPacksByTheme(theme: String): Flow<List<WordPack>>
    fun getWordPacksByDifficulty(level: Int): Flow<List<WordPack>>
    fun searchWordPacks(query: String): Flow<List<WordPack>>
    suspend fun getWordPackById(wordPackId: String): WordPack?
    suspend fun insertWordPack(wordPack: WordPack)
    suspend fun insertWordPacks(wordPacks: List<WordPack>)
    suspend fun updateWordPack(wordPack: WordPack)
    suspend fun deleteWordPack(wordPack: WordPack)
    suspend fun deleteWordPackById(wordPackId: String)
    suspend fun addWordPackToCollection(wordPackId: String)
    suspend fun removeWordPackFromCollection(wordPackId: String)
    suspend fun syncDownloadedWordPacks()
}
