package io.github.gaurav95998030.habittracker.features.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import dagger.hilt.android.EntryPointAccessors
import io.github.gaurav95998030.habittracker.R
import io.github.gaurav95998030.habittracker.features.habits.domain.model.Habit
import io.github.gaurav95998030.habittracker.features.reminders.ReminderConstants
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

/**
 * Supplies one row per active habit. [onDataSetChanged] runs on a binder thread, so
 * the blocking Room read here is safe — it never touches the main thread.
 */
class HabitWidgetFactory(
    private val context: Context,
) : RemoteViewsService.RemoteViewsFactory {

    private var habits: List<Habit> = emptyList()

    private val repository by lazy {
        EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .habitRepository()
    }

    override fun onCreate() {}

    override fun onDataSetChanged() {
        habits = runCatching { runBlocking { repository.getHabits() } }
            .getOrDefault(emptyList())
            .filter { it.isActive }
    }

    override fun onDestroy() {
        habits = emptyList()
    }

    override fun getCount(): Int = habits.size

    override fun getViewAt(position: Int): RemoteViews {
        val habit = habits[position]
        val row = RemoteViews(context.packageName, R.layout.widget_habit_item)
        TodayCard.bindRow(context, row, habit)
        // Fill-in intent carries the habit id back to the provider's toggle template.
        val fillIn = Intent().putExtra(ReminderConstants.EXTRA_HABIT_ID, habit.id)
        row.setOnClickFillInIntent(R.id.widget_item_check_box, fillIn)
        row.setOnClickFillInIntent(R.id.widget_item_root, fillIn)
        return row
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = habits[position].id.hashCode().toLong()

    override fun hasStableIds(): Boolean = true
}
