package com.mfstracker.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import com.mfstracker.app.ui.components.CompactTableInputField
import com.mfstracker.app.ui.components.DecimalOutlinedTextField
import com.mfstracker.app.ui.components.ExpenseIncomeToggle
import com.mfstracker.app.ui.components.TrackerDatePickerDialog
import com.mfstracker.app.ui.components.TrackerIcons
import com.mfstracker.app.ui.components.TrackerTimePickerDialog
import com.mfstracker.app.ui.theme.ExpenseDark
import com.mfstracker.app.ui.theme.ExpenseLight
import com.mfstracker.app.ui.theme.IncomeDark
import com.mfstracker.app.ui.theme.IncomeLight
import com.mfstracker.app.util.NumberFormatUtils.formatBdt
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
            StickyDashboardHeader(
                formattedDate = uiState.formattedDate,
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
            BottomSaveExportBar(
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
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // PART 1: I. MFS ACCOUNT BALANCES
            MfsBalancesCard(
                mfsInputs = uiState.mfsInputs,
                totalMfsBalance = uiState.totalMfsBalance,
                onUpdateBalance = onUpdateMfsBalance
            )

            // PART 2: EXPENSE & INCOME LOG
            ExpenseIncomeLogCard(
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

            // PART 3: RECONCILIATION & MSA SUMMARY
            ReconciliationSummaryCard(
                availableBalance = uiState.availableBalance,
                previousBalanceInput = uiState.previousBalanceInput,
                msaInput = uiState.msaInput,
                previousMsaInput = uiState.previousMsaInput,
                onUpdatePreviousBalance = onUpdatePreviousBalance,
                onUpdateMsa = onUpdateMsa,
                onUpdatePreviousMsa = onUpdatePreviousMsa
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StickyDashboardHeader(
    formattedDate: String,
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
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            // Top App Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "MFS FUND & EXPENSE TRACKER",
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Manual Theme Toggle Button (Light / Dark)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable(onClick = onToggleTheme)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) TrackerIcons.LightMode else TrackerIcons.DarkMode,
                                contentDescription = "Toggle Light or Dark Theme",
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isDarkTheme) "Light" else "Dark",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // History / Export Button
                    OutlinedButton(
                        onClick = onOpenHistory,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = TrackerIcons.History,
                            contentDescription = "History and Export",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (historyCount > 0) "History ($historyCount)" else "History / Export",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

            // Interactive Date & Time Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Date Selector Chip (DD/MM/YYYY)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .clickable(onClick = onOpenDatePicker)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = TrackerIcons.Calendar,
                        contentDescription = "Select Date",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Date: $formattedDate",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Time Selector Chip (hh:mm a)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .clickable(onClick = onOpenTimePicker)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = TrackerIcons.Clock,
                        contentDescription = "Select Time",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // New Sheet / Active Edit Reset Button
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(
                        1.dp,
                        if (editingRecordId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ),
                    color = if (editingRecordId != null) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    modifier = Modifier.clickable(onClick = onStartNewSheet)
                ) {
                    Text(
                        text = if (editingRecordId != null) "+ New Sheet" else "Reset",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (editingRecordId != null) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }

            HorizontalDivider(thickness = 1.5.dp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun MfsBalancesCard(
    mfsInputs: List<MfsBalanceInputUi>,
    totalMfsBalance: Double,
    onUpdateBalance: (String, String) -> Unit
) {
    val borderColor = MaterialTheme.colorScheme.outline
    val groupedBySl = remember(mfsInputs) { mfsInputs.groupBy { it.slNo } }

    Card(
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.5.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Section Title Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PART 1  •  I. MFS ACCOUNT BALANCES",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            HorizontalDivider(thickness = 1.5.dp, color = borderColor)

            // Table Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell(text = "SL#", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                VerticalDivider(thickness = 1.dp, color = borderColor)
                TableHeaderCell(text = "ACCOUNT NUMBER", modifier = Modifier.width(104.dp))
                VerticalDivider(thickness = 1.dp, color = borderColor)
                TableHeaderCell(text = "MFS PROVIDER", modifier = Modifier.width(78.dp))
                VerticalDivider(thickness = 1.dp, color = borderColor)
                TableHeaderCell(
                    text = "CURRENT BALANCE (BDT)",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            HorizontalDivider(thickness = 1.5.dp, color = borderColor)

            // Fixed Account Groups (SL# 1..4) with merged SL# and Account Number cells for dual Bkash/Nagad rows
            val slKeys = groupedBySl.keys.sorted()
            slKeys.forEachIndexed { groupIdx, slNo ->
                val rowsInGroup = groupedBySl[slNo].orEmpty()
                val accountNumber = rowsInGroup.firstOrNull()?.accountNumber.orEmpty()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    // SL# Merged Cell
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(30.dp)
                            .fillMaxHeight()
                            .padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = slNo.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    VerticalDivider(thickness = 1.dp, color = borderColor)

                    // Account Number Merged Cell
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .width(104.dp)
                            .fillMaxHeight()
                            .padding(horizontal = 5.dp)
                    ) {
                        Text(
                            text = accountNumber,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            ),
                            maxLines = 1
                        )
                    }

                    VerticalDivider(thickness = 1.dp, color = borderColor)

                    // Sub-rows for Provider + Current Balance (BDT)
                    Column(modifier = Modifier.weight(1f)) {
                        rowsInGroup.forEachIndexed { subIdx, item ->
                            val isLastOverall =
                                groupIdx == slKeys.lastIndex && subIdx == rowsInGroup.lastIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier
                                        .width(78.dp)
                                        .fillMaxHeight()
                                        .padding(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = item.provider,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                VerticalDivider(thickness = 1.dp, color = borderColor)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 5.dp, vertical = 4.dp)
                                ) {
                                    DecimalOutlinedTextField(
                                        value = item.rawBalance,
                                        onValueChange = { onUpdateBalance(item.key, it) },
                                        placeholderText = "0.00",
                                        imeAction = if (isLastOverall) ImeAction.Done else ImeAction.Next,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            if (subIdx < rowsInGroup.lastIndex) {
                                HorizontalDivider(thickness = 1.dp, color = borderColor)
                            }
                        }
                    }
                }

                HorizontalDivider(
                    thickness = if (groupIdx == slKeys.lastIndex) 1.5.dp else 1.dp,
                    color = borderColor
                )
            }

            // Footer Row: TOTAL = [Auto-calculated sum of all 7 MFS balance inputs in BDT]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL =",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${totalMfsBalance.formatBdt()} BDT",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ExpenseIncomeLogCard(
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
    val borderColor = MaterialTheme.colorScheme.outline
    val expenseColor = if (isDarkTheme) ExpenseDark else ExpenseLight
    val incomeColor = if (isDarkTheme) IncomeDark else IncomeLight

    Card(
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.5.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Section Title + Add Row Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PART 2  •  EXPENSE / INCOME LOG",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, borderColor),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clickable(onClick = onAddRow)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = TrackerIcons.Add,
                            contentDescription = "Add Row",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Add Row",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 1.5.dp, color = borderColor)

            // Column Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell(
                    text = "TYPE",
                    modifier = Modifier.width(42.dp),
                    textAlign = TextAlign.Center
                )
                VerticalDivider(thickness = 1.dp, color = borderColor)
                TableHeaderCell(
                    text = "Description",
                    modifier = Modifier.weight(1.2f)
                )
                VerticalDivider(thickness = 1.dp, color = borderColor)
                TableHeaderCell(
                    text = "Amount (BDT)",
                    modifier = Modifier.weight(0.95f),
                    textAlign = TextAlign.End
                )
                VerticalDivider(thickness = 1.dp, color = borderColor)
                TableHeaderCell(
                    text = "Notes",
                    modifier = Modifier.weight(0.9f)
                )
                VerticalDivider(thickness = 1.dp, color = borderColor)
                TableHeaderCell(
                    text = "Act",
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
            }

            HorizontalDivider(thickness = 1.5.dp, color = borderColor)

            if (logRows.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp)
                ) {
                    Text(
                        text = "No rows. Tap '+ Add Row' to record an Expense or Income.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(thickness = 1.5.dp, color = borderColor)
            } else {
                logRows.forEachIndexed { idx, row ->
                    val isLast = idx == logRows.lastIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // [Expense | Income] compact toggle
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .width(42.dp)
                                .padding(horizontal = 3.dp, vertical = 4.dp)
                        ) {
                            ExpenseIncomeToggle(
                                selectedType = row.type,
                                isDark = isDarkTheme,
                                onSelect = { onUpdateType(row.id, it) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        VerticalDivider(thickness = 1.dp, color = borderColor)

                        // Description
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .padding(horizontal = 3.dp, vertical = 4.dp)
                        ) {
                            CompactTableInputField(
                                value = row.description,
                                onValueChange = { onUpdateDescription(row.id, it) },
                                placeholder = "Item...",
                                isDecimal = false,
                                imeAction = ImeAction.Next,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        VerticalDivider(thickness = 1.dp, color = borderColor)

                        // Amount (BDT)
                        Box(
                            modifier = Modifier
                                .weight(0.95f)
                                .padding(horizontal = 3.dp, vertical = 4.dp)
                        ) {
                            CompactTableInputField(
                                value = row.rawAmount,
                                onValueChange = { onUpdateAmount(row.id, it) },
                                placeholder = "0.00",
                                isDecimal = true,
                                imeAction = ImeAction.Next,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        VerticalDivider(thickness = 1.dp, color = borderColor)

                        // Notes
                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(horizontal = 3.dp, vertical = 4.dp)
                        ) {
                            CompactTableInputField(
                                value = row.notes,
                                onValueChange = { onUpdateNotes(row.id, it) },
                                placeholder = "Note",
                                isDecimal = false,
                                imeAction = if (isLast) ImeAction.Done else ImeAction.Next,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        VerticalDivider(thickness = 1.dp, color = borderColor)

                        // Delete Row Action Icon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .width(32.dp)
                                .fillMaxHeight()
                        ) {
                            IconButton(
                                onClick = { onDeleteRow(row.id) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = TrackerIcons.Delete,
                                    contentDescription = "Delete row",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        thickness = if (isLast) 1.5.dp else 1.dp,
                        color = borderColor
                    )
                }
            }

            // Bottom "+ Add Row" bar inside Part 2 for fast reach while typing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                OutlinedButton(
                    onClick = onAddRow,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, borderColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = TrackerIcons.Add,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Add Row", style = MaterialTheme.typography.labelMedium)
                }
            }

            HorizontalDivider(thickness = 1.dp, color = borderColor)

            // Footer Rows: TOTAL DAILY EXPENSES & TOTAL DAILY INCOME
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL DAILY EXPENSES:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${totalExpenses.formatBdt()} BDT",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = expenseColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL DAILY INCOME:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${totalIncome.formatBdt()} BDT",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = incomeColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ReconciliationSummaryCard(
    availableBalance: Double,
    previousBalanceInput: String,
    msaInput: String,
    previousMsaInput: String,
    onUpdatePreviousBalance: (String) -> Unit,
    onUpdateMsa: (String) -> Unit,
    onUpdatePreviousMsa: (String) -> Unit
) {
    val borderColor = MaterialTheme.colorScheme.outline

    Card(
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.5.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Section Title Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PART 3  •  RECONCILIATION & MSA SUMMARY",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            HorizontalDivider(thickness = 1.5.dp, color = borderColor)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Available Balance (strictly mirrors Part 1 TOTAL MFS Balances)
                AlignedSummaryRow(
                    label = "Available Balance:",
                    subLabel = "Auto-mirrored from Part 1 Total",
                    value = availableBalance.toEditableString(),
                    onValueChange = {},
                    readOnly = true,
                    imeAction = ImeAction.Next
                )

                // 2. Previous Balance (auto-populated from latest saved record's Available Balance, manually editable)
                AlignedSummaryRow(
                    label = "Previous Balance:",
                    subLabel = "From last saved record (editable)",
                    value = previousBalanceInput,
                    onValueChange = onUpdatePreviousBalance,
                    readOnly = false,
                    imeAction = ImeAction.Next
                )

                // 3. MSA (numeric input for current MSA)
                AlignedSummaryRow(
                    label = "MSA:",
                    subLabel = "Current MSA (BDT)",
                    value = msaInput,
                    onValueChange = onUpdateMsa,
                    readOnly = false,
                    imeAction = ImeAction.Next
                )

                // 4. Previous MSA (auto-populated from latest saved record's MSA, manually editable)
                AlignedSummaryRow(
                    label = "Previous MSA:",
                    subLabel = "From last saved record (editable)",
                    value = previousMsaInput,
                    onValueChange = onUpdatePreviousMsa,
                    readOnly = false,
                    imeAction = ImeAction.Done
                )
            }
        }
    }
}

@Composable
private fun AlignedSummaryRow(
    label: String,
    subLabel: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean,
    imeAction: ImeAction
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(0.95f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DecimalOutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            placeholderText = "0.00",
            imeAction = imeAction,
            modifier = Modifier.weight(1.05f)
        )
    }
}

@Composable
private fun BottomSaveExportBar(
    isSaving: Boolean,
    onSaveRecord: () -> Unit,
    onSaveAndExport: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(thickness = 1.5.dp, color = MaterialTheme.colorScheme.outline)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSaveRecord,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    modifier = Modifier.weight(0.85f)
                ) {
                    Text(
                        text = "SAVE RECORD",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = onSaveAndExport,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    modifier = Modifier.weight(1.15f)
                ) {
                    Icon(
                        imageVector = TrackerIcons.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAVE & EXPORT TO STORAGE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Box(
        contentAlignment = when (textAlign) {
            TextAlign.Center -> Alignment.Center
            TextAlign.End -> Alignment.CenterEnd
            else -> Alignment.CenterStart
        },
        modifier = modifier.padding(horizontal = 5.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            textAlign = textAlign,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

