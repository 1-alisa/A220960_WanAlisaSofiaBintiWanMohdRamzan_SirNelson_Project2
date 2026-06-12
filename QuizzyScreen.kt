package com.example.a220960_sirnelson_lab01

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizzyScreen(
    navController: NavController,
    viewModel: QuizViewModel
) {
    val questions by viewModel.questions.collectAsStateWithLifecycle(initialValue = emptyList())
    var isPlayMode by rememberSaveable { mutableStateOf(false) }

    var questionInput by rememberSaveable { mutableStateOf("") }
    var correctOptionIndex by rememberSaveable { mutableIntStateOf(0) }
    var editingQuiz: QuizEntity? by remember { mutableStateOf(null) }

    val optionsInput = remember { mutableStateListOf("", "") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("QUIZ CREATOR", color = Color(0xFFF8B72C), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    if (questions.isNotEmpty()) {
                        IconButton(onClick = { isPlayMode = !isPlayMode }) {
                            Icon(
                                if (isPlayMode) Icons.Default.Edit else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFFF8B72C)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF0A1A3A))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFF0A1A3A), Color.Black)))
                .padding(12.dp)
        ) {
            if (isPlayMode) {
                PlayQuizView(questions)
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = if (editingQuiz == null) "New Question" else "Edit Question",
                            color = Color(0xFFF8B72C),
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = questionInput,
                            onValueChange = { questionInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Question", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFF8B72C)
                            )
                        )

                        Spacer(Modifier.height(8.dp))

                        optionsInput.forEachIndexed { index, text ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = correctOptionIndex == index,
                                    onClick = { correctOptionIndex = index },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFF8B72C))
                                )
                                OutlinedTextField(
                                    value = text,
                                    onValueChange = { optionsInput[index] = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Option ${index + 1}") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                if (optionsInput.size > 2) {
                                    IconButton(onClick = { optionsInput.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                                    }
                                }
                            }
                        }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { optionsInput.add("") }) {
                                Text("+ ADD OPTION", color = Color(0xFFF8B72C))
                            }
                            Button(
                                onClick = {
                                    if (questionInput.isNotBlank() && optionsInput.all { it.isNotBlank() }) {
                                        val newQuiz = QuizEntity(
                                            id = editingQuiz?.id ?: 0,
                                            question = questionInput,
                                            options = optionsInput.toList(),
                                            correctIndex = correctOptionIndex
                                        )
                                        viewModel.addQuestion(newQuiz)
                                        
                                        questionInput = ""
                                        optionsInput.clear()
                                        optionsInput.addAll(listOf("", ""))
                                        correctOptionIndex = 0
                                        editingQuiz = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8B72C))
                            ) {
                                Text(if (editingQuiz == null) "SAVE" else "UPDATE", color = Color.Black)
                            }
                        }
                    }
                }

                Text("Questions List", color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    questions.forEach { item ->
                        ExpandableQuestionCard(
                            item = item,
                            onDelete = { viewModel.deleteQuestion(item) },
                            onEdit = {
                                editingQuiz = item
                                questionInput = item.question
                                optionsInput.clear()
                                optionsInput.addAll(item.options)
                                correctOptionIndex = item.correctIndex
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableQuestionCard(item: QuizEntity, onDelete: () -> Unit, onEdit: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.05f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.question, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", tint = Color.LightGray) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red) }
            }
            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(0.1f))
                item.options.forEachIndexed { i, o ->
                    Text(text = "${i + 1}. $o ${if (i == item.correctIndex) "✔" else ""}", color = if (i == item.correctIndex) Color.Green else Color.Gray, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
fun PlayQuizView(questions: List<QuizEntity>) {
    var currentIdx by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var finished by remember { mutableStateOf(false) }

    if (finished) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFF8B72C), modifier = Modifier.size(80.dp))
            Text("SCORE: $score / ${questions.size}", color = Color(0xFFF8B72C), fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Button(onClick = { currentIdx = 0; score = 0; finished = false; selectedAnswer = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8B72C))) { Text("RETRY", color = Color.Black) }
        }
    } else if (questions.isNotEmpty()) {
        val q = questions[currentIdx]
        Column {
            LinearProgressIndicator(progress = { (currentIdx + 1).toFloat() / questions.size }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = Color(0xFFF8B72C), trackColor = Color.White.copy(0.1f))
            Spacer(Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))) {
                Text(q.question, Modifier.padding(24.dp), color = Color.White, fontSize = 22.sp)
            }
            Spacer(Modifier.height(24.dp))
            q.options.forEachIndexed { index, opt ->
                val containerColor = when {
                    selectedAnswer == null -> Color.White.copy(0.05f)
                    index == q.correctIndex -> Color.Green.copy(0.5f)
                    index == selectedAnswer -> Color.Red.copy(0.5f)
                    else -> Color.White.copy(0.05f)
                }
                Button(onClick = { if (selectedAnswer == null) { selectedAnswer = index; if (index == q.correctIndex) score++ } }, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = ButtonDefaults.buttonColors(containerColor = containerColor)) { Text(opt, color = Color.White) }
            }
            if (selectedAnswer != null) {
                Button(onClick = { if (currentIdx < questions.size - 1) { currentIdx++; selectedAnswer = null } else { finished = true } }, modifier = Modifier.align(Alignment.End).padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8B72C))) {
                    Text(if (currentIdx < questions.size - 1) "NEXT" else "FINISH", color = Color.Black)
                }
            }
        }
    }
}
