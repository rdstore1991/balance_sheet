package com.mfstracker.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mfstracker.app.data.local.TrackerRecordEntity
import com.mfstracker.app.ui.DashboardScreen
import com.mfstracker.app.ui.ExportCsvScreen
import com.mfstracker.app.ui.HistoryScreen
import com.mfstracker.app.ui.SettingsScreen
import com.mfstracker.app.ui.TrackerScreen
import com.mfstracker.app.ui.TrackerUiState
import com.mfstracker.app.ui.TrackerViewModel
import com.mfstracker.app.ui.components.ReceiptShareBottomSheet
import com.mfstracker.app.ui.components.TrackerIcons
import kotlinx.coroutines.launch

@Composable
fun TrackerAppNavigation(
    viewModel: TrackerViewModel,
    uiState: TrackerUiState,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onLaunchSafCreateDocument: (String) -> Unit,
    onLaunchSafOpenDocument: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route ?: Screen.Dashboard.route
    var shareReceiptRecord by remember { mutableStateOf<TrackerRecordEntity?>(null) }

    val drawerNavigationItems = listOf(
        Screen.Dashboard,
        Screen.History,
        Screen.ExportCsv,
        Screen.Settings
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute != Screen.Editor.route,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.width(310.dp)
            ) {
                // 1. Drawer Header (Branded Banner)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = TrackerIcons.Wallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "MFS Tracker",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "Financial Ledger & Reconciliation",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
                        ) {
                            Text(
                                text = "v1.0 • Android M3",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Navigation Items (>= 48.dp target with modern icons)
                drawerNavigationItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .heightIn(min = 50.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Footer in Drawer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Snapshots: ${uiState.historyRecords.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route
        ) {
            // Screen 1: Dashboard / Homepage
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    uiState = uiState,
                    isDarkTheme = isDarkTheme,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                    onToggleTheme = onToggleTheme,
                    onNewRecordClick = {
                        viewModel.startNewSheet(showMessage = false)
                        navController.navigate(Screen.Editor.route)
                    },
                    onRecordClick = { record: TrackerRecordEntity ->
                        viewModel.loadRecordFromHistory(record, showMessage = false)
                        navController.navigate(Screen.Editor.route)
                    },
                    onShareRecord = { record: TrackerRecordEntity ->
                        shareReceiptRecord = record
                    }
                )
            }

            // Screen 2: Record Entry & Balance Sheet (Editor)
            composable(Screen.Editor.route) {
                TrackerScreen(
                    uiState = uiState,
                    isDarkTheme = isDarkTheme,
                    onNavigateBack = { navController.popBackStack() },
                    onDateSelected = viewModel::updateSelectedDate,
                    onTimeSelected = viewModel::updateSelectedTime,
                    onStartNewSheet = { viewModel.startNewSheet(showMessage = true) },
                    onCarryForwardLastRecord = viewModel::carryForwardLastRecord,
                    onUpdateMfsBalance = viewModel::updateMfsBalance,
                    onAddLogRow = viewModel::addLogRow,
                    onDeleteLogRow = viewModel::deleteLogRow,
                    onUpdateLogType = viewModel::updateLogRowType,
                    onUpdateLogDescription = viewModel::updateLogRowDescription,
                    onUpdateLogAmount = viewModel::updateLogRowAmount,
                    onUpdateLogNotes = viewModel::updateLogRowNotes,
                    onUpdatePreviousBalance = viewModel::updatePreviousBalance,
                    onUpdateMsa = viewModel::updateMsa,
                    onUpdatePreviousMsa = viewModel::updatePreviousMsa,
                    onSaveRecord = viewModel::saveRecord,
                    onShareReceipt = {
                        shareReceiptRecord = viewModel.buildEntityFromState(uiState)
                    },
                    onStatusConsumed = viewModel::clearStatusMessage
                )
            }

            // Screen 3: History & Ledger Screen
            composable(Screen.History.route) {
                HistoryScreen(
                    uiState = uiState,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                    onSearchQueryChange = viewModel::updateHistorySearchQuery,
                    onStartDateChange = viewModel::updateHistoryFilterStartDate,
                    onEndDateChange = viewModel::updateHistoryFilterEndDate,
                    onTypeFilterChange = viewModel::updateHistoryTypeFilter,
                    onClearFilters = viewModel::clearHistoryFilters,
                    onRecordClick = { record ->
                        viewModel.loadRecordFromHistory(record, showMessage = false)
                        navController.navigate(Screen.Editor.route)
                    },
                    onExportRecordToDocuments = viewModel::exportHistoryRecordToDocuments,
                    onExportRecordViaSaf = { record ->
                        val fileName = viewModel.prepareSafExportForRecord(record)
                        onLaunchSafCreateDocument(fileName)
                    },
                    onDeleteRecord = viewModel::deleteHistoryRecord
                )
            }

            // Screen 4: Save & Export CSV Screen
            composable(Screen.ExportCsv.route) {
                ExportCsvScreen(
                    uiState = uiState,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                    onSelectPreset = viewModel::selectExportPreset,
                    onCustomStartDateChange = viewModel::updateExportCustomStartDate,
                    onCustomEndDateChange = viewModel::updateExportCustomEndDate,
                    onExportToDocuments = viewModel::exportSelectedRangeToDocuments,
                    onExportViaSaf = {
                        val fileName = viewModel.prepareSafBatchExport()
                        if (fileName != null) {
                            onLaunchSafCreateDocument(fileName)
                        }
                    }
                )
            }

            // Screen 5: Options / Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    uiState = uiState,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                    onSelectThemeMode = viewModel::setThemeMode,
                    onSelectCurrencyFormat = viewModel::setCurrencyFormat,
                    onExportFullBackup = viewModel::exportFullDatabaseBackupToDocuments,
                    onImportCsvBackup = onLaunchSafOpenDocument,
                    onClearAllData = viewModel::clearAllRecords
                )
            }
        }
    }

    if (shareReceiptRecord != null) {
        ReceiptShareBottomSheet(
            record = shareReceiptRecord!!,
            currencyPrefix = uiState.currencyFormat.prefix,
            onDismiss = { shareReceiptRecord = null }
        )
    }
}
