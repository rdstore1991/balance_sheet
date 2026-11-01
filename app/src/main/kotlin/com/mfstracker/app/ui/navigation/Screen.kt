package com.mfstracker.app.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.mfstracker.app.ui.components.TrackerIcons

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "MFS Dashboard", TrackerIcons.Dashboard)
    data object Editor : Screen("editor", "Record Entry", TrackerIcons.Wallet)
    data object History : Screen("history", "History & Ledger", TrackerIcons.History)
    data object ExportCsv : Screen("export_csv", "Save & Export CSV", TrackerIcons.Download)
    data object Settings : Screen("settings", "Options / Settings", TrackerIcons.Settings)
}

