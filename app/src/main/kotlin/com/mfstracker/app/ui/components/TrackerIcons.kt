package com.mfstracker.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Minimal, dependency-free Material 24dp vector icons without heavy external dependencies.
 */
object TrackerIcons {

    val Wallet: ImageVector by lazy {
        ImageVector.Builder(
            name = "Wallet",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 7.28f)
                verticalLineTo(5f)
                curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
                horizontalLineTo(5f)
                curveTo(3.89f, 3f, 3f, 3.9f, 3f, 5f)
                verticalLineTo(19f)
                curveTo(3f, 20.1f, 3.89f, 21f, 5f, 21f)
                horizontalLineTo(19f)
                curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
                verticalLineTo(16.72f)
                curveTo(21.59f, 16.37f, 22f, 15.74f, 22f, 15f)
                verticalLineTo(9f)
                curveTo(22f, 8.26f, 21.59f, 7.63f, 21f, 7.28f)
                close()
                moveTo(5f, 5f)
                horizontalLineTo(19f)
                verticalLineTo(6f)
                horizontalLineTo(5f)
                close()
                moveTo(20f, 15f)
                horizontalLineTo(16f)
                curveTo(14.9f, 15f, 14f, 14.1f, 14f, 13f)
                verticalLineTo(11f)
                curveTo(14f, 9.9f, 14.9f, 9f, 16f, 9f)
                horizontalLineTo(20f)
                verticalLineTo(15f)
                close()
                moveTo(16f, 13f)
                curveTo(15.45f, 13f, 15f, 12.55f, 15f, 12f)
                curveTo(15f, 11.45f, 15.45f, 11f, 16f, 11f)
                curveTo(16.55f, 11f, 17f, 11.45f, 17f, 12f)
                curveTo(17f, 12.55f, 16.55f, 13f, 16f, 13f)
                close()
            }
        }.build()
    }

    val SimCard: ImageVector by lazy {
        ImageVector.Builder(
            name = "SimCard",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19.99f, 4f)
                curveTo(19.99f, 2.9f, 19.1f, 2f, 18f, 2f)
                horizontalLineTo(9.83f)
                curveTo(9.3f, 2f, 8.79f, 2.21f, 8.42f, 2.59f)
                lineTo(4.59f, 6.41f)
                curveTo(4.21f, 6.79f, 4f, 7.3f, 4f, 7.83f)
                verticalLineTo(20f)
                curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
                horizontalLineTo(18f)
                curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
                lineTo(19.99f, 4f)
                close()
                moveTo(9f, 19f)
                horizontalLineTo(7f)
                verticalLineTo(17f)
                horizontalLineTo(9f)
                verticalLineTo(19f)
                close()
                moveTo(17f, 19f)
                horizontalLineTo(15f)
                verticalLineTo(17f)
                horizontalLineTo(17f)
                verticalLineTo(19f)
                close()
                moveTo(13f, 19f)
                horizontalLineTo(11f)
                verticalLineTo(15f)
                horizontalLineTo(13f)
                verticalLineTo(19f)
                close()
                moveTo(9f, 15f)
                horizontalLineTo(7f)
                verticalLineTo(11f)
                horizontalLineTo(9f)
                verticalLineTo(15f)
                close()
                moveTo(13f, 13f)
                horizontalLineTo(11f)
                verticalLineTo(11f)
                horizontalLineTo(13f)
                verticalLineTo(13f)
                close()
                moveTo(17f, 15f)
                horizontalLineTo(15f)
                verticalLineTo(11f)
                horizontalLineTo(17f)
                verticalLineTo(15f)
                close()
            }
        }.build()
    }

    val LightMode: ImageVector by lazy {
        ImageVector.Builder(
            name = "LightMode",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
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

    val Check: ImageVector by lazy {
        ImageVector.Builder(
            name = "Check",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(9f, 16.17f)
                lineTo(4.83f, 12f)
                lineTo(3.41f, 13.41f)
                lineTo(9f, 19f)
                lineTo(21f, 7f)
                lineTo(19.59f, 5.59f)
                close()
            }
        }.build()
    }

    val Menu: ImageVector by lazy {
        ImageVector.Builder(
            name = "Menu",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 6f)
                horizontalLineTo(21f)
                verticalLineTo(8f)
                horizontalLineTo(3f)
                close()
                moveTo(3f, 11f)
                horizontalLineTo(21f)
                verticalLineTo(13f)
                horizontalLineTo(3f)
                close()
                moveTo(3f, 16f)
                horizontalLineTo(21f)
                verticalLineTo(18f)
                horizontalLineTo(3f)
                close()
            }
        }.build()
    }

    val ArrowBack: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArrowBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 11f)
                horizontalLineTo(7.83f)
                lineTo(13.42f, 5.41f)
                lineTo(12f, 4f)
                lineTo(4f, 12f)
                lineTo(12f, 20f)
                lineTo(13.41f, 18.59f)
                lineTo(7.83f, 13f)
                horizontalLineTo(20f)
                verticalLineTo(11f)
                close()
            }
        }.build()
    }

    val ChevronRight: ImageVector by lazy {
        ImageVector.Builder(
            name = "ChevronRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(10f, 6f)
                lineTo(8.59f, 7.41f)
                lineTo(13.17f, 12f)
                lineTo(8.59f, 16.59f)
                lineTo(10f, 18f)
                lineTo(16f, 12f)
                close()
            }
        }.build()
    }

    val Dashboard: ImageVector by lazy {
        ImageVector.Builder(
            name = "Dashboard",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 13f)
                horizontalLineTo(11f)
                verticalLineTo(3f)
                horizontalLineTo(3f)
                verticalLineTo(13f)
                close()
                moveTo(3f, 21f)
                horizontalLineTo(11f)
                verticalLineTo(15f)
                horizontalLineTo(3f)
                verticalLineTo(21f)
                close()
                moveTo(13f, 21f)
                horizontalLineTo(21f)
                verticalLineTo(11f)
                horizontalLineTo(13f)
                verticalLineTo(21f)
                close()
                moveTo(13f, 3f)
                verticalLineTo(9f)
                horizontalLineTo(21f)
                verticalLineTo(3f)
                horizontalLineTo(13f)
                close()
            }
        }.build()
    }

    val Search: ImageVector by lazy {
        ImageVector.Builder(
            name = "Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(15.5f, 14f)
                horizontalLineTo(14.71f)
                lineTo(14.43f, 13.73f)
                curveTo(15.41f, 12.59f, 16f, 11.11f, 16f, 9.5f)
                curveTo(16f, 5.91f, 13.09f, 3f, 9.5f, 3f)
                reflectiveCurveTo(3f, 5.91f, 3f, 9.5f)
                reflectiveCurveTo(5.91f, 16f, 9.5f, 16f)
                curveTo(11.11f, 16f, 12.59f, 15.41f, 13.73f, 14.43f)
                lineTo(14f, 14.71f)
                verticalLineTo(15.5f)
                lineTo(19f, 20.49f)
                lineTo(20.49f, 19f)
                lineTo(15.5f, 14f)
                close()
                moveTo(9.5f, 14f)
                curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f)
                reflectiveCurveTo(7.01f, 5f, 9.5f, 5f)
                reflectiveCurveTo(14f, 7.01f, 14f, 9.5f)
                reflectiveCurveTo(11.99f, 14f, 9.5f, 14f)
                close()
            }
        }.build()
    }

    val Settings: ImageVector by lazy {
        ImageVector.Builder(
            name = "Settings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19.14f, 12.94f)
                curveTo(19.18f, 12.64f, 19.2f, 12.33f, 19.2f, 12f)
                curveTo(19.2f, 11.68f, 19.18f, 11.36f, 19.13f, 11.06f)
                lineTo(21.16f, 9.48f)
                curveTo(21.34f, 9.34f, 21.39f, 9.07f, 21.28f, 8.87f)
                lineTo(19.36f, 5.55f)
                curveTo(19.24f, 5.33f, 18.99f, 5.26f, 18.77f, 5.33f)
                lineTo(16.38f, 6.29f)
                curveTo(15.88f, 5.91f, 15.35f, 5.59f, 14.76f, 5.35f)
                lineTo(14.4f, 2.81f)
                curveTo(14.36f, 2.57f, 14.16f, 2.4f, 13.92f, 2.4f)
                horizontalLineTo(10.08f)
                curveTo(9.84f, 2.4f, 9.65f, 2.57f, 9.61f, 2.81f)
                lineTo(9.25f, 5.35f)
                curveTo(8.66f, 5.59f, 8.12f, 5.92f, 7.63f, 6.29f)
                lineTo(5.24f, 5.33f)
                curveTo(5.02f, 5.25f, 4.77f, 5.33f, 4.65f, 5.55f)
                lineTo(2.74f, 8.87f)
                curveTo(2.62f, 9.08f, 2.66f, 9.34f, 2.86f, 9.48f)
                lineTo(4.89f, 11.06f)
                curveTo(4.84f, 11.36f, 4.8f, 11.69f, 4.8f, 12f)
                curveTo(4.8f, 12.31f, 4.82f, 12.64f, 4.87f, 12.94f)
                lineTo(2.84f, 14.52f)
                curveTo(2.66f, 14.66f, 2.61f, 14.93f, 2.72f, 15.13f)
                lineTo(4.64f, 18.45f)
                curveTo(4.76f, 18.67f, 5.01f, 18.74f, 5.23f, 18.67f)
                lineTo(7.62f, 17.71f)
                curveTo(8.12f, 18.09f, 8.65f, 18.41f, 9.24f, 18.65f)
                lineTo(9.6f, 21.19f)
                curveTo(9.65f, 21.43f, 9.84f, 21.6f, 10.08f, 21.6f)
                horizontalLineTo(13.92f)
                curveTo(14.16f, 21.6f, 14.36f, 21.43f, 14.39f, 21.19f)
                lineTo(14.75f, 18.65f)
                curveTo(15.34f, 18.41f, 15.88f, 18.09f, 16.37f, 17.71f)
                lineTo(18.76f, 18.67f)
                curveTo(18.98f, 18.75f, 19.23f, 18.67f, 19.35f, 18.45f)
                lineTo(21.27f, 15.13f)
                curveTo(21.39f, 14.91f, 21.34f, 14.66f, 21.15f, 14.52f)
                lineTo(19.14f, 12.94f)
                close()
                moveTo(12f, 15.6f)
                curveTo(10.02f, 15.6f, 8.4f, 13.98f, 8.4f, 12f)
                reflectiveCurveTo(10.02f, 8.4f, 12f, 8.4f)
                reflectiveCurveTo(15.6f, 10.02f, 15.6f, 12f)
                reflectiveCurveTo(13.98f, 15.6f, 12f, 15.6f)
                close()
            }
        }.build()
    }

    val Close: ImageVector by lazy {
        ImageVector.Builder(
            name = "Close",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 6.41f)
                lineTo(17.59f, 5f)
                lineTo(12f, 10.59f)
                lineTo(6.41f, 5f)
                lineTo(5f, 6.41f)
                lineTo(10.59f, 12f)
                lineTo(5f, 17.59f)
                lineTo(6.41f, 19f)
                lineTo(12f, 13.41f)
                lineTo(17.59f, 19f)
                lineTo(19f, 17.59f)
                lineTo(13.41f, 12f)
                close()
            }
        }.build()
    }

    val Save: ImageVector by lazy {
        ImageVector.Builder(
            name = "Save",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17f, 3f)
                horizontalLineTo(5f)
                curveTo(3.89f, 3f, 3f, 3.9f, 3f, 5f)
                verticalLineTo(19f)
                curveTo(3f, 20.1f, 3.89f, 21f, 5f, 21f)
                horizontalLineTo(19f)
                curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
                verticalLineTo(7f)
                lineTo(17f, 3f)
                close()
                moveTo(12f, 19f)
                curveTo(10.34f, 19f, 9f, 17.66f, 9f, 16f)
                reflectiveCurveTo(10.34f, 13f, 12f, 13f)
                reflectiveCurveTo(15f, 14.34f, 15f, 16f)
                reflectiveCurveTo(13.66f, 19f, 12f, 19f)
                close()
                moveTo(15f, 9f)
                horizontalLineTo(5f)
                verticalLineTo(5f)
                horizontalLineTo(15f)
                verticalLineTo(9f)
                close()
            }
        }.build()
    }
}
