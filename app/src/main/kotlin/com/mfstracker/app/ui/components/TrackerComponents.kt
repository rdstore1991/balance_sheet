package com.mfstracker.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mfstracker.app.data.model.TransactionType
import com.mfstracker.app.ui.theme.AgentTeal
import com.mfstracker.app.ui.theme.AgentTealContainerDark
import com.mfstracker.app.ui.theme.AgentTealContainerLight
import com.mfstracker.app.ui.theme.BkashPink
import com.mfstracker.app.ui.theme.BkashPinkContainerDark
import com.mfstracker.app.ui.theme.BkashPinkContainerLight
import com.mfstracker.app.ui.theme.ExpenseColor
import com.mfstracker.app.ui.theme.ExpenseContainerDark
import com.mfstracker.app.ui.theme.ExpenseContainerLight
import com.mfstracker.app.ui.theme.IncomeColor
import com.mfstracker.app.ui.theme.IncomeContainerDark
import com.mfstracker.app.ui.theme.IncomeContainerLight
import com.mfstracker.app.ui.theme.NagadOrange
import com.mfstracker.app.ui.theme.NagadOrangeContainerDark
import com.mfstracker.app.ui.theme.NagadOrangeContainerLight
import com.mfstracker.app.util.NumberFormatUtils
import java.time.LocalDate
import java.time.LocalTime

/**
 * Modern, high-legibility Material 3 OutlinedTextField with soft rounded corners (12.dp),
 * crisp BDT ৳ currency prefix marker, enlarged bold numeric font (17.sp), and >= 52.dp touch target.
 */
@Composable
fun ModernDecimalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "0.00",
    readOnly: Boolean = false,
    showPrefix: Boolean = true,
    currencyPrefix: String = "৳",
    imeAction: ImeAction = ImeAction.Next
) {
    val focusManager = LocalFocusManager.current
    val shape = RoundedCornerShape(12.dp)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = 54.dp),
        readOnly = readOnly,
        singleLine = true,
        shape = shape,
        leadingIcon = if (showPrefix) {
            {
                Text(
                    text = currencyPrefix,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (currencyPrefix.length > 1) 14.sp else 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        } else null,
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurface
        ),
        placeholder = {
            Text(
                text = placeholderText,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    textAlign = TextAlign.End
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = if (readOnly) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = if (readOnly) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * Enlarged, readable soft-corner text field (16.sp – 17.sp) for Descriptions, Amounts, and Notes
 * with minimum 52.dp tap target.
 */
@Composable
fun ModernTextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isDecimal: Boolean = false,
    showPrefix: Boolean = false,
    currencyPrefix: String = "৳",
    imeAction: ImeAction = ImeAction.Next
) {
    val focusManager = LocalFocusManager.current
    val shape = RoundedCornerShape(12.dp)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = if (isDecimal) TextAlign.End else TextAlign.Start
                ),
                modifier = if (isDecimal) Modifier.fillMaxWidth() else Modifier,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = if (showPrefix) {
            {
                Text(
                    text = currencyPrefix,
                    style = TextStyle(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (currencyPrefix.length > 1) 14.sp else 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        } else null,
        singleLine = true,
        shape = shape,
        textStyle = TextStyle(
            fontFamily = if (isDecimal) FontFamily.Monospace else FontFamily.SansSerif,
            fontWeight = if (isDecimal) FontWeight.Bold else FontWeight.Medium,
            fontSize = if (isDecimal) 17.sp else 16.sp,
            textAlign = if (isDecimal) TextAlign.End else TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Text,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.heightIn(min = 54.dp)
    )
}

/**
 * Branded pill chip for MFS providers (bKash rose, Nagad orange, Agent teal)
 * with enlarged 14.sp high-contrast label and >= 48.dp touch-friendly height alignment.
 */
@Composable
fun ProviderBadge(
    provider: String,
    isDark: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier
) {
    val isBkash = provider.contains("bkash", ignoreCase = true)
    val isNagad = provider.contains("nagad", ignoreCase = true)
    val isAgent = provider.contains("agent", ignoreCase = true)

    val (bgColor, textColor) = when {
        isAgent -> Pair(
            if (isDark) AgentTealContainerDark else AgentTealContainerLight,
            if (isDark) Color(0xFF2DD4BF) else AgentTeal
        )
        isBkash -> Pair(
            if (isDark) BkashPinkContainerDark else BkashPinkContainerLight,
            if (isDark) Color(0xFFF472B6) else BkashPink
        )
        isNagad -> Pair(
            if (isDark) NagadOrangeContainerDark else NagadOrangeContainerLight,
            if (isDark) Color(0xFFFB923C) else NagadOrange
        )
        else -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val shape = RoundedCornerShape(10.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.35f), shape)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = provider,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            textAlign = TextAlign.Center,
            color = textColor
        )
    }
}

/**
 * Sleek 2-way Segmented Button / Pill Selector for Expense vs Income
 * with 48.dp minimum tap target and 14.sp high-contrast typography.
 */
@Composable
fun ModernSegmentedTransactionToggle(
    selectedType: TransactionType,
    isDark: Boolean = isSystemInDarkTheme(),
    onSelect: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val expSelected = selectedType == TransactionType.EXPENSE
    val incSelected = selectedType == TransactionType.INCOME

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expense segment
            val expBg = if (expSelected) {
                if (isDark) ExpenseContainerDark else ExpenseContainerLight
            } else Color.Transparent
            val expFg = if (expSelected) ExpenseColor else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .defaultMinSize(minWidth = 88.dp, minHeight = 40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(expBg)
                    .clickable { onSelect(TransactionType.EXPENSE) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (expSelected) {
                        Icon(
                            imageVector = TrackerIcons.Check,
                            contentDescription = null,
                            tint = expFg,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (expSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = expFg
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Income segment
            val incBg = if (incSelected) {
                if (isDark) IncomeContainerDark else IncomeContainerLight
            } else Color.Transparent
            val incFg = if (incSelected) IncomeColor else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .defaultMinSize(minWidth = 88.dp, minHeight = 40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(incBg)
                    .clickable { onSelect(TransactionType.INCOME) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (incSelected) {
                        Icon(
                            imageVector = TrackerIcons.Check,
                            contentDescription = null,
                            tint = incFg,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (incSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = incFg
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = NumberFormatUtils.localDateToUtcPickerMillis(initialDate)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(NumberFormatUtils.utcPickerMillisToLocalDate(millis))
                    }
                    onDismiss()
                },
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Confirm", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerTimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Time",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                    onDismiss()
                },
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Confirm", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}
