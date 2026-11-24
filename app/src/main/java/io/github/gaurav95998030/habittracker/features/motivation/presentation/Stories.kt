package io.github.gaurav95998030.habittracker.features.motivation.presentation

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.gaurav95998030.habittracker.commons.ui.HfButton
import io.github.gaurav95998030.habittracker.commons.ui.HfButtonSize
import io.github.gaurav95998030.habittracker.commons.ui.PhIcon
import io.github.gaurav95998030.habittracker.commons.ui.PhIconNamed
import io.github.gaurav95998030.habittracker.commons.ui.PhWeight
import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.motivation.domain.DayTally
import io.github.gaurav95998030.habittracker.features.motivation.domain.Quote
import io.github.gaurav95998030.habittracker.features.motivation.domain.Quotes
import io.github.gaurav95998030.habittracker.features.motivation.domain.ReviewStats
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.AppTheme
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import io.github.gaurav95998030.habittracker.ui.theme.StoryPalette
import io.github.gaurav95998030.habittracker.ui.theme.glow
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** The stories offered on Home (and Year from Insights). */
enum class StoryKind { Boost, Week, Month, Year }

private fun DayTally.initial() = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
private fun monthName(m: Int, style: TextStyle = TextStyle.FULL) = java.time.Month.of(m).getDisplayName(style, Locale.getDefault())
private val RANGE: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

@RequiresApi(Build.VERSION_CODES.O)
fun weekRangeLabel(today: LocalDate = LocalDate.now()) = "${today.minusDays(6).format(RANGE)} – ${today.format(RANGE)}"

@RequiresApi(Build.VERSION_CODES.O)
fun lastMonthName(today: LocalDate = LocalDate.now()) = monthName(today.minusMonths(1).monthValue)

/** Opens [kind] as a full-screen story. [year] is only used for [StoryKind.Year]. */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MotivationStory(
    kind: StoryKind,
    habits: List<Habit>,
    userName: String,
    savedQuotes: Set<String>,
    onToggleQuote: (String, Boolean) -> Unit,
    onClose: () -> Unit,
    year: Int = LocalDate.now().year,
) {
    val name = userName.trim().ifBlank { "You" }
    when (kind) {
        StoryKind.Boost -> StoryViewer(
            slides = remember(habits) { boostSlides(habits, name) }.withSaved(savedQuotes, onToggleQuote),
            title = "Daily boost", subtitle = "Today", color = StoryPalette.lime, icon = PhIcons.Sparkle, onClose = onClose,
        )
        StoryKind.Week -> StoryViewer(
            slides = remember(habits) { weekSlides(habits, name) },
            title = "Your week", subtitle = weekRangeLabel(), color = StoryPalette.sky, icon = PhIcons.CalendarCheck, onClose = onClose,
        )
        StoryKind.Month -> StoryViewer(
            slides = remember(habits) { monthSlides(habits, name) },
            title = "${lastMonthName()} review", subtitle = "Monthly recap", color = StoryPalette.amber, icon = PhIcons.CalendarDots, onClose = onClose,
        )
        StoryKind.Year -> StoryViewer(
            slides = remember(habits, year) { yearSlides(habits, name, year) },
            title = "HabitForge Wrapped", subtitle = "$year", color = StoryPalette.lime, icon = PhIcons.Trophy, onClose = onClose,
        )
    }
}

// Boost quote slides need live "saved" state; the rest is static per open.
private class QuoteSlot(val quote: Quote, val kicker: String, val color: Color, val tint: Color, val center: Offset)

private class BoostSlides(val quotes: List<QuoteSlot>, val forYou: StorySlide)

@Composable
private fun BoostSlides.withSaved(saved: Set<String>, onToggle: (String, Boolean) -> Unit): List<StorySlide> =
    quotes.map { q ->
        StorySlide(q.tint, q.center) { QuoteSlide(q, Quotes.storageKey(q.quote) in saved, onToggle) }
    } + forYou

