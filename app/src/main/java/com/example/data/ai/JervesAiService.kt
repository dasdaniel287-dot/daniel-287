package com.example.data.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "JervesAiService"

class JervesAiService {

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    You are Jerves, an ultra-advanced AI personal assistant with the personality, intellect, and refined wit of JARVIS.
                    - Address the user courteously as 'Sir' (or their specified title), unless instructed otherwise.
                    - Tone: Sophisticated, exceptionally capable, calm under pressure, lightly humorous, and hyper-efficient.
                    - Style: Crisp, actionable, well-structured. Use technical readouts when appropriate (e.g., [STATUS: NOMINAL], [DIAGNOSTIC], [DIRECTIVE]).
                    - Never give long-winded generic boilerplate or sycophantic apologies. Give high-level tactical solutions and clear steps.
                    - If the user asks for a daily briefing, provide a structured breakdown: Core Directives, Priority Alerts, Weather/System status, and an inspirational quote.
                    """.trimIndent()
                )
            }
        )
    }

    /**
     * Stream responses chunk-by-chunk for conversational UI.
     */
    fun streamResponse(
        prompt: String,
        recentHistory: List<Pair<String, String>> = emptyList()
    ): Flow<String> = flow {
        try {
            withTimeout(60_000L) {
                // If history exists, use chat session or contextually prepended prompt
                if (recentHistory.isNotEmpty()) {
                    val historyContent = recentHistory.takeLast(10).map { (role, msg) ->
                        val normalizedRole = if (role.equals("user", ignoreCase = true)) "user" else "model"
                        content(normalizedRole) { text(msg) }
                    }
                    val chat = model.startChat(history = historyContent)
                    chat.sendMessageStream(prompt).collect { chunk ->
                        val piece = chunk.text.orEmpty()
                        if (piece.isNotEmpty()) {
                            emit(piece)
                        }
                    }
                } else {
                    model.generateContentStream(prompt).collect { chunk ->
                        val piece = chunk.text.orEmpty()
                        if (piece.isNotEmpty()) {
                            emit(piece)
                        }
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Streaming timed out", e)
            emit("\n[ERROR: Jerves neural link connection timed out. Please retry, sir.]")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Streaming generation failed", e)
            emit("\n[COMMUNICATION FAULT: ${e.localizedMessage ?: "Unknown transmission error"}]")
        }
    }

    /**
     * Generate daily briefing summary based on current tasks and time of day.
     */
    suspend fun generateDailyBriefing(
        userTitle: String = "Sir",
        pendingTasks: List<String>,
        memories: List<String>
    ): String {
        return try {
            withTimeout(60_000L) {
                val prompt = """
                    Generate a high-tech Daily Briefing for $userTitle.
                    Known active directives/tasks: ${if (pendingTasks.isEmpty()) "No pending tasks." else pendingTasks.joinToString("; ")}
                    Stored memories & key facts: ${if (memories.isEmpty()) "Standard protocols." else memories.joinToString("; ")}
                    
                    Format the briefing strictly in this style:
                    1. Elegant greeting and status check.
                    2. Tactical Overview: Key directives to conquer today.
                    3. AI Optimization Tip / Tactical Insight.
                    4. Parting closing line.
                """.trimIndent()
                val response = model.generateContent(prompt)
                response.text ?: "Good day, $userTitle. All systems are operational and ready for your command."
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Briefing timed out", e)
            "Briefing request timed out, $userTitle. However, your local subsystems remain operational."
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Briefing generation failed", e)
            "Unable to compile remote telemetry, $userTitle. Error: ${e.localizedMessage ?: "Connection interrupted."}"
        }
    }

    /**
     * Analyze a quick directive (e.g. "remind me to fix the bug") and return a parsed task title.
     */
    suspend fun parseSmartDirective(input: String): Pair<String, String> {
        return try {
            withTimeout(30_000L) {
                val prompt = """
                    Extract the core task title and a category (one of: DIRECTIVE, CODING, PERSONAL, RESEARCH, SYSTEM) from this request:
                    "$input"
                    Respond strictly in this format on two lines:
                    TITLE: <extracted title>
                    CATEGORY: <CATEGORY>
                """.trimIndent()
                val response = model.generateContent(prompt).text.orEmpty()
                var title = input
                var category = "DIRECTIVE"
                response.lines().forEach { line ->
                    if (line.startsWith("TITLE:", ignoreCase = true)) {
                        title = line.substringAfter(":").trim()
                    } else if (line.startsWith("CATEGORY:", ignoreCase = true)) {
                        category = line.substringAfter(":").trim().uppercase()
                    }
                }
                title to category
            }
        } catch (e: Exception) {
            input to "DIRECTIVE"
        }
    }
}
