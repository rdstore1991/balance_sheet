package com.mfstracker.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TrackerScreen(
    uiState: TrackerUiState,
    isDarkTheme: Boolean,
    onNavigateBack: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    onStartNewSheet: () -> Unit,
    onCarryForwardLastRecord: () -> Unit,
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
    onShareReceipt: () -> Unit,
    onDeleteRecord: (Long) -> Unit = {},
    onStatusConsumed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val isImeVisible = WindowInsets.isImeVisible

    val currencyPrefix = uiState.currencyFormat.prefix

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

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = if (uiState.editingRecordId != null) "Delete Balance Sheet?" else "Reset Balance Sheet?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = if (uiState.editingRecordId != null)
                        "Are you sure you want to permanently delete this record from history? This cannot be undone."
                    else
                        "Are you sure you want to clear all entered data and reset this balance sheet?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        val id = uiState.editingRecordId
                        if (id != null) {
                            onDeleteRecord(id)
                            onNavigateBack()
                        } else {
                            onStartNewSheet()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseColor)
                ) {
                    Text(
                        text = if (uiState.editingRecordId != null) "Delete" else "Reset",
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Record Entry",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (uiState.editingRecordId != null) "Editing Snapshot #${uiState.editingRecordId}" else "New Balance Sheet",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.editingRecordId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = TrackerIcons.ArrowBack,
                            contentDescription = "Return to Homepage",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    // Delete Icon Button
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = TrackerIcons.Delete,
                            contentDescription = "Delete or Reset Record",
                            tint = ExpenseColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Share Icon Button
                    IconButton(
                        onClick = onShareReceipt,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = TrackerIcons.Share,
                            contentDescription = "Share Receipt",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Save Record Icon Button
                    IconButton(
                        onClick = onSaveRecord,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = TrackerIcons.Save,
                            contentDescription = "Save Record",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Automatically hide bottom bar when keyboard is visible to free up 100% of viewport above keyboard
            if (!isImeVisible) {
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
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onSaveRecord,
                                enabled = !uiState.isSaving,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .heightIn(min = 52.dp)
                            ) {
                                Icon(
                                    imageVector = TrackerIcons.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.isSaving) "Saving…" else "Save Record",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                            }

                            FilledTonalButton(
                                onClick = onShareReceipt,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 52.dp)
                            ) {
                                Icon(
                                    imageVector = TrackerIcons.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Share",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 0. Compact Date, Time & Carry Forward Bar (Inside LazyColumn)
            item {
                CompactDateTimeAndActionsCard(
                    dateString = formatDatePretty(uiState.selectedDate),
                    timeString = uiState.formattedTime,
                    onOpenDatePicker = { showDatePicker = true },
                    onOpenTimePicker = { showTimePicker = true },
                    onCarryForward = onCarryForwardLastRecord,
                    onReset = onStartNewSheet
                )
            }

            // 1. PART 1: MFS ACCOUNTS (CARD-BASED GROUPS)
            item {
                ModernMfsBalancesCard(
                    mfsInputs = uiState.mfsInputs,
                    totalMfsBalance = uiState.totalMfsBalance,
                    currencyPrefix = currencyPrefix,
                    isDarkTheme = isDarkTheme,
                    onUpdateBalance = onUpdateMfsBalance
                )
            }

            // 2. PART 2: TRANSACTIONS LOG (MODERN ITEM CARDS)
            item {
                ModernTransactionsLogCard(
                    logRows = uiState.logRows,
                    totalExpenses = uiState.totalDailyExpenses,
                    totalIncome = uiState.totalDailyIncome,
                    currencyPrefix = currencyPrefix,
                    isDarkTheme = isDarkTheme,
                    onAddRow = onAddLogRow,
                    onDeleteRow = onDeleteLogRow,
                    onUpdateType = onUpdateLogType,
                    onUpdateDescription = onUpdateLogDescription,
                    onUpdateAmount = onUpdateLogAmount,
                    onUpdateNotes = onUpdateLogNotes
                )
            }

            // 3. PART 3: RECONCILIATION SUMMARY (DASHBOARD METRICS)
            item {
                ModernReconciliationCard(
                    availableBalance = uiState.availableBalance,
                    previousBalanceInput = uiState.previousBalanceInput,
                    msaInput = uiState.msaInput,
                    previousMsaInput = uiState.previousMsaInput,
                    currencyPrefix = currencyPrefix,
                    isDarkTheme = isDarkTheme,
                    onUpdatePreviousBalance = onUpdatePreviousBalance,
                    onUpdateMsa = onUpdateMsa,
                    onUpdatePreviousMsa = onUpdatePreviousMsa
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Compact Date, Time & Carry Forward Bar arranged as a clean 2-row card.
 */
@Composable
private fun CompactDateTimeAndActionsCard(
    dateString: String,
    timeString: String,
    onOpenDatePicker: () -> Unit,
    onOpenTimePicker: () -> Unit,
    onCarryForward: () -> Unit,
    onReset: () -> Unit
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
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Two equal-width pill buttons (Modifier.weight(1f).height(42.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Left: Date Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clickable(onClick = onOpenDatePicker)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = TrackerIcons.Calendar,
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right: Time Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clickable(onClick = onOpenTimePicker)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = TrackerIcons.Clock,
                            contentDescription = "Select Time",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Row 2: Two compact action buttons side-by-side (height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: FilledTonalButton Carry Forward
                FilledTonalButton(
                    onClick = onCarryForward,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Icon(
                        imageVector = TrackerIcons.Replay,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🔁 Carry Forward",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Right: OutlinedButton Reset
                OutlinedButton(
                    onClick = onReset,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(40.dp)
                ) {
                    Text(
                        text = "↺ Reset",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
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
    currencyPrefix: String,
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
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${mfsInputs.size} accounts",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // Render Account Groups (4 distinct accounts)
            groupedBySl.forEach { (slNo, rowsInGroup) ->
                val primaryAcc = rowsInGroup.firstOrNull()?.accountNumber.orEmpty()

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Phone Number Header with SIM Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = TrackerIcons.SimCard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = formatPhoneNumber(primaryAcc),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false
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
                                    modifier = Modifier.width(108.dp)
                                )

                                ModernDecimalTextField(
                                    value = item.rawBalance,
                                    onValueChange = { onUpdateBalance(item.key, it) },
                                    placeholderText = "0.00",
                                    showPrefix = true,
                                    currencyPrefix = currencyPrefix,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Section Footer: Total MFS Balance Banner Card (Stacked Vertically to avoid wrap)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Total MFS Balance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Sum of all 7 account inputs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "৳ ${totalMfsBalance.formatBdt()}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
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
    currencyPrefix: String,
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
            // Header with only title and count badge
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
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${logRows.size} items",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
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
                            text = "No transactions logged yet. Tap '+ Add Transaction Row' below.",
                            style = MaterialTheme.typography.bodyMedium,
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
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Line 1: Type Segmented Toggle + Row # + Delete Action (>= 48.dp target)
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
                                        text = "#${index + 1}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = TrackerIcons.Delete,
                                        contentDescription = "Delete item",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
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
                                    modifier = Modifier.weight(1.3f),
                                    isDecimal = false
                                )

                                ModernTextInputField(
                                    value = row.rawAmount,
                                    onValueChange = { onUpdateAmount(row.id, it) },
                                    placeholder = "0.00",
                                    showPrefix = true,
                                    currencyPrefix = currencyPrefix,
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

            // + Add Transaction Row Button at the bottom
            OutlinedButton(
                onClick = onAddRow,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(
                    imageVector = TrackerIcons.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "+ Add Transaction Row",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    softWrap = false
                )
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
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Total Expenses",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = ExpenseColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currencyPrefix ${totalExpenses.formatBdt()}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            ),
                            color = ExpenseColor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
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
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Total Income",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = IncomeColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currencyPrefix ${totalIncome.formatBdt()}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            ),
                            color = IncomeColor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
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
    currencyPrefix: String,
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
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false
            )

            // 1. Available Balance Highlight Metric Card (Stacked Vertically to avoid wrap)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Available Balance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Auto-mirrored from Part 1 Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "৳ ${availableBalance.formatBdt()}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 2. Previous Balance Field (Stacked label + full-width input)
            ModernMetricInputField(
                label = "Previous Balance",
                helperText = "From last saved record (manually editable)",
                value = previousBalanceInput,
                currencyPrefix = currencyPrefix,
                onValueChange = onUpdatePreviousBalance,
                imeAction = ImeAction.Next
            )

            // 3. Current MSA Field (Stacked label + full-width input)
            ModernMetricInputField(
                label = "Current MSA",
                helperText = "Enter today's MSA amount",
                value = msaInput,
                currencyPrefix = currencyPrefix,
                onValueChange = onUpdateMsa,
                imeAction = ImeAction.Next
            )

            // 4. Previous MSA Field (Stacked label + full-width input)
            ModernMetricInputField(
                label = "Previous MSA",
                helperText = "From last saved record (manually editable)",
                value = previousMsaInput,
                currencyPrefix = currencyPrefix,
                onValueChange = onUpdatePreviousMsa,
                imeAction = ImeAction.Done
            )
        }
    }
}

/**
 * Metric input field with label and helper text stacked *above* full-width input.
 */
@Composable
private fun ModernMetricInputField(
    label: String,
    helperText: String,
    value: String,
    currencyPrefix: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )
            }

            ModernDecimalTextField(
                value = value,
                onValueChange = onValueChange,
                placeholderText = "0.00",
                showPrefix = true,
                currencyPrefix = currencyPrefix,
                imeAction = imeAction,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