@Composable
private fun ColumnScope.QuoteSlide(q: QuoteSlot, saved: Boolean, onToggle: (String, Boolean) -> Unit) {
    val context = LocalContext.current
    StoryKicker(q.kicker, q.color)
    Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.Center) {
        PhIcon(PhIcons.Quotes, size = 46.dp, tint = q.color, weight = PhWeight.Fill, modifier = Modifier.glow(q.color.copy(alpha = 0.4f), 16.dp, 23.dp))
        Text(
            q.quote.text,
            style = AppTheme.textStyles.titleLarge.copy(fontSize = 31.sp, lineHeight = 38.sp, letterSpacing = (-0.6).sp),
            fontWeight = FontWeight.Medium,
            color = StoryPalette.text,
            modifier = Modifier.padding(top = 18.dp),
        )
        Text("— ${q.quote.author}", style = AppTheme.textStyles.bodyLarge.copy(fontSize = 15.sp), color = StoryPalette.sub, modifier = Modifier.padding(top = 18.dp))
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
        StoryPill(if (saved) "Saved" else "Save", PhIcons.BookmarkSimple, active = saved) {
            onToggle(Quotes.storageKey(q.quote), !saved)
        }
        StoryPill("Share", PhIcons.ShareNetwork, active = false) {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "“${q.quote.text}” — ${q.quote.author}")
            }
            context.startActivity(Intent.createChooser(send, "Share quote"))
        }
    }
}

