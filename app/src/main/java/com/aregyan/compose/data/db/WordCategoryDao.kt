package com.aregyan.compose.data.db

import androidx.room.*
import com.aregyan.compose.data.model.WordCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface WordCategoryDao {
    @Query("SELECT * FROM word_categories ORDER BY `order` ASC")
    fun getAllCategories(): Flow<List<WordCategory>>
    
    @Query("SELECT * FROM word_categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): WordCategory?
    
    @Query("SELECT * FROM word_categories WHERE difficulty = :level ORDER BY `order` ASC")
    fun getCategoriesByDifficulty(level: Int): Flow<List<WordCategory>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: WordCategory)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<WordCategory>)
    
    @Update
    suspend fun updateCategory(category: WordCategory)
    
    @Delete
    suspend fun deleteCategory(category: WordCategory)
    
    @Query("DELETE FROM word_categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)
    
    @Query("DELETE FROM word_categories")
    suspend fun deleteAllCategories()
}
