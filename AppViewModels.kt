package com.example.a220960_sirnelson_lab01

import android.app.Application
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// TODOVIEWMODEL
class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val todoList: StateFlow<List<TodoEntity>> = dao.getAllTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var newTaskText by mutableStateOf("")

    fun addTask() {
        if (newTaskText.isNotBlank()) {
            viewModelScope.launch {
                dao.insertTodo(TodoEntity(task = newTaskText))
                newTaskText = ""
            }
        }
    }

    fun toggleTodo(todo: TodoEntity) = viewModelScope.launch {
        dao.insertTodo(todo.copy(isDone = !todo.isDone))
    }

    fun deleteTodo(todo: TodoEntity) = viewModelScope.launch {
        dao.deleteTodo(todo)
    }
}

// --- NOTES VIEWMODEL ---
class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val allNotes: StateFlow<List<NoteEntity>> = dao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertNote(note: NoteEntity) = viewModelScope.launch { dao.insertNote(note) }
    fun deleteNote(note: NoteEntity) = viewModelScope.launch { dao.deleteNote(note) }
}

// --- BOOK VIEWMODEL ---
class BookViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val savedBooks: StateFlow<List<BookEntity>> = dao.getAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var adviceText by mutableStateOf("Click the button to get motivation!")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var apiErrorMessage by mutableStateOf<String?>(null)
        private set

    fun fetchMotivation() {
        viewModelScope.launch {
            isLoading = true
            apiErrorMessage = null
            try {
                val response = RetrofitClient.apiService.getRandomAdvice()
                if (response.isSuccessful && response.body() != null) {
                    adviceText = response.body()?.slip?.advice ?: "Keep working hard!"
                } else {
                    apiErrorMessage = "Service busy, please try again."
                }
            } catch (e: Exception) {
                apiErrorMessage = "Check your internet connection."
            } finally {
                isLoading = false
            }
        }
    }

    fun saveBook(title: String, authors: String, description: String) {
        viewModelScope.launch {
            dao.insertBook(BookEntity(title = title, authors = authors, description = description))
        }
    }

    fun updateBook(book: BookEntity) = viewModelScope.launch { dao.insertBook(book) }
    fun deleteBook(book: BookEntity) = viewModelScope.launch { dao.deleteBook(book) }
}

// --- QUIZ VIEWMODEL ---
class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val questions: StateFlow<List<QuizEntity>> = dao.getAllQuizzes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addQuestion(q: QuizEntity) = viewModelScope.launch { dao.insertQuiz(q) }
    fun deleteQuestion(q: QuizEntity) = viewModelScope.launch { dao.deleteQuiz(q) }
}

// --- TIMETABLE VIEWMODEL ---
class TimetableViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val timetables: StateFlow<List<TimetableEntity>> = dao.getAllTimetables()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentProfileId = mutableStateOf("")

    fun saveTimetable(t: TimetableEntity) = viewModelScope.launch { dao.insertTimetable(t) }
    fun deleteTimetable(t: TimetableEntity) = viewModelScope.launch { dao.deleteTimetable(t) }
}

// --- CLOUD SYNC VIEWMODEL
class CloudViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    private val firestore = FirebaseFirestore.getInstance()

    var isSyncing by mutableStateOf(false)
        private set

    fun syncAllDataToCloud(onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            isSyncing = true
            try {
                // 1. Sync Todos
                val todos = dao.getAllTodos().first()
                todos.forEach {
                    firestore.collection("todos").document("todo_${it.id}")
                        .set(mapOf("task" to it.task, "isDone" to it.isDone)).await()
                }

                // 2. Sync Notes
                val notes = dao.getAllNotes().first()
                notes.forEach {
                    firestore.collection("notes").document("note_${it.id}")
                        .set(mapOf("title" to it.title, "content" to it.content, "colorHex" to it.colorHex)).await()
                }

                // 3. Sync Exams
                val exams = dao.getAllExams().first()
                exams.forEach {
                    firestore.collection("exams").document("exam_${it.id}")
                        .set(mapOf("subject" to it.subject, "score" to it.score)).await()
                }

                // 4. Sync Timetables
                val timetables = dao.getAllTimetables().first()
                timetables.forEach { t ->
                    val scheduleMap = t.schedule.mapIndexed { index, row ->
                        "day_$index" to row.map { mapOf("sub" to it.first, "col" to it.second) }
                    }.toMap()

                    val data = mapOf(
                        "profileId" to t.profileId,
                        "studentName" to t.studentName,
                        "className" to t.className,
                        "schoolName" to t.schoolName,
                        "timeSlots" to t.timeSlots,
                        "schedule" to scheduleMap
                    )
                    firestore.collection("timetables").document("timetable_${t.id}").set(data).await()
                }

                onComplete(null) // Berjaya
            } catch (e: Exception) {
                Log.e("CloudSync", "Error: ${e.message}")
                onComplete(e.localizedMessage ?: "Unknown Error") // Pulangkan mesej ralat
            } finally {
                isSyncing = false
            }
        }
    }
}

// --- TIMER VIEWMODEL ---
class TimerViewModel : ViewModel() {
    var timeLeftMs by mutableLongStateOf(25 * 60 * 1000L)
    var isRunning by mutableStateOf(false)
    var isStudyMode by mutableStateOf(true)

    fun tick() {
        if (isRunning && timeLeftMs > 0) timeLeftMs -= 1000L
        else if (timeLeftMs <= 0L) isRunning = false
    }

    fun toggleRunning() { isRunning = !isRunning }
    fun resetTimer() {
        isRunning = false
        timeLeftMs = if (isStudyMode) 25 * 60 * 1000L else 5 * 60 * 1000L
    }
    fun changeMode(isStudy: Boolean) {
        isStudyMode = isStudy
        resetTimer()
    }
}

// --- USER VIEWMODEL ---
class UserViewModel : ViewModel() {
    var userName by mutableStateOf("")
}

// --- ACADEMIC VIEWMODEL ---
class AcademicViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()
    val exams: StateFlow<List<ExamEntity>> = dao.getAllExams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addExam(exam: ExamEntity) = viewModelScope.launch { dao.insertExam(exam) }
    fun deleteExam(exam: ExamEntity) = viewModelScope.launch { dao.deleteExam(exam) }
}
