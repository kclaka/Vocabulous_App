package com.aregyan.compose.ui.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aregyan.compose.data.model.UserProgress
import com.aregyan.compose.data.model.VocabularyWord
import com.aregyan.compose.data.model.WordCategory
import com.aregyan.compose.repository.UserProgressRepository
import com.aregyan.compose.repository.VocabularyRepository
import com.aregyan.compose.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import android.media.MediaPlayer
import android.net.Uri
import android.content.Context
import java.net.URLEncoder

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class VocabularyViewModel @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val userProgressRepository: UserProgressRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Current user ID
    private val userId: String
        get() = authRepository.currentUser?.uid ?: ""

    // Categories
    private val _categories = MutableStateFlow<List<WordCategory>>(emptyList())
    val categories: StateFlow<List<WordCategory>> = _categories

    // Words for the selected category
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId

    // Words for the current category
    val wordsForSelectedCategory: StateFlow<List<VocabularyWord>> = _selectedCategoryId
        .flatMapLatest { categoryId ->
            categoryId?.let {
                vocabularyRepository.getWordsByCategory(it)
            } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current flashcard
    private val _currentFlashcardIndex = MutableStateFlow(0)
    val currentFlashcardIndex: StateFlow<Int> = _currentFlashcardIndex

    // Flashcard state (front/back)
    private val _isFlashcardFlipped = MutableStateFlow(false)
    val isFlashcardFlipped: StateFlow<Boolean> = _isFlashcardFlipped

    // Audio playback state
    private val _isAudioPlaying = MutableStateFlow(false)
    val isAudioPlaying: StateFlow<Boolean> = _isAudioPlaying

    // Words due for review
    val wordsForReview: StateFlow<List<UserProgress>> = flow {
        if (userId.isNotEmpty()) {
            userProgressRepository.getWordsForReview(userId).collect {
                emit(it)
            }
        } else {
            emit(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bookmarked words
    val bookmarkedWords: StateFlow<List<UserProgress>> = flow {
        if (userId.isNotEmpty()) {
            userProgressRepository.getBookmarkedWords(userId).collect {
                emit(it)
            }
        } else {
            emit(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var mediaPlayer: MediaPlayer? = null

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            vocabularyRepository.getAllCategories().collect {
                _categories.value = it
                if (it.isNotEmpty() && _selectedCategoryId.value == null) {
                    _selectedCategoryId.value = it.first().id
                }
            }
        }
    }

    fun selectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
        _currentFlashcardIndex.value = 0
        _isFlashcardFlipped.value = false
    }

    fun flipFlashcard() {
        _isFlashcardFlipped.value = !_isFlashcardFlipped.value
    }

    fun nextFlashcard(words: List<VocabularyWord>) {
        if (words.isNotEmpty()) {
            _currentFlashcardIndex.value = (_currentFlashcardIndex.value + 1) % words.size
            _isFlashcardFlipped.value = false
        }
    }

    fun previousFlashcard(words: List<VocabularyWord>) {
        if (words.isNotEmpty()) {
            _currentFlashcardIndex.value = if (_currentFlashcardIndex.value > 0) {
                _currentFlashcardIndex.value - 1
            } else {
                words.size - 1
            }
            _isFlashcardFlipped.value = false
        }
    }

    // Spaced repetition logic
    fun updateWordProficiency(wordId: String, newLevel: Int) {
        if (userId.isEmpty()) return
        
        viewModelScope.launch {
            // Get current progress or create new one
            val progress = userProgressRepository.getUserProgressForWord(userId, wordId) ?: 
                UserProgress(
                    userId = userId,
                    wordId = wordId,
                    proficiencyLevel = 0,
                    reviewCount = 0
                )
            
            // Calculate next review date based on spaced repetition algorithm
            val nextReviewDue = calculateNextReviewDate(newLevel, progress.reviewCount + 1)
            
            val updatedProgress = progress.copy(
                proficiencyLevel = newLevel,
                lastReviewedAt = System.currentTimeMillis(),
                nextReviewDue = nextReviewDue,
                reviewCount = progress.reviewCount + 1,
                updatedAt = System.currentTimeMillis()
            )
            
            userProgressRepository.updateUserProgress(updatedProgress)
        }
    }
    
    fun toggleBookmark(wordId: String) {
        if (userId.isEmpty()) return
        
        viewModelScope.launch {
            val progress = userProgressRepository.getUserProgressForWord(userId, wordId)
            if (progress != null) {
                userProgressRepository.updateBookmarkStatus(userId, wordId, !progress.isBookmarked)
            } else {
                val newProgress = UserProgress(
                    userId = userId,
                    wordId = wordId,
                    proficiencyLevel = 0,
                    isBookmarked = true
                )
                userProgressRepository.insertUserProgress(newProgress)
            }
        }
    }
    
    fun addNewWord(word: VocabularyWord) {
        viewModelScope.launch {
            vocabularyRepository.insertWord(word)
        }
    }
    
    fun addNewCategory(category: WordCategory) {
        viewModelScope.launch {
            vocabularyRepository.insertCategory(category)
        }
    }
    
    // Spaced repetition algorithm
    private fun calculateNextReviewDate(proficiencyLevel: Int, reviewCount: Int): Long {
        // Simple spaced repetition algorithm based on proficiency level and review count
        val now = System.currentTimeMillis()
        val daysToAdd = when (proficiencyLevel) {
            0 -> 1 // New or difficult word - review tomorrow
            1 -> 2 // Review in 2 days
            2 -> 4 // Review in 4 days
            3 -> 7 // Review in a week
            4 -> 14 // Review in two weeks
            5 -> 30 // Review in a month
            else -> 1
        }
        
        // Adjust for review count - the more times reviewed, the longer the interval
        val reviewMultiplier = if (reviewCount > 1) {
            1.0 + (reviewCount - 1) * 0.5 // Each additional review extends time by 50%
        } else {
            1.0
        }
        
        val millisToAdd = (daysToAdd * reviewMultiplier * 24 * 60 * 60 * 1000).toLong()
        return now + millisToAdd
    }
    
    /**
     * Plays the pronunciation of the given word using the text-to-speech API
     * @param word The word to pronounce
     * @param context The context needed to create the MediaPlayer
     */
    fun playWordPronunciation(word: String, context: Context) {
        viewModelScope.launch {
            try {
                // Clean up any existing MediaPlayer
                mediaPlayer?.release()
                
                // Create a new MediaPlayer
                mediaPlayer = MediaPlayer().apply {
                    // Encode the word for URL safety
                    val encodedWord = URLEncoder.encode(word, "UTF-8")
                    val url = "https://voca-speech.azurewebsites.net/pronounce/$encodedWord"
                    
                    _isAudioPlaying.value = true
                    setDataSource(context, Uri.parse(url))
                    setOnPreparedListener { it.start() }
                    setOnCompletionListener {
                        _isAudioPlaying.value = false
                        it.release()
                        mediaPlayer = null
                    }
                    setOnErrorListener { _, _, _ ->
                        _isAudioPlaying.value = false
                        mediaPlayer?.release()
                        mediaPlayer = null
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                _isAudioPlaying.value = false
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
