package com.mfstracker.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Minimal, dependency-free Material 24dp vector icons so we avoid pulling in the heavy
 * material-icons-extended artifact.
 */
object TrackerIcons {

    val LightMode: ImageVector by lazy {
        ImageVector.Builder(
            name = "LightMode",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                // Sun center circle + 8 rays
                moveTo(12f, 7f)
                curveTo(9.24f, 7f, 7f, 9.24f, 7f, 12f)
                reflectiveCurveTo(9.24f, 17f, 12f, 17f)
                reflectiveCurveTo(17f, 14.76f, 17f, 12f)
                reflectiveCurveTo(14.76f, 7f, 12f, 7f)
                close()
                moveTo(11f, 1f)
                horizontalLineTo(13f)
                verticalLineTo(4f)
                horizontalLineTo(11f)
                close()
                moveTo(11f, 20f)
                horizontalLineTo(13f)
                verticalLineTo(23f)
                horizontalLineTo(11f)
                close()
                moveTo(1f, 11f)
                horizontalLineTo(4f)
                verticalLineTo(13f)
                horizontalLineTo(1f)
                close()
                moveTo(20f, 11f)
                horizontalLineTo(23f)
                verticalLineTo(13f)
                horizontalLineTo(20f)
                close()
                moveTo(4.22f, 5.64f)
                lineTo(5.64f, 4.22f)
                lineTo(7.76f, 6.34f)
                lineTo(6.34f, 7.76f)
                close()
                moveTo(16.24f, 17.66f)
                lineTo(17.66f, 16.24f)
                lineTo(19.78f, 18.36f)
                lineTo(18.36f, 19.78f)
                close()
                moveTo(18.36f, 4.22f)
                lineTo(19.78f, 5.64f)
                lineTo(17.66f, 7.76f)
                lineTo(16.24f, 6.34f)
                close()
                moveTo(6.34f, 16.24f)
                lineTo(7.76f, 17.66f)
                lineTo(5.64f, 19.78f)
                lineTo(4.22f, 18.36f)
                close()
            }
        }.build()
    }

    val DarkMode: ImageVector by lazy {
        ImageVector.Builder(
            name = "DarkMode",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 3f)
                curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
                reflectiveCurveTo(7.03f, 21f, 12f, 21f)
                curveTo(16.97f, 21f, 21f, 16.97f, 21f, 12f)
                curveTo(21f, 11.54f, 20.96f, 11.08f, 20.9f, 10.64f)
                curveTo(19.92f, 12.01f, 18.32f, 12.9f, 16.5f, 12.9f)
                curveTo(13.52f, 12.9f, 11.1f, 10.48f, 11.1f, 7.5f)
                curveTo(11.1f, 5.68f, 11.99f, 4.08f, 13.36f, 3.1f)
                curveTo(12.92f, 3.04f, 12.46f, 3f, 12f, 3f)
                close()
            }
        }.build()
    }

    val History: ImageVector by lazy {
        ImageVector.Builder(
            name = "History",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(13f, 3f)
                curveTo(8.03f, 3f, 4f, 7.03f, 4f, 12f)
                horizontalLineTo(1f)
                lineTo(4.89f, 15.89f)
                lineTo(4.96f, 16.03f)
                lineTo(9f, 12f)
                horizontalLineTo(6f)
                curveTo(6f, 8.13f, 9.13f, 5f, 13f, 5f)
                reflectiveCurveTo(20f, 8.13f, 20f, 12f)
                reflectiveCurveTo(16.87f, 19f, 13f, 19f)
                curveTo(11.07f, 19f, 9.32f, 18.21f, 8.06f, 16.94f)
                lineTo(6.64f, 18.36f)
                curveTo(8.27f, 19.99f, 10.51f, 21f, 13f, 21f)
                curveTo(17.97f, 21f, 22f, 16.97f, 22f, 12f)
                reflectiveCurveTo(17.97f, 3f, 13f, 3f)
                close()
                moveTo(12f, 8f)
                verticalLineTo(13f)
                lineTo(16.28f, 15.54f)
                lineTo(17f, 14.33f)
                lineTo(13.5f, 12.25f)
                verticalLineTo(8f)
                horizontalLineTo(12f)
                close()
            }
        }.build()
    }

    val Calendar: ImageVector by lazy {
        ImageVector.Builder(
            name = "Calendar",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 4f)
                horizontalLineTo(18f)
                verticalLineTo(2f)
                horizontalLineTo(16f)
                verticalLineTo(4f)
                horizontalLineTo(8f)
                verticalLineTo(2f)
                horizontalLineTo(6f)
                verticalLineTo(4f)
                horizontalLineTo(5f)
                curveTo(3.89f, 4f, 3f, 4.9f, 3f, 6f)
                verticalLineTo(20f)
                curveTo(3f, 21.1f, 3.89f, 22f, 5f, 22f)
                horizontalLineTo(19f)
                curveTo(20.1f, 22f, 21f, 21.1f, 21f, 20f)
                verticalLineTo(6f)
                curveTo(21f, 4.9f, 20.1f, 4f, 19f, 4f)
                close()
                moveTo(19f, 20f)
                horizontalLineTo(5f)
                verticalLineTo(10f)
                horizontalLineTo(19f)
                verticalLineTo(20f)
                close()
                moveTo(19f, 8f)
                horizontalLineTo(5f)
                verticalLineTo(6f)
                horizontalLineTo(19f)
                verticalLineTo(8f)
                close()
            }
        }.build()
    }

    val Clock: ImageVector by lazy {
        ImageVector.Builder(
            name = "Clock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                curveTo(6.5f, 2f, 2f, 6.5f, 2f, 12f)
                reflectiveCurveTo(6.5f, 22f, 12f, 22f)
                reflectiveCurveTo(22f, 17.5f, 22f, 12f)
                reflectiveCurveTo(17.5f, 2f, 12f, 2f)
                close()
                moveTo(12f, 20f)
                curveTo(7.59f, 20f, 4f, 16.41f, 4f, 12f)
                reflectiveCurveTo(7.59f, 4f, 12f, 4f)
                reflectiveCurveTo(20f, 7.59f, 20f, 12f)
                reflectiveCurveTo(16.41f, 20f, 12f, 20f)
                close()
                moveTo(12.5f, 7f)
                horizontalLineTo(11f)
                verticalLineTo(13f)
                lineTo(16.2f, 16.2f)
                lineTo(17f, 14.9f)
                lineTo(12.5f, 12.2f)
                verticalLineTo(7f)
                close()
            }
        }.build()
    }

    val Delete: ImageVector by lazy {
        ImageVector.Builder(
            name = "Delete",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 19f)
                curveTo(6f, 20.1f, 6.9f, 21f, 8f, 21f)
                horizontalLineTo(16f)
                curveTo(17.1f, 21f, 18f, 20.1f, 18f, 19f)
                verticalLineTo(7f)
                horizontalLineTo(6f)
                verticalLineTo(19f)
                close()
                moveTo(19f, 4f)
                horizontalLineTo(15.5f)
                lineTo(14.5f, 3f)
                horizontalLineTo(9.5f)
                lineTo(8.5f, 4f)
                horizontalLineTo(5f)
                verticalLineTo(6f)
                horizontalLineTo(19f)
                verticalLineTo(4f)
                close()
            }
        }.build()
    }

    val Add: ImageVector by lazy {
        ImageVector.Builder(
            name = "Add",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 13f)
                horizontalLineTo(13f)
                verticalLineTo(19f)
                horizontalLineTo(11f)
                verticalLineTo(13f)
                horizontalLineTo(5f)
                verticalLineTo(11f)
                horizontalLineTo(11f)
                verticalLineTo(5f)
                horizontalLineTo(13f)
                verticalLineTo(11f)
                horizontalLineTo(19f)
                verticalLineTo(13f)
                close()
            }
        }.build()
    }

    val Download: ImageVector by lazy {
        ImageVector.Builder(
            name = "Download",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 9f)
                horizontalLineTo(15f)
                verticalLineTo(3f)
                horizontalLineTo(9f)
                verticalLineTo(9f)
                horizontalLineTo(5f)
                lineTo(12f, 16f)
                lineTo(19f, 9f)
                close()
                moveTo(5f, 18f)
                verticalLineTo(20f)
                horizontalLineTo(19f)
                verticalLineTo(18f)
                horizontalLineTo(5f)
                close()
            }
        }.build()
    }
}

