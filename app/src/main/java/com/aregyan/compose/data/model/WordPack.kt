package com.aregyan.compose.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.aregyan.compose.data.db.Converters
import com.google.firebase.Timestamp

/**
 * WordPack entity for Room database
 */
@Entity(tableName = "word_packs")
data class WordPack(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val language: String = "",
    val theme: String = "",
    val difficulty: Int = 1,
    val wordCount: Int = 0,
    val imageUrl: String? = null,
    @TypeConverters(Converters::class)
    val tags: List<String> = emptyList(),
    val isDownloaded: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Convert a FirestoreWordPack to a WordPack for Room
         */
        fun fromFirestore(firestorePack: FirestoreWordPack): WordPack {
            return WordPack(
                id = firestorePack.id,
                name = firestorePack.name,
                description = firestorePack.description,
                language = firestorePack.language,
                theme = firestorePack.theme,
                difficulty = firestorePack.difficulty,
                wordCount = firestorePack.wordCount,
                imageUrl = firestorePack.imageUrl,
                tags = firestorePack.tags,
                isDownloaded = firestorePack.isDownloaded,
                createdAtMillis = firestorePack.createdAt?.seconds?.times(1000)?.plus(firestorePack.createdAt.nanoseconds / 1000000) ?: System.currentTimeMillis(),
                updatedAtMillis = firestorePack.updatedAt?.seconds?.times(1000)?.plus(firestorePack.updatedAt.nanoseconds / 1000000) ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * WordPack model for Firestore
 */
data class FirestoreWordPack(
    var id: String = "",
    val name: String = "",
    val description: String = "",
    val language: String = "",
    val theme: String = "",
    val difficulty: Int = 1,
    val wordCount: Int = 0,
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val isDownloaded: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    // No-arg constructor for Firestore
    constructor() : this(id = "")
}
