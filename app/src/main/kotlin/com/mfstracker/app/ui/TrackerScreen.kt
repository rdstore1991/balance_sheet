package com.mfstracker.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mfstracker.app.data.local.TrackerRecordEntity
import com.mfstracker.app.data.model.TransactionType
import com.mfstracker.app.ui.components.ModernDecimalTextField
import com.mfstracker.app.ui.components.ModernSegmentedTransactionToggle
import com.mfstracker.app.ui.components.ModernTextInputField
import com.mfstracker.app.ui.components.ProviderBadge
import com.mfstracker.app.ui.components.TrackerDatePickerDialog
import com.mfstracker.app.ui.components.TrackerIcons
import com.mfstracker.app.ui.components.TrackerTimePickerDialog
import com.mfstracker.app.ui.theme.ExpenseColor
import com.mfstracker.app.ui.theme.ExpenseContainerDark
import com.mfstracker.app.ui.theme.ExpenseContainerLight
import com.mfstracker.app.ui.theme.IncomeColor
import com.mfstracker.app.ui.theme.IncomeContainerDark
import com.mfstracker.app.ui.theme.IncomeContainerLight
import com.mfstracker.app.util.NumberFormatUtils.formatBdt
import com.mfstracker.app.util.NumberFormatUtils.formatDatePretty
import com.mfstracker.app.util.NumberFormatUtils.formatPhoneNumber
import com.mfstracker.app.util.NumberFormatUtils.toEditableString
import java.time.LocalDate

