# Task Management

Implement AI Task Management via Prompt to allow users to add and manage tasks using natural language.

## Todo List
- [x] Research and Setup
    - [x] Research existing AI prioritization logic
    - [x] Research Firebase AI Logic SDK usage and App Check integration
    - [x] Create implementation plan (Prioritization)
- [x] App Check Configuration
    - [x] Instruct user to register App Check debug secret in Firebase Console
- [x] AI Backend Verification
    - [x] Verify `gemini-2.0-flash` model usage
    - [x] Clarify `GEMINI_API_KEY` usage
- [x] End-to-End Testing (Prioritization)
    - [x] Deploy and test "Smart Prioritize" flow
    - [x] Verify Room updates and UI sort order
- [x] AI Task Management via Prompt
    - [x] Research and design prompt management
    - [x] Create implementation plan (Prompt Management)
    - [x] Implement `processTaskCommand` in `AIRepository` and `AIRepositoryImpl`
    - [x] Create `ProcessAICommandUseCase`
    - [x] Update `TaskListViewModel` and `TaskListState`
    - [x] Update `TaskListScreen` UI with AI command button and dialog
    - [x] End-to-End Testing (AI Command)
- [x] Final Walkthrough and Documentation
    - [x] Create walkthrough artifact
