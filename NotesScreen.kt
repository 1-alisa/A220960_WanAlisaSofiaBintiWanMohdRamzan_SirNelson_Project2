package com.example.a220960_sirnelson_lab01

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    navController: NavController,
    viewModel: NotesViewModel = viewModel()
) {
    val context = LocalContext.current
    val notesList by viewModel.allNotes.collectAsStateWithLifecycle(initialValue = emptyList())

    var showForm by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    val colors = listOf(Color(0xFFFF9E9E), Color(0xFF91F48F), Color(0xFFFFF599), Color(0xFF9EFFFF), Color(0xB3B69EFF))
    var selectedColor by remember { mutableStateOf(colors[0]) }

    var editingNoteId by remember { mutableIntStateOf(-1) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A1A3A), Color.Black)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Notes", color = Color(0xFFF8B72C), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFFF8B72C))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A1A3A))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingNoteId = -1
                    title = ""
                    content = ""
                    showForm = !showForm
                },
                containerColor = Color(0xFFF8B72C)
            ) {
                Icon(if (showForm) Icons.Default.Close else Icons.Default.Add, contentDescription = "Toggle Form", tint = Color.Black)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            AnimatedVisibility(visible = showForm) {
                NoteFormCard(
                    title = title,
                    onTitleChange = { title = it },
                    content = content,
                    onContentChange = { content = it },
                    colors = colors,
                    selectedColor = selectedColor,
                    onColorChange = { selectedColor = it },
                    isEditing = editingNoteId != -1,
                    onSave = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            val note = NoteEntity(
                                id = if (editingNoteId == -1) 0 else editingNoteId,
                                title = title,
                                content = content,
                                colorHex = selectedColor.toArgb()
                            )
                            viewModel.insertNote(note)
                            Toast.makeText(context, if (editingNoteId == -1) "Note Saved!" else "Note Updated!", Toast.LENGTH_SHORT).show()

                            title = ""
                            content = ""
                            showForm = false
                            editingNoteId = -1
                        } else {
                            Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            if (notesList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No notes found. Click + to add one.", color = Color.Gray, textAlign = TextAlign.Center)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notesList) { note ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(note.colorHex)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(note.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                                Spacer(Modifier.height(6.dp))
                                Text(note.content, fontSize = 14.sp, color = Color.DarkGray, maxLines = 4)
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            title = note.title
                                            content = note.content
                                            selectedColor = Color(note.colorHex)
                                            editingNoteId = note.id
                                            showForm = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, "Edit", tint = Color.Black.copy(0.6f))
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteNote(note)
                                            Toast.makeText(context, "Note Deleted!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, "Delete", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteFormCard(
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    colors: List<Color>,
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
    isEditing: Boolean,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            Spacer(Modifier.height(8.dp))
            TextField(
                value = content,
                onValueChange = onContentChange,
                placeholder = { Text("Note content...") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(25.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onColorChange(color) }
                            .border(if (selectedColor == color) 2.dp else 0.dp, Color.White, CircleShape)
                    )
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = if (isEditing) Color.White else Color(0xFFF8B72C))) {
                    Text(if (isEditing) "UPDATE" else "SAVE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
