package com.aregyan.compose.repository

import com.aregyan.compose.data.db.VocabularyWordDao
import com.aregyan.compose.data.db.WordCategoryDao
import com.aregyan.compose.data.model.VocabularyWord
import com.aregyan.compose.data.model.WordCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface VocabularyRepository {
    fun getAllWords(): Flow<List<VocabularyWord>>
    fun getWordsByCategory(categoryId: String): Flow<List<VocabularyWord>>
    fun getWordsByDifficulty(level: Int): Flow<List<VocabularyWord>>
    fun searchWords(query: String): Flow<List<VocabularyWord>>
    suspend fun getWordById(wordId: String): VocabularyWord?
    suspend fun insertWord(word: VocabularyWord)
    suspend fun insertWords(words: List<VocabularyWord>)
    suspend fun updateWord(word: VocabularyWord)
    suspend fun deleteWord(word: VocabularyWord)
    suspend fun deleteWordById(wordId: String)
    suspend fun deleteWordsByCategory(categoryId: String)
    
    fun getAllCategories(): Flow<List<WordCategory>>
    fun getCategoriesByDifficulty(level: Int): Flow<List<WordCategory>>
    suspend fun getCategoryById(categoryId: String): WordCategory?
    suspend fun insertCategory(category: WordCategory)
    suspend fun insertCategories(categories: List<WordCategory>)
    suspend fun updateCategory(category: WordCategory)
    suspend fun deleteCategory(category: WordCategory)
    suspend fun deleteCategoryById(categoryId: String)
}

class VocabularyRepositoryImpl @Inject constructor(
    private val vocabularyWordDao: VocabularyWordDao,
    private val wordCategoryDao: WordCategoryDao
) : VocabularyRepository {
    
    override fun getAllWords(): Flow<List<VocabularyWord>> = 
        vocabularyWordDao.getAllWords()
    
    override fun getWordsByCategory(categoryId: String): Flow<List<VocabularyWord>> = 
        vocabularyWordDao.getWordsByCategory(categoryId)
    
    override fun getWordsByDifficulty(level: Int): Flow<List<VocabularyWord>> = 
        vocabularyWordDao.getWordsByDifficulty(level)
    
    override fun searchWords(query: String): Flow<List<VocabularyWord>> = 
        vocabularyWordDao.searchWords(query)
    
    override suspend fun getWordById(wordId: String): VocabularyWord? = 
        vocabularyWordDao.getWordById(wordId)
    
    override suspend fun insertWord(word: VocabularyWord) = 
        vocabularyWordDao.insertWord(word)
    
    override suspend fun insertWords(words: List<VocabularyWord>) = 
        vocabularyWordDao.insertWords(words)
    
    override suspend fun updateWord(word: VocabularyWord) = 
        vocabularyWordDao.updateWord(word)
    
    override suspend fun deleteWord(word: VocabularyWord) = 
        vocabularyWordDao.deleteWord(word)
    
    override suspend fun deleteWordById(wordId: String) = 
        vocabularyWordDao.deleteWordById(wordId)
    
    override suspend fun deleteWordsByCategory(categoryId: String) =
        vocabularyWordDao.deleteWordsByCategory(categoryId)
    
    override fun getAllCategories(): Flow<List<WordCategory>> = 
        wordCategoryDao.getAllCategories()
    
    override fun getCategoriesByDifficulty(level: Int): Flow<List<WordCategory>> = 
        wordCategoryDao.getCategoriesByDifficulty(level)
    
    override suspend fun getCategoryById(categoryId: String): WordCategory? = 
        wordCategoryDao.getCategoryById(categoryId)
    
    override suspend fun insertCategory(category: WordCategory) = 
        wordCategoryDao.insertCategory(category)
    
    override suspend fun insertCategories(categories: List<WordCategory>) = 
        wordCategoryDao.insertCategories(categories)
    
    override suspend fun updateCategory(category: WordCategory) = 
        wordCategoryDao.updateCategory(category)
    
    override suspend fun deleteCategory(category: WordCategory) = 
        wordCategoryDao.deleteCategory(category)
    
    override suspend fun deleteCategoryById(categoryId: String) = 
        wordCategoryDao.deleteCategoryById(categoryId)
}
