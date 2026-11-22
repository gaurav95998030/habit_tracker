package io.github.gaurav95998030.habittracker.commons.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.gaurav95998030.habittracker.ui.theme.AppShapes
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme

/**
 * Aurora modal bottom sheet: raised surface, 28dp top corners, design grip and
 * scrim. The sheet keeps clear of the status bar; content handles nav-bar/IME
 * insets itself so sheets can pin footers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HfBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = AppTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = AppShapes.sheet, topEnd = AppShapes.sheet),
        containerColor = if (c.isDark) c.surface2 else c.surface,
        contentColor = c.textPrimary,
        scrimColor = c.scrim,
        tonalElevation = androidx.compose.ui.unit.Dp.Hairline,
        dragHandle = { HfSheetGrip() },
        contentWindowInsets = { WindowInsets.statusBars },
        content = content,
    )
}
