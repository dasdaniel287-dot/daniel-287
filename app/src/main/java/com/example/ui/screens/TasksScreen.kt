package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TaskCategory
import com.example.data.local.TaskEntity
import com.example.data.local.TaskPriority
import com.example.ui.theme.JervesAmber
import com.example.ui.theme.JervesBgDark
import com.example.ui.theme.JervesCrimson
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
fun TasksScreen(
    viewModel: JervesViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredTasks = remember(tasks, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> tasks.filter { !it.isCompleted }
            "COMPLETED" -> tasks.filter { it.isCompleted }
            "CRITICAL" -> tasks.filter { it.priority == TaskPriority.CRITICAL.name }
            else -> tasks
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = JervesBgDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = JervesCyanPrimary,
                contentColor = Color(0xFF001F29),
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("add_directive_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Directive")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(JervesBgDark)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(JervesCyanPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DIRECTIVES MATRIX",
                        color = JervesCyanPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(JervesSurfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, JervesSurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    DirectiveStat(label = "TOTAL", count = tasks.size.toString(), color = JervesTextPrimary)
                    DirectiveStat(
                        label = "PENDING",
                        count = tasks.count { !it.isCompleted }.toString(),
                        color = JervesAmber
                    )
                    DirectiveStat(
                        label = "COMPLETED",
                        count = tasks.count { it.isCompleted }.toString(),
                        color = JervesNeonGreen
                    )
                }
            }

            // Filters
            val filters = listOf("ALL", "PENDING", "COMPLETED", "CRITICAL")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) JervesCyanContainer else JervesSurfaceDark)
                            .border(
                                1.dp,
                                if (isSelected) JervesCyanPrimary else JervesSurfaceBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) JervesCyanPrimary else JervesTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Task list
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "[ NO DIRECTIVES FOUND ]",
                            color = JervesTextTertiary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + to issue a new tactical objective.",
                            color = JervesTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        DirectiveCard(
                            task = task,
                            onToggle = { viewModel.toggleTask(task) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddDirectiveDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, cat, prio, due ->
                viewModel.addTask(title, desc, cat, prio, due)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DirectiveStat(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = JervesTextTertiary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun DirectiveCard(
    task: TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val prioColor = when (task.priority) {
        "CRITICAL" -> JervesCrimson
        "HIGH" -> JervesAmber
        "NORMAL" -> JervesCyanPrimary
        else -> JervesTextTertiary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(JervesSurfaceDark)
            .border(
                1.dp,
                if (task.isCompleted) JervesSurfaceBorder else JervesSurfaceBorder.copy(alpha = 0.8f),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onToggle)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onToggle,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = "Toggle status",
                tint = if (task.isCompleted) JervesNeonGreen else JervesCyanPrimary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "[${task.category}]",
                    color = JervesTextTertiary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = task.priority,
                    color = prioColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = task.title,
                color = if (task.isCompleted) JervesTextTertiary else JervesTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = task.description,
                    color = JervesTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            if (task.dueDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "TARGET: ${task.dueDate}",
                    color = JervesCyanPrimary.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Directive",
                tint = JervesTextTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AddDirectiveDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, TaskCategory, TaskPriority, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.DIRECTIVE) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.HIGH) }
    var dueDate by remember { mutableStateOf("Immediate") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JervesSurfaceDark,
        title = {
            Text(
                text = "NEW DIRECTIVE // PROTOCOL",
                color = JervesCyanPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Directive Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JervesCyanPrimary,
                        unfocusedBorderColor = JervesSurfaceBorder,
                        focusedTextColor = JervesTextPrimary,
                        unfocusedTextColor = JervesTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details / Execution notes") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JervesCyanPrimary,
                        unfocusedBorderColor = JervesSurfaceBorder,
                        focusedTextColor = JervesTextPrimary,
                        unfocusedTextColor = JervesTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Target Schedule (e.g. 18:00, Tomorrow)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JervesCyanPrimary,
                        unfocusedBorderColor = JervesSurfaceBorder,
                        focusedTextColor = JervesTextPrimary,
                        unfocusedTextColor = JervesTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Priority Level:",
                    color = JervesTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskPriority.entries.forEach { prio ->
                        val isSelected = selectedPriority == prio
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
                                .clickable { selectedPriority = prio }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prio.name.take(4),
                                color = if (isSelected) JervesCyanPrimary else JervesTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description, selectedCategory, selectedPriority, dueDate)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JervesCyanPrimary,
                    contentColor = Color(0xFF001F29)
                )
            ) {
                Text("ISSUE DIRECTIVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = JervesTextSecondary)
            }
        }
    )
}
