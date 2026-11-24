package io.github.gaurav95998030.habittracker.commons.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons

/** Back button + title/subtitle + optional trailing icon (the design's FHeader). */
@Composable
fun FeatureHeader(title: String, subtitle: String?, onBack: () -> Unit, icon: String? = null, trailing: (@Composable () -> Unit)? = null) {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HfIconButton(PhIcons.ArrowLeft, onClick = onBack, size = 40.dp, iconSize = 18.dp, tint = c.textPrimary, contentDescription = "Back")
        HSpace(12.dp)
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTheme.textStyles.titleLarge.copy(fontSize = 22.sp, letterSpacing = (-0.4).sp), color = c.textPrimary, maxLines = 1)
            if (subtitle != null) Text(subtitle, style = AppTheme.textStyles.bodySmall, color = c.textTertiary, maxLines = 1)
        }
        trailing?.invoke()
        if (icon != null) PhIcon(icon, size = 24.dp, tint = c.primary, weight = PhWeight.Fill)
    }
}

/** Full-screen, scrollable page with a [FeatureHeader] — used by the non-tab feature routes. */
@Composable
fun FeatureScreen(
    title: String,
    subtitle: String?,
    onBack: () -> Unit,
    icon: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize().background(AppTheme.colors.background).statusBarsPadding()) {
        FeatureHeader(title, subtitle, onBack, icon, trailing)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 40.dp),
            content = content,
        )
    }
}

/** Titled card section (the design's Panel). */
@Composable
fun HfPanel(title: String, subtitle: String? = null, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = AppTheme.colors
    HfCard(modifier.fillMaxWidth().padding(top = 14.dp), padding = PaddingValues(18.dp)) {
        Column(Modifier.padding(bottom = 16.dp)) {
            Text(title, style = AppTheme.textStyles.titleMedium.copy(fontSize = 16.sp, letterSpacing = (-0.2).sp), color = c.textPrimary)
            if (subtitle != null) Text(subtitle, style = AppTheme.textStyles.bodySmall, color = c.textTertiary, modifier = Modifier.padding(top = 2.dp))
        }
        content()
    }
}
