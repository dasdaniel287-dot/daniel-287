package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.JervesAiService
import com.example.data.local.ChatMessageEntity
import com.example.data.local.JervesDatabase
import com.example.data.local.MemoryEntity
import com.example.data.local.TaskCategory
import com.example.data.local.TaskEntity
import com.example.data.local.TaskPriority
import com.example.data.tts.JervesVoiceController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "JervesViewModel"

enum class AssistantCoreState {
    STANDBY,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

data class JervesUiState(
    val coreState: AssistantCoreState = AssistantCoreState.STANDBY,
    val streamingText: String = "",
    val isStreaming: Boolean = false,
    val activeBriefing: String? = null,
    val isBriefingLoading: Boolean = false,
    val userTitle: String = "Sir",
    val autoSpeak: Boolean = true,
    val errorMessage: String? = null,
    val systemStatus: String = "ALL SYSTEMS NOMINAL",
    val cpuTempSim: String = "34°C",
    val networkLatencySim: String = "24ms"
)

class JervesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JervesDatabase.getDatabase(application)
    private val taskDao = db.taskDao()
    private val memoryDao = db.memoryDao()
    private val chatDao = db.chatDao()

    private val aiService = JervesAiService()
    val voiceController = JervesVoiceController(application)

    val tasks: StateFlow<List<TaskEntity>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryEntity>> = memoryDao.getAllMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(JervesUiState())
    val uiState: StateFlow<JervesUiState> = _uiState.asStateFlow()

