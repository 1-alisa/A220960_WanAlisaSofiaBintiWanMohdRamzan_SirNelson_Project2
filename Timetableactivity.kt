package com.example.a220960_sirnelson_lab01

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(navController: NavController, viewModel: TimetableViewModel) {
    val profiles by viewModel.timetables.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentId by viewModel.currentProfileId
    val profile = profiles.find { it.profileId == currentId }

    val presetColors = listOf(
        Color.White, Color(0xFFBBDEFB), Color(0xFFC8E6C9),
        Color(0xFFFFECB3), Color(0xFFF8BBD0), Color(0xFFE1BEE7), Color(0xFFFFCDD2)
    )

    var showEditDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf("") }
    var selectedDayIdx by remember { mutableIntStateOf(0) }
    var selectedSlotIdx by remember { mutableIntStateOf(0) }

    var input1 by remember { mutableStateOf("") }
    var input2 by remember { mutableStateOf("") }
    var input3 by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(presetColors[0]) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("TIMETABLE", color = Color(0xFFF8B72C), fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    if (profile != null) {
                        IconButton(onClick = { viewModel.deleteTimetable(profile) }) {
                            Icon(Icons.Default.Delete, "Delete Profile", tint = Color.Red)
                        }
                    }
                    IconButton(onClick = {
                        input1 = ""; dialogType = "ADD PROFILE"; showEditDialog = true
                    }) { Icon(Icons.Default.Add, null, tint = Color(0xFFF8B72C)) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF0A1A3A))
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Brush.verticalGradient(listOf(Color(0xFF0A1A3A), Color.Black)))) {

            if (profiles.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(100.dp), tint = Color.White.copy(0.1f))
                    Text("CLICK + TO ADD THE TIMETABLE", color = Color.White.copy(0.4f))
                }
            } else {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())) {

                    // Profile Switcher
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        profiles.forEach { p ->
                            FilterChip(
                                selected = currentId == p.profileId,
                                onClick = { viewModel.currentProfileId.value = p.profileId },
                                label = { Text(p.profileId) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFF8B72C), selectedLabelColor = Color.Black, labelColor = Color.White)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (profile != null) {
                        Card(modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                input1 = profile.studentName; input2 = profile.className; input3 = profile.schoolName
                                dialogType = "PROFILE INFO"; showEditDialog = true
                            },
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.1f)),
                            border = BorderStroke(1.dp, Color(0xFFF8B72C).copy(0.5f))) {
                            Column(Modifier.padding(16.dp)) {
                                Text("STUDENT NAME: ${profile.studentName}", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("CLASS: ${profile.className} | ${profile.schoolName}", color = Color(0xFFF8B72C), fontSize = 12.sp)
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Column(Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFF8B72C), RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)) {

                            // Header Masa
                            Row(Modifier.fillMaxWidth().background(Color(0xFF2C3E50))) {
                                Box(Modifier.weight(1.5f).padding(8.dp), contentAlignment = Alignment.Center) {
                                    Text("DAYS", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                profile.timeSlots.forEachIndexed { i, time ->
                                    Box(Modifier
                                        .weight(1f)
                                        .border(0.5.dp, Color.White.copy(0.1f))
                                        .clickable { selectedSlotIdx = i; input1 = time; dialogType = "TIME"; showEditDialog = true }
                                        .padding(vertical = 8.dp, horizontal = 2.dp),
                                        contentAlignment = Alignment.Center) {
                                        Text(text = time, color = Color(0xFFF8B72C), fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                    }
                                }
                            }

                            // Rows
                            val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                            profile.schedule.forEachIndexed { dIdx, row ->
                                if (dIdx < days.size) {
                                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                        Box(Modifier.weight(1.5f).fillMaxHeight().background(Color(0xFFF8B72C)).border(0.5.dp, Color.Black.copy(0.1f)).padding(4.dp), contentAlignment = Alignment.Center) {
                                            Text(days[dIdx], fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.Black)
                                        }
                                        row.forEachIndexed { sIdx, (sub, col) ->
                                            Box(Modifier.weight(1f).fillMaxHeight().background(Color(col)).border(0.5.dp, Color.Black.copy(0.05f)).clickable {
                                                selectedDayIdx = dIdx; selectedSlotIdx = sIdx; input1 = sub; selectedColor = Color(col); dialogType = "SUBJECT"; showEditDialog = true
                                            }.padding(2.dp), contentAlignment = Alignment.Center) {
                                                Text(text = sub, fontSize = 8.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(dialogType) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = input1, onValueChange = { input1 = it }, label = { Text("Input") })
                    if (dialogType == "PROFILE INFO") {
                        OutlinedTextField(value = input2, onValueChange = { input2 = it }, label = { Text("CLASS") })
                        OutlinedTextField(value = input3, onValueChange = { input3 = it }, label = { Text("SCHOOL") })
                    }
                    if (dialogType == "SUBJECT") {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            presetColors.forEach { color ->
                                Box(Modifier.size(30.dp).clip(CircleShape).background(color).border(if (selectedColor == color) 2.dp else 0.dp, Color.Black, CircleShape).clickable { selectedColor = color })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (profile != null) {
                        val p = profile
                        when (dialogType) {
                            "SUBJECT" -> {
                                val newSched = p.schedule.map { it.toMutableList() }
                                newSched[selectedDayIdx][selectedSlotIdx] = input1 to selectedColor.toArgb()
                                viewModel.saveTimetable(p.copy(schedule = newSched))
                            }
                            "TIME" -> {
                                val newTimes = p.timeSlots.toMutableList()
                                newTimes[selectedSlotIdx] = input1
                                viewModel.saveTimetable(p.copy(timeSlots = newTimes))
                            }
                            "PROFILE INFO" -> {
                                viewModel.saveTimetable(p.copy(studentName = input1, className = input2, schoolName = input3))
                            }
                            "ADD PROFILE" -> {
                                if (input1.isNotBlank()) {
                                    val newP = TimetableEntity(
                                        profileId = input1,
                                        studentName = input1.uppercase(),
                                        className = "CLASS",
                                        schoolName = "SCHOOL",
                                        timeSlots = List(10) { (it + 1).toString() },
                                        schedule = List(7) { List(10) { "" to Color.White.toArgb() } }
                                    )
                                    viewModel.saveTimetable(newP)
                                    viewModel.currentProfileId.value = input1
                                }
                            }
                        }
                    } else if (dialogType == "ADD PROFILE" && input1.isNotBlank()) {
                        val newP = TimetableEntity(
                            profileId = input1,
                            studentName = input1.uppercase(),
                            className = "CLASS",
                            schoolName = "SCHOOL",
                            timeSlots = List(10) { (it + 1).toString() },
                            schedule = List(7) { List(10) { "" to Color.White.toArgb() } }
                        )
                        viewModel.saveTimetable(newP)
                        viewModel.currentProfileId.value = input1
                    }
                    showEditDialog = false
                }) { Text("SAVE") }
            }
        )
    }
}
