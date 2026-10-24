package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TaskEntity
import com.example.ui.components.ArcReactorOrb
import com.example.ui.components.StatusHeader
import com.example.ui.navigation.Screen
import com.example.ui.theme.JervesAmber
import com.example.ui.theme.JervesBgDark
import com.example.ui.theme.JervesCardGradEnd
import com.example.ui.theme.JervesCardGradStart
import com.example.ui.theme.JervesCyanContainer
import com.example.ui.theme.JervesCyanPrimary
import com.example.ui.theme.JervesNeonGreen
import com.example.ui.theme.JervesSurfaceBorder
import com.example.ui.theme.JervesSurfaceDark
import com.example.ui.theme.JervesSurfaceVariant
import com.example.ui.theme.JervesTextPrimary
import com.example.ui.theme.JervesTextSecondary
import com.example.ui.theme.JervesTextTertiary
import com.example.ui.viewmodel.AssistantCoreState
import com.example.ui.viewmodel.JervesViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: JervesViewModel,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }

    // Speech recognition launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.setListeningState(false)
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenSpans = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenSpans?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.sendMessage(spokenText)
                onNavigate(Screen.CHAT)
            }
        }
    }

    val triggerSpeech = {
        try {
            viewModel.setListeningState(true)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Jerves is listening. State your directive, ${uiState.userTitle}...")
            }
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            viewModel.setListeningState(false)
            Toast.makeText(context, "Speech recognition unavailable: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JervesBgDark),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            StatusHeader(
                userTitle = uiState.userTitle,
                isSpeaking = uiState.coreState == AssistantCoreState.SPEAKING,
                onStopSpeech = { viewModel.stopSpeaking() }
            )
        }

        // Arc Reactor Visualizer
        item {
            Spacer(modifier = Modifier.height(12.dp))
            ArcReactorOrb(
                state = uiState.coreState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                size = 200.dp,
                onClick = {
                    if (uiState.coreState == AssistantCoreState.SPEAKING) {
                        viewModel.stopSpeaking()
                    } else {
                        triggerSpeech()
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Input Command Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Command Jerves or state directive...",
                            color = JervesTextTertiary,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        IconButton(
                            onClick = triggerSpeech,
                            modifier = Modifier.testTag("dashboard_mic_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = if (uiState.coreState == AssistantCoreState.LISTENING) JervesNeonGreen else JervesCyanPrimary
                            )
                        }
                    },
                    trailingIcon = {
                        if (inputText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val query = inputText
                                    inputText = ""
                                    viewModel.sendMessage(query)
                                    onNavigate(Screen.CHAT)
                                },
                                modifier = Modifier.testTag("dashboard_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send Command",
                                    tint = JervesCyanPrimary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JervesCyanPrimary,
                        unfocusedBorderColor = JervesSurfaceBorder,
                        focusedContainerColor = JervesSurfaceDark,
                        unfocusedContainerColor = JervesSurfaceDark,
                        focusedTextColor = JervesTextPrimary,
                        unfocusedTextColor = JervesTextPrimary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_input_field")
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Daily Briefing Protocol Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(JervesCardGradStart, JervesCardGradEnd)
                        )
                    )
                    .border(1.dp, JervesSurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = JervesAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PROTOCOL // DAILY BRIEFING",
                            color = JervesTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    if (uiState.activeBriefing != null) {
                        Row {
                            IconButton(
                                onClick = { uiState.activeBriefing?.let { viewModel.speak(it) } },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("briefing_speak_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Listen to briefing",
                                    tint = JervesCyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.generateDailyBriefing() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("briefing_refresh_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh briefing",
                                    tint = JervesTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.isBriefingLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = JervesCyanPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Compiling intelligence briefing...",
                            color = JervesCyanPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else if (uiState.activeBriefing != null) {
                    Text(
                        text = uiState.activeBriefing ?: "",
                        color = JervesTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                } else {
                    Text(
                        text = "Initialize your morning intelligence overview. Jerves will synthesize all pending directives, memory records, and tactical insights.",
                        color = JervesTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.generateDailyBriefing() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JervesCyanContainer,
                            contentColor = JervesCyanPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("compile_briefing_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COMPILE BRIEFING",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Action Protocols Row
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "RAPID PROTOCOLS",
                    color = JervesTextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val protocols = listOf(
                    "Daily Briefing",
                    "System Diagnostic",
                    "Security Scan",
                    "Creative Brainstorm",
                    "Draft Communication"
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(protocols) { protocol ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(JervesSurfaceDark)
                                .border(1.dp, JervesSurfaceBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.executeQuickProtocol(protocol)
                                    if (protocol != "Daily Briefing") {
                                        onNavigate(Screen.CHAT)
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = protocol,
                                color = JervesCyanPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Active Directives Snapshot
        item {
            val pendingTasks = tasks.filter { !it.isCompleted }.take(3)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE DIRECTIVES (${tasks.count { !it.isCompleted }})",
                        color = JervesTextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "VIEW ALL >",
                        color = JervesCyanPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clickable { onNavigate(Screen.TASKS) }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (pendingTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(JervesSurfaceDark)
                            .border(1.dp, JervesSurfaceBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active directives. All systems clear, ${uiState.userTitle}.",
                            color = JervesTextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    pendingTasks.forEach { task ->
                        TaskSnapshotRow(
                            task = task,
                            onToggle = { viewModel.toggleTask(task) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskSnapshotRow(
    task: TaskEntity,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(JervesSurfaceDark)
            .border(1.dp, JervesSurfaceBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onToggle)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (task.isCompleted) JervesNeonGreen else JervesCyanPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                color = JervesTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    color = JervesTextTertiary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "[${task.priority}]",
            color = if (task.priority == "CRITICAL") JervesAmber else JervesTextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
