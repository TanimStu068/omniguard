package com.example.omniguard.presentation.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.omniguard.BuildConfig
import com.example.omniguard.utils.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    
    // States
    var hasUsageStatsPermission by remember {
        mutableStateOf(PermissionHelper.hasUsageStatsPermission(context))
    }
    var showReportDialog by remember { mutableStateOf(false) }
    var showMethodDialog by remember { mutableStateOf(false) }
    var reportData by remember { mutableStateOf<ReportData?>(null) }
    
    var isCheckingUpdates by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Re-check permission when screen becomes visible
    LaunchedEffect(Unit) {
        hasUsageStatsPermission = PermissionHelper.hasUsageStatsPermission(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Permissions Section
            item {
                Text(
                    text = "Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.History,
                    title = "Usage Access",
                    description = "Required for detecting unused apps",
                    isPermission = true,
                    isGranted = hasUsageStatsPermission,
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }
                )
            }

            // About Section
            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About OmniGuard",
                    description = "Version ${BuildConfig.VERSION_NAME}",
                    onClick = {
                        navController.navigate("about")
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    description = "Read our privacy policy",
                    onClick = {
                        navController.navigate("privacy_policy")
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "Open Source Licenses",
                    description = "View licenses",
                    onClick = {
                        navController.navigate("licenses")
                    }
                )
            }

            // Support Section
            item {
                Text(
                    text = "Support",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "Share App",
                    description = "Share OmniGuard with friends",
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Check out OmniGuard - Privacy Dashboard! https://github.com/TanimStu068/omniguard")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.SystemUpdate,
                    title = "Check for Updates",
                    description = if (isCheckingUpdates) "Checking..." else "Version ${BuildConfig.VERSION_NAME}",
                    onClick = {
                        if (isCheckingUpdates) return@SettingsItem
                        isCheckingUpdates = true
                        checkForUpdates { hasUpdate, latestVersion, releaseNotes ->
                            isCheckingUpdates = false
                            if (hasUpdate && latestVersion != null) {
                                updateInfo = Pair(latestVersion, releaseNotes ?: "New version available")
                                showUpdateDialog = true
                            } else if (!hasUpdate) {
                                Toast.makeText(context, "You're on the latest version!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to check updates", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.BugReport,
                    title = "Report Issue",
                    description = "Report a bug or suggest a feature",
                    onClick = {
                        showReportDialog = true
                    }
                )
            }
        }
    }

    // Dialogs
    if (showUpdateDialog && updateInfo != null) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("Update Available!") },
            text = { Text("Version ${updateInfo!!.first} is available.\n\n${updateInfo!!.second}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TanimStu068/omniguard/releases"))
                        context.startActivity(intent)
                        showUpdateDialog = false
                    }
                ) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Later")
                }
            }
        )
    }

    if (showReportDialog) {
        ReportIssueDialog(
            onDismiss = { showReportDialog = false },
            onConfirm = { data ->
                reportData = data
                showReportDialog = false
                showMethodDialog = true
            }
        )
    }

    if (showMethodDialog && reportData != null) {
        ReportMethodDialog(
            reportData = reportData!!,
            onDismiss = { showMethodDialog = false }
        )
    }
}

data class ReportData(
    val type: String,
    val title: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIssueDialog(
    onDismiss: () -> Unit,
    onConfirm: (ReportData) -> Unit
) {
    var issueTitle by remember { mutableStateOf("") }
    var issueDescription by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Bug Report") }
    var expanded by remember { mutableStateOf(false) }
    val issueTypes = listOf("Bug Report", "Feature Request", "General Feedback")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Issue") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Issue Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        issueTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = issueTitle,
                    onValueChange = { issueTitle = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = issueDescription,
                    onValueChange = { issueDescription = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "Device Info will be included:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Model: ${Build.MODEL}, Android: ${Build.VERSION.RELEASE}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (issueTitle.isNotBlank() && issueDescription.isNotBlank()) {
                        onConfirm(ReportData(selectedType, issueTitle, issueDescription))
                    }
                },
                enabled = issueTitle.isNotBlank() && issueDescription.isNotBlank()
            ) {
                Text("Next")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReportMethodDialog(
    reportData: ReportData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send Report") },
        text = { Text("Choose how you would like to send the report.") },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                TextButton(
                    onClick = {
                        val body = """
                            Type: ${reportData.type}
                            Title: ${reportData.title}
                            
                            Description:
                            ${reportData.description}
                            
                            ---
                            Device: ${Build.MANUFACTURER} ${Build.MODEL}
                            Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
                            App Version: ${BuildConfig.VERSION_NAME}
                        """.trimIndent()
                        
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:tanim.mahmud.stu@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "[OmniGuard ${reportData.type}] ${reportData.title}")
                            putExtra(Intent.EXTRA_TEXT, body)
                        }
                        context.startActivity(Intent.createChooser(intent, "Send Email"))
                        onDismiss()
                    }
                ) {
                    Text("Send via Email")
                }
                
                TextButton(
                    onClick = {
                        val url = "https://github.com/TanimStu068/omniguard/issues/new"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                        onDismiss()
                    }
                ) {
                    Text("Open GitHub Issues")
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    description: String,
    isPermission: Boolean = false,
    isGranted: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isPermission) {
                if (isGranted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Granted",
                        tint = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Not Granted",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun checkForUpdates(onResult: (Boolean, String?, String?) -> Unit) {
    // This is a mock update check. In a real app, you'd call a server/GitHub API.
    // For now, let's assume no update is available.
    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
        onResult(false, null, null)
    }, 2000)
}
