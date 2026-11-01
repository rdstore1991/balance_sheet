package com.mfstracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mfstracker.app.ui.TrackerScreen
import com.mfstracker.app.ui.TrackerViewModel
import com.mfstracker.app.ui.theme.MFSFundExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: TrackerViewModel = viewModel(factory = TrackerViewModel.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = uiState.darkModePreference ?: systemDark

            val safCreateDocumentLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("text/csv")
            ) { uri ->
                viewModel.completeSafExport(uri)
            }

            MFSFundExpenseTrackerTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TrackerScreen(
                        uiState = uiState,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { viewModel.toggleTheme(isDarkTheme) },
                        onOpenHistory = { viewModel.setHistoryVisible(true) },
                        onDismissHistory = { viewModel.setHistoryVisible(false) },
                        onDateSelected = viewModel::updateSelectedDate,
                        onTimeSelected = viewModel::updateSelectedTime,
                        onStartNewSheet = viewModel::startNewSheet,
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
                        onSaveAndExport = viewModel::saveAndExportToStorage,
                        onLoadHistoryRecord = viewModel::loadRecordFromHistory,
                        onExportHistoryRecord = viewModel::exportHistoryRecordToDocuments,
                        onExportHistoryViaSaf = { record ->
                            val suggestedName = viewModel.prepareSafExportForRecord(record)
                            safCreateDocumentLauncher.launch(suggestedName)
                        },
                        onDeleteHistoryRecord = viewModel::deleteHistoryRecord,
                        onStatusConsumed = viewModel::clearStatusMessage
                    )
                }
            }
        }
    }
}
