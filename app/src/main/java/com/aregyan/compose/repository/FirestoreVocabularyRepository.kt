package com.aregyan.compose.repository

import com.aregyan.compose.data.model.VocabularyWord
import com.aregyan.compose.data.model.WordCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of VocabularyRepository that stores data in Firestore
 * Each user has their own collection of vocabulary words and categories
 */
@Singleton
class FirestoreVocabularyRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : VocabularyRepository {
    
    private val wordsCollection = "vocabulary_words"
    private val categoriesCollection = "word_categories"
    
    // Word methods
    override fun getAllWords(): Flow<List<VocabularyWord>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val collection = firestore.collection("users")
            .document(userId)
            .collection(wordsCollection)
        
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to vocabulary words")
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val words = snapshot?.documents?.mapNotNull {
                it.toObject(VocabularyWord::class.java)
            } ?: emptyList()
            
            trySend(words)
        }
        
        awaitClose { listener.remove() }
    }
    
    override fun getWordsByCategory(categoryId: String): Flow<List<VocabularyWord>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val collection = firestore.collection("users")
            .document(userId)
            .collection(wordsCollection)
            .whereEqualTo("categoryId", categoryId)
        
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to vocabulary words by category")
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val words = snapshot?.documents?.mapNotNull {
                it.toObject(VocabularyWord::class.java)
            } ?: emptyList()
            
            trySend(words)
        }
        
        awaitClose { listener.remove() }
    }
    
    override fun getWordsByDifficulty(level: Int): Flow<List<VocabularyWord>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val collection = firestore.collection("users")
            .document(userId)
            .collection(wordsCollection)
            .whereEqualTo("difficulty", level)
        
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to vocabulary words by difficulty")
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val words = snapshot?.documents?.mapNotNull {
                it.toObject(VocabularyWord::class.java)
            } ?: emptyList()
            
            trySend(words)
        }
        
        awaitClose { listener.remove() }
    }
    
    override fun searchWords(query: String): Flow<List<VocabularyWord>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        // Firestore doesn't support full-text search, so we'll do a client-side filter
        val collection = firestore.collection("users")
            .document(userId)
            .collection(wordsCollection)
        
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error searching vocabulary words")
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val words = snapshot?.documents?.mapNotNull {
                it.toObject(VocabularyWord::class.java)
            } ?: emptyList()
            
            // Filter words that contain the query in word or definition
            val filteredWords = words.filter { word ->
                val lowerQuery = query.lowercase()
                word.word.lowercase().contains(lowerQuery) ||
                word.definition.lowercase().contains(lowerQuery)
            }
            
            trySend(filteredWords)
        }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun getWordById(wordId: String): VocabularyWord? {
        val userId = auth.currentUser?.uid ?: return null
        
        try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection(wordsCollection)
                .document(wordId)
            
            val snapshot = docRef.get().await()
            return snapshot.toObject(VocabularyWord::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error getting vocabulary word by ID")
            return null
        }
    }
    
    override suspend fun insertWord(word: VocabularyWord) {
        val userId = auth.currentUser?.uid ?: return
        
        try {
            firestore.collection("users")
                .document(userId)
                .collection(wordsCollection)
                .document(word.id)
                .set(word)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error inserting vocabulary word")
        }
    }
    
    override suspend fun insertWords(words: List<VocabularyWord>) {
        val userId = auth.currentUser?.uid ?: return
        
        try {
            val batch = firestore.batch()
            
            words.forEach { word ->
                val docRef = firestore.collection("users")
                    .document(userId)
                    .collection(wordsCollection)
                    .document(word.id)
                batch.set(docRef, word)
            }
            
            batch.commit().await()
        } catch (e: Exception) {
            Timber.e(e, "Error inserting vocabulary words")
        }
    }
    
    override suspend fun updateWord(word: VocabularyWord) {
        // Same implementation as insert since Firestore's set() will update if document exists
        insertWord(word)
    }
    
    override suspend fun deleteWord(word: VocabularyWord) {
        val userId = auth.currentUser?.uid ?: return
        
        try {
            firestore.collection("users")
                .document(userId)
                .collection(wordsCollection)
                .document(word.id)
                .delete()
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error deleting vocabulary word")
        }
    }
    
    override suspend fun deleteWordById(wordId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        try {
            firestore.collection("users")
                .document(userId)
                .collection(wordsCollection)
                .document(wordId)
                .delete()
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error deleting vocabulary word by ID")
        }
    }
    
    override suspend fun deleteWordsByCategory(categoryId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        try {
            // Get all words with the given category ID
            val documents = firestore.collection("users")
                .document(userId)
                .collection(wordsCollection)
                .whereEqualTo("categoryId", categoryId)
                .get()
                .await()
                .documents
            
            // Batch delete all words
            if (documents.isNotEmpty()) {
                val batch = firestore.batch()
                documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting words by category")
        }
    }
    
    // Category methods
    override fun getAllCategories(): Flow<List<WordCategory>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val collection = firestore.collection("users")
            .document(userId)
            .collection(categoriesCollection)
            .orderBy("order")
        
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to word categories")
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val categories = snapshot?.documents?.mapNotNull {
                it.toObject(WordCategory::class.java)
            } ?: emptyList()
            
            trySend(categories)
        }
        
        awaitClose { listener.remove() }
    }
    
    override fun getCategoriesByDifficulty(level: Int): Flow<List<WordCategory>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val collection = firestore.collection("users")
            .document(userId)
            .collection(categoriesCollection)
            .whereEqualTo("difficulty", level)
            .orderBy("order")
        
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to word categories by difficulty")
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val categories = snapshot?.documents?.mapNotNull {
                it.toObject(WordCategory::class.java)
            } ?: emptyList()
            
            trySend(categories)
        }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun getCategoryById(categoryId: String): WordCategory? {
        val userId = auth.currentUser?.uid ?: return null
        
        try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection(categoriesCollection)
                .document(categoryId)
            
            val snapshot = docRef.get().await()
            return snapshot.toObject(WordCategory::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error getting word category by ID")
            return null
        }
    }
    
    override suspend fun insertCategory(category: WordCategory) {
        val userId = auth.currentUser?.uid ?: return
        
        try {
            firestore.collection("users")
                .document(userId)
                .collection(categoriesCollection)
                .document(category.id)
                .set(category)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error inserting word category")
        }
    }
    
    override suspend fun insertCategories(categories: List<WordCategory>) {
        val userId = auth.currentUser?.uid ?: return
        
        try {
            val batch = firestore.batch()
            
            categories.forEach { category ->
                val docRef = firestore.collection("users")
                    .document(userId)
                    .collection(categoriesCollection)
                    .document(category.id)
                batch.set(docRef, category)
            }
            
            batch.commit().await()
        } catch (e: Exception) {
            Timber.e(e, "Error inserting word categories")
        }
    }
    
    override suspend fun updateCategory(category: WordCategory) {
        // Same implementation as insert
        insertCategory(category)
    }
    
    override suspend fun deleteCategory(category: WordCategory) {
        val userId = auth.currentUser?.uid ?: return
        
        try {
            // Delete the category
            firestore.collection("users")
                .document(userId)
                .collection(categoriesCollection)
                .document(category.id)
                .delete()
                .await()
            
            // Also delete all words in this category
            deleteWordsByCategory(category.id)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting word category")
        }
    }
    
    override suspend fun deleteCategoryById(categoryId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        try {
            // Delete the category
            firestore.collection("users")
                .document(userId)
                .collection(categoriesCollection)
                .document(categoryId)
                .delete()
                .await()
            
            // Also delete all words in this category
            deleteWordsByCategory(categoryId)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting word category by ID")
        }
    }
}
