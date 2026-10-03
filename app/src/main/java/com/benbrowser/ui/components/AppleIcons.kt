package com.benbrowser.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom Apple SF-style vector icons to ensure zero dependency on material-icons-extended.
 */
object AppleIcons {
    val BookmarkOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "BookmarkOutline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f
            ) {
                moveTo(6f, 4.5f)
                curveTo(6f, 3.67f, 6.67f, 3f, 7.5f, 3f)
                horizontalLineTo(16.5f)
                curveTo(17.33f, 3f, 18f, 3.67f, 18f, 4.5f)
                verticalLineTo(21f)
                lineTo(12f, 16.5f)
                lineTo(6f, 21f)
                close()
            }
        }.build()
    }

    val BookmarkFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "BookmarkFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF0A84FF))
            ) {
                moveTo(6f, 4.5f)
                curveTo(6f, 3.67f, 6.67f, 3f, 7.5f, 3f)
                horizontalLineTo(16.5f)
                curveTo(17.33f, 3f, 18f, 3.67f, 18f, 4.5f)
                verticalLineTo(21f)
                lineTo(12f, 16.5f)
                lineTo(6f, 21f)
                close()
            }
        }.build()
    }

    val BookmarksList: ImageVector by lazy {
        ImageVector.Builder(
            name = "BookmarksList",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f
            ) {
                // Open book shape (Safari bookmarks symbol)
                moveTo(4f, 5.5f)
                curveTo(4f, 4.67f, 4.67f, 4f, 5.5f, 4f)
                horizontalLineTo(11f)
                verticalLineTo(19.5f)
                horizontalLineTo(5.5f)
                curveTo(4.67f, 19.5f, 4f, 18.83f, 4f, 18f)
                close()

                moveTo(13f, 4f)
                horizontalLineTo(18.5f)
                curveTo(19.33f, 4f, 20f, 4.67f, 20f, 5.5f)
                verticalLineTo(18f)
                curveTo(20f, 18.83f, 19.33f, 19.5f, 18.5f, 19.5f)
                horizontalLineTo(13f)
                close()
            }
        }.build()
    }

    val Lock: ImageVector by lazy {
        ImageVector.Builder(
            name = "Lock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White)
            ) {
                moveTo(18f, 10f)
                horizontalLineTo(17f)
                verticalLineTo(7f)
                curveTo(17f, 4.24f, 14.76f, 2f, 12f, 2f)
                curveTo(9.24f, 2f, 7f, 4.24f, 7f, 7f)
                verticalLineTo(10f)
                horizontalLineTo(6f)
                curveTo(4.9f, 10f, 4f, 10.9f, 4f, 12f)
                verticalLineTo(20f)
                curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
                horizontalLineTo(18f)
                curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
                verticalLineTo(12f)
                curveTo(20f, 10.9f, 19.1f, 10f, 18f, 10f)
                close()

                moveTo(9f, 7f)
                curveTo(9f, 5.34f, 10.34f, 4f, 12f, 4f)
                curveTo(13.66f, 4f, 15f, 5.34f, 15f, 7f)
                verticalLineTo(10f)
                horizontalLineTo(9f)
                verticalLineTo(7f)
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
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f
            ) {
                moveTo(10.5f, 18f)
                curveTo(14.64f, 18f, 18f, 14.64f, 18f, 10.5f)
                curveTo(18f, 6.36f, 14.64f, 3f, 10.5f, 3f)
                curveTo(6.36f, 3f, 3f, 6.36f, 3f, 10.5f)
                curveTo(3f, 14.64f, 6.36f, 18f, 10.5f, 18f)
                close()

                moveTo(16f, 16f)
                lineTo(21f, 21f)
            }
        }.build()
    }

    val Home: ImageVector by lazy {
        ImageVector.Builder(
            name = "Home",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f
            ) {
                moveTo(3f, 11f)
                lineTo(12f, 4f)
                lineTo(21f, 11f)
                verticalLineTo(19.5f)
                curveTo(21f, 20.33f, 20.33f, 21f, 19.5f, 21f)
                horizontalLineTo(14.5f)
                verticalLineTo(14.5f)
                horizontalLineTo(9.5f)
                verticalLineTo(21f)
                horizontalLineTo(4.5f)
                curveTo(3.67f, 21f, 3f, 20.33f, 3f, 19.5f)
                close()
            }
        }.build()
    }
}
