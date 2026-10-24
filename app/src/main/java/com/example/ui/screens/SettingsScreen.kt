package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.JervesAmber
import com.example.ui.theme.JervesBgDark
import com.example.ui.theme.JervesCyanContainer
import com.example.ui.theme.JervesCyanPrimary
import com.example.ui.theme.JervesNeonGreen
import com.example.ui.theme.JervesSurfaceBorder
import com.example.ui.theme.JervesSurfaceDark
import com.example.ui.theme.JervesSurfaceVariant
import com.example.ui.theme.JervesTextPrimary
import com.example.ui.theme.JervesTextSecondary
import com.example.ui.theme.JervesTextTertiary
import com.example.ui.viewmodel.JervesViewModel

@Composable
fun SettingsScreen(
    viewModel: JervesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var customTitle by remember(uiState.userTitle) { mutableStateOf(uiState.userTitle) }
    var pitch by remember { mutableFloatStateOf(0.95f) }
    var speed by remember { mutableFloatStateOf(1.0f) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JervesBgDark),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Title Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(JervesCyanPrimary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SYSTEM PROTOCOLS & CONFIGURATION",
                    color = JervesCyanPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        // Persona & Address
        item {
            ConfigCard(
                title = "ASSISTANT PROTOCOL & ADDRESS",
                icon = Icons.Default.Tune
            ) {
                Text(
                    text = "How Jerves should address you:",
                    color = JervesTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                val presetTitles = listOf("Sir", "Commander", "Doctor", "Boss", "Captain")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetTitles.forEach { title ->
                        val isSelected = uiState.userTitle.equals(title, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) JervesCyanContainer else JervesSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) JervesCyanPrimary else JervesSurfaceBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    viewModel.setUserTitle(title)
                                    customTitle = title
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) JervesCyanPrimary else JervesTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customTitle,
                    onValueChange = {
                        customTitle = it
                        viewModel.setUserTitle(it)
                    },
                    label = { Text("Custom Designation / Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JervesCyanPrimary,
                        unfocusedBorderColor = JervesSurfaceBorder,
                        focusedTextColor = JervesTextPrimary,
                        unfocusedTextColor = JervesTextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Voice & Synthesizer Settings
        item {
            ConfigCard(
                title = "SPEECH SYNTHESIS ENGINE",
                icon = Icons.Default.RecordVoiceOver
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Vocalize Responses",
                            color = JervesTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Jerves speaks out loud automatically when replies arrive",
                            color = JervesTextTertiary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = uiState.autoSpeak,
                        onCheckedChange = { viewModel.setAutoSpeak(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JervesCyanPrimary,
                            checkedTrackColor = JervesCyanContainer,
                            uncheckedThumbColor = JervesTextTertiary,
                            uncheckedTrackColor = JervesSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Vocal Pitch (${String.format("%.2f", pitch)}x):",
                    color = JervesTextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Slider(
                    value = pitch,
                    onValueChange = {
                        pitch = it
                        viewModel.voiceController.setPitch(it)
                    },
                    valueRange = 0.7f..1.4f,
                    colors = SliderDefaults.colors(
                        thumbColor = JervesCyanPrimary,
                        activeTrackColor = JervesCyanPrimary,
                        inactiveTrackColor = JervesSurfaceVariant
                    )
                )

                Text(
                    text = "Speech Rate (${String.format("%.2f", speed)}x):",
                    color = JervesTextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Slider(
                    value = speed,
                    onValueChange = {
                        speed = it
                        viewModel.voiceController.setSpeechRate(it)
                    },
                    valueRange = 0.7f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = JervesCyanPrimary,
                        activeTrackColor = JervesCyanPrimary,
                        inactiveTrackColor = JervesSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.speak("Synthesizer test confirmed. All audio frequencies nominal, ${uiState.userTitle}.")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JervesCyanContainer,
                        contentColor = JervesCyanPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TEST VOCAL SYNTHESIZER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // System Diagnostic Readout
        item {
            ConfigCard(
                title = "CORE ARCHITECTURE TELEMETRY",
                icon = Icons.Default.Security
            ) {
                TelemetryRow("AI Reasoning Core", "Gemini 3.8 Flash (Latest)", JervesCyanPrimary)
                TelemetryRow("Backend Attestation", "Firebase App Check", JervesNeonGreen)
                TelemetryRow("Streaming Latency", "~28ms", JervesCyanPrimary)
                TelemetryRow("Persistence Layer", "Android Room SQLite (Encrypted)", JervesAmber)
                TelemetryRow("Audio Synthesizer", "Android Native TTS Engine", JervesCyanPrimary)
                TelemetryRow("Speech Recognition", "Intent Speech Recognizer", JervesNeonGreen)
            }
        }

        // About Jerves
        item {
            ConfigCard(
                title = "ABOUT JERVES AI",
                icon = Icons.Default.Info
            ) {
                Text(
                    text = "JERVES (Just Extremely Responsive & Versatile Electronic System) is an intelligent personal assistant powered by Google Gemini and modern Android Jetpack Compose.",
                    color = JervesTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "VERSION 3.8.0 // PRODUCTION BUILD",
                    color = JervesCyanPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun ConfigCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(JervesSurfaceDark)
            .border(1.dp, JervesSurfaceBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = JervesCyanPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = JervesTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
        content()
    }
}

@Composable
private fun TelemetryRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = JervesTextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
