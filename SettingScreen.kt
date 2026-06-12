package com.example.a220960_sirnelson_lab01

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(userViewModel: UserViewModel, cloudViewModel: CloudViewModel) {
    val context = LocalContext.current
    val isSyncing = cloudViewModel.isSyncing

    // STATE UNTUK POPUP
    var showNameDialog by remember { mutableStateOf(false) }
    var showContactPopup by remember { mutableStateOf(false) }
    var showAboutPopup by remember { mutableStateOf(false) }

    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(Color(0xFF0A1A3A), Color.Black)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = "SETTINGS", color = Color(0xFFF8B72C), fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(text = "Manage your profile & get support", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(32.dp))

        // CLOUD SYNC SECTION
        SettingsSection(title = "Data Management") {
            SettingsItem(
                icon = Icons.Default.CloudUpload,
                title = "Cloud Backup",
                subtitle = if (isSyncing) "Syncing..." else "Backup all data to Firebase",
                trailing = {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFFF8B72C), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.ChevronRight, null, tint = Color.DarkGray)
                    }
                },
                onClick = {
                    if (!isSyncing) {
                        cloudViewModel.syncAllDataToCloud { error ->
                            if (error == null) {
                                Toast.makeText(context, "Berjaya Backup!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Gagal: $error", Toast.LENGTH_LONG).show()
                                Log.e("SyncError", error)
                            }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PROFIL
        SettingsSection(title = "Account") {
            SettingsItem(
                icon = Icons.Default.Person,
                title = "User Name",
                subtitle = if (userViewModel.userName.isEmpty()) "Not set" else userViewModel.userName,
                onClick = {
                    textInput = userViewModel.userName
                    showNameDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SUPPORT
        SettingsSection(title = "Help & Support") {
            SettingsItem(
                icon = Icons.Default.SupportAgent,
                title = "Contact Support",
                subtitle = "Call or Email us",
                onClick = { showContactPopup = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ABOUT
        SettingsSection(title = "About App") {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Version & Info",
                subtitle = "v1.0.5-beta",
                onClick = { showAboutPopup = true }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // DIALOGS
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Set Name", color = Color(0xFFF8B72C)) },
            text = {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Enter name", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF8B72C)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    userViewModel.userName = textInput
                    showNameDialog = false
                }) {
                    Text("SAVE", color = Color(0xFFF8B72C))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }

    if (showContactPopup) {
        AlertDialog(
            onDismissRequest = { showContactPopup = false },
            containerColor = Color(0xFF1E1E1E),
            icon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFFF8B72C)) },
            title = { Text("Support Details", color = Color(0xFFF8B72C)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Email: support@gmail.com", color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("Phone: +60 12-345 6789", color = Color.White)
                }
            },
            confirmButton = {
                TextButton(onClick = { showContactPopup = false }) {
                    Text("CLOSE", color = Color(0xFFF8B72C))
                }
            }
        )
    }

    if (showAboutPopup) {
        AlertDialog(
            onDismissRequest = { showAboutPopup = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("App Information", color = Color(0xFFF8B72C)) },
            text = {
                Text(
                    "SlideGenie is a smart productivity app designed to help students stay organized and focused in their daily studies. \n\nVersion: 1.0.5-beta",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutPopup = false }) {
                    Text("OK", color = Color(0xFFF8B72C))
                }
            }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            color = Color(0xFFF8B72C),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(0.08f))
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF8B72C).copy(0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFFF8B72C), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) Text(subtitle, color = Color.Gray, fontSize = 13.sp)
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(Icons.Default.ChevronRight, null, tint = Color.DarkGray)
        }
    }
}