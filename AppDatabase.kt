package com.example.a220960_sirnelson_lab01

import android.content.Context
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

// --- TYPE CONVERTERS ---
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromSchedule(value: String): List<List<Pair<String, Int>>> {
        val listType = object : TypeToken<List<List<Pair<String, Int>>>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toSchedule(list: List<List<Pair<String, Int>>>): String {
        return gson.toJson(list)
    }
}

// --- ENTITIES ---

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val task: String,
    val isDone: Boolean = false
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val colorHex: Int
)

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val authors: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

@Entity(tableName = "timetable")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: String,
    val studentName: String,
    val className: String,
    val schoolName: String,
    val timeSlots: List<String>,
    val schedule: List<List<Pair<String, Int>>>
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val score: Float
)

// --- DAO ---

@Dao
interface AppDao {
    @Query("SELECT * FROM todos") fun getAllTodos(): Flow<List<TodoEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTodo(todo: TodoEntity)
    @Delete suspend fun deleteTodo(todo: TodoEntity)

    @Query("SELECT * FROM notes") fun getAllNotes(): Flow<List<NoteEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertNote(note: NoteEntity)
    @Delete suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT * FROM books ORDER BY timestamp DESC") fun getAllBooks(): Flow<List<BookEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertBook(book: BookEntity)
    @Delete suspend fun deleteBook(book: BookEntity)

    @Query("SELECT * FROM quizzes") fun getAllQuizzes(): Flow<List<QuizEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertQuiz(quiz: QuizEntity)
    @Delete suspend fun deleteQuiz(quiz: QuizEntity)

    @Query("SELECT * FROM timetable") fun getAllTimetables(): Flow<List<TimetableEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTimetable(t: TimetableEntity)
    @Delete suspend fun deleteTimetable(t: TimetableEntity)

    @Query("SELECT * FROM exams") fun getAllExams(): Flow<List<ExamEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertExam(exam: ExamEntity)
    @Delete suspend fun deleteExam(exam: ExamEntity)
}

// --- DATABASE ---

@Database(
    entities = [TodoEntity::class, NoteEntity::class, BookEntity::class, QuizEntity::class, TimetableEntity::class, ExamEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "edu_hub_db")
                    .fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
