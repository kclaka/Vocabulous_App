package com.aregyan.compose.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aregyan.compose.data.model.WordPack
import kotlinx.coroutines.launch
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.FunnelSimple
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.X

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToWordPackDetail: (String) -> Unit,
    viewModel: WordPackViewModel = hiltViewModel()
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val downloadedWordPacks by viewModel.downloadedWordPacks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    
    var showSearchBar by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Show error message if any
    LaunchedEffect(error) {
        error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Word Packs") },
                actions = {
                    IconButton(onClick = { showSearchBar = true }) {
                        Icon(PhosphorIcons.Regular.MagnifyingGlass, contentDescription = "Search")
                    }
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(PhosphorIcons.Regular.FunnelSimple, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar (when active)
            if (showSearchBar) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::setSearchQuery,
                    onSearch = { showSearchBar = false },
                    active = true,
                    onActiveChange = { showSearchBar = it },
                    placeholder = { Text("Search word packs") },
                    leadingIcon = { Icon(PhosphorIcons.Regular.MagnifyingGlass, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(PhosphorIcons.Regular.X, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {}
            }
            
            // Active filters
            if (selectedLanguage != null || selectedTheme != null || selectedDifficulty != null) {
                ActiveFilters(
                    language = selectedLanguage,
                    theme = selectedTheme,
                    difficulty = selectedDifficulty,
                    onClearLanguage = { viewModel.setLanguageFilter(null) },
                    onClearTheme = { viewModel.setThemeFilter(null) },
                    onClearDifficulty = { viewModel.setDifficultyFilter(null) },
                    onClearAll = { viewModel.clearFilters() }
                )
            }
            
            // Word packs list
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (searchResults.isEmpty()) {
                EmptySearchResults(searchQuery, hasFilters = selectedLanguage != null || selectedTheme != null || selectedDifficulty != null)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(searchResults, key = { it.id }) { wordPack ->
                        val isDownloaded = downloadedWordPacks.any { it.id == wordPack.id }
                        WordPackItem(
                            wordPack = wordPack,
                            isDownloaded = isDownloaded,
                            onAddToCollection = { 
                                // First add the word pack to collection, then navigate to flashcards
                                viewModel.addWordPackToCollection(wordPack.id) {
                                    // Only navigate after successfully adding the word pack
                                    onNavigateToWordPackDetail(wordPack.id)
                                }
                            },
                            onRemoveFromCollection = { viewModel.removeWordPackFromCollection(wordPack.id) },
                            onClick = { wordPackId -> 
                                // If already downloaded, navigate directly
                                if (isDownloaded) {
                                    onNavigateToWordPackDetail(wordPackId)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
    
    // Show filter dialog if needed
    if (showFilterDialog) {
        WordPackFilterDialog(
            selectedLanguage = selectedLanguage,
            selectedTheme = selectedTheme,
            selectedDifficulty = selectedDifficulty,
            onLanguageSelected = { viewModel.setLanguageFilter(it) },
            onThemeSelected = { viewModel.setThemeFilter(it) },
            onDifficultySelected = { viewModel.setDifficultyFilter(it) },
            onClearFilters = { viewModel.clearFilters() },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveFilters(
    language: String?,
    theme: String?,
    difficulty: Int?,
    onClearLanguage: () -> Unit,
    onClearTheme: () -> Unit,
    onClearDifficulty: () -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Active Filters:",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Using a simple Row + Column layout instead of FlowRow
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                language?.let {
                    FilterChip(
                        selected = true,
                        onClick = onClearLanguage,
                        label = { Text("Language: $it") },
                        trailingIcon = { Icon(PhosphorIcons.Regular.X, contentDescription = "Clear") },
                        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                    )
                }
                
                difficulty?.let {
                    FilterChip(
                        selected = true,
                        onClick = onClearDifficulty,
                        label = { Text("Difficulty: $it") },
                        trailingIcon = { Icon(PhosphorIcons.Regular.X, contentDescription = "Clear") },
                        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                theme?.let {
                    FilterChip(
                        selected = true,
                        onClick = onClearTheme,
                        label = { Text("Theme: $it") },
                        trailingIcon = { Icon(PhosphorIcons.Regular.X, contentDescription = "Clear") },
                        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                    )
                }
                
                FilterChip(
                    selected = true,
                    onClick = onClearAll,
                    label = { Text("Clear All") },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
fun EmptySearchResults(
    query: String,
    hasFilters: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = PhosphorIcons.Regular.MagnifyingGlass,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = when {
                query.isNotBlank() -> "No word packs found for '$query'"
                hasFilters -> "No word packs match your filters"
                else -> "No word packs available yet"
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = when {
                query.isNotBlank() -> "Try a different search term or clear your filters"
                hasFilters -> "Try adjusting or clearing your filters"
                else -> "Word packs will appear here once they're available"
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (query.isNotBlank() || hasFilters) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    // This will be handled by the parent component
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text("Clear All Filters")
            }
        }
    }
}
