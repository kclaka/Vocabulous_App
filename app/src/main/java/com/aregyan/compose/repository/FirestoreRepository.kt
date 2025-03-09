package com.aregyan.compose.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Base repository class for Firestore operations
 * Provides common functionality for all Firestore repositories
 */
abstract class FirestoreRepository<T : Any>(
    protected val firestore: FirebaseFirestore,
    protected val auth: FirebaseAuth,
    private val collectionPath: String
) {
    
    /**
     * Get the collection reference for the current user
     * Each user has their own collection to ensure data isolation
     */
    protected fun getUserCollection(): CollectionReference? {
        val userId = auth.currentUser?.uid ?: return null
        return firestore.collection("users")
            .document(userId)
            .collection(collectionPath)
    }
    
    /**
     * Add a document to the user's collection
     */
    protected suspend fun addDocument(id: String, data: T): Result<Unit> {
        return try {
            val collection = getUserCollection() 
            if (collection == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                collection.document(id).set(data).await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding document to Firestore")
            Result.failure(e)
        }
    }
    
    /**
     * Add multiple documents to the user's collection
     */
    protected suspend fun addDocuments(documents: Map<String, T>): Result<Unit> {
        return try {
            val collection = getUserCollection()
            if (collection == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                // Use a batch write for better performance
                val batch = firestore.batch()
                documents.forEach { (id, data) ->
                    val docRef = collection.document(id)
                    batch.set(docRef, data)
                }
                
                batch.commit().await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding documents to Firestore")
            Result.failure(e)
        }
    }
    
    /**
     * Update a document in the user's collection
     */
    protected suspend fun updateDocument(id: String, data: T): Result<Unit> {
        return try {
            val collection = getUserCollection()
            if (collection == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                collection.document(id).set(data).await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating document in Firestore")
            Result.failure(e)
        }
    }
    
    /**
     * Delete a document from the user's collection
     */
    protected suspend fun deleteDocument(id: String): Result<Unit> {
        return try {
            val collection = getUserCollection()
            if (collection == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                collection.document(id).delete().await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting document from Firestore")
            Result.failure(e)
        }
    }
    
    /**
     * Get a document from the user's collection
     */
    protected suspend fun getDocument(id: String, clazz: Class<T>): Result<T?> {
        return try {
            val collection = getUserCollection()
            if (collection == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val document = collection.document(id).get().await()
                val data = document.toObject(clazz)
                Result.success(data)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting document from Firestore")
            Result.failure(e)
        }
    }
    
    /**
     * Get all documents from the user's collection
     */
    protected suspend fun getAllDocuments(clazz: Class<T>): Result<List<T>> {
        return try {
            val collection = getUserCollection()
            if (collection == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val querySnapshot = collection.get().await()
                val documents = querySnapshot.documents.mapNotNull { it.toObject(clazz) }
                Result.success(documents)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting all documents from Firestore")
            Result.failure(e)
        }
    }
    
    /**
     * Query documents from the user's collection
     */
    protected suspend fun queryDocuments(queryBuilder: (CollectionReference) -> Query, clazz: Class<T>): Result<List<T>> {
        return try {
            val collection = getUserCollection()
            if (collection == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val query = queryBuilder(collection)
                val querySnapshot = query.get().await()
                val documents = querySnapshot.documents.mapNotNull { it.toObject(clazz) }
                Result.success(documents)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error querying documents from Firestore")
            Result.failure(e)
        }
    }
}