    init {
        // Observe voice controller speaking state
        viewModelScope.launch {
            voiceController.isSpeaking.collect { speaking ->
                if (speaking) {
                    _uiState.update { it.copy(coreState = AssistantCoreState.SPEAKING) }
                } else if (_uiState.value.coreState == AssistantCoreState.SPEAKING) {
                    _uiState.update { it.copy(coreState = AssistantCoreState.STANDBY) }
                }
            }
        }
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        voiceController.stop()
        viewModelScope.launch {
            try {
                // Save user message to Room
                chatDao.insertMessage(
                    ChatMessageEntity(
                        sender = "user",
                        message = trimmed
                    )
                )

                _uiState.update {
                    it.copy(
                        coreState = AssistantCoreState.PROCESSING,
                        isStreaming = true,
                        streamingText = "",
                        errorMessage = null
                    )
                }

                // Check if user is asking to add a task/directive
                val lower = trimmed.lowercase()
                if (lower.startsWith("remind me to") || lower.startsWith("add task") || lower.startsWith("add directive")) {
                    val (taskTitle, cat) = aiService.parseSmartDirective(trimmed)
                    val category = try { TaskCategory.valueOf(cat) } catch (_: Exception) { TaskCategory.DIRECTIVE }
                    taskDao.insertTask(
                        TaskEntity(
                            title = taskTitle,
                            description = "Created automatically by Jerves via voice/chat command.",
                            category = category.name,
                            priority = TaskPriority.HIGH.name,
                            dueDate = "Scheduled"
                        )
                    )
                }

                // Fetch recent messages for context
                val recentHistory = chatDao.getRecentMessages()
                    .reversed()
                    .map { it.sender to it.message }

                val fullResponse = StringBuilder()
                aiService.streamResponse(trimmed, recentHistory).collect { chunk ->
                    fullResponse.append(chunk)
                    _uiState.update { it.copy(streamingText = fullResponse.toString()) }
                }

                val finalAssistantText = fullResponse.toString().ifBlank {
                    "Directive executed, ${_uiState.value.userTitle}."
                }

                // Save assistant message to Room
                chatDao.insertMessage(
                    ChatMessageEntity(
                        sender = "jerves",
                        message = finalAssistantText
                    )
                )

                _uiState.update {
                    it.copy(
                        isStreaming = false,
                        streamingText = "",
                        coreState = AssistantCoreState.STANDBY
                    )
                }

                // Auto speak response if enabled
                if (_uiState.value.autoSpeak) {
                    voiceController.speak(finalAssistantText)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error in sendMessage", e)
                _uiState.update {
                    it.copy(
                        isStreaming = false,
                        coreState = AssistantCoreState.ERROR,
                        errorMessage = e.localizedMessage ?: "Failed to process transmission."
                    )
                }
            }
        }
    }

    fun generateDailyBriefing() {
        voiceController.stop()
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBriefingLoading = true,
                    coreState = AssistantCoreState.PROCESSING,
                    errorMessage = null
                )
            }
            try {
                val pendingTaskTitles = tasks.value.filter { !it.isCompleted }.map { it.title }
                val memoryTitles = memories.value.map { "${it.title}: ${it.content}" }
                val briefing = aiService.generateDailyBriefing(
                    userTitle = _uiState.value.userTitle,
                    pendingTasks = pendingTaskTitles,
                    memories = memoryTitles
                )
                _uiState.update {
                    it.copy(
                        activeBriefing = briefing,
                        isBriefingLoading = false,
                        coreState = AssistantCoreState.STANDBY
                    )
                }
                if (_uiState.value.autoSpeak) {
                    voiceController.speak(briefing)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate briefing", e)
                _uiState.update {
                    it.copy(
                        isBriefingLoading = false,
                        coreState = AssistantCoreState.ERROR,
                        errorMessage = "Briefing compilation failed: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun executeQuickProtocol(protocolName: String) {
        when (protocolName) {
            "Daily Briefing" -> generateDailyBriefing()
            "System Diagnostic" -> {
                val diagMessage = "Diagnostics complete, ${_uiState.value.userTitle}. Gemini 3.8 Flash Neural Engine is responsive. Memory Matrix holds ${memories.value.size} records. Active directives count: ${tasks.value.count { !it.isCompleted }}. Core integrity is 100%."
                sendMessage("Run Jerves system diagnostic and report status")
            }
            "Security Scan" -> {
                sendMessage("Run a cyber security check on local protocols and data integrity.")
            }
            "Creative Brainstorm" -> {
                sendMessage("Let's brainstorm inventive project ideas utilizing Android and Gemini AI.")
            }
            "Draft Communication" -> {
                sendMessage("Help me compose a high-priority executive email update.")
            }
            else -> {
                sendMessage("Initiate $protocolName protocol.")
            }
        }
    }

    fun setListeningState(isListening: Boolean) {
        _uiState.update {
            it.copy(
                coreState = if (isListening) AssistantCoreState.LISTENING else AssistantCoreState.STANDBY
            )
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.setTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun addTask(
        title: String,
        description: String,
        category: TaskCategory,
        priority: TaskPriority,
        dueDate: String
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskDao.insertTask(
                TaskEntity(
                    title = title.trim(),
                    description = description.trim(),
                    category = category.name,
                    priority = priority.name,
                    isCompleted = false,
                    dueDate = dueDate.ifBlank { "Immediate" }
                )
            )
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    fun addMemory(title: String, content: String, tag: String) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            memoryDao.insertMemory(
                MemoryEntity(
                    title = title.trim(),
                    content = content.trim(),
                    tag = tag.trim().uppercase().ifBlank { "CORE" }
                )
            )
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            memoryDao.deleteMemory(memory)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatDao.clearHistory()
            // re-insert greeting
            chatDao.insertMessage(
                ChatMessageEntity(
                    sender = "jerves",
                    message = "Memory buffer cleared, ${_uiState.value.userTitle}. Standing by for new directives."
                )
            )
        }
    }

    fun speak(text: String) {
        voiceController.speak(text)
    }

    fun stopSpeaking() {
        voiceController.stop()
    }

    fun setUserTitle(title: String) {
        _uiState.update { it.copy(userTitle = title.trim().ifBlank { "Sir" }) }
    }

    fun setAutoSpeak(enabled: Boolean) {
        _uiState.update { it.copy(autoSpeak = enabled) }
    }

    override fun onCleared() {
        super.onCleared()
        voiceController.shutdown()
    }
}
