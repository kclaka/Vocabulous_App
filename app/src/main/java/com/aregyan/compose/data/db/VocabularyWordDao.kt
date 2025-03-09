package com.aregyan.compose.data.db

import androidx.room.*
import com.aregyan.compose.data.model.VocabularyWord
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyWordDao {
    @Query("SELECT * FROM vocabulary_words ORDER BY word ASC")
    fun getAllWords(): Flow<List<VocabularyWord>>
    
    @Query("SELECT * FROM vocabulary_words WHERE id = :wordId")
    suspend fun getWordById(wordId: String): VocabularyWord?
    
    @Query("SELECT * FROM vocabulary_words WHERE categoryId = :categoryId ORDER BY word ASC")
    fun getWordsByCategory(categoryId: String): Flow<List<VocabularyWord>>
    
    @Query("SELECT * FROM vocabulary_words WHERE difficulty = :level ORDER BY word ASC")
    fun getWordsByDifficulty(level: Int): Flow<List<VocabularyWord>>
    
    @Query("SELECT * FROM vocabulary_words WHERE word LIKE '%' || :query || '%' ORDER BY word ASC")
    fun searchWords(query: String): Flow<List<VocabularyWord>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: VocabularyWord)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<VocabularyWord>)
    
    @Update
    suspend fun updateWord(word: VocabularyWord)
    
    @Delete
    suspend fun deleteWord(word: VocabularyWord)
    
    @Query("DELETE FROM vocabulary_words WHERE id = :wordId")
    suspend fun deleteWordById(wordId: String)
    
    @Query("DELETE FROM vocabulary_words WHERE categoryId = :categoryId")
    suspend fun deleteWordsByCategory(categoryId: String)
    
    @Query("DELETE FROM vocabulary_words")
    suspend fun deleteAllWords()
    
    @Query("DELETE FROM vocabulary_words")
    suspend fun deleteAllVocabularyWords()
}
