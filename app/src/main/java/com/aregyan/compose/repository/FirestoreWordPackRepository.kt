package com.aregyan.compose.repository

import com.aregyan.compose.data.db.VocabularyWordDao
import com.aregyan.compose.data.db.WordCategoryDao
import com.aregyan.compose.data.db.WordPackDao
import com.aregyan.compose.data.model.FirestoreWordPack
import com.aregyan.compose.data.model.VocabularyWord
import com.aregyan.compose.data.model.WordCategory
import com.aregyan.compose.data.model.WordPack
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WordPackRepository that stores data in Firestore and Room
 * Public word packs are stored in a global collection
 * User's downloaded word packs are tracked in their user document
 */
@Singleton
class FirestoreWordPackRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val wordPackDao: WordPackDao,
    private val vocabularyWordDao: VocabularyWordDao,
    private val wordCategoryDao: WordCategoryDao
) : WordPackRepository {
    
    private val wordPacksCollection = "word_packs"
    private val userWordPacksCollection = "user_word_packs"
    
    override fun getAllWordPacks(): Flow<List<WordPack>> = callbackFlow {
        // First, get all public word packs from Firestore
        val listener = firestore.collection(wordPacksCollection)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to word packs")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val wordPacks = snapshot?.documents?.mapNotNull {
                    it.toObject(FirestoreWordPack::class.java)?.let { pack ->
                        // Set the ID from the document
                        pack.id = it.id
                        // Convert to Room entity
                        WordPack.fromFirestore(pack)
                    }
                } ?: emptyList()
                
                // Always send a result, even if empty
                trySend(wordPacks)
            }
        
        awaitClose { listener.remove() }
    }
    
    override fun getDownloadedWordPacks(): Flow<List<WordPack>> {
        // Return downloaded word packs from Room database
        return wordPackDao.getDownloadedWordPacks()
    }
    
    override fun getWordPacksByLanguage(language: String): Flow<List<WordPack>> = callbackFlow {
        val listener = firestore.collection(wordPacksCollection)
            .whereEqualTo("language", language)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to word packs by language")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val wordPacks = snapshot?.documents?.mapNotNull {
                    it.toObject(FirestoreWordPack::class.java)?.let { pack ->
                        // Set the ID from the document
                        pack.id = it.id
                        // Convert to Room entity
                        WordPack.fromFirestore(pack)
                    }
                } ?: emptyList()
                
                trySend(wordPacks)
            }
        
        awaitClose { listener.remove() }
    }
    
    override fun getWordPacksByTheme(theme: String): Flow<List<WordPack>> = callbackFlow {
        val listener = firestore.collection(wordPacksCollection)
            .whereEqualTo("theme", theme)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to word packs by theme")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val wordPacks = snapshot?.documents?.mapNotNull {
                    it.toObject(FirestoreWordPack::class.java)?.let { pack ->
                        // Set the ID from the document
                        pack.id = it.id
                        // Convert to Room entity
                        WordPack.fromFirestore(pack)
                    }
                } ?: emptyList()
                
                trySend(wordPacks)
            }
        
        awaitClose { listener.remove() }
    }
    
    override fun getWordPacksByDifficulty(level: Int): Flow<List<WordPack>> = callbackFlow {
        val listener = firestore.collection(wordPacksCollection)
            .whereEqualTo("difficulty", level)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to word packs by difficulty")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val wordPacks = snapshot?.documents?.mapNotNull {
                    it.toObject(FirestoreWordPack::class.java)?.let { pack ->
                        // Set the ID from the document
                        pack.id = it.id
                        // Convert to Room entity
                        WordPack.fromFirestore(pack)
                    }
                } ?: emptyList()
                
                trySend(wordPacks)
            }
        
        awaitClose { listener.remove() }
    }
    
    override fun searchWordPacks(query: String): Flow<List<WordPack>> = callbackFlow {
        // Firestore doesn't support full-text search, so we'll get all word packs and filter client-side
        val listener = firestore.collection(wordPacksCollection)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error searching word packs")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val wordPacks = snapshot?.documents?.mapNotNull {
                    it.toObject(FirestoreWordPack::class.java)?.let { pack ->
                        // Set the ID from the document
                        pack.id = it.id
                        // Convert to Room entity
                        WordPack.fromFirestore(pack)
                    }
                } ?: emptyList()
                
                // Filter word packs that match the query
                val lowerQuery = query.lowercase()
                val filteredPacks = wordPacks.filter { pack ->
                    pack.name.lowercase().contains(lowerQuery) ||
                    pack.description.lowercase().contains(lowerQuery) ||
                    pack.theme.lowercase().contains(lowerQuery) ||
                    pack.language.lowercase().contains(lowerQuery) ||
                    pack.tags.any { it.lowercase().contains(lowerQuery) }
                }
                
                trySend(filteredPacks)
            }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun getWordPackById(wordPackId: String): WordPack? {
        // First check local database
        val localPack = wordPackDao.getWordPackById(wordPackId)
        if (localPack != null) {
            return localPack
        }
        
        // If not found locally, check Firestore
        return try {
            val document = firestore.collection(wordPacksCollection).document(wordPackId).get().await()
            document.toObject(FirestoreWordPack::class.java)?.let { pack ->
                // Set the ID from the document
                pack.id = document.id
                // Convert to Room entity
                WordPack.fromFirestore(pack)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting word pack by ID")
            null
        }
    }
    
    override suspend fun insertWordPack(wordPack: WordPack) {
        try {
            // Save to local database
            wordPackDao.insertWordPack(wordPack)
            
            // We don't save to Firestore here as that's an admin function
        } catch (e: Exception) {
            Timber.e(e, "Error inserting word pack")
            throw e
        }
    }
    
    override suspend fun insertWordPacks(wordPacks: List<WordPack>) {
        try {
            // Save to local database
            wordPackDao.insertWordPacks(wordPacks)
            
            // We don't save to Firestore here as that's an admin function
        } catch (e: Exception) {
            Timber.e(e, "Error inserting word packs")
            throw e
        }
    }
    
    override suspend fun updateWordPack(wordPack: WordPack) {
        // Same implementation as insert since Room's insert will update if entity exists
        insertWordPack(wordPack)
    }
    
    override suspend fun deleteWordPack(wordPack: WordPack) {
        deleteWordPackById(wordPack.id)
    }
    
    override suspend fun deleteWordPackById(wordPackId: String) {
        try {
            // Remove from local database
            wordPackDao.deleteWordPackById(wordPackId)
            
            // We don't delete from Firestore here as that's an admin function
        } catch (e: Exception) {
            Timber.e(e, "Error deleting word pack by ID")
            throw e
        }
    }
    
    override suspend fun addWordPackToCollection(wordPackId: String) {
        try {
            // Get the word pack from Firestore
            val wordPack = getWordPackById(wordPackId) ?: throw Exception("Word pack not found")
            
            // Mark as downloaded and save to local database
            val downloadedPack = wordPack.copy(isDownloaded = true)
            wordPackDao.insertWordPack(downloadedPack)
            
            // Also track in Firestore that this user has downloaded this pack
            val userId = auth.currentUser?.uid ?: return
            firestore.collection("users")
                .document(userId)
                .collection(userWordPacksCollection)
                .document(wordPackId)
                .set(mapOf(
                    "addedAt" to System.currentTimeMillis(),
                    "wordPackId" to wordPackId,
                    "wordPackName" to wordPack.name
                ))
                .await()
                
            // Create a category for this word pack
            val categoryId = UUID.randomUUID().toString()
            val category = WordCategory(
                id = categoryId,
                name = wordPack.name,
                description = wordPack.description,
                difficulty = wordPack.difficulty,
                order = 0 // Will be at the top of the list
            )
            
            // Save the category to the local database
            wordCategoryDao.insertCategory(category)
            
            // Also save the category to Firestore for this user
            firestore.collection("users")
                .document(userId)
                .collection("word_categories")
                .document(categoryId)
                .set(mapOf(
                    "id" to categoryId,
                    "name" to wordPack.name,
                    "description" to wordPack.description,
                    "difficulty" to wordPack.difficulty,
                    "order" to 0,
                    "createdAt" to System.currentTimeMillis(),
                    "wordPackId" to wordPackId  // Link back to the word pack
                ))
                .await()
            
            Timber.d("Created category '${wordPack.name}' with ID: $categoryId for word pack: $wordPackId")
            
            // Try to fetch the words for this word pack from Firestore
            Timber.d("Fetching words for word pack ${wordPack.name} (ID: $wordPackId)")
            try {
                // First try to get words from a subcollection
                var wordsSnapshot = firestore.collection(wordPacksCollection)
                    .document(wordPackId)
                    .collection("words")
                    .get()
                    .await()
                
                // If no words found in subcollection, try to get words array from the document itself
                if (wordsSnapshot.documents.isEmpty()) {
                    Timber.d("No words found in subcollection, checking for words array in the document")
                    val wordPackDoc = firestore.collection(wordPacksCollection)
                        .document(wordPackId)
                        .get()
                        .await()
                    
                    val wordsArray = wordPackDoc.get("words") as? List<Map<String, Any>>
                    if (wordsArray != null && wordsArray.isNotEmpty()) {
                        Timber.d("Found ${wordsArray.size} words in the document's words array")
                        
                        // Convert the words array to vocabulary words
                        val words = wordsArray.mapIndexedNotNull { index, wordMap ->
                            try {
                                val word = wordMap["word"] as? String ?: ""
                                val definition = wordMap["definition"] as? String ?: ""
                                val partOfSpeech = wordMap["partOfSpeech"] as? String ?: ""
                                val pronunciation = wordMap["pronunciation"] as? String ?: ""
                                
                                // Handle both example and exampleSentence field names
                                val example = (wordMap["example"] as? String) ?: (wordMap["exampleSentence"] as? String) ?: ""
                                
                                val difficulty = when (val diff = wordMap["difficulty"]) {
                                    is Long -> diff.toInt()
                                    is Int -> diff
                                    is Double -> diff.toInt()
                                    else -> wordPack.difficulty
                                }
                                
                                val imageUrl = wordMap["imageUrl"] as? String
                                
                                Timber.d("Processing array word: $word, definition: $definition, example: $example")
                                
                                if (word.isNotBlank() && definition.isNotBlank()) {
                                    val wordId = UUID.randomUUID().toString()
                                    
                                    // Create vocabulary word with categoryId to link it to the category
                                    val vocabularyWord = VocabularyWord(
                                        id = wordId,
                                        word = word,
                                        definition = definition,
                                        partOfSpeech = partOfSpeech,
                                        pronunciation = pronunciation,
                                        example = example,
                                        difficulty = difficulty,
                                        imageUrl = imageUrl,
                                        categoryId = categoryId,  // Link to the category
                                        createdAt = System.currentTimeMillis()
                                    )
                                    
                                    // Also save the word to Firestore for this user
                                    firestore.collection("users")
                                        .document(userId)
                                        .collection("vocabulary_words")
                                        .document(wordId)
                                        .set(mapOf(
                                            "id" to wordId,
                                            "word" to word,
                                            "definition" to definition,
                                            "partOfSpeech" to partOfSpeech,
                                            "pronunciation" to pronunciation,
                                            "example" to example,
                                            "difficulty" to difficulty,
                                            "imageUrl" to imageUrl,
                                            "categoryId" to categoryId,  // Link to the category
                                            "createdAt" to System.currentTimeMillis(),
                                            "wordPackId" to wordPackId  // Link back to the word pack
                                        ))
                                    
                                    vocabularyWord
                                } else {
                                    Timber.w("Skipping array word with empty word or definition: $word")
                                    null
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Error converting array word to VocabularyWord: ${e.message}")
                                null
                            }
                        }
                        
                        // If we got valid words from the array, add them to the database
                        if (words.isNotEmpty()) {
                            vocabularyWordDao.insertWords(words)
                            Timber.d("Added ${words.size} words from word pack array for ${wordPack.name} in category $categoryId")
                            return@addWordPackToCollection
                        } else {
                            Timber.w("No valid words found in the array for word pack ${wordPack.name}, trying subcollection")
                        }
                    }
                }
                
                Timber.d("Found ${wordsSnapshot.documents.size} words in Firestore subcollection for word pack ${wordPack.name}")
                
                // If we got words from the subcollection, use them
                if (wordsSnapshot.documents.isNotEmpty()) {
                    val words = wordsSnapshot.documents.mapNotNull { doc ->
                        try {
                            val word = doc.getString("word") ?: ""
                            val definition = doc.getString("definition") ?: ""
                            val partOfSpeech = doc.getString("partOfSpeech") ?: ""
                            val pronunciation = doc.getString("pronunciation") ?: ""
                            
                            // Handle both example and exampleSentence field names
                            val example = doc.getString("example") ?: doc.getString("exampleSentence") ?: ""
                            
                            val difficulty = doc.getLong("difficulty")?.toInt() ?: wordPack.difficulty
                            val imageUrl = doc.getString("imageUrl")
                            
                            Timber.d("Processing subcollection word: $word, definition: $definition, example: $example")
                            
                            if (word.isNotBlank() && definition.isNotBlank()) {
                                val wordId = doc.id
                                
                                // Create vocabulary word with categoryId to link it to the category
                                val vocabularyWord = VocabularyWord(
                                    id = wordId,
                                    word = word,
                                    definition = definition,
                                    partOfSpeech = partOfSpeech,
                                    pronunciation = pronunciation,
                                    example = example,
                                    difficulty = difficulty,
                                    imageUrl = imageUrl,
                                    categoryId = categoryId,  // Link to the category
                                    createdAt = System.currentTimeMillis()
                                )
                                
                                // Also save the word to Firestore for this user
                                firestore.collection("users")
                                    .document(userId)
                                    .collection("vocabulary_words")
                                    .document(wordId)
                                    .set(mapOf(
                                        "id" to wordId,
                                        "word" to word,
                                        "definition" to definition,
                                        "partOfSpeech" to partOfSpeech,
                                        "pronunciation" to pronunciation,
                                        "example" to example,
                                        "difficulty" to difficulty,
                                        "imageUrl" to imageUrl,
                                        "categoryId" to categoryId,  // Link to the category
                                        "createdAt" to System.currentTimeMillis(),
                                        "wordPackId" to wordPackId  // Link back to the word pack
                                    ))
                                
                                vocabularyWord
                            } else {
                                Timber.w("Skipping subcollection word with empty word or definition: $word")
                                null
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error converting subcollection word document to VocabularyWord: ${e.message}")
                            null
                        }
                    }
                    
                    // If we got valid words from Firestore, add them to the database
                    if (words.isNotEmpty()) {
                        vocabularyWordDao.insertWords(words)
                        Timber.d("Added ${words.size} words from Firestore subcollection for ${wordPack.name} in category $categoryId")
                    } else {
                        Timber.w("No valid words found in Firestore subcollection for word pack ${wordPack.name}")
                    }
                } else {
                    Timber.w("No words found in Firestore for word pack ${wordPack.name}")
                }
            } catch (e: Exception) {
                // Just log the error
                Timber.e(e, "Error fetching words from Firestore: ${e.message}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding word pack to collection: ${e.message}")
            throw e
        }
    }
    
    /**
     * Create placeholder words for a word pack if no words are found in Firestore
     * Note: This is no longer used as we don't add placeholder words anymore
     */
    private fun createPlaceholderWords(wordPack: WordPack, categoryId: String): List<VocabularyWord> {
        val words = mutableListOf<VocabularyWord>()
        
        // Create placeholder words based on the theme
        when (wordPack.theme.lowercase()) {
            "food" -> {
                words.add(createVocabularyWord("apple", "A round fruit with red, yellow, or green skin and a white flesh", "noun", "ˈæpəl", "I eat an apple every day.", wordPack.difficulty, categoryId))
                words.add(createVocabularyWord("bread", "A food made of flour, water, and yeast mixed together and baked", "noun", "brɛd", "I like to eat fresh bread for breakfast.", wordPack.difficulty, categoryId))
                words.add(createVocabularyWord("cheese", "A food made from milk, often white or yellow in color", "noun", "tʃiːz", "This sandwich has cheese and tomato in it.", wordPack.difficulty, categoryId))
                words.add(createVocabularyWord("cook", "To prepare food by heating it", "verb", "kʊk", "I'm going to cook dinner tonight.", wordPack.difficulty, categoryId))
                words.add(createVocabularyWord("recipe", "A set of instructions for preparing a dish, including ingredients and method", "noun", "ˈrɛsəpi", "This recipe requires three eggs.", wordPack.difficulty, categoryId))
            }
            else -> {
                // Generic placeholder words
                words.add(createVocabularyWord("example", "A thing characteristic of its kind or illustrating a general rule", "noun", "ɪɡˈzɑːmpəl", "This is an example of a placeholder word.", wordPack.difficulty, categoryId))
                words.add(createVocabularyWord("placeholder", "A person or thing that occupies the position or place of another person or thing", "noun", "ˈpleɪsˌhoʊldər", "These are placeholder words until real content is added.", wordPack.difficulty, categoryId))
                words.add(createVocabularyWord("vocabulary", "The body of words used in a particular language", "noun", "vəˈkæbjəˌlɛri", "Learning vocabulary is important for language acquisition.", wordPack.difficulty, categoryId))
            }
        }
        
        return words
    }
    
    /**
     * Helper method to create a VocabularyWord
     */
    private fun createVocabularyWord(
        word: String,
        definition: String,
        partOfSpeech: String,
        pronunciation: String,
        example: String,
        difficulty: Int,
        categoryId: String
    ): VocabularyWord {
        return VocabularyWord(
            id = UUID.randomUUID().toString(),
            word = word,
            definition = definition,
            partOfSpeech = partOfSpeech,
            pronunciation = pronunciation,
            example = example,
            difficulty = difficulty,
            categoryId = categoryId,
            createdAt = System.currentTimeMillis()
        )
    }

    override suspend fun removeWordPackFromCollection(wordPackId: String) {
        try {
            // Get the word pack to find the category name
            val wordPack = wordPackDao.getWordPackById(wordPackId)
            
            // Find the category with the same name as the word pack
            if (wordPack != null) {
                // Get all categories
                val categories = wordCategoryDao.getAllCategories().first()
                
                // Find the category that matches the word pack name
                val category = categories.find { it.name == wordPack.name }
                
                // If found, delete the category and its words
                if (category != null) {
                    // Delete all words in this category
                    vocabularyWordDao.deleteWordsByCategory(category.id)
                    
                    // Delete the category
                    wordCategoryDao.deleteCategoryById(category.id)
                    
                    Timber.d("Removed category ${category.name} and its words")
                }
            }
            
            // Remove from local database
            wordPackDao.deleteWordPackById(wordPackId)
            
            // Also remove from user's Firestore collection
            val userId = auth.currentUser?.uid ?: return
            firestore.collection("users")
                .document(userId)
                .collection(userWordPacksCollection)
                .document(wordPackId)
                .delete()
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error removing word pack from collection")
            throw e
        }
    }
    
    override suspend fun syncDownloadedWordPacks() {
        try {
            val userId = auth.currentUser?.uid ?: return
            
            // Get user's word packs from Firestore
            val userWordPacks = firestore.collection("users")
                .document(userId)
                .collection(userWordPacksCollection)
                .get()
                .await()
                .documents
                .mapNotNull { it.id }
            
            // For each word pack ID, get the full word pack data
            for (wordPackId in userWordPacks) {
                val wordPack = getWordPackById(wordPackId)
                if (wordPack != null) {
                    // Mark as downloaded and save to local database
                    val downloadedPack = wordPack.copy(isDownloaded = true)
                    wordPackDao.insertWordPack(downloadedPack)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing downloaded word packs")
            // Don't throw, just log the error
        }
    }
}
