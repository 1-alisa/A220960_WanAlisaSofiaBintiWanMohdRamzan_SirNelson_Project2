package com.example.a220960_sirnelson_lab01

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.a220960_sirnelson_lab01.TimerViewModel

@Composable
fun TimerScreen(navController: NavController, viewModel: TimerViewModel) {

    LaunchedEffect(viewModel.isRunning) {
        while (viewModel.isRunning && viewModel.timeLeftMs > 0) {
            delay(1000L)
            viewModel.tick()
        }
    }

    val totalDuration = if (viewModel.isStudyMode) 25 * 60 * 1000L else 5 * 60 * 1000L
    val progress = viewModel.timeLeftMs.toFloat() / totalDuration.toFloat()

    val minutes = (viewModel.timeLeftMs / 1000) / 60
    val seconds = (viewModel.timeLeftMs / 1000) % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    val isLandscape = LocalConfiguration.current.orientation ==
            android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A1A3A), Color.Black)))
    ) {
        if (isLandscape) {
            LandscapeLayout(
                navController, timeText, progress,
                viewModel.isRunning, viewModel.isStudyMode,
                onPlayPause = { viewModel.toggleRunning() },
                onReset = { viewModel.resetTimer() },
                onStudyMode = { viewModel.changeMode(true) },
                onBreakMode = { viewModel.changeMode(false) }
            )
        } else {
            PortraitLayout(
                navController, timeText, progress,
                viewModel.isRunning, viewModel.isStudyMode,
                onPlayPause = { viewModel.toggleRunning() },
                onReset = { viewModel.resetTimer() },
                onStudyMode = { viewModel.changeMode(true) },
                onBreakMode = { viewModel.changeMode(false) }
            )
        }
    }
}

@Composable
fun PortraitLayout(
    navController: NavController, timeText: String, progress: Float,
    isRunning: Boolean, isStudyMode: Boolean,
    onPlayPause: () -> Unit, onReset: () -> Unit,
    onStudyMode: () -> Unit, onBreakMode: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar(navController)
        Spacer(Modifier.height(16.dp))
        ModeToggle(isStudyMode, onStudyMode, onBreakMode)
        Spacer(Modifier.height(40.dp))
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(260.dp)) { drawTimerRing(progress) }
            TimerText(timeText, isStudyMode)
        }
        Spacer(Modifier.height(48.dp))
        ControlButtons(isRunning, onPlayPause, onReset)
        Spacer(Modifier.height(40.dp))
        SessionInfoCard(isStudyMode)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun LandscapeLayout(
    navController: NavController, timeText: String, progress: Float,
    isRunning: Boolean, isStudyMode: Boolean,
    onPlayPause: () -> Unit, onReset: () -> Unit,
    onStudyMode: () -> Unit, onBreakMode: () -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
            Canvas(Modifier.size(180.dp)) { drawTimerRing(progress) }
            TimerText(timeText, isStudyMode, fontSize = 34.sp)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TopBar(navController)
            ModeToggle(isStudyMode, onStudyMode, onBreakMode)
            ControlButtons(isRunning, onPlayPause, onReset)
            SessionInfoCard(isStudyMode)
        }
    }
}

@Composable
fun TopBar(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Text("STUDY TIMER", color = Color(0xFFF8B72C), fontSize = 16.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
}

@Composable
fun ModeToggle(isStudyMode: Boolean, onStudy: () -> Unit, onBreak: () -> Unit) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.07f)).padding(4.dp)
    ) {
        ModeChip("STUDY", isStudyMode, onStudy)
        Spacer(Modifier.width(4.dp))
        ModeChip("BREAK", !isStudyMode, onBreak)
    }
}

@Composable
fun TimerText(timeText: String, isStudyMode: Boolean, fontSize: TextUnit = 58.sp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(timeText, color = Color.White, fontSize = fontSize,
            fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(
            if (isStudyMode) "Focus Session" else "Break Time",
            color = Color(0xFFF8B72C).copy(alpha = 0.8f), fontSize = 12.sp
        )
    }
}

@Composable
fun ControlButtons(isRunning: Boolean, onPlayPause: () -> Unit, onReset: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onReset,
            modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f))
        ) {
            Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(70.dp).shadow(8.dp, CircleShape).clip(CircleShape).background(Color(0xFFF8B72C))
        ) {
            Icon(
                if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                null, tint = Color(0xFF0A1A3A), modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFFF8B72C) else Color.Transparent,
            contentColor = if (selected) Color(0xFF0A1A3A) else Color.Gray
        ),
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun SessionInfoCard(isStudyMode: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f)).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        InfoItem("MODE", if (isStudyMode) "Study" else "Break")
        InfoItem("DURATION", if (isStudyMode) "25 min" else "5 min")
        InfoItem("METHOD", "Pomodoro")
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 10.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

fun DrawScope.drawTimerRing(progress: Float) {
    val strokeWidth = 12.dp.toPx()
    val arcSize = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth)
    val topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)

    drawArc(
        color = Color.White.copy(alpha = 0.08f),
        startAngle = -90f, sweepAngle = 360f, useCenter = false,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        topLeft = topLeft, size = arcSize
    )
    if (progress > 0f) {
        drawArc(
            brush = Brush.sweepGradient(listOf(Color(0xFFF8B72C), Color(0xFFFFD700), Color(0xFFF8B72C))),
            startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = topLeft, size = arcSize
        )
    }
}