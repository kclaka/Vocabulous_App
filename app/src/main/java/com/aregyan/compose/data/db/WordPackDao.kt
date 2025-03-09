package com.aregyan.compose.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aregyan.compose.data.model.WordPack
import kotlinx.coroutines.flow.Flow

@Dao
interface WordPackDao {
    @Query("SELECT * FROM word_packs ORDER BY name ASC")
    fun getAllWordPacks(): Flow<List<WordPack>>
    
    @Query("SELECT * FROM word_packs WHERE isDownloaded = 1 ORDER BY name ASC")
    fun getDownloadedWordPacks(): Flow<List<WordPack>>
    
    @Query("SELECT * FROM word_packs WHERE language = :language ORDER BY name ASC")
    fun getWordPacksByLanguage(language: String): Flow<List<WordPack>>
    
    @Query("SELECT * FROM word_packs WHERE theme = :theme ORDER BY name ASC")
    fun getWordPacksByTheme(theme: String): Flow<List<WordPack>>
    
    @Query("SELECT * FROM word_packs WHERE difficulty = :level ORDER BY name ASC")
    fun getWordPacksByDifficulty(level: Int): Flow<List<WordPack>>
    
    @Query("SELECT * FROM word_packs WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR theme LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchWordPacks(query: String): Flow<List<WordPack>>
    
    @Query("SELECT * FROM word_packs WHERE id = :wordPackId LIMIT 1")
    suspend fun getWordPackById(wordPackId: String): WordPack?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordPack(wordPack: WordPack)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordPacks(wordPacks: List<WordPack>)
    
    @Update
    suspend fun updateWordPack(wordPack: WordPack)
    
    @Delete
    suspend fun deleteWordPack(wordPack: WordPack)
    
    @Query("DELETE FROM word_packs WHERE id = :wordPackId")
    suspend fun deleteWordPackById(wordPackId: String)
    
    @Query("DELETE FROM word_packs")
    suspend fun deleteAllWordPacks()
}