@Composable
private fun StoryPill(label: String, icon: String, active: Boolean, onClick: () -> Unit) {
    val fg = if (active) StoryPalette.lime else StoryPalette.text
    Row(
        Modifier
            .clip(CircleShape)
            .background(if (active) StoryPalette.lime.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.08f))
            .border(1.dp, if (active) StoryPalette.lime.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.16f), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PhIcon(icon, size = 17.dp, tint = fg, weight = if (active) PhWeight.Fill else PhWeight.Bold)
        Text(label, style = AppTheme.textStyles.labelLarge, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun boostSlides(habits: List<Habit>, name: String): BoostSlides {
    val b = ReviewStats.boost(habits)
    val hour = LocalTime.now().hour
    val hello = if (hour < 12) "Good morning" else if (hour < 18) "Good afternoon" else "Good evening"
    val quotes = listOf(
        QuoteSlot(b.quotes[0], "Daily boost · 1 of 3", StoryPalette.lime, StoryPalette.tintGreen, Offset(0.2f, 0.1f)),
        QuoteSlot(b.quotes[1], "Daily boost · 2 of 3", StoryPalette.mint, StoryPalette.tintTeal, Offset(0.8f, 0.15f)),
    )
    val forYou = StorySlide(StoryPalette.tintAmber, Offset(0.5f, 0f)) { scope ->
        StoryKicker("For you · 3 of 3", StoryPalette.amber)
        StoryBig("$hello,\n$name.", size = 48.sp, modifier = Modifier.padding(top = 22.dp))
        Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.Center) {
            val hero = b.hero
            if (hero != null) {
                val color = Accents.colorOf(hero.habit.color)
                val shape = RoundedCornerShape(26.dp)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .glow(color.copy(alpha = 0.11f), 40.dp, 26.dp)
                        .clip(shape)
                        .background(StoryPalette.glass)
                        .border(1.dp, color.copy(alpha = 0.27f), shape)
                        .padding(20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(color.copy(alpha = 0.13f)),
                            contentAlignment = Alignment.Center,
                        ) { PhIconNamed(HabitCatalog.iconFor(hero.habit), size = 26.dp, tint = color) }
                        Column {
                            Text(hero.habit.name, style = AppTheme.textStyles.titleMedium, fontWeight = FontWeight.SemiBold, color = StoryPalette.text,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 3.dp)) {
                                PhIcon(PhIcons.Fire, size = 15.dp, tint = color, weight = PhWeight.Fill)
                                Text("${hero.streak}-day streak", style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp), color = StoryPalette.sub)
                            }
                        }
                    }
                    Text(
                        if (hero.streak > 0) "One more today makes it ${hero.streak + 1}." else "Check in today to start a fresh streak.",
                        style = AppTheme.textStyles.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp),
                        color = StoryPalette.text,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }
            StoryNote("You have ${b.activeCount} ${if (b.activeCount == 1) "habit" else "habits"} planned today.")
        }
        HfButton("Start today", onClick = scope.close, icon = PhIcons.Lightning, iconWeight = PhWeight.Fill, size = HfButtonSize.Large, fullWidth = true)
    }
    return BoostSlides(quotes, forYou)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun weekSlides(habits: List<Habit>, name: String): List<StorySlide> {
    val w = ReviewStats.week(habits)
    val range = weekRangeLabel()
    val n = w.habitCount
    return listOf(
        StorySlide(StoryPalette.tintBlue, Offset(0.8f, 0f)) {
            StoryKicker("Your week · $range", StoryPalette.sky)
            StoryBig("${w.percent}%", modifier = Modifier.padding(top = 26.dp))
            StoryLine("of habits completed")
            Box(Modifier.padding(top = 16.dp)) { DeltaChip(w.deltaVsPrevious, "vs last week") }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth().height(170.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                w.days.forEachIndexed { k, d ->
                    val best = d == w.best
                    Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.Bottom)) {
                        Text("${d.percent}", style = AppTheme.textStyles.caption, color = StoryPalette.sub)
                        Box(
                            Modifier
                                .weight(1f, fill = false)
                                .fillMaxHeight(maxOf(d.percent, 3) / 100f)
                                .fillMaxWidth()
                                .glow(StoryPalette.sky.copy(alpha = 0.66f), 16.dp, 7.dp, enabled = best)
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (best) StoryPalette.sky else StoryPalette.sky.copy(alpha = 0.35f)),
                        )
                        Text(d.initial(), style = AppTheme.textStyles.caption, fontWeight = FontWeight.SemiBold, color = if (k == 6) StoryPalette.text else StoryPalette.ter)
                    }
                }
            }
        },
        StorySlide(StoryPalette.tintDeepGreen, Offset(0.2f, 0f)) {
            StoryKicker("Best day", StoryPalette.lime)
            StoryBig(w.best.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()), size = 64.sp, modifier = Modifier.padding(top = 24.dp))
            StoryLine("${w.best.done} of $n habits done")
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                    w.days.forEach { d ->
                        val best = d == w.best
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            (n - 1 downTo 0).forEach { j ->
                                val on = j < d.done
                                Box(
                                    Modifier
                                        .size(20.dp)
                                        .glow(StoryPalette.lime.copy(alpha = 0.66f), 10.dp, 6.dp, enabled = on && best)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (on) StoryPalette.lime.copy(alpha = if (best) 1f else 0.55f) else StoryPalette.dim),
                                )
                            }
                            Text(d.initial(), style = AppTheme.textStyles.caption, fontWeight = FontWeight.SemiBold,
                                color = if (best) StoryPalette.text else StoryPalette.ter, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
            StoryNote(
                if (w.perfectDays > 0) "${w.perfectDays} perfect day${if (w.perfectDays > 1) "s" else ""} this week, with every habit done."
                else "Next goal: one perfect day with all $n habits done.",
                Modifier.padding(top = 0.dp),
            )
        },
        StorySlide(StoryPalette.tintAmber, Offset(0.5f, 0f)) {
            StoryKicker("MVP habit", StoryPalette.amber)
            val top = w.ranking.firstOrNull()
            if (top != null) {
                val color = Accents.colorOf(top.habit.color)
                Box(
                    Modifier
                        .padding(top = 28.dp)
                        .size(96.dp)
                        .glow(color.copy(alpha = 0.27f), 50.dp, 28.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(color.copy(alpha = 0.15f))
                        .border(1.dp, color.copy(alpha = 0.33f), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center,
                ) { PhIconNamed(HabitCatalog.iconFor(top.habit), size = 48.dp, tint = color) }
                StoryBig(top.habit.name, size = 40.sp, modifier = Modifier.padding(top = 22.dp))
                StoryLine("${top.count} of 7 days · ${top.streak}-day streak", color = StoryPalette.sub, size = 20.sp)
            }
            Spacer(Modifier.weight(1f))
            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                w.ranking.drop(1).take(4).forEach { hc ->
                    val color = Accents.colorOf(hc.habit.color)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PhIconNamed(HabitCatalog.iconFor(hc.habit), size = 18.dp, tint = color)
                        Text(hc.habit.name, style = AppTheme.textStyles.bodyMedium, color = StoryPalette.text, maxLines = 1,
                            overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Box(Modifier.width(90.dp).height(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f))) {
                            Box(Modifier.fillMaxWidth(hc.count / 7f).fillMaxHeight().clip(CircleShape).background(color))
                        }
                        Text("${hc.count}/7", style = AppTheme.textStyles.titleSmall.copy(fontSize = 14.sp), fontWeight = FontWeight.SemiBold,
                            color = StoryPalette.sub, textAlign = TextAlign.End, modifier = Modifier.width(30.dp))
                    }
                }
            }
        },
        StorySlide(StoryPalette.tintDeepGreen, Offset(0.5f, 1f)) {
            val layer = rememberShareLayer()
            StoryKicker("Your week, wrapped", StoryPalette.lime)
            ShareCard(layer, StoryPalette.lime, Modifier.padding(top = 24.dp)) {
                ShareCardHead("$name · $range")
                ShareStatGrid(listOf(
                    "${w.percent}%" to "completion",
                    "${w.checkIns}" to "check-ins",
                    w.best.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()) to "best day",
                    "${w.ranking.firstOrNull()?.streak ?: 0}" to "top streak",
                ))
                Row(Modifier.fillMaxWidth().padding(top = 20.dp).height(48.dp), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.Bottom) {
                    w.days.forEach { d ->
                        Box(
                            Modifier.weight(1f).fillMaxHeight(maxOf(d.percent, 4) / 100f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(StoryPalette.lime.copy(alpha = 0.35f + 0.65f * d.percent / 100f)),
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            ShareCardButton("Share your week", layer, "habitforge-week")
        },
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun monthSlides(habits: List<Habit>, name: String): List<StorySlide> {
    val m = ReviewStats.month(habits)
    val mName = monthName(m.month.monthValue)
    return listOf(
        StorySlide(StoryPalette.tintAmber, Offset(0.8f, 0f)) {
            StoryKicker("$mName ${m.month.year}", StoryPalette.amber)
            StoryBig("${m.total}", modifier = Modifier.padding(top = 24.dp))
            StoryLine("check-ins across ${m.habitCount} ${if (m.habitCount == 1) "habit" else "habits"}")
            Spacer(Modifier.weight(1f))
            MonthGrid(m.days, m.month.atDay(1).dayOfWeek.value % 7)
        },
        StorySlide(StoryPalette.tintBlue, Offset(0.2f, 0f)) {
            StoryKicker("Completion", StoryPalette.sky)
            StoryBig("${m.percent}%", modifier = Modifier.padding(top = 24.dp))
            StoryLine("of all habits, all month")
            Box(Modifier.padding(top = 16.dp)) { DeltaChip(m.deltaVsPrevious, "vs ${monthName(m.previous.monthValue)}") }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth().height(160.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
                m.weeks.forEachIndexed { k, v ->
                    val best = k == m.bestWeek
                    Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.Bottom)) {
                        Text("$v%", style = AppTheme.textStyles.caption, color = StoryPalette.sub)
                        Box(
                            Modifier.weight(1f, fill = false).fillMaxHeight(maxOf(v, 3) / 100f).fillMaxWidth()
                                .glow(StoryPalette.sky.copy(alpha = 0.66f), 16.dp, 8.dp, enabled = best)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (best) StoryPalette.sky else StoryPalette.sky.copy(alpha = 0.35f)),
                        )
                        Text("Wk ${k + 1}", style = AppTheme.textStyles.caption, fontWeight = FontWeight.SemiBold, color = if (best) StoryPalette.text else StoryPalette.ter)
                    }
                }
            }
            StoryNote("Week ${m.bestWeek + 1} was your strongest at ${m.weeks.getOrElse(m.bestWeek) { 0 }}%.")
        },
        StorySlide(StoryPalette.tintRed, Offset(0.5f, 0f)) {
            StoryKicker("Longest run", StoryPalette.coral)
            val longest = m.longest
            val color = longest?.let { Accents.colorOf(it.habit.color) } ?: StoryPalette.coral
            Row(Modifier.padding(top = 22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PhIcon(PhIcons.Fire, size = 70.dp, tint = color, weight = PhWeight.Fill, modifier = Modifier.glow(color.copy(alpha = 0.5f), 22.dp, 35.dp))
                StoryBig("${longest?.bestRun ?: 0}", size = 110.sp)
            }
            StoryLine(if (longest != null && longest.bestRun > 0) "days of ${longest.habit.name}" else "No runs yet this month")
            Spacer(Modifier.weight(1f))
            StoryKicker("Most consistent", StoryPalette.sub)
            Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                m.mostConsistent.take(3).forEachIndexed { k, hm ->
                    val hc = Accents.colorOf(hm.habit.color)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("${k + 1}", style = AppTheme.textStyles.titleSmall, fontWeight = FontWeight.SemiBold, color = StoryPalette.ter, modifier = Modifier.width(14.dp))
                        Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(hc.copy(alpha = 0.13f)), contentAlignment = Alignment.Center) {
                            PhIconNamed(HabitCatalog.iconFor(hm.habit), size = 17.dp, tint = hc)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(hm.habit.name, style = AppTheme.textStyles.bodyMedium, color = StoryPalette.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Box(Modifier.padding(top = 6.dp).fillMaxWidth().height(5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f))) {
                                Box(Modifier.fillMaxWidth(hm.ratePercent / 100f).fillMaxHeight().clip(CircleShape).background(hc))
                            }
                        }
                        Text("${hm.ratePercent}%", style = AppTheme.textStyles.titleSmall.copy(fontSize = 16.sp), fontWeight = FontWeight.SemiBold,
                            color = StoryPalette.text, textAlign = TextAlign.End, modifier = Modifier.width(48.dp))
                    }
                }
            }
        },
        StorySlide(StoryPalette.tintAmber, Offset(0.5f, 1f)) {
            val layer = rememberShareLayer()
            StoryKicker("$mName, wrapped", StoryPalette.amber)
            ShareCard(layer, StoryPalette.amber, Modifier.padding(top = 24.dp)) {
                ShareCardHead("$name · $mName ${m.month.year}")
                ShareStatGrid(listOf(
                    "${m.percent}%" to "completion",
                    "${m.total}" to "check-ins",
                    "${m.longest?.bestRun ?: 0}" to "longest run",
                    "Wk ${m.bestWeek + 1}" to "best week",
                ))
                HeatStrip(m.days.map { it.percent }, columns = 16, color = StoryPalette.amber, modifier = Modifier.padding(top = 20.dp))
            }
            Spacer(Modifier.weight(1f))
            ShareCardButton("Share your month", layer, "habitforge-month")
        },
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun yearSlides(habits: List<Habit>, name: String, year: Int): List<StorySlide> {
    val y = ReviewStats.year(habits, year)
    val bestMonthName = monthName(y.bestMonth + 1)
    val span = if (y.longestFrom != null && y.longestTo != null)
        "From ${y.longestFrom.format(DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault()))} to " +
            "${y.longestTo.format(DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault()))}, without a single miss."
    else "Your first streak is one check-in away."
    return listOf(
        StorySlide(StoryPalette.tintGreen, Offset(0.2f, 0.1f)) {
            StoryKicker("${name.uppercase()}’S $year", StoryPalette.lime)
            StoryBig("${y.daysShowedUp}", size = 150.sp, modifier = Modifier.padding(top = 30.dp))
            StoryLine("days you showed up", size = 26.sp)
            StoryNote("That’s ${"%,d".format(y.checkIns)} check-ins across ${y.habitCount} ${if (y.habitCount == 1) "habit" else "habits"}.")
            Spacer(Modifier.weight(1f))
            Text("Tap to continue", style = AppTheme.textStyles.bodyMedium.copy(fontSize = 13.sp), color = StoryPalette.ter)
        },
        StorySlide(StoryPalette.tintAmber, Offset(0.8f, 0.2f)) {
            StoryKicker("Longest streak", StoryPalette.amber)
            Row(Modifier.padding(top = 30.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                PhIcon(PhIcons.Fire, size = 84.dp, tint = StoryPalette.amber, weight = PhWeight.Fill, modifier = Modifier.glow(StoryPalette.amber.copy(alpha = 0.5f), 24.dp, 42.dp))
                StoryBig("${y.longestStreak}", size = 130.sp)
            }
            StoryLine(y.longestHabit?.let { "days of ${it.name}" } ?: "days", size = 26.sp, modifier = Modifier.padding(top = 4.dp))
            StoryNote(span)
        },
        StorySlide(StoryPalette.tintBlue, Offset(0.5f, 0f)) {
            StoryKicker("Best month", StoryPalette.sky)
            StoryBig(bestMonthName, size = 72.sp, modifier = Modifier.padding(top = 30.dp))
            StoryLine("${y.months[y.bestMonth]}% completion", size = 26.sp)
            Row(Modifier.fillMaxWidth().padding(top = 40.dp).height(150.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                y.months.forEachIndexed { i, v ->
                    val best = i == y.bestMonth
                    Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom)) {
                        Box(
                            Modifier.weight(1f, fill = false).fillMaxHeight(maxOf(v, 2) / 100f).fillMaxWidth()
                                .glow(StoryPalette.sky.copy(alpha = 0.66f), 16.dp, 5.dp, enabled = best)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (best) StoryPalette.sky else Color.White.copy(alpha = 0.18f)),
                        )
                        Text(monthName(i + 1, TextStyle.NARROW), style = AppTheme.textStyles.caption.copy(fontSize = 10.sp),
                            color = if (best) StoryPalette.text else StoryPalette.ter)
                    }
                }
            }
        },
        StorySlide(StoryPalette.tintDeepGreen, Offset(0.5f, 1f)) {
            val layer = rememberShareLayer()
            StoryKicker("Your year", StoryPalette.lime)
            ShareCard(layer, StoryPalette.lime, Modifier.padding(top = 24.dp)) {
                ShareCardHead("$name · $year")
                ShareStatGrid(listOf(
                    "${y.daysShowedUp}" to "days showed up",
                    "${y.longestStreak}" to "longest streak",
                    bestMonthName to "best month",
                    "%,d".format(y.checkIns) to "check-ins",
                ))
                HeatStrip(y.recentDays, columns = 26, color = StoryPalette.lime, columnMajor = true, modifier = Modifier.padding(top = 22.dp))
            }
            Spacer(Modifier.weight(1f))
            ShareCardButton("Share your year", layer, "habitforge-$year")
        },
    )
}

