package com.aregyan.compose.ui.grammar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aregyan.compose.data.model.GrammarExercise
import com.aregyan.compose.data.model.GrammarLesson
import com.aregyan.compose.repository.GrammarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class GrammarViewModel @Inject constructor(
    private val grammarRepository: GrammarRepository
) : ViewModel() {
    
    // State for lessons
    private val _selectedLessonId = MutableStateFlow<String?>(null)
    val selectedLessonId: StateFlow<String?> = _selectedLessonId
    
    val allLessons: StateFlow<List<GrammarLesson>> = grammarRepository.getAllLessons()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val selectedLesson: StateFlow<GrammarLesson?> = _selectedLessonId
        .filterNotNull()
        .flatMapLatest { lessonId ->
            grammarRepository.getLessonById(lessonId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    val exercisesForSelectedLesson: StateFlow<List<GrammarExercise>> = _selectedLessonId
        .filterNotNull()
        .flatMapLatest { lessonId ->
            grammarRepository.getExercisesByLessonId(lessonId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // State for exercises
    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex
    
    private val _selectedAnswer = MutableStateFlow<String?>(null)
    val selectedAnswer: StateFlow<String?> = _selectedAnswer
    
    private val _isAnswerChecked = MutableStateFlow(false)
    val isAnswerChecked: StateFlow<Boolean> = _isAnswerChecked
    
    private val _isAnswerCorrect = MutableStateFlow(false)
    val isAnswerCorrect: StateFlow<Boolean> = _isAnswerCorrect
    
    fun selectLesson(lessonId: String) {
        _selectedLessonId.value = lessonId
    }
    
    fun resetExerciseState() {
        _currentExerciseIndex.value = 0
        _selectedAnswer.value = null
        _isAnswerChecked.value = false
    }
    
    fun selectAnswer(answer: String) {
        if (!_isAnswerChecked.value) {
            _selectedAnswer.value = answer
        }
    }
    
    fun checkAnswer(correctAnswer: String) {
        _isAnswerChecked.value = true
        _isAnswerCorrect.value = _selectedAnswer.value == correctAnswer
    }
    
    fun nextExercise(totalExercises: Int) {
        if (_currentExerciseIndex.value < totalExercises - 1) {
            _currentExerciseIndex.value += 1
            _selectedAnswer.value = null
            _isAnswerChecked.value = false
        }
    }
    
    fun previousExercise() {
        if (_currentExerciseIndex.value > 0) {
            _currentExerciseIndex.value -= 1
            _selectedAnswer.value = null
            _isAnswerChecked.value = false
        }
    }
}
