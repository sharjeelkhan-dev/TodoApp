package com.todoapp.presentation.screens.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todoapp.domain.model.FilterOption
import com.todoapp.domain.model.Task
import com.todoapp.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

enum class SessionType(val durationMinutes: Int) {
    WORK(25),
    SHORT_BREAK(5),
    LONG_BREAK(15)
}

data class FocusState(
    val timeRemaining: Int = SessionType.WORK.durationMinutes * 60,
    val baseDurationMinutes: Int = SessionType.WORK.durationMinutes,
    val timerRunning: Boolean = false,
    val currentSessionType: SessionType = SessionType.WORK,
    val selectedTask: Task? = null,
    val sessionCount: Int = 0,
    val tasks: List<Task> = emptyList()
)

sealed class FocusEvent {
    data object StartPause : FocusEvent()
    data object Reset : FocusEvent()
    data class SelectTask(val task: Task?) : FocusEvent()
    data class AdjustTime(val minutes: Int) : FocusEvent()
}

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FocusState())
    val state: StateFlow<FocusState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            taskRepository.getTasks(FilterOption(status = false)).collectLatest { tasks ->
                _state.update { it.copy(tasks = tasks) }
            }
        }
    }

    fun onEvent(event: FocusEvent) {
        when (event) {
            FocusEvent.StartPause -> toggleTimer()
            FocusEvent.Reset -> resetTimer()
            is FocusEvent.SelectTask -> {
                _state.update { it.copy(selectedTask = event.task) }
            }
            is FocusEvent.AdjustTime -> adjustTimer(event.minutes)
        }
    }

    private fun adjustTimer(minutes: Int) {
        if (_state.value.timerRunning) return
        
        _state.update {
            val newMinutes = (it.baseDurationMinutes + minutes).coerceIn(1, 120)
            it.copy(
                baseDurationMinutes = newMinutes,
                timeRemaining = newMinutes * 60
            )
        }
    }

    private fun toggleTimer() {
        if (_state.value.timerRunning) {
            timerJob?.cancel()
            _state.update { it.copy(timerRunning = false) }
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _state.update { it.copy(timerRunning = true) }
        timerJob = viewModelScope.launch {
            while (_state.value.timeRemaining > 0) {
                delay(1000.milliseconds)
                _state.update { it.copy(timeRemaining = it.timeRemaining - 1) }
            }
            onSessionComplete()
        }
    }

    private fun onSessionComplete() {
        _state.update {
            val nextSessionCount = if (it.currentSessionType == SessionType.WORK) it.sessionCount + 1 else it.sessionCount
            val nextType = when (it.currentSessionType) {
                SessionType.WORK -> {
                    if (nextSessionCount % 4 == 0) SessionType.LONG_BREAK else SessionType.SHORT_BREAK
                }
                else -> SessionType.WORK
            }
            it.copy(
                timerRunning = false,
                currentSessionType = nextType,
                baseDurationMinutes = nextType.durationMinutes,
                timeRemaining = nextType.durationMinutes * 60,
                sessionCount = nextSessionCount
            )
        }
        timerJob?.cancel()
    }

    private fun resetTimer() {
        timerJob?.cancel()
        _state.update {
            it.copy(
                timerRunning = false,
                timeRemaining = it.baseDurationMinutes * 60
            )
        }
    }
}
