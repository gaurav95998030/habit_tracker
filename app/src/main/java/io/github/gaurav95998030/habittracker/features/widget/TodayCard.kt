package io.github.gaurav95998030.habittracker.features.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import kotlin.math.roundToInt
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.View
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import io.github.gaurav95998030.habittracker.R
import io.github.gaurav95998030.habittracker.features.habits.HabitCatalog
import io.github.gaurav95998030.habittracker.features.habits.HabitUtil
import io.github.gaurav95998030.habittracker.features.habits.domain.HabitStats
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.ui.theme.Accents
import io.github.gaurav95998030.habittracker.ui.theme.PhIcons
import java.time.LocalDate

/** Today's progress across active habits — the data behind the widget and lock-screen card. */
data class TodaySummary(val habits: List<Habit>, val done: Int) {
    val total: Int get() = habits.size
    val percent: Int get() = if (total == 0) 0 else done * 100 / total
    val allDone: Boolean get() = total > 0 && done == total
    /** First habit not yet checked off today. */
    val next: Habit? get() = habits.firstOrNull { !HabitStats.isDoneOn(it, LocalDate.now()) }
}

/**
 * Where a row is drawn: the always-dark home-screen widget, or the lock-screen card on the
 * system's notification surface (light or dark with the phone theme).
 */
enum class CardSurface(val text: Int, val secondary: Int, val tertiary: Int) {
    Widget(R.color.widget_text, R.color.widget_text_secondary, R.color.widget_text_tertiary),
    Notification(R.color.notif_text, R.color.notif_text_secondary, R.color.notif_text_tertiary),
}

/** Shared RemoteViews binding so the home-screen widget and lock-screen card look identical. */
object TodayCard {

    private val typefaces = HashMap<Int, Typeface>()

    /**
     * Phosphor glyph rendered to a bitmap. Launchers don't load an app's font files inside
     * widgets/notifications (glyphs show as boxes), so icons are drawn here and passed as
     * images — identical on every launcher.
     */
    fun glyph(context: Context, glyph: String, fontRes: Int, sizeDp: Int, color: Int): Bitmap {
        val px = (sizeDp * context.resources.displayMetrics.density).roundToInt().coerceAtLeast(1)
        val tf = typefaces.getOrPut(fontRes) { ResourcesCompat.getFont(context, fontRes) ?: Typeface.DEFAULT }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = tf
            textSize = px.toFloat()
            this.color = color
            textAlign = Paint.Align.CENTER
        }
        val bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val fm = paint.fontMetrics
        Canvas(bmp).drawText(glyph, px / 2f, px / 2f - (fm.ascent + fm.descent) / 2f, paint)
        return bmp
    }

    fun summary(all: List<Habit>, today: LocalDate = LocalDate.now()): TodaySummary {
        val active = all.filter { it.isActive }
        return TodaySummary(active, active.count { HabitStats.isDoneOn(it, today) })
    }

    /**
     * Binds one `widget_habit_item` row: tinted icon badge, name (struck through once
     * done), streak with fire, and the check circle (filled in the habit colour when done).
     */
    fun bindRow(
        context: Context,
        row: RemoteViews,
        habit: Habit,
        surface: CardSurface = CardSurface.Widget,
        today: LocalDate = LocalDate.now(),
    ) {
        val accent = Accents.colorOf(habit.color)
        val accentInt = accent.toArgb()
        val done = HabitStats.isDoneOn(habit, today)
        val streak = HabitUtil.currentStreak(HabitStats.completedSet(habit), today)

        // Icon badge: accent at 13% behind the glyph in full accent.
        row.setInt(R.id.widget_item_badge, "setColorFilter", accentInt)
        row.setInt(R.id.widget_item_badge, "setImageAlpha", 34)
        row.setImageViewBitmap(
            R.id.widget_item_icon,
            glyph(context, PhIcons.byName(HabitCatalog.iconFor(habit)) ?: PhIcons.Sparkle, R.font.phosphor_bold, 16, accentInt),
        )

        val name = habit.name.ifBlank { "Untitled" }
        if (done) {
            row.setTextViewText(R.id.widget_item_name, SpannableString(name).apply {
                setSpan(StrikethroughSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            })
            row.setTextColor(R.id.widget_item_name, context.getColor(surface.secondary))
        } else {
            row.setTextViewText(R.id.widget_item_name, name)
            row.setTextColor(R.id.widget_item_name, context.getColor(surface.text))
        }

        if (streak > 0) {
            row.setViewVisibility(R.id.widget_item_streak, View.VISIBLE)
            row.setImageViewBitmap(R.id.widget_item_fire, glyph(context, PhIcons.Fire, R.font.phosphor_fill, 12, accentInt))
            row.setTextViewText(R.id.widget_item_streak_count, streak.toString())
            row.setTextColor(R.id.widget_item_streak_count, context.getColor(surface.secondary))
        } else {
            row.setViewVisibility(R.id.widget_item_streak, View.GONE)
        }

        if (done) {
            row.setImageViewResource(R.id.widget_item_check, R.drawable.widget_circle)
            row.setInt(R.id.widget_item_check, "setColorFilter", accentInt)
            row.setImageViewBitmap(
                R.id.widget_item_check_mark,
                glyph(context, PhIcons.Check, R.font.phosphor_bold, 15, Accents.inkFor(accent).toArgb()),
            )
            row.setViewVisibility(R.id.widget_item_check_mark, View.VISIBLE)
            row.setContentDescription(R.id.widget_item_check_box, "$name done today. Tap to undo.")
        } else {
            row.setImageViewResource(R.id.widget_item_check, R.drawable.widget_check_ring)
            row.setInt(R.id.widget_item_check, "setColorFilter", context.getColor(surface.tertiary))
            row.setViewVisibility(R.id.widget_item_check_mark, View.INVISIBLE)
            row.setContentDescription(R.id.widget_item_check_box, "Mark $name done today")
        }
    }
}
