package com.example.a220960_sirnelson_lab01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.a220960_sirnelson_lab01.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    private val todoViewModel by viewModels<TodoViewModel>()
    private val notesViewModel by viewModels<NotesViewModel>()
    private val quizViewModel by viewModels<QuizViewModel>()
    private val timerViewModel by viewModels<TimerViewModel>()
    private val timetableViewModel by viewModels<TimetableViewModel>()
    private val userViewModel by viewModels<UserViewModel>()
    private val bookViewModel by viewModels<BookViewModel>()
    private val academicViewModel by viewModels<AcademicViewModel>()
    private val cloudViewModel by viewModels<CloudViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                Homepage(
                    todoViewModel,
                    notesViewModel,
                    quizViewModel,
                    timerViewModel,
                    timetableViewModel,
                    userViewModel,
                    bookViewModel,
                    academicViewModel,
                    cloudViewModel
                )
            }
        }
    }
}

@Composable
fun Homepage(
    todoViewModel: TodoViewModel,
    notesViewModel: NotesViewModel,
    quizViewModel: QuizViewModel,
    timerViewModel: TimerViewModel,
    timetableViewModel: TimetableViewModel,
    userViewModel: UserViewModel,
    bookViewModel: BookViewModel,
    academicViewModel: AcademicViewModel,
    cloudViewModel: CloudViewModel
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("main") { MainScreen(navController, todoViewModel, userViewModel) }
            composable("settings") { SettingsScreen(userViewModel, cloudViewModel) }
            composable("notes") { NotesScreen(navController, notesViewModel) }
            composable("timer") { TimerScreen(navController, timerViewModel) }
            composable("quizzy") { QuizzyScreen(navController, quizViewModel) }
            composable("timetable") { TimetableScreen(navController, timetableViewModel) }
            composable("diagram") { DiagramScreen(navController, academicViewModel) }
            composable("camera") { CameraScreen(navController) }
            composable("books") { BookScreen(navController, bookViewModel) }
        }
    }
}

@Composable
fun MainScreen(navController: NavController, viewModel: TodoViewModel, userViewModel: UserViewModel) {
    val name = userViewModel.userName

    val currentDate = remember {
        val sdf = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault())
        sdf.format(java.util.Date())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(Color(0xFF0A1A3A), Color.Black)))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentDate,
                color = Color(0xFFF8B72C).copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Welcome, ${if (name.isEmpty()) "Student" else name}!",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Text(
            text = "LEARNING IS A PRIVILEGE",
            color = Color(0xFFF8B72C),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier
                .padding(start = 24.dp, top = 4.dp, end = 24.dp)
                .align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(32.dp))

        WeeklyCalendar()

        Spacer(modifier = Modifier.height(28.dp))

        InteractiveTodoList(viewModel)

        Spacer(modifier = Modifier.height(28.dp))

        MenuGrid(navController)
    }
}

@Composable
fun InteractiveTodoList(viewModel: TodoViewModel) {
    val taskList by viewModel.todoList.collectAsStateWithLifecycle()
    val newTaskText = viewModel.newTaskText

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(16.dp)
    ) {
        Text(
            text = "TODAY'S TASKS",
            color = Color(0xFFF8B72C),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = newTaskText,
            onValueChange = { viewModel.newTaskText = it },
            placeholder = { Text("What needs to be done?", color = Color.Gray, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
            trailingIcon = {
                IconButton(onClick = { viewModel.addTask() }) {
                    Icon(Icons.Default.Add, null, tint = Color(0xFFF8B72C))
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Black.copy(0.3f),
                unfocusedContainerColor = Color.Black.copy(0.3f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (taskList.isEmpty()) {
            Text("No tasks yet.", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally), fontSize = 12.sp)
        } else {
            taskList.forEach { todo ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.toggleTodo(todo) }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            if (todo.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            null,
                            tint = if (todo.isDone) Color(0xFFF8B72C) else Color.Gray
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        todo.task,
                        color = if (todo.isDone) Color.Gray else Color.White,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp
                    )
                    IconButton(onClick = { viewModel.deleteTodo(todo) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MenuGrid(navController: NavController) {
    val menus = listOf(
        Triple("QUIZZY", Icons.Default.Quiz, "quizzy"),
        Triple("NOTES", Icons.AutoMirrored.Filled.Note, "notes"),
        Triple("TIMETABLE", Icons.Default.Schedule, "timetable"),
        Triple("ACADEMIA", Icons.Default.Analytics, "diagram"),
        Triple("BOOKS", Icons.Default.MenuBook, "books")
    )

    Column(Modifier.padding(8.dp)) {
        menus.chunked(2).forEach { rowMenus ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowMenus.forEach { menu ->
                    PressedButton(
                        onClick = { navController.navigate(menu.third) },
                        text = menu.first,
                        icon = menu.second,
                        modifier = Modifier.weight(1f).padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PressedButton(onClick: () -> Unit, text: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .height(100.dp)
            .shadow(8.dp, RoundedCornerShape(15.dp))
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0xFFF8B72C))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = Color.Black) {
        val items = listOf(
            Triple("Home", Icons.Filled.Home, "main"),
            Triple("Timer", Icons.Filled.Timer, "timer"),
            Triple("SMART SCAN", Icons.Default.CameraAlt, "camera"),
            Triple("Settings", Icons.Filled.Settings, "settings")
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.second, contentDescription = item.first) },
                selected = currentRoute == item.third,
                onClick = {
                    navController.navigate(item.third) {
                        popUpTo("main") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Yellow,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.White.copy(0.1f)
                )
            )
        }
    }
}

@Composable
fun WeeklyCalendar() {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val calendar = java.util.Calendar.getInstance()
    val todayIndex = when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
        java.util.Calendar.MONDAY -> 0
        java.util.Calendar.TUESDAY -> 1
        java.util.Calendar.WEDNESDAY -> 2
        java.util.Calendar.THURSDAY -> 3
        java.util.Calendar.FRIDAY -> 4
        java.util.Calendar.SATURDAY -> 5
        java.util.Calendar.SUNDAY -> 6
        else -> -1
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEachIndexed { index, day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = day,
                    color = if (index == todayIndex) Color(0xFFF8B72C) else Color.White,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .size(30.dp)
                        .border(
                            1.dp,
                            if (index == todayIndex) Color(0xFFF8B72C) else Color.Gray,
                            CircleShape
                        )
                        .background(
                            if (index == todayIndex) Color(0xFFF8B72C).copy(alpha = 0.2f)
                            else Color.Transparent,
                            CircleShape
                        )
                )
            }
        }
    }
}