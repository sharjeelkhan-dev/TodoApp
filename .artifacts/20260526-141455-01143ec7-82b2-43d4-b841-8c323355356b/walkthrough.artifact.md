# Walkthrough - AI Task Management via Prompt

I have implemented a new feature that allows users to add and manage tasks using a single natural language AI prompt.

## Key Accomplishments

### 1. Structured AI Commands
- Defined `AIAction` sealed class to represent `Add`, `Update`, `Delete`, and `ToggleCompletion` operations.
- Updated `AIRepository` to include `processTaskCommand`, which uses Gemini to parse natural language into these structured actions.

### 2. Smart Command Processing
- Implemented `ProcessAICommandUseCase` which:
    - Fetches current tasks to provide context to the AI (e.g., knowing which task ID to update/delete).
    - Calls the AI to parse the user's prompt.
    - Sequentially applies the returned actions using existing domain use cases.

### 3. AI Command UI
- Added a new **AI Command** button (Sparkles icon) in the Task List header.
- Implemented an **AI Command Dialog** where users can type their requests.
- Integrated the "Smart Prioritize" feature into this dialog for a unified AI experience.

## Verification Results

### Build Status
- **Result**: Success.
- **Evidence**: `app:assembleDebug` finished successfully.

### Manual Verification Flow (Conceptual)
1. **Add Task**: Type *"Add a task to buy milk today"* -> AI parses to `AIAction.Add` -> Task appears in list.
2. **Update Task**: Type *"Change priority of buy milk to high"* -> AI finds task ID and parses to `AIAction.Update` -> Priority updates.
3. **Toggle Completion**: Type *"Mark buy milk as done"* -> AI parses to `AIAction.ToggleCompletion` -> Checkbox is checked.
4. **Delete Task**: Type *"Delete the milk task"* -> AI parses to `AIAction.Delete` -> Task is removed.

## Final Notes for the User
- The AI is instructed to output JSON. If it fails to do so, an error message will appear.
- Ensure your Google AI project has sufficient quota for the `gemini-2.0-flash` model.
