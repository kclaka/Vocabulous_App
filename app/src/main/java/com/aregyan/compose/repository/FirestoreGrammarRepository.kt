package com.aregyan.compose.repository

import com.aregyan.compose.data.model.GrammarExercise
import com.aregyan.compose.data.model.GrammarLesson
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of IGrammarRepository that stores data in Firestore
 * Each user has their own collection of grammar lessons and exercises
 */
@Singleton
class FirestoreGrammarRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : IGrammarRepository {
    
    private val lessonCollection = "grammar_lessons"
    private val exerciseCollection = "grammar_exercises"
    
    // Lesson operations
    override fun getAllLessons(): Flow<List<GrammarLesson>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val collection = firestore.collection("users")
            .document(userId)
            .collection(lessonCollection)
            .orderBy("order")
        
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to grammar lessons")
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val lessons = snapshot?.documents?.mapNotNull {
                it.toObject(GrammarLesson::class.java)
            } ?: emptyList()
            
            trySend(lessons)
        }
        
        awaitClose { listener.remove() }
    }
    
    override fun getLessonById(lessonId: String): Flow<GrammarLesson?> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            return@callbackFlow
        }
        
        val docRef = firestore.collection("users")
            .document(userId)
            .collection(lessonCollection)
            .document(lessonId)
        
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to grammar lesson")
                trySend(null)
                return@addSnapshotListener
            }
            
            val lesson = snapshot?.toObject(GrammarLesson::class.java)
            trySend(lesson)
        }
        
        awaitClose { listener.remove() }
    }
    
    override fun getLessonsByDifficulty(difficulty: Int): Flow<List<GrammarLesson>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val collection = firestore.collection("users")
            .document(userId)
            .collection(lessonCollection)
            .whereEqualTo("difficulty", difficulty)
            .orderBy("order")
        
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to grammar lessons by difficulty")
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val lessons = snapshot?.documents?.mapNotNull {
                it.toObject(GrammarLesson::class.java)
            } ?: emptyList()
            
            trySend(lessons)
        }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun insertLesson(lesson: GrammarLesson) {
        val lessonWithId = if (lesson.id.isEmpty()) {
            lesson.copy(id = UUID.randomUUID().toString())
        } else {
            lesson
        }
        
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection(lessonCollection)
            .document(lessonWithId.id)
            .set(lessonWithId)
            .await()
    }
    
    override suspend fun updateLesson(lesson: GrammarLesson) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection(lessonCollection)
            .document(lesson.id)
            .set(lesson)
            .await()
    }
    
    override suspend fun deleteLesson(lesson: GrammarLesson) {
        deleteLessonById(lesson.id)
    }
    
    override suspend fun deleteLessonById(lessonId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        // Delete the lesson
        firestore.collection("users")
            .document(userId)
            .collection(lessonCollection)
            .document(lessonId)
            .delete()
            .await()
        
        // Also delete associated exercises
        deleteExercisesByLessonId(lessonId)
    }
    
    // Exercise operations
    override fun getExercisesByLessonId(lessonId: String): Flow<List<GrammarExercise>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val collection = firestore.collection("users")
            .document(userId)
            .collection(exerciseCollection)
            .whereEqualTo("lessonId", lessonId)
            .orderBy("order")
        
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to grammar exercises")
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val exercises = snapshot?.documents?.mapNotNull {
                it.toObject(GrammarExercise::class.java)
            } ?: emptyList()
            
            trySend(exercises)
        }
        
        awaitClose { listener.remove() }
    }
    
    override fun getExerciseById(exerciseId: String): Flow<GrammarExercise?> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            return@callbackFlow
        }
        
        val docRef = firestore.collection("users")
            .document(userId)
            .collection(exerciseCollection)
            .document(exerciseId)
        
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to grammar exercise")
                trySend(null)
                return@addSnapshotListener
            }
            
            val exercise = snapshot?.toObject(GrammarExercise::class.java)
            trySend(exercise)
        }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun insertExercise(exercise: GrammarExercise) {
        val exerciseWithId = if (exercise.id.isEmpty()) {
            exercise.copy(id = UUID.randomUUID().toString())
        } else {
            exercise
        }
        
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection(exerciseCollection)
            .document(exerciseWithId.id)
            .set(exerciseWithId)
            .await()
    }
    
    override suspend fun insertExercises(exercises: List<GrammarExercise>) {
        val exercisesWithIds = exercises.map { exercise ->
            if (exercise.id.isEmpty()) {
                exercise.copy(id = UUID.randomUUID().toString())
            } else {
                exercise
            }
        }
        
        val userId = auth.currentUser?.uid ?: return
        val batch = firestore.batch()
        
        exercisesWithIds.forEach { exercise ->
            val docRef = firestore.collection("users")
                .document(userId)
                .collection(exerciseCollection)
                .document(exercise.id)
            batch.set(docRef, exercise)
        }
        
        batch.commit().await()
    }
    
    override suspend fun updateExercise(exercise: GrammarExercise) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection(exerciseCollection)
            .document(exercise.id)
            .set(exercise)
            .await()
    }
    
    override suspend fun deleteExercise(exercise: GrammarExercise) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection(exerciseCollection)
            .document(exercise.id)
            .delete()
            .await()
    }
    
    override suspend fun deleteExercisesByLessonId(lessonId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        // Query for exercises with the given lessonId
        val exercises = firestore.collection("users")
            .document(userId)
            .collection(exerciseCollection)
            .whereEqualTo("lessonId", lessonId)
            .get()
            .await()
            .documents
        
        // Batch delete all exercises
        if (exercises.isNotEmpty()) {
            val batch = firestore.batch()
            exercises.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
        }
    }
}
