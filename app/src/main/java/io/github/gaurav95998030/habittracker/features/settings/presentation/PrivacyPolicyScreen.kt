package io.github.gaurav95998030.habittracker.features.settings.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.commons.ui.FeatureScreen
import io.github.gaurav95998030.habittracker.commons.ui.HfButton
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonVariant
import io.github.gaurav95998030.habittracker.features.settings.domain.model.PrivacyPolicy
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons

private val URL = Regex("""https?://\S+""")

/** In-app copy of the privacy policy (same text as docs/privacy-policy.html). */
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val c = AppTheme.colors
    val context = LocalContext.current
    FeatureScreen("Privacy policy", "Effective ${PrivacyPolicy.EFFECTIVE_DATE}", onBack, icon = PhIcons.Eye) {
        PrivacyPolicy.sections.forEach { section ->
            Text(
                section.title,
                style = AppTheme.textStyles.titleMedium.copy(fontSize = 17.sp),
                fontWeight = FontWeight.SemiBold,
                color = c.textPrimary,
                modifier = Modifier.padding(top = 22.dp, bottom = 6.dp),
            )
            section.paragraphs.forEach { p ->
                Text(p, style = AppTheme.textStyles.bodyMedium.copy(lineHeight = 21.sp), color = c.textSecondary, modifier = Modifier.padding(bottom = 8.dp))
            }
            section.bullets.forEach { b ->
                val link = URL.find(b)?.value
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.Top) {
                    Box(Modifier.padding(top = 8.dp, end = 10.dp).size(5.dp).clip(CircleShape).background(c.primary))
                    Text(
                        buildAnnotatedString {
                            if (link == null) append(b) else {
                                append(b.substringBefore(link))
                                withStyle(SpanStyle(color = c.primary, textDecoration = TextDecoration.Underline)) { append(link) }
                                append(b.substringAfter(link))
                            }
                        },
                        style = AppTheme.textStyles.bodyMedium.copy(lineHeight = 21.sp),
                        color = c.textSecondary,
                        modifier = if (link != null) Modifier.clickable(onClickLabel = "Open link") {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                        } else Modifier,
                    )
                }
            }
        }
        if (!PrivacyPolicy.CONTACT_EMAIL.startsWith("[")) {
            HfButton(
                "Email ${PrivacyPolicy.CONTACT_EMAIL}",
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${PrivacyPolicy.CONTACT_EMAIL}")))
                },
                variant = HfButtonVariant.Secondary,
                icon = PhIcons.ArrowRight,
                fullWidth = true,
                modifier = Modifier.padding(top = 18.dp),
            )
        }
    }
}