@Composable
fun TrackerScreen(
    uiState: TrackerUiState,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenHistory: () -> Unit,
    onDismissHistory: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    onStartNewSheet: () -> Unit,
    onUpdateMfsBalance: (String, String) -> Unit,
    onAddLogRow: () -> Unit,
    onDeleteLogRow: (String) -> Unit,
    onUpdateLogType: (String, TransactionType) -> Unit,
    onUpdateLogDescription: (String, String) -> Unit,
    onUpdateLogAmount: (String, String) -> Unit,
    onUpdateLogNotes: (String, String) -> Unit,
    onUpdatePreviousBalance: (String) -> Unit,
    onUpdateMsa: (String) -> Unit,
    onUpdatePreviousMsa: (String) -> Unit,
    onSaveRecord: () -> Unit,
    onSaveAndExport: () -> Unit,
    onLoadHistoryRecord: (TrackerRecordEntity) -> Unit,
    onExportHistoryRecord: (TrackerRecordEntity) -> Unit,
    onExportHistoryViaSaf: (TrackerRecordEntity) -> Unit,
    onDeleteHistoryRecord: (Long) -> Unit,
    onStatusConsumed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            onStatusConsumed()
        }
    }

    if (showDatePicker) {
        TrackerDatePickerDialog(
            initialDate = uiState.selectedDate,
            onDateSelected = onDateSelected,
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        TrackerTimePickerDialog(
            initialTime = uiState.selectedTime,
            onTimeSelected = onTimeSelected,
            onDismiss = { showTimePicker = false }
        )
    }

    if (uiState.isHistoryVisible) {
        HistoryExportDialog(
            records = uiState.historyRecords,
            activeRecordId = uiState.editingRecordId,
            onDismiss = onDismissHistory,
            onLoadRecord = onLoadHistoryRecord,
            onExportToDocuments = onExportHistoryRecord,
            onExportViaSaf = onExportHistoryViaSaf,
            onDeleteRecord = onDeleteHistoryRecord
        )
    }

    Scaffold(
        topBar = {
            ModernDashboardHeader(
                selectedDate = uiState.selectedDate,
                formattedTime = uiState.formattedTime,
                editingRecordId = uiState.editingRecordId,
                historyCount = uiState.historyRecords.size,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onOpenHistory = onOpenHistory,
                onOpenDatePicker = { showDatePicker = true },
                onOpenTimePicker = { showTimePicker = true },
                onStartNewSheet = onStartNewSheet
            )
        },
        bottomBar = {
            ModernBottomActionBar(
                isSaving = uiState.isSaving,
                onSaveRecord = onSaveRecord,
                onSaveAndExport = onSaveAndExport
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PART 1: MFS ACCOUNTS (CARD-BASED GROUPS)
            ModernMfsBalancesCard(
                mfsInputs = uiState.mfsInputs,
                totalMfsBalance = uiState.totalMfsBalance,
                isDarkTheme = isDarkTheme,
                onUpdateBalance = onUpdateMfsBalance
            )

            // PART 2: TRANSACTIONS LOG (MODERN ITEM CARDS)
            ModernTransactionsLogCard(
                logRows = uiState.logRows,
                totalExpenses = uiState.totalDailyExpenses,
                totalIncome = uiState.totalDailyIncome,
                isDarkTheme = isDarkTheme,
                onAddRow = onAddLogRow,
                onDeleteRow = onDeleteLogRow,
                onUpdateType = onUpdateLogType,
                onUpdateDescription = onUpdateLogDescription,
                onUpdateAmount = onUpdateLogAmount,
                onUpdateNotes = onUpdateLogNotes
            )

            // PART 3: RECONCILIATION SUMMARY (DASHBOARD METRICS)
            ModernReconciliationCard(
                availableBalance = uiState.availableBalance,
                previousBalanceInput = uiState.previousBalanceInput,
                msaInput = uiState.msaInput,
                previousMsaInput = uiState.previousMsaInput,
                isDarkTheme = isDarkTheme,
                onUpdatePreviousBalance = onUpdatePreviousBalance,
                onUpdateMsa = onUpdateMsa,
                onUpdatePreviousMsa = onUpdatePreviousMsa
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * Clean TopAppBar and Sub-Header Bar with pill date/time selectors.
 */
@Composable
private fun ModernDashboardHeader(
    selectedDate: LocalDate,
    formattedTime: String,
    editingRecordId: Long?,
    historyCount: Int,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDatePicker: () -> Unit,
    onOpenTimePicker: () -> Unit,
    onStartNewSheet: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            // Main Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = TrackerIcons.Wallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "MFS Tracker",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (editingRecordId != null) "Editing Saved Snapshot" else "Daily Ledger",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (editingRecordId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Manual Theme Toggle Button (Moon / Sun)
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) TrackerIcons.LightMode else TrackerIcons.DarkMode,
                            contentDescription = "Toggle Light/Dark Theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // History Icon Button with badge count
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.size(38.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (historyCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text(historyCount.toString(), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = TrackerIcons.History,
                                contentDescription = "History and Export",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // Sub-Header: Clickable Date & Time Pills + New Sheet Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Date Chip Pill [ 📅 01 Nov 2026 ]
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.clickable(onClick = onOpenDatePicker)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = TrackerIcons.Calendar,
                            contentDescription = "Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formatDatePretty(selectedDate),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Time Chip Pill [ 🕒 06:30 PM ]
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.clickable(onClick = onOpenTimePicker)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = TrackerIcons.Clock,
                            contentDescription = "Time",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // New Sheet / Reset Outlined Button
                OutlinedButton(
                    onClick = onStartNewSheet,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (editingRecordId != null) "+ New Sheet" else "Reset",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/**
 * PART 1: Modern Card-Based MFS Account Groups.
 */
@Composable
private fun ModernMfsBalancesCard(
    mfsInputs: List<MfsBalanceInputUi>,
    totalMfsBalance: Double,
    isDarkTheme: Boolean,
    onUpdateBalance: (String, String) -> Unit
) {
    val groupedBySl = remember(mfsInputs) { mfsInputs.groupBy { it.slNo } }

    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Account Count Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "MFS Accounts",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${groupedBySl.size} Numbers",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Dedicated Child Card for each Account Group
            groupedBySl.keys.sorted().forEach { slNo ->
                val rowsInGroup = groupedBySl[slNo].orEmpty()
                val rawNumber = rowsInGroup.firstOrNull()?.accountNumber.orEmpty()
                val formattedNumber = formatPhoneNumber(rawNumber)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Phone Number Header with SIM / Phone indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = TrackerIcons.SimCard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Account $slNo: $formattedNumber",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Providers & Balance Inputs
                        rowsInGroup.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProviderBadge(
                                    provider = item.provider,
                                    isDark = isDarkTheme,
                                    modifier = Modifier.width(96.dp)
                                )

                                ModernDecimalTextField(
                                    value = item.rawBalance,
                                    onValueChange = { onUpdateBalance(item.key, it) },
                                    placeholderText = "0.00",
                                    showPrefix = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Section Footer: Total MFS Balance Banner Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total MFS Balance",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Sum of all 7 account inputs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                    }

                    Text(
                        text = "৳ ${totalMfsBalance.formatBdt()}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * PART 2: Modern Transactions Log (Elevated Item Cards).
 */
@Composable
private fun ModernTransactionsLogCard(
    logRows: List<LogRowUi>,
    totalExpenses: Double,
    totalIncome: Double,
    isDarkTheme: Boolean,
    onAddRow: () -> Unit,
    onDeleteRow: (String) -> Unit,
    onUpdateType: (String, TransactionType) -> Unit,
    onUpdateDescription: (String, String) -> Unit,
    onUpdateAmount: (String, String) -> Unit,
    onUpdateNotes: (String, String) -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with + Add Item Tonal Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Transactions Log",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${logRows.size} items",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onAddRow,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = TrackerIcons.Add,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Add Item", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            // Dynamic Transaction Cards
            if (logRows.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 24.dp)
                    ) {
                        Text(
                            text = "No transactions logged yet. Tap '+ Add Item' above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                logRows.forEachIndexed { index, row ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Line 1: Type Segmented Toggle + Row # + Delete Action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "#${index + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    ModernSegmentedTransactionToggle(
                                        selectedType = row.type,
                                        isDark = isDarkTheme,
                                        onSelect = { onUpdateType(row.id, it) }
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteRow(row.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = TrackerIcons.Delete,
                                        contentDescription = "Delete item",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            // Line 2: Description & Amount side-by-side
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ModernTextInputField(
                                    value = row.description,
                                    onValueChange = { onUpdateDescription(row.id, it) },
                                    placeholder = "Description (e.g. Lunch)",
                                    modifier = Modifier.weight(1.35f),
                                    isDecimal = false
                                )

                                ModernTextInputField(
                                    value = row.rawAmount,
                                    onValueChange = { onUpdateAmount(row.id, it) },
                                    placeholder = "0.00",
                                    showPrefix = true,
                                    modifier = Modifier.weight(1.0f),
                                    isDecimal = true
                                )
                            }

                            // Line 3: Full-width optional Notes input field
                            ModernTextInputField(
                                value = row.notes,
                                onValueChange = { onUpdateNotes(row.id, it) },
                                placeholder = "Notes (optional)",
                                modifier = Modifier.fillMaxWidth(),
                                isDecimal = false,
                                imeAction = if (index == logRows.lastIndex) ImeAction.Done else ImeAction.Next
                            )
                        }
                    }
                }
            }

            // Section Footer: Split Summary Cards (Expenses & Income)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Expenses
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkTheme) ExpenseContainerDark else ExpenseContainerLight,
                    border = BorderStroke(1.dp, ExpenseColor.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Total Expenses",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = ExpenseColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "৳ ${totalExpenses.formatBdt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = ExpenseColor
                        )
                    }
                }

                // Total Income
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkTheme) IncomeContainerDark else IncomeContainerLight,
                    border = BorderStroke(1.dp, IncomeColor.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Total Income",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = IncomeColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "৳ ${totalIncome.formatBdt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = IncomeColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * PART 3: Reconciliation Summary (Dashboard Metrics Card).
 */
@Composable
private fun ModernReconciliationCard(
    availableBalance: Double,
    previousBalanceInput: String,
    msaInput: String,
    previousMsaInput: String,
    isDarkTheme: Boolean,
    onUpdatePreviousBalance: (String) -> Unit,
    onUpdateMsa: (String) -> Unit,
    onUpdatePreviousMsa: (String) -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Reconciliation & MSA Summary",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // 1. Available Balance Highlight Metric Card (Auto-mirrored from Part 1 Total)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Available Balance",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Auto-mirrored from Part 1 Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Text(
                        text = "৳ ${availableBalance.formatBdt()}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 2. Previous Balance Field
            ModernMetricInputField(
                label = "Previous Balance",
                helperText = "From last saved record (manually editable)",
                value = previousBalanceInput,
                onValueChange = onUpdatePreviousBalance,
                imeAction = ImeAction.Next
            )

            // 3. Current MSA Field
            ModernMetricInputField(
                label = "Current MSA",
                helperText = "Enter today's MSA amount",
                value = msaInput,
                onValueChange = onUpdateMsa,
                imeAction = ImeAction.Next
            )

            // 4. Previous MSA Field
            ModernMetricInputField(
                label = "Previous MSA",
                helperText = "From last saved record (manually editable)",
                value = previousMsaInput,
                onValueChange = onUpdatePreviousMsa,
                imeAction = ImeAction.Done
            )
        }
    }
}

@Composable
private fun ModernMetricInputField(
    label: String,
    helperText: String,
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ModernDecimalTextField(
                value = value,
                onValueChange = onValueChange,
                placeholderText = "0.00",
                showPrefix = true,
                imeAction = imeAction,
                modifier = Modifier.width(140.dp)
            )
        }
    }
}

/**
 * Polished Sticky Bottom Navigation / Action Bar.
 */
@Composable
private fun ModernBottomActionBar(
    isSaving: Boolean,
    onSaveRecord: () -> Unit,
    onSaveAndExport: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Save Record: Outlined Tonal Button
                OutlinedButton(
                    onClick = onSaveRecord,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    modifier = Modifier.weight(0.85f)
                ) {
                    Text(
                        text = "Save Record",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    )
                }

                // Save & Export CSV: Filled Primary Button with Icon
                Button(
                    onClick = onSaveAndExport,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    modifier = Modifier.weight(1.15f)
                ) {
                    Icon(
                        imageVector = TrackerIcons.Download,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save & Export CSV",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
