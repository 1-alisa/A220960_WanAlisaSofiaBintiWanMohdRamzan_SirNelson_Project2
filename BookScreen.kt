package com.example.a220960_sirnelson_lab01

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@Composable
fun BookScreen(navController: NavController, viewModel: BookViewModel) {
    val context = LocalContext.current
    val savedBooks by viewModel.savedBooks.collectAsStateWithLifecycle(initialValue = emptyList())
    val advice = viewModel.adviceText
    val isLoading = viewModel.isLoading

    var titleInput by remember { mutableStateOf("") }
    var authorInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var editingBook: BookEntity? by remember { mutableStateOf(null) }

    val backgroundGradient = Brush.verticalGradient(colors = listOf(Color(0xFF0A1A3A), Color.Black))

    // UNTUK SCROLL PAGE
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. TOP BAR
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFFF8B72C))
                }
                Text("STUDENT RESOURCES", color = Color(0xFFF8B72C), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
        }

        // 2. BAHAGIAN API: DAILY MOTIVATION
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = MaterialTheme.shapes.large
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DAILY MOTIVATION", color = Color(0xFFF8B72C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (isLoading) {
                        CircularProgressIndicator(color = Color(0xFFF8B72C))
                    } else {
                        Text(
                            text = "\"$advice\"",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.fetchMotivation() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8B72C))
                    ) {
                        Text("GET NEW ADVICE", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        item {
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color.White.copy(0.1f))
        }

        // 3. FORM MANUAL
        item {
            Text(
                if (editingBook == null) "ADD STUDY RESOURCE" else "EDIT RESOURCE",
                color = Color(0xFFF8B72C),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = titleInput, onValueChange = { titleInput = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            OutlinedTextField(value = authorInput, onValueChange = { authorInput = it }, label = { Text("Author/Source") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            OutlinedTextField(value = descInput, onValueChange = { descInput = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))

            Button(
                onClick = {
                    if (titleInput.isNotBlank()) {
                        if (editingBook == null) viewModel.saveBook(titleInput, authorInput, descInput)
                        else viewModel.updateBook(editingBook!!.copy(title = titleInput, authors = authorInput, description = descInput))
                        editingBook = null; titleInput = ""; authorInput = ""; descInput = ""; Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8B72C))
            ) {
                Text(if (editingBook == null) "SAVE TO COLLECTION" else "UPDATE RESOURCE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }

        // 4. LIST LOCAL ROOM (HEADLINE)
        item {
            Text("MY SAVED RESOURCES", color = Color(0xFFF8B72C), fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
            Spacer(Modifier.height(8.dp))
        }

        // 5. LIST ITEMS (Dynamic)
        items(savedBooks) { book ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.05f))
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(book.title, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(book.authors, color = Color.Gray, fontSize = 12.sp)
                    }
                    IconButton(onClick = {
                        editingBook = book
                        titleInput = book.title
                        authorInput = book.authors
                        descInput = book.description
                    }) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                    IconButton(onClick = { viewModel.deleteBook(book) }) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                    }
                }
            }
        }

        // Ruang kosong kt bawah sekali
        item {
            Spacer(Modifier.height(32.dp))
        }
    }
}