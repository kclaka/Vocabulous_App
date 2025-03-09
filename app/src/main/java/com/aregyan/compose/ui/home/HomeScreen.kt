package com.aregyan.compose.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.bold.House
import com.adamglin.phosphoricons.regular.ArrowRight
import com.adamglin.phosphoricons.regular.Bookmark
import com.adamglin.phosphoricons.regular.Cards
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.Folder
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.Plus
import com.aregyan.compose.data.model.UserProgress
import com.aregyan.compose.data.model.WordCategory
import com.aregyan.compose.ui.auth.AuthViewModel
import com.aregyan.compose.ui.vocabulary.VocabularyViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToFlashcards: () -> Unit,
    navigateToAddWord: () -> Unit,
    navigateToProfile: () -> Unit,
    navigateToReview: () -> Unit,
    navigateToSearch: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    vocabularyViewModel: VocabularyViewModel = hiltViewModel()
) {
    // Get the current user from AuthViewModel
    val userName = remember { mutableStateOf("") }
    LaunchedEffect(authViewModel.currentUser) {
        userName.value = authViewModel.currentUser?.displayName ?: ""
    }
    
    // Get data from VocabularyViewModel
    val categories by vocabularyViewModel.categories.collectAsState()
    val wordsForReview by vocabularyViewModel.wordsForReview.collectAsState()
    val bookmarkedWords by vocabularyViewModel.bookmarkedWords.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vocabulous") },
                actions = {
                    IconButton(onClick = navigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = navigateToSearch) {
                        Icon(PhosphorIcons.Regular.MagnifyingGlass, contentDescription = "Search")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                WelcomeSection(userName = userName.value)
            }
            
            item {
                LearningActionsSection(
                    navigateToFlashcards = navigateToFlashcards,
                    navigateToAddWord = navigateToAddWord,
                    navigateToReview = navigateToReview
                )
            }
            
            if (wordsForReview.isNotEmpty()) {
                item {
                    ReviewWordsSection(wordsForReview = wordsForReview, navigateToReview = navigateToReview)
                }
            }
            
            if (categories.isNotEmpty()) {
                item {
                    CategoriesSection(categories = categories, navigateToFlashcards = navigateToFlashcards)
                }
            }
            
            if (bookmarkedWords.isNotEmpty()) {
                item {
                    BookmarkedWordsSection(bookmarkedWords = bookmarkedWords, navigateToFlashcards = navigateToFlashcards)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun WelcomeSection(userName: String) {
    Column {
        Text(
            text = "Welcome back,",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Text(
            text = userName.ifEmpty { "Learner" },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Continue your vocabulary journey today!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun LearningActionsSection(
    navigateToFlashcards: () -> Unit,
    navigateToAddWord: () -> Unit,
    navigateToReview: () -> Unit
) {
    Column {
        Text(
            text = "Learning Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionCard(
                icon = PhosphorIcons.Regular.Cards,
                title = "Flashcards",
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = navigateToFlashcards,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            ActionCard(
                icon = PhosphorIcons.Regular.Plus,
                title = "Add Word",
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = navigateToAddWord,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ActionCard(
            icon = PhosphorIcons.Regular.ClockCounterClockwise,
            title = "Review Due Words",
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            onClick = navigateToReview
        )
    }
}

@Composable
fun ActionCard(
    icon: ImageVector,
    title: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ReviewWordsSection(wordsForReview: List<UserProgress>, navigateToReview: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Words Due for Review",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(onClick = navigateToReview) {
                Text("View All")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = navigateToReview),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${wordsForReview.size} words to review",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = "Keep your memory fresh!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    Icon(
                        imageVector = PhosphorIcons.Regular.ArrowRight,
                        contentDescription = "Start Review",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LinearProgressIndicator(
                    progress = { 0.75f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CategoriesSection(categories: List<WordCategory>, navigateToFlashcards: () -> Unit) {
    Column {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                CategoryCard(
                    category = category,
                    onClick = navigateToFlashcards
                )
            }
        }
    }
}

@Composable
fun CategoryCard(category: WordCategory, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${category.difficulty} difficulty",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun BookmarkedWordsSection(bookmarkedWords: List<UserProgress>, navigateToFlashcards: () -> Unit) {
    Column {
        Text(
            text = "Bookmarked Words",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = navigateToFlashcards),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${bookmarkedWords.size} bookmarked words",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = "Review your favorite words",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    Icon(
                        imageVector = PhosphorIcons.Regular.Bookmark,
                        contentDescription = "View Bookmarks",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
