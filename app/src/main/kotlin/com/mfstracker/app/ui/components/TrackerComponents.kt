package com.mfstracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
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
import com.mfstracker.app.ui.theme.ExpenseContainerDark
import com.mfstracker.app.ui.theme.ExpenseContainerLight
import com.mfstracker.app.ui.theme.ExpenseDark
import com.mfstracker.app.ui.theme.ExpenseLight
import com.mfstracker.app.ui.theme.IncomeContainerDark
import com.mfstracker.app.ui.theme.IncomeContainerLight
import com.mfstracker.app.ui.theme.IncomeDark
import com.mfstracker.app.ui.theme.IncomeLight
import com.mfstracker.app.util.NumberFormatUtils
import java.time.LocalDate
import java.time.LocalTime

/**
 * High-contrast Material 3 OutlinedTextField strictly configured for Decimal numeric input.
 * Used in Part 1 (MFS Account Balances) and Part 3 (Reconciliation & MSA Summary).
 */
@Composable
fun DecimalOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "0.00",
    readOnly: Boolean = false,
    imeAction: ImeAction = ImeAction.Next
) {
    val focusManager = LocalFocusManager.current
    val shape = RoundedCornerShape(4.dp)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        readOnly = readOnly,
        singleLine = true,
        shape = shape,
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = if (readOnly) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurface
        ),
        placeholder = {
            Text(
                text = placeholderText,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
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
            focusedContainerColor = if (readOnly) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            },
            unfocusedContainerColor = if (readOnly) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    )
}

/**
 * Compact high-contrast bordered input field for multi-column table rows in Part 2
 * (Description, Amount, Notes) so digits and text remain fully legible on narrow mobile screens.
 */
@Composable
fun CompactTableInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isDecimal: Boolean = false,
    imeAction: ImeAction = ImeAction.Next
) {
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor = if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    val borderWidth = if (isFocused) 2.dp else 1.dp
    val shape = RoundedCornerShape(4.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .heightIn(min = 38.dp)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(borderWidth, borderColor, shape)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        singleLine = true,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        textStyle = TextStyle(
            fontFamily = if (isDecimal) FontFamily.Monospace else FontFamily.SansSerif,
            fontWeight = if (isDecimal) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp,
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
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = if (isDecimal) Alignment.CenterEnd else Alignment.CenterStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontFamily = if (isDecimal) FontFamily.Monospace else FontFamily.SansSerif,
                            fontSize = 12.sp,
                            textAlign = if (isDecimal) TextAlign.End else TextAlign.Start,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        }
    )
}

/**
 * Compact [Exp | Inc] segmented toggle for each row in Part 2.
 */
@Composable
fun ExpenseIncomeToggle(
    selectedType: TransactionType,
    isDark: Boolean = isSystemInDarkTheme(),
    onSelect: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(4.dp)
    val expSelected = selectedType == TransactionType.EXPENSE
    val incSelected = selectedType == TransactionType.INCOME

    val expBg = if (expSelected) {
        if (isDark) ExpenseContainerDark else ExpenseContainerLight
    } else {
        MaterialTheme.colorScheme.surface
    }
    val expText = if (expSelected) {
        if (isDark) ExpenseDark else ExpenseLight
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val incBg = if (incSelected) {
        if (isDark) IncomeContainerDark else IncomeContainerLight
    } else {
        MaterialTheme.colorScheme.surface
    }
    val incText = if (incSelected) {
        if (isDark) IncomeDark else IncomeLight
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(expBg)
                .clickable { onSelect(TransactionType.EXPENSE) }
                .padding(vertical = 3.dp, horizontal = 2.dp)
        ) {
            Text(
                text = "EXP",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (expSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = expText
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.outline)
                .padding(top = 1.dp)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(incBg)
                .clickable { onSelect(TransactionType.INCOME) }
                .padding(vertical = 3.dp, horizontal = 2.dp)
        ) {
            Text(
                text = "INC",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (incSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = incText
            )
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
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
                text = "Select Entry Time",
                style = MaterialTheme.typography.titleMedium
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
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