/** Month calendar of daily completion % (amber intensity). [lead] = blank cells before day 1. */
@Composable
private fun MonthGrid(days: List<DayTally>, lead: Int) {
    val cells: List<DayTally?> = List(lead) { null } + days
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(java.time.DayOfWeek.SUNDAY).plus((1..6).map { java.time.DayOfWeek.of(it) }).forEach {
                Text(it.getDisplayName(TextStyle.NARROW, Locale.getDefault()), style = AppTheme.textStyles.caption.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.SemiBold, color = StoryPalette.ter, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        cells.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (0 until 7).forEach { i ->
                    val d = week.getOrNull(i)
                    Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (d != null) {
                            val on = d.percent > 0
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .glow(StoryPalette.amber.copy(alpha = 0.5f), 10.dp, 7.dp, enabled = d.percent >= 85)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(if (on) StoryPalette.amber.copy(alpha = 0.3f + 0.7f * d.percent / 100f) else Color.White.copy(alpha = 0.06f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("${d.date.dayOfMonth}", style = AppTheme.textStyles.caption.copy(fontSize = 10.sp), fontWeight = FontWeight.SemiBold,
                                    color = if (d.percent >= 60) Color(0xFF3A2400) else StoryPalette.sub)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Compact heat strip for share cards. Values are 0..100. */
@Composable
private fun HeatStrip(values: List<Int>, columns: Int, color: Color, modifier: Modifier = Modifier, columnMajor: Boolean = false) {
    val rows = (values.size + columns - 1) / columns
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.5.dp)) {
        (0 until rows).forEach { r ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp)) {
                (0 until columns).forEach { col ->
                    val idx = if (columnMajor) col * rows + r else r * columns + col
                    val v = values.getOrNull(idx)
                    Box(
                        Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(2.dp))
                            .alpha(if (v == null) 0f else 1f)
                            .background(if (v != null && v > 0) color.copy(alpha = 0.3f + 0.7f * v / 100f) else Color.White.copy(alpha = 0.06f)),
                    )
                }
            }
        }
    }
}

/** Whether [habits] has anything to recap (strip/story is hidden otherwise). */
fun hasRecapData(habits: List<Habit>) = habits.any { it.isActive }
