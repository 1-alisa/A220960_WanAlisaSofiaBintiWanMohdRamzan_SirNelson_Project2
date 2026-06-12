package com.example.a220960_sirnelson_lab01

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagramScreen(navController: NavController, viewModel: AcademicViewModel) {
    val examList by viewModel.exams.collectAsStateWithLifecycle(initialValue = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<ExamEntity?>(null) }

    val averageScore by remember(examList) {
        derivedStateOf {
            if (examList.isNotEmpty()) examList.map { it.score }.average() else 0.0
        }
    }

    val statusColor = when {
        averageScore >= 80 -> Color(0xFF00C853)
        averageScore >= 50 -> Color(0xFFF8B72C)
        else -> Color(0xFFFF5252)
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A1A3A), Color.Black)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "MY ACADEMIC RECORD",
                        color = Color(0xFFF8B72C),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color(0xFFF8B72C)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedRecord = null
                    showDialog = true
                },
                containerColor = Color(0xFFF8B72C)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 20.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("OVERALL AVERAGE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${String.format("%.1f", averageScore)}%",
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            StatusBadge(
                                label = if (averageScore >= 50) "PASSING" else "BELOW AVG",
                                color = statusColor
                            )
                        }
                    }
                }

                item {
                    Text("PERFORMANCE CHART", color = Color(0xFFF8B72C), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(top = 8.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        if (examList.isEmpty()) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("No records found", color = Color.DarkGray)
                            }
                        } else {
                            Row(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                examList.takeLast(6).forEach { record ->
                                    ScoreBar(label = record.subject, score = record.score)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("SUBJECT BREAKDOWN", color = Color(0xFFF8B72C), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }

                items(examList) { record ->
                    ExamResultCard(
                        record = record,
                        onDelete = { viewModel.deleteExam(record) },
                        onEdit = {
                            selectedRecord = record
                            showDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        RecordDialog(
            existingRecord = selectedRecord,
            onDismiss = { showDialog = false },
            onConfirm = { subject, score ->
                val exam = ExamEntity(
                    id = selectedRecord?.id ?: 0,
                    subject = subject,
                    score = score
                )
                viewModel.addExam(exam)
                showDialog = false
            }
        )
    }
}

@Composable
fun StatusBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ScoreBar(label: String, score: Float) {
    val heightFactor = (score / 100f).coerceIn(0f, 1f)
    val animatedHeight by animateFloatAsState(targetValue = heightFactor, label = "BarHeight")

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = "${score.toInt()}",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .width(18.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(animatedHeight.coerceAtLeast(0.05f))
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFF8B72C), Color(0xFFFF9100))
                        )
                    )
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = label.take(3).uppercase(),
            color = Color.Gray,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultCard(record: ExamEntity, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f)),
        shape = RoundedCornerShape(16.dp),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(record.subject, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Score: ${record.score.toInt()}/100", color = Color.Gray, fontSize = 13.sp)
            }

            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp).padding(end = 8.dp)
            )

            StatusBadge(
                label = if (record.score >= 40) "PASS" else "FAIL",
                color = if (record.score >= 40) Color(0xFF00C853) else Color(0xFFFF5252)
            )

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun RecordDialog(existingRecord: ExamEntity?, onDismiss: () -> Unit, onConfirm: (String, Float) -> Unit) {
    var subjectInput by remember { mutableStateOf(existingRecord?.subject ?: "") }
    var scoreInput by remember { mutableStateOf(existingRecord?.score?.toInt()?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = {
            Text(
                if (existingRecord == null) "New Result" else "Edit Result",
                color = Color(0xFFF8B72C)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = subjectInput,
                    onValueChange = { subjectInput = it },
                    label = { Text("Subject Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF8B72C)
                    )
                )
                OutlinedTextField(
                    value = scoreInput,
                    onValueChange = { if (it.length <= 3) scoreInput = it },
                    label = { Text("Score (0-100)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF8B72C)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val score = scoreInput.toFloatOrNull() ?: 0f
                    if (subjectInput.isNotBlank() && score in 0f..100f) {
                        onConfirm(subjectInput, score)
                    }
                }
            ) {
                Text(if (existingRecord == null) "ADD" else "UPDATE", color = Color(0xFFF8B72C))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        }
    )
}
