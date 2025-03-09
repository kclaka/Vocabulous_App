package com.aregyan.compose.repository

import com.aregyan.compose.data.db.GrammarExerciseDao
import com.aregyan.compose.data.db.GrammarLessonDao
import com.aregyan.compose.data.model.GrammarExercise
import com.aregyan.compose.data.model.GrammarLesson
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrammarRepository @Inject constructor(
    private val grammarLessonDao: GrammarLessonDao,
    private val grammarExerciseDao: GrammarExerciseDao
) : IGrammarRepository {
    // Lesson operations
    override fun getAllLessons(): Flow<List<GrammarLesson>> = grammarLessonDao.getAllLessons()
    
    override fun getLessonById(lessonId: String): Flow<GrammarLesson?> = grammarLessonDao.getLessonById(lessonId)
    
    override fun getLessonsByDifficulty(difficulty: Int): Flow<List<GrammarLesson>> = 
        grammarLessonDao.getLessonsByDifficulty(difficulty)
    
    override suspend fun insertLesson(lesson: GrammarLesson) {
        val lessonWithId = if (lesson.id.isEmpty()) {
            lesson.copy(id = UUID.randomUUID().toString())
        } else {
            lesson
        }
        grammarLessonDao.insertLesson(lessonWithId)
    }
    
    override suspend fun updateLesson(lesson: GrammarLesson) = grammarLessonDao.updateLesson(lesson)
    
    override suspend fun deleteLesson(lesson: GrammarLesson) = grammarLessonDao.deleteLesson(lesson)
    
    override suspend fun deleteLessonById(lessonId: String) = grammarLessonDao.deleteLessonById(lessonId)
    
    // Exercise operations
    override fun getExercisesByLessonId(lessonId: String): Flow<List<GrammarExercise>> = 
        grammarExerciseDao.getExercisesByLessonId(lessonId)
    
    override fun getExerciseById(exerciseId: String): Flow<GrammarExercise?> = 
        grammarExerciseDao.getExerciseById(exerciseId)
    
    override suspend fun insertExercise(exercise: GrammarExercise) {
        val exerciseWithId = if (exercise.id.isEmpty()) {
            exercise.copy(id = UUID.randomUUID().toString())
        } else {
            exercise
        }
        grammarExerciseDao.insertExercise(exerciseWithId)
    }
    
    override suspend fun insertExercises(exercises: List<GrammarExercise>) {
        val exercisesWithIds = exercises.map { exercise ->
            if (exercise.id.isEmpty()) {
                exercise.copy(id = UUID.randomUUID().toString())
            } else {
                exercise
            }
        }
        grammarExerciseDao.insertExercises(exercisesWithIds)
    }
    
    override suspend fun updateExercise(exercise: GrammarExercise) = grammarExerciseDao.updateExercise(exercise)
    
    override suspend fun deleteExercise(exercise: GrammarExercise) = grammarExerciseDao.deleteExercise(exercise)
    
    override suspend fun deleteExercisesByLessonId(lessonId: String) = 
        grammarExerciseDao.deleteExercisesByLessonId(lessonId)
}
