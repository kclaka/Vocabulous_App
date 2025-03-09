package com.aregyan.compose.ui.grammar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import com.aregyan.compose.data.model.ExerciseType
import com.aregyan.compose.data.model.GrammarExercise
import com.adamglin.phosphoricons.regular.CaretLeft
import com.adamglin.phosphoricons.regular.ArrowLeft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarExerciseScreen(
    lessonId: String,
    navigateBack: () -> Unit,
    grammarViewModel: GrammarViewModel = hiltViewModel()
) {
    LaunchedEffect(lessonId) {
        grammarViewModel.selectLesson(lessonId)
    }
    
    val exercises by grammarViewModel.exercisesForSelectedLesson.collectAsState()
    val currentExerciseIndex by grammarViewModel.currentExerciseIndex.collectAsState()
    val userAnswer by grammarViewModel.selectedAnswer.collectAsState()
    val answerSubmitted by grammarViewModel.isAnswerChecked.collectAsState()
    val isAnswerCorrect by grammarViewModel.isAnswerCorrect.collectAsState()
    
    val currentExercise = exercises.getOrNull(currentExerciseIndex)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercise ${currentExerciseIndex + 1}/${exercises.size}") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (exercises.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No exercises available for this lesson")
            }
            return@Scaffold
        }
        
        currentExercise?.let { exercise ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Exercise title and instruction
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = exercise.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = exercise.instruction,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Exercise content based on type
                when (exercise.type) {
                    ExerciseType.MULTIPLE_CHOICE -> MultipleChoiceExercise(
                        exercise = exercise,
                        userAnswer = userAnswer,
                        answerSubmitted = answerSubmitted,
                        onAnswerSelected = { grammarViewModel.selectAnswer(it) }
                    )
                    
                    ExerciseType.FILL_IN_BLANK -> FillInBlankExercise(
                        exercise = exercise,
                        userAnswer = userAnswer,
                        answerSubmitted = answerSubmitted,
                        onAnswerChanged = { grammarViewModel.selectAnswer(it) }
                    )
                    
                    ExerciseType.TRUE_FALSE -> TrueFalseExercise(
                        exercise = exercise,
                        userAnswer = userAnswer,
                        answerSubmitted = answerSubmitted,
                        onAnswerSelected = { grammarViewModel.selectAnswer(it) }
                    )
                    
                    else -> {
                        Text("This exercise type is not supported yet")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Submit button or feedback
                if (!answerSubmitted) {
                    Button(
                        onClick = { currentExercise?.let { grammarViewModel.checkAnswer(it.correctAnswer) } },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !userAnswer.isNullOrEmpty()
                    ) {
                        Text("Check Answer")
                    }
                } else {
                    // Show feedback
                    FeedbackCard(
                        isCorrect = isAnswerCorrect,
                        explanation = exercise.explanation
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Navigation buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Previous button
                        Button(
                            onClick = { grammarViewModel.previousExercise() },
                            enabled = currentExerciseIndex > 0
                        ) {
                            Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Previous")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Previous")
                        }
                        
                        // Next button
                        Button(
                            onClick = { grammarViewModel.nextExercise(exercises.size) },
                            enabled = currentExerciseIndex < exercises.size - 1 && answerSubmitted
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(PhosphorIcons.Regular.ArrowRight, contentDescription = "Next")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun MultipleChoiceExercise(
    exercise: GrammarExercise,
    userAnswer: String?,
    answerSubmitted: Boolean,
    onAnswerSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        exercise.options.forEachIndexed { index, option ->
            val isSelected = option == userAnswer
            val isCorrect = option == exercise.correctAnswer
            val showCorrectness = answerSubmitted
            
            val backgroundColor = when {
                showCorrectness && isCorrect -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                showCorrectness && isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
            
            val borderColor = when {
                showCorrectness && isCorrect -> MaterialTheme.colorScheme.primary
                showCorrectness && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }
            
            val optionPrefix = when (index) {
                0 -> "A"
                1 -> "B"
                2 -> "C"
                3 -> "D"
                else -> (index + 1).toString()
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(enabled = !answerSubmitted) { onAnswerSelected(option) },
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(borderColor.copy(alpha = 0.1f))
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = optionPrefix,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = borderColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (showCorrectness) {
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isCorrect) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Check,
                                contentDescription = "Correct",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else if (isSelected) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.X,
                                contentDescription = "Incorrect",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FillInBlankExercise(
    exercise: GrammarExercise,
    userAnswer: String?,
    answerSubmitted: Boolean,
    onAnswerChanged: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = userAnswer ?: "",
            onValueChange = { if (!answerSubmitted) onAnswerChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your answer") },
            enabled = !answerSubmitted,
            isError = answerSubmitted && userAnswer != exercise.correctAnswer,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
        
        if (answerSubmitted) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Correct answer: ${exercise.correctAnswer}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TrueFalseExercise(
    exercise: GrammarExercise,
    userAnswer: String?,
    answerSubmitted: Boolean,
    onAnswerSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val options = listOf("True", "False")
        
        options.forEach { option ->
            val isSelected = option == userAnswer
            val isCorrect = option == exercise.correctAnswer
            val showCorrectness = answerSubmitted
            
            val backgroundColor = when {
                showCorrectness && isCorrect -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                showCorrectness && isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
            
            val borderColor = when {
                showCorrectness && isCorrect -> MaterialTheme.colorScheme.primary
                showCorrectness && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }
            
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .clickable(enabled = !answerSubmitted) { onAnswerSelected(option) },
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (showCorrectness) {
                            Spacer(modifier = Modifier.height(8.dp))
                            if (isCorrect) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Check,
                                    contentDescription = "Correct",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else if (isSelected) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.X,
                                    contentDescription = "Incorrect",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackCard(
    isCorrect: Boolean,
    explanation: String
) {
    val backgroundColor = if (isCorrect) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
    }
    
    val borderColor = if (isCorrect) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    
    val icon = if (isCorrect) {
        PhosphorIcons.Regular.CheckCircle
    } else {
        PhosphorIcons.Regular.WarningCircle
    }
    
    val feedbackTitle = if (isCorrect) "Correct!" else "Incorrect"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = feedbackTitle,
                tint = borderColor,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = feedbackTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = borderColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}
