# Implementation Plan - AI Task Management via Prompt

This plan outlines the steps to implement a feature that allows users to add and manage tasks (edit, delete, toggle completion) using a single natural language AI prompt.

## User Review Required

- **Prompt Input UI**: I propose adding a new "AI Command" button in the `HeaderTopRow` which opens a dialog with a text field for the user to enter their command.
- **AI Backend Capability**: We will use the `gemini-2.0-flash` model. The AI will be instructed to respond with a JSON array of commands (e.g., `ADD`, `UPDATE`, `DELETE`, `TOGGLE_COMPLETION`).

## Proposed Changes

### Domain Layer

#### [NEW] [ProcessAICommandUseCase.kt](file:///D:/Files/University/Projects/To-Do-List-App/app/src/main/java/com/todoapp/domain/usecase/ProcessAICommandUseCase.kt)
- Orchestrates the flow: Gets current tasks, calls AI repository to parse the command, and applies the returned actions using existing use cases (`AddTaskUseCase`, `DeleteTaskUseCase`, etc.).

#### [AIRepository.kt](file:///D:/Files/University/Projects/To-Do-List-App/app/src/main/java/com/todoapp/domain/repository/AIRepository.kt)
- Add `processTaskCommand(prompt: String, currentTasks: List<Task>): Result<List<AIAction>>` interface.

### Data Layer

#### [AIRepositoryImpl.kt](file:///D:/Files/University/Projects/To-Do-List-App/app/src/main/java/com/todoapp/data/repository/AIRepositoryImpl.kt)
- Implement `processTaskCommand` using Gemini.
- Prompt will include context of current tasks and instructions to output structured JSON actions.

### Presentation Layer

#### [TaskListState.kt](file:///D:/Files/University/Projects/To-Do-List-App/app/src/main/java/com/todoapp/presentation/screens/tasklist/TaskListState.kt)
- Add `isAICommandDialogOpen: Boolean` to state.
- Add `TaskListEvent.ToggleAICommandDialog` and `TaskListEvent.ExecuteAICommand(prompt: String)`.

#### [TaskListViewModel.kt](file:///D:/Files/University/Projects/To-Do-List-App/app/src/main/java/com/todoapp/presentation/screens/tasklist/TaskListViewModel.kt)
- Handle the new events.
- Call `ProcessAICommandUseCase` when executing a command.

#### [TaskListScreen.kt](file:///D:/Files/University/Projects/To-Do-List-App/app/src/main/java/com/todoapp/presentation/screens/tasklist/TaskListScreen.kt)
- Add the "AI Command" button in `HeaderTopRow`.
- Implement the `AICommandDialog` for text input.

---

## Verification Plan

### Manual Verification
1. **Trigger AI Command**: Tap the new AI icon in the header.
2. **Add Task**: Type "Add a task to buy milk today" -> Verify task appears in list.
3. **Delete Task**: Type "Delete the milk task" -> Verify task is removed.
4. **Update Task**: Type "Change priority of milk task to high" -> Verify task priority updates.
5. **Toggle Completion**: Type "Mark milk task as done" -> Verify checkbox is checked.
6. **Complex Command**: Type "Add two tasks: walk the dog and cook dinner" -> Verify both tasks are added.

### Automated Tests
- Run `app:assembleDebug` to ensure no build regressions.
