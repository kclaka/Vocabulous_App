package com.aregyan.compose.ui.vocabulary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import com.adamglin.phosphoricons.Regular
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.regular.File
import com.aregyan.compose.data.model.VocabularyWord
import com.aregyan.compose.data.model.WordCategory
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(
    navigateBack: () -> Unit,
    viewModel: VocabularyViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    
    var word by remember { mutableStateOf("") }
    var definition by remember { mutableStateOf("") }
    var partOfSpeech by remember { mutableStateOf("") }
    var pronunciation by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf(1) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    
    // Initialize selected category if available
    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && selectedCategoryId == null) {
            selectedCategoryId = categories.firstOrNull()?.id
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Word") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (word.isNotBlank() && definition.isNotBlank()) {
                                val newWord = VocabularyWord(
                                    id = UUID.randomUUID().toString(),
                                    word = word.trim(),
                                    definition = definition.trim(),
                                    partOfSpeech = partOfSpeech.trim(),
                                    pronunciation = pronunciation.trim(),
                                    example = example.trim(),
                                    difficulty = difficulty,
                                    categoryId = selectedCategoryId
                                )
                                viewModel.addNewWord(newWord)
                                navigateBack()
                            }
                        },
                        enabled = word.isNotBlank() && definition.isNotBlank()
                    ) {
                        Icon(PhosphorIcons.Regular.File, contentDescription = "Save word")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Word
            OutlinedTextField(
                value = word,
                onValueChange = { word = it },
                label = { Text("Word*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            
            // Definition
            OutlinedTextField(
                value = definition,
                onValueChange = { definition = it },
                label = { Text("Definition*") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            
            // Part of Speech
            OutlinedTextField(
                value = partOfSpeech,
                onValueChange = { partOfSpeech = it },
                label = { Text("Part of Speech (e.g., noun, verb)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            
            // Pronunciation
            OutlinedTextField(
                value = pronunciation,
                onValueChange = { pronunciation = it },
                label = { Text("Pronunciation") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            
            // Example
            OutlinedTextField(
                value = example,
                onValueChange = { example = it },
                label = { Text("Example Sentence") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
            
            // Difficulty
            Text(
                text = "Difficulty Level: $difficulty",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Slider(
                value = difficulty.toFloat(),
                onValueChange = { difficulty = it.toInt() },
                valueRange = 1f..5f,
                steps = 3,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Category selection
            Text(
                text = "Category",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (categories.isEmpty()) {
                    Text(
                        text = "No categories available. Create one first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = false,
                            onDismissRequest = {}
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = { selectedCategoryId = category.id }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = { showAddCategoryDialog = true }
                ) {
                    Text("Add Category")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (word.isNotBlank() && definition.isNotBlank()) {
                        val newWord = VocabularyWord(
                            id = UUID.randomUUID().toString(),
                            word = word.trim(),
                            definition = definition.trim(),
                            partOfSpeech = partOfSpeech.trim(),
                            pronunciation = pronunciation.trim(),
                            example = example.trim(),
                            difficulty = difficulty,
                            categoryId = selectedCategoryId
                        )
                        viewModel.addNewWord(newWord)
                        navigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = word.isNotBlank() && definition.isNotBlank()
            ) {
                Icon(PhosphorIcons.Regular.File, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Word")
            }
        }
    }
    
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onCategoryAdded = { newCategory ->
                viewModel.addNewCategory(newCategory)
                selectedCategoryId = newCategory.id
                showAddCategoryDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onCategoryAdded: (WordCategory) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var categoryDescription by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf(1) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Category") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = categoryDescription,
                    onValueChange = { categoryDescription = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Difficulty Level: $difficulty",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Slider(
                    value = difficulty.toFloat(),
                    onValueChange = { difficulty = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        val newCategory = WordCategory(
                            id = UUID.randomUUID().toString(),
                            name = categoryName.trim(),
                            description = categoryDescription.trim(),
                            difficulty = difficulty,
                            order = 0 // Will be updated later if needed
                        )
                        onCategoryAdded(newCategory)
                    }
                },
                enabled = categoryName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
