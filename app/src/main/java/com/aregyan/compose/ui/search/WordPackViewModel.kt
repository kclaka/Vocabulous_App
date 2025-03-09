package com.aregyan.compose.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aregyan.compose.data.model.WordPack
import com.aregyan.compose.repository.WordPackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WordPackViewModel @Inject constructor(
    private val wordPackRepository: WordPackRepository
) : ViewModel() {
    
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    // Selected filters
    private val _selectedLanguage = MutableStateFlow<String?>(null)
    val selectedLanguage = _selectedLanguage.asStateFlow()
    
    private val _selectedTheme = MutableStateFlow<String?>(null)
    val selectedTheme = _selectedTheme.asStateFlow()
    
    private val _selectedDifficulty = MutableStateFlow<Int?>(null)
    val selectedDifficulty = _selectedDifficulty.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    // State for all word packs
    private val allWordPacks = wordPackRepository.getAllWordPacks()
        .catch { e -> 
            Timber.e(e, "Error fetching all word packs")
            _error.value = "Failed to load word packs: ${e.message}"
            _isLoading.value = false
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Downloaded word packs
    val downloadedWordPacks = wordPackRepository.getDownloadedWordPacks()
        .catch { e -> 
            Timber.e(e, "Error fetching downloaded word packs")
            _error.value = "Failed to load your word packs: ${e.message}"
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Filtered search results
    val searchResults: StateFlow<List<WordPack>> = combine(
        allWordPacks,
        _searchQuery,
        _selectedLanguage,
        _selectedTheme,
        _selectedDifficulty
    ) { packs, query, language, theme, difficulty ->
        _isLoading.value = false
        
        packs.filter { pack ->
            // Apply search query filter
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val lowerQuery = query.lowercase()
                pack.name.lowercase().contains(lowerQuery) ||
                pack.description.lowercase().contains(lowerQuery) ||
                pack.theme.lowercase().contains(lowerQuery) ||
                pack.language.lowercase().contains(lowerQuery) ||
                pack.tags.any { it.lowercase().contains(lowerQuery) }
            }
            
            // Apply language filter
            val matchesLanguage = language == null || pack.language == language
            
            // Apply theme filter
            val matchesTheme = theme == null || pack.theme == theme
            
            // Apply difficulty filter
            val matchesDifficulty = difficulty == null || pack.difficulty == difficulty
            
            matchesQuery && matchesLanguage && matchesTheme && matchesDifficulty
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        // Set initial loading state
        _isLoading.value = true
        
        // Sync downloaded word packs when ViewModel is created
        viewModelScope.launch {
            try {
                wordPackRepository.syncDownloadedWordPacks()
            } catch (e: Exception) {
                Timber.e(e, "Error syncing downloaded word packs")
            }
        }
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setLanguageFilter(language: String?) {
        _selectedLanguage.value = language
    }
    
    fun setThemeFilter(theme: String?) {
        _selectedTheme.value = theme
    }
    
    fun setDifficultyFilter(difficulty: Int?) {
        _selectedDifficulty.value = difficulty
    }
    
    fun clearFilters() {
        _selectedLanguage.value = null
        _selectedTheme.value = null
        _selectedDifficulty.value = null
    }
    
    fun addWordPackToCollection(wordPackId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                wordPackRepository.addWordPackToCollection(wordPackId)
                // Call onSuccess callback after successfully adding the word pack
                onSuccess()
            } catch (e: Exception) {
                Timber.e(e, "Error adding word pack to collection")
                _error.value = "Failed to add word pack: ${e.message}"
            }
        }
    }
    
    fun navigateToFlashcards(wordPackId: String) {
        // Implement navigation logic here
    }
    
    fun removeWordPackFromCollection(wordPackId: String) {
        viewModelScope.launch {
            try {
                wordPackRepository.removeWordPackFromCollection(wordPackId)
            } catch (e: Exception) {
                Timber.e(e, "Error removing word pack from collection")
                _error.value = "Failed to remove word pack: ${e.message}"
            }
        }
    }
}

data class SearchFilters(
    val query: String = "",
    val language: String? = null,
    val theme: String? = null,
    val difficulty: Int? = null
)
