package com.tanim.omniguard.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tanim.omniguard.presentation.components.AppPermissionRow
import com.tanim.omniguard.presentation.components.SentinelLoadingAnimation
import com.tanim.omniguard.presentation.viewmodel.PermissionFilter
import com.tanim.omniguard.presentation.viewmodel.SentinelViewModel
import com.tanim.omniguard.presentation.viewmodel.SentinelStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentinelScreen(
    navController: NavController,
    viewModel: SentinelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "SENTINEL",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadApps() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stats Section (Horizontal Scrollable)
            StatsCarousel(stats = uiState.stats)

            // Search and Filter Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.searchApps(it) },
                    placeholder = { Text("Search by app or package...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                FilterChips(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = { viewModel.filterByPermissionType(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Area
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = uiState.isLoading,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "content_switch"
                ) { isLoading ->
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            SentinelLoadingAnimation(label = "DETECTING APPS WITH PERMISSIONS...")
                        }
                    } else if (uiState.filteredApps.isEmpty()) {
                        EmptyStateView()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.filteredApps,
                                key = { it.packageName }
                            ) { app ->
                                AppPermissionRow(
                                    appInfo = app,
                                    onClick = {
                                        navController.navigate("app_detail/${app.packageName}")
                                    },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCarousel(stats: SentinelStats) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StatItem(
                label = "High Risk",
                value = stats.highRiskApps.toString(),
                icon = Icons.Default.Warning,
                color = MaterialTheme.colorScheme.error
            )
        }
        item {
            StatItem(
                label = "Shadow Apps",
                value = stats.shadowApps.toString(),
                icon = Icons.Default.VisibilityOff,
                color = Color(0xFF607D8B)
            )
        }
        item {
            StatItem(
                label = "Camera",
                value = stats.appsWithCamera.toString(),
                icon = Icons.Default.PhotoCamera,
                color = Color(0xFF9C27B0)
            )
        }
        item {
            StatItem(
                label = "Microphone",
                value = stats.appsWithMicrophone.toString(),
                icon = Icons.Default.Mic,
                color = Color(0xFFE91E63)
            )
        }
        item {
            StatItem(
                label = "Location",
                value = stats.appsWithLocation.toString(),
                icon = Icons.Default.LocationOn,
                color = Color(0xFF00BCD4)
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            @Suppress("DEPRECATION")
            Column {
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    selectedFilter: PermissionFilter?,
    onFilterSelected: (PermissionFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(PermissionFilter.entries) { filter ->
            val isRiskFilter = filter == PermissionFilter.HIGH_RISK ||
                    filter == PermissionFilter.MEDIUM_RISK ||
                    filter == PermissionFilter.LOW_RISK ||
                    filter == PermissionFilter.SHADOW_APPS

            val chipColor = if (isRiskFilter) {
                when (filter) {
                    PermissionFilter.HIGH_RISK -> MaterialTheme.colorScheme.error
                    PermissionFilter.MEDIUM_RISK -> Color(0xFFFF9800)
                    PermissionFilter.SHADOW_APPS -> Color(0xFF607D8B)
                    else -> Color(0xFF4CAF50)
                }
            } else {
                MaterialTheme.colorScheme.primary
            }

            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        filter.displayName,
                        color = if (selectedFilter == filter) MaterialTheme.colorScheme.onPrimaryContainer else chipColor
                    )
                },
                leadingIcon = if (selectedFilter == filter) {
                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                } else null,
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = chipColor.copy(alpha = 0.2f),
                    selectedLabelColor = chipColor,
                    selectedLeadingIconColor = chipColor
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == filter,
                    borderColor = chipColor.copy(alpha = 0.5f),
                    selectedBorderColor = chipColor
                )
            )
        }
    }
}

@Composable
fun EmptyStateView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.SearchOff,
                null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No apps match your filter",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
