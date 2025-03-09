package com.aregyan.compose.ui.vocabulary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.BookOpen
import com.adamglin.phosphoricons.regular.Bookmark
import com.adamglin.phosphoricons.regular.House
import com.adamglin.phosphoricons.regular.SpeakerHigh
import com.aregyan.compose.data.model.VocabularyWord
import com.aregyan.compose.data.model.WordCategory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    navigateToAddWord: () -> Unit,
    navigateToHome: () -> Unit = {},
    viewModel: VocabularyViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val words by viewModel.wordsForSelectedCategory.collectAsState()
    val currentIndex by viewModel.currentFlashcardIndex.collectAsState()
    val isFlipped by viewModel.isFlashcardFlipped.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flashcards") },
                navigationIcon = {
                    IconButton(onClick = navigateToHome) {
                        Icon(PhosphorIcons.Regular.House, contentDescription = "Go to Home")
                    }
                },
                actions = {
                    IconButton(onClick = navigateToAddWord) {
                        Icon(Icons.Default.Add, contentDescription = "Add new word")
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
            // Categories horizontal scrollable list
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    CategoryChip(
                        category = category,
                        isSelected = category.id == selectedCategoryId,
                        onClick = { viewModel.selectCategory(category.id) }
                    )
                }
            }
            
            if (words.isEmpty()) {
                EmptyWordsView(navigateToAddWord)
            } else {
                // Flashcard
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val currentWord = words.getOrNull(currentIndex)
                    if (currentWord != null) {
                        FlashcardView(
                            word = currentWord,
                            isFlipped = isFlipped,
                            onFlip = { viewModel.flipFlashcard() },
                            onBookmark = { viewModel.toggleBookmark(currentWord.id) },
                            viewModel = viewModel
                        )
                    }
                }
                
                // Navigation controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Previous button
                    IconButton(
                        onClick = { viewModel.previousFlashcard(words) },
                        enabled = currentIndex > 0
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous word",
                            tint = if (currentIndex > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                    
                    // Next button
                    IconButton(
                        onClick = { viewModel.nextFlashcard(words) },
                        enabled = currentIndex < words.size - 1
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next word",
                            tint = if (currentIndex < words.size - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
                
                // Proficiency rating
                if (isFlipped) {
                    ProficiencyRatingBar(
                        onRatingSelected = { rating ->
                            coroutineScope.launch {
                                words.getOrNull(currentIndex)?.let { word ->
                                    viewModel.updateWordProficiency(word.id, rating)
                                    viewModel.nextFlashcard(words)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: WordCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = category.name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun FlashcardView(
    word: VocabularyWord,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onBookmark: () -> Unit,
    viewModel: VocabularyViewModel = hiltViewModel()
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(500),
        label = "card_rotation"
    )
    
    val context = LocalContext.current
    val isAudioPlaying by viewModel.isAudioPlaying.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(1.5f)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(onClick = onFlip),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = !isFlipped,
            enter = fadeIn(animationSpec = tween(250, 250)),
            exit = fadeOut(animationSpec = tween(250))
        ) {
            // Front of card (word)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = word.word,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = word.partOfSpeech,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                if (word.pronunciation.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "[${word.pronunciation}]",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Audio pronunciation button
                        IconButton(
                            onClick = { viewModel.playWordPronunciation(word.word, context) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.SpeakerHigh,
                                contentDescription = "Pronounce word",
                                tint = if (isAudioPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Even if there's no pronunciation text, still offer audio
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(
                        onClick = { viewModel.playWordPronunciation(word.word, context) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.SpeakerHigh,
                            contentDescription = "Pronounce word",
                            tint = if (isAudioPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Tap to see definition",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        AnimatedVisibility(
            visible = isFlipped,
            enter = fadeIn(animationSpec = tween(250, 250)),
            exit = fadeOut(animationSpec = tween(250))
        ) {
            // Back of card (definition)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .graphicsLayer { rotationY = 180f },
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = word.definition,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (word.example.isNotEmpty()) {
                        Text(
                            text = "Example:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "\"${word.example}\"",
                            fontSize = 16.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Bookmark button
                IconButton(
                    onClick = onBookmark,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Bookmark,
                        contentDescription = "Bookmark",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ProficiencyRatingBar(onRatingSelected: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How well did you know this word?",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProficiencyButton(level = 1, text = "Not at all", onRatingSelected)
            ProficiencyButton(level = 2, text = "Barely", onRatingSelected)
            ProficiencyButton(level = 3, text = "Somewhat", onRatingSelected)
            ProficiencyButton(level = 4, text = "Well", onRatingSelected)
            ProficiencyButton(level = 5, text = "Perfect", onRatingSelected)
        }
    }
}

@Composable
fun ProficiencyButton(level: Int, text: String, onRatingSelected: (Int) -> Unit) {
    val colors = listOf(
        Color(0xFFE57373), // Red for level 1
        Color(0xFFFFB74D), // Orange for level 2
        Color(0xFFFFD54F), // Yellow for level 3
        Color(0xFFAED581), // Light green for level 4
        Color(0xFF81C784)  // Green for level 5
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { onRatingSelected(level) },
            colors = ButtonDefaults.buttonColors(containerColor = colors[level - 1]),
            modifier = Modifier.size(48.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(text = level.toString())
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyWordsView(onAddWord: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = PhosphorIcons.Regular.BookOpen,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No vocabulary words found",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Add your first word to start learning",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddWord
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add New Word")
        }
    }
}
