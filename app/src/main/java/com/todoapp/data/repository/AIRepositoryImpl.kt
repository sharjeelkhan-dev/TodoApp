package com.todoapp.data.repository

import android.util.Log
import com.google.ai.client.generativeai.type.generationConfig
import com.google.ai.client.generativeai.GenerativeModel as GoogleGenerativeModel
import com.google.firebase.ai.GenerativeModel as FirebaseGenerativeModel
import com.todoapp.data.local.PreferenceManager
import com.todoapp.domain.model.AIAction
import com.todoapp.domain.model.Task
import com.todoapp.domain.model.TaskCategory
import com.todoapp.domain.model.TaskPriority
import com.todoapp.domain.repository.AIRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class AIRepositoryImpl @Inject constructor(
    private val firebaseModel: FirebaseGenerativeModel,
    private val preferenceManager: PreferenceManager
) : AIRepository {

    private suspend fun generateContent(prompt: String): String? {
        val customKey = preferenceManager.apiKey.first()?.trim()
        return if (customKey.isNullOrBlank()) {
            Log.d("AIRepository", "Using default Firebase-managed Gemini model")
            try {
                withContext(Dispatchers.IO) {
                    firebaseModel.generateContent(prompt).text
                }
            } catch (e: Exception) {
                Log.e("AIRepository", "Error with Firebase-managed model", e)
                val errorMessage = when {
                    e.message?.contains("403") == true -> "Firebase AI: Access Forbidden (403). Check App Check or API restrictions."
                    e.message?.contains("429") == true -> "Firebase AI: Quota exceeded (429)."
                    else -> "Firebase AI error: ${e.message}"
                }
                throw Exception(errorMessage)
            }
        } else {
            Log.d("AIRepository", "Using custom API key (length: ${customKey.length}) with Google AI SDK")
            try {
                withContext(Dispatchers.IO) {
                    val config = generationConfig {
                        responseMimeType = "application/json"
                    }
                    val googleModel = GoogleGenerativeModel(
                        modelName = "gemini-3.5-flash",
                        apiKey = customKey,
                        generationConfig = config
                    )
                    googleModel.generateContent(prompt).text
                }
            } catch (e: Exception) {
                Log.e("AIRepository", "Error with custom API key model", e)
                val errorMessage = when {
                    e.message?.contains("403") == true -> "Custom Key: Access Forbidden (403). Ensure 'Generative Language API' is enabled in Google Cloud Console."
                    e.message?.contains("API_KEY_INVALID") == true -> "Custom Key: Invalid API Key. Please check your key in Settings."
                    e.message?.contains("429") == true -> "Custom Key: Quota exceeded (429)."
                    else -> "Custom Key error: ${e.message}"
                }
                throw Exception(errorMessage)
            }
        }
    }

    override suspend fun getPrioritizationScores(tasks: List<Task>): Result<Map<String, Int>> = withContext(Dispatchers.IO) {
        try {
            Log.d("AIRepository", "Starting prioritization for ${tasks.size} tasks")
            if (tasks.isEmpty()) return@withContext Result.success(emptyMap())

            val taskListString = tasks.joinToString("\n") { task ->
                "- ID: ${task.id}, Title: ${task.title}, Description: ${task.description}, Due: ${task.dueDate}, Priority: ${task.priority.label}, Completed: ${task.isCompleted}"
            }

            val prompt = """
                Analyze the following list of tasks for a To-Do app and recommend a priority score (0 to 100) for each task.
                Consider:
                1. Deadlines (closer deadlines = higher score)
                2. User-set priority levels (High > Medium > Low)
                3. Completion status (Completed tasks should generally be 0 or very low)
                
                Higher score means higher importance/urgency.
                
                Tasks:
                $taskListString
                
                Respond ONLY with a valid JSON object where keys are task IDs and values are the priority scores as integers.
                Example: {"task1": 85, "task2": 40}
            """.trimIndent()

            Log.d("AIRepository", "Sending prompt to AI")
            val responseText = generateContent(prompt) ?: return@withContext Result.failure(Exception("Empty AI response"))
            Log.d("AIRepository", "AI Response received: $responseText")

            val jsonString = responseText.replace("```json", "").replace("```", "").trim()

            val jsonObject = JSONObject(jsonString)
            val scores = mutableMapOf<String, Int>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val id = keys.next()
                scores[id] = jsonObject.getInt(id)
            }

            Log.d("AIRepository", "Prioritization successful. Scores: $scores")
            Result.success(scores)
        } catch (e: Exception) {
            Log.e("AIRepository", "Error during AI prioritization", e)
            Result.failure(e)
        }
    }

    override suspend fun processTaskCommand(prompt: String, currentTasks: List<Task>): Result<List<AIAction>> = withContext(Dispatchers.IO) {
        try {
            Log.d("AIRepository", "Processing AI command: $prompt")
            val taskListString = currentTasks.joinToString("\n") { task ->
                "- ID: ${task.id}, Title: ${task.title}, Category: ${task.category.name}, Priority: ${task.priority.name}, Completed: ${task.isCompleted}"
            }

            val systemPrompt = """
                You are an AI task assistant. Your job is to parse user commands into a list of structured JSON actions.
                
                Current Tasks:
                $taskListString
                
                Available Categories: ${TaskCategory.entries.joinToString { it.name }}
                Available Priorities: ${TaskPriority.entries.joinToString { it.name }}
                
                Supported Actions and Rules:
                1. "ADD":
                   - Fields: "title", "description" (optional), "priority" (default MEDIUM), "category" (default OTHER), "dueDate" (YYYY-MM-DD, optional), "subTasks" (string array, optional).
                2. "ADD_SUBTASK":
                   - Fields: "taskId", "subTaskTitle".
                3. "UPDATE":
                   - Fields: "taskId", "title" (optional), "description" (optional), "priority" (optional), "category" (optional), "dueDate" (optional).
                4. "DELETE":
                   - Fields: "taskId".
                5. "DELETE_MULTIPLE":
                   - Fields: "taskIds" (array of task ID strings).
                6. "DELETE_SUBTASK":
                   - Fields: "taskId", "subTaskId".
                7. "TOGGLE_COMPLETION":
                   - Fields: "taskId", "isCompleted" (boolean).
                8. "TOGGLE_SUBTASK_COMPLETION":
                   - Fields: "taskId", "subTaskId", "isCompleted" (boolean).
                9. "GENERATE_SUBTASKS":
                   - Fields: "taskId".

                Respond ONLY with a valid JSON array of action objects.
                
                Example:
                [
                  {
                    "action": "ADD",
                    "title": "Buy groceries",
                    "priority": "HIGH",
                    "category": "SHOPPING",
                    "subTasks": ["Milk", "Eggs", "Bread"]
                  },
                  {
                    "action": "ADD_SUBTASK",
                    "taskId": "task123",
                    "subTaskTitle": "Review PR"
                  }
                ]
                
                User Command: "$prompt"
            """.trimIndent()

            Log.d("AIRepository", "Sending command prompt to AI")
            val responseText = generateContent(systemPrompt) ?: return@withContext Result.failure(Exception("Empty AI response"))
            Log.d("AIRepository", "AI Response received: $responseText")

            val jsonString = responseText.replace("```json", "").replace("```", "").trim()
            val jsonArray = JSONArray(jsonString)
            val actions = mutableListOf<AIAction>()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val actionType = obj.getString("action")

                try {
                    when (actionType) {
                        "ADD" -> {
                            val subTasksList = mutableListOf<String>()
                            if (obj.has("subTasks")) {
                                val subTasksJson = obj.getJSONArray("subTasks")
                                for (j in 0 until subTasksJson.length()) {
                                    subTasksList.add(subTasksJson.getString(j))
                                }
                            }
                            actions.add(
                                AIAction.Add(
                                    title = obj.getString("title"),
                                    description = obj.optString("description", ""),
                                    priority = TaskPriority.valueOf(obj.optString("priority", "MEDIUM")),
                                    category = TaskCategory.valueOf(obj.optString("category", "OTHER")),
                                    dueDate = if (obj.has("dueDate")) dateFormat.parse(obj.getString("dueDate")) else null,
                                    subTasks = subTasksList
                                )
                            )
                        }
                        "ADD_SUBTASK" -> {
                            actions.add(
                                AIAction.AddSubTask(
                                    taskId = obj.getString("taskId"),
                                    subTaskTitle = obj.getString("subTaskTitle")
                                )
                            )
                        }
                        "UPDATE" -> {
                            actions.add(
                                AIAction.Update(
                                    taskId = obj.getString("taskId"),
                                    title = if (obj.has("title")) obj.getString("title") else null,
                                    description = if (obj.has("description")) obj.getString("description") else null,
                                    priority = if (obj.has("priority")) TaskPriority.valueOf(obj.getString("priority")) else null,
                                    category = if (obj.has("category")) TaskCategory.valueOf(obj.getString("category")) else null,
                                    dueDate = if (obj.has("dueDate")) dateFormat.parse(obj.getString("dueDate")) else null
                                )
                            )
                        }
                        "DELETE" -> {
                            actions.add(AIAction.Delete(taskId = obj.getString("taskId")))
                        }
                        "DELETE_MULTIPLE" -> {
                            val idList = mutableListOf<String>()
                            val array = obj.getJSONArray("taskIds")
                            for (j in 0 until array.length()) {
                                idList.add(array.getString(j))
                            }
                            actions.add(AIAction.DeleteMultiple(taskIds = idList))
                        }
                        "DELETE_SUBTASK" -> {
                            actions.add(
                                AIAction.DeleteSubTask(
                                    taskId = obj.getString("taskId"),
                                    subTaskId = obj.getString("subTaskId")
                                )
                            )
                        }
                        "TOGGLE_COMPLETION" -> {
                            actions.add(
                                AIAction.ToggleCompletion(
                                    taskId = obj.getString("taskId"),
                                    isCompleted = obj.getBoolean("isCompleted")
                                )
                            )
                        }
                        "TOGGLE_SUBTASK_COMPLETION" -> {
                            actions.add(
                                AIAction.ToggleSubTaskCompletion(
                                    taskId = obj.getString("taskId"),
                                    subTaskId = obj.getString("subTaskId"),
                                    isCompleted = obj.getBoolean("isCompleted")
                                )
                            )
                        }
                        "GENERATE_SUBTASKS" -> {
                            actions.add(
                                AIAction.GenerateSubTasks(
                                    taskId = obj.getString("taskId")
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.w("AIRepository", "Skipping invalid AI action: $obj", e)
                }
            }

            Log.d("AIRepository", "Parsed ${actions.size} actions from AI")
            Result.success(actions)
        } catch (e: Exception) {
            Log.e("AIRepository", "Error during AI command processing", e)
            Result.failure(e)
        }
    }
}