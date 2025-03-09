package com.aregyan.compose.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular

@Composable
fun WordPackFilterDialog(
    selectedLanguage: String?,
    selectedTheme: String?,
    selectedDifficulty: Int?,
    onLanguageSelected: (String?) -> Unit,
    onThemeSelected: (String?) -> Unit,
    onDifficultySelected: (Int?) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    var currentTab by remember { mutableStateOf(FilterTab.LANGUAGE) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large)
                .padding(16.dp)
        ) {
            Text(
                text = "Filter Word Packs",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Filter tabs
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterTab.values().forEach { tab ->
                    TextButton(
                        onClick = { currentTab = tab },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = tab.title,
                            color = if (currentTab == tab) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Divider()
            
            // Filter content
            when (currentTab) {
                FilterTab.LANGUAGE -> LanguageFilterContent(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = onLanguageSelected
                )
                FilterTab.THEME -> ThemeFilterContent(
                    selectedTheme = selectedTheme,
                    onThemeSelected = onThemeSelected
                )
                FilterTab.DIFFICULTY -> DifficultyFilterContent(
                    selectedDifficulty = selectedDifficulty,
                    onDifficultySelected = onDifficultySelected
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        onClearFilters()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
fun LanguageFilterContent(
    selectedLanguage: String?,
    onLanguageSelected: (String?) -> Unit
) {
    val languages = remember {
        listOf(
            "English",
            "Spanish",
            "French",
            "German",
            "Italian",
            "Portuguese",
            "Russian",
            "Chinese",
            "Japanese",
            "Korean"
        )
    }
    
    FilterRadioGroup(
        title = "Select Language",
        options = languages,
        selectedOption = selectedLanguage,
        onOptionSelected = onLanguageSelected
    )
}

@Composable
fun ThemeFilterContent(
    selectedTheme: String?,
    onThemeSelected: (String?) -> Unit
) {
    val themes = remember {
        listOf(
            "Travel Essentials",
            "Business Vocabulary",
            "Food & Dining",
            "Academic Terms",
            "Medical Terminology",
            "Technology",
            "Arts & Culture",
            "Sports & Recreation",
            "Nature & Environment",
            "Everyday Conversation"
        )
    }
    
    FilterRadioGroup(
        title = "Select Theme",
        options = themes,
        selectedOption = selectedTheme,
        onOptionSelected = onThemeSelected
    )
}

@Composable
fun DifficultyFilterContent(
    selectedDifficulty: Int?,
    onDifficultySelected: (Int?) -> Unit
) {
    val difficulties = remember {
        listOf(
            "Beginner (1)" to 1,
            "Elementary (2)" to 2,
            "Intermediate (3)" to 3,
            "Advanced (4)" to 4,
            "Expert (5)" to 5
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .selectableGroup()
    ) {
        Text(
            text = "Select Difficulty Level",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        difficulties.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = value == selectedDifficulty,
                        onClick = { onDifficultySelected(value) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = value == selectedDifficulty,
                    onClick = null
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
        
        // Option to clear selection
        if (selectedDifficulty != null) {
            TextButton(
                onClick = { onDifficultySelected(null) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Clear Selection")
            }
        }
    }
}

@Composable
fun <T> FilterRadioGroup(
    title: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        LazyColumn(
            modifier = Modifier.height(300.dp)
        ) {
            items(options) { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = option == selectedOption,
                            onClick = { onOptionSelected(option) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = option == selectedOption,
                        onClick = null
                    )
                    Text(
                        text = option.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
        
        // Option to clear selection
        if (selectedOption != null) {
            TextButton(
                onClick = { onOptionSelected(null) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Clear Selection")
            }
        }
    }
}

enum class FilterTab(val title: String) {
    LANGUAGE("Language"),
    THEME("Theme"),
    DIFFICULTY("Difficulty")
}
