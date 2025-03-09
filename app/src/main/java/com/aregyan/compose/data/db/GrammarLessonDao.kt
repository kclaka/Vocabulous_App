package com.aregyan.compose.data.db

import androidx.room.*
import com.aregyan.compose.data.model.GrammarLesson
import kotlinx.coroutines.flow.Flow

@Dao
interface GrammarLessonDao {
    @Query("SELECT * FROM grammar_lessons ORDER BY `order` ASC")
    fun getAllLessons(): Flow<List<GrammarLesson>>
    
    @Query("SELECT * FROM grammar_lessons WHERE id = :lessonId")
    fun getLessonById(lessonId: String): Flow<GrammarLesson?>
    
    @Query("SELECT * FROM grammar_lessons WHERE difficulty = :difficulty ORDER BY `order` ASC")
    fun getLessonsByDifficulty(difficulty: Int): Flow<List<GrammarLesson>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: GrammarLesson)
    
    @Update
    suspend fun updateLesson(lesson: GrammarLesson)
    
    @Delete
    suspend fun deleteLesson(lesson: GrammarLesson)
    
    @Query("DELETE FROM grammar_lessons WHERE id = :lessonId")
    suspend fun deleteLessonById(lessonId: String)
    
    @Query("DELETE FROM grammar_lessons")
    suspend fun deleteAllLessons()
}
