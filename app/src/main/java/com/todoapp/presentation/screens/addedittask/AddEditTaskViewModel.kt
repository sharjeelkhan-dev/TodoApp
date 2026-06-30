package com.todoapp.presentation.screens.addedittask
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todoapp.data.util.ReminderManager
import com.todoapp.domain.model.SubTask
import com.todoapp.domain.model.Task
import com.todoapp.domain.repository.AuthRepository
import com.todoapp.domain.usecase.AddTaskUseCase
import com.todoapp.domain.usecase.DeleteTaskUseCase
import com.todoapp.domain.usecase.GetTaskByIdUseCase
import com.todoapp.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val authRepository: AuthRepository,
    private val reminderManager: ReminderManager,
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditTaskState())
    val state: StateFlow<AddEditTaskState> = _state.asStateFlow()

    private var currentTask: Task? = null

    init {
        val taskId = savedStateHandle.get<String>("taskId")
        if (taskId != null && (taskId != "new")) {
            loadTask(taskId)
        }
    }

    fun onEvent(event: AddEditTaskEvent) {
        when (event) {
            is AddEditTaskEvent.TitleChanged -> {
                _state.update { it.copy(title = event.title, error = null) }
            }
            is AddEditTaskEvent.DescriptionChanged -> {
                _state.update { it.copy(description = event.desc, error = null) }
            }
            is AddEditTaskEvent.CategoryChanged -> {
                _state.update { it.copy(category = event.category) }
            }
            is AddEditTaskEvent.PriorityChanged -> {
                _state.update { it.copy(priority = event.priority) }
            }
            is AddEditTaskEvent.DueDateChanged -> {
                _state.update { it.copy(dueDate = event.date, showDatePicker = false) }
            }
            is AddEditTaskEvent.DueTimeChanged -> {
                _state.update { it.copy(dueTime = event.time, showTimePicker = false) }
            }
            AddEditTaskEvent.ToggleDatePicker -> {
                _state.update { it.copy(showDatePicker = !it.showDatePicker) }
            }
            AddEditTaskEvent.ToggleTimePicker -> {
                _state.update { it.copy(showTimePicker = !it.showTimePicker) }
            }
            is AddEditTaskEvent.ToggleReminder -> {
                _state.update { it.copy(isReminderEnabled = event.isEnabled) }
            }
            AddEditTaskEvent.Save -> saveTask()
            AddEditTaskEvent.Delete -> deleteTask()
            AddEditTaskEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
            AddEditTaskEvent.AddSubTask -> {
                _state.update {
                    it.copy(subTasks = it.subTasks + SubTask())
                }
            }
            is AddEditTaskEvent.RemoveSubTask -> {
                _state.update {
                    it.copy(subTasks = it.subTasks.filter { subTask -> subTask.id != event.subTaskId })
                }
            }
            is AddEditTaskEvent.SubTaskTitleChanged -> {
                _state.update {
                    it.copy(subTasks = it.subTasks.map { subTask ->
                        if (subTask.id == event.subTaskId) subTask.copy(title = event.title) else subTask
                    })
                }
            }
            is AddEditTaskEvent.ToggleSubTaskCompletion -> {
                _state.update {
                    it.copy(subTasks = it.subTasks.map { subTask ->
                        if (subTask.id == event.subTaskId) subTask.copy(isCompleted = !subTask.isCompleted) else subTask
                    })
                }
            }
        }
    }

    private fun loadTask(taskId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val task = getTaskByIdUseCase(taskId).firstOrNull()
            if (task != null) {
                currentTask = task
                _state.update {
                    it.copy(
                        taskId = task.id,
                        title = task.title,
                        description = task.description,
                        category = task.category,
                        priority = task.priority,
                        dueDate = task.dueDate,
                        dueTime = task.dueTime,
                        isReminderEnabled = task.isReminderEnabled,
                        subTasks = task.subTasks,
                        isEditing = true,
                        isLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun saveTask() {
        val currentState = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val userId = authRepository.currentUserId ?: ""
            val taskId = currentState.taskId ?: UUID.randomUUID().toString()
            
            val task = Task(
                id = taskId,
                title = currentState.title.trim(),
                description = currentState.description.trim(),
                category = currentState.category,
                priority = currentState.priority,
                dueDate = currentState.dueDate,
                dueTime = currentState.dueTime,
                isReminderEnabled = currentState.isReminderEnabled,
                createdAt = currentTask?.createdAt ?: Date(),
                updatedAt = Date(),
                userId = userId,
                isCompleted = currentTask?.isCompleted ?: false,
                subTasks = currentState.subTasks
            )

            val result = if (currentState.isEditing) {
                updateTaskUseCase(task)
            } else {
                addTaskUseCase(task)
            }

            result.fold(
                onSuccess = {
                    reminderManager.scheduleReminder(task)
                    _state.update { it.copy(isLoading = false, isSaved = true) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isLoading = false, error = error.message ?: "Failed to save task")
                    }
                },
            )
        }
    }

    private fun deleteTask() {
        val taskToDelete = currentTask ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            deleteTaskUseCase(taskToDelete).fold(
                onSuccess = {
                    reminderManager.cancelReminder(taskToDelete.id)
                    _state.update { it.copy(isLoading = false, isSaved = true) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isLoading = false, error = error.message ?: "Failed to delete task")
                    }
                }
            )
        }
    }
}
