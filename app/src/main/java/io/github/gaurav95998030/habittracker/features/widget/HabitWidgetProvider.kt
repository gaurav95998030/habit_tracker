package io.github.gaurav95998030.habittracker.features.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import dagger.hilt.android.EntryPointAccessors
import io.github.gaurav95998030.habittracker.MainActivity
import io.github.gaurav95998030.habittracker.R
import io.github.gaurav95998030.habittracker.features.reminders.ReminderConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * "Glowstreak — today" home-screen widget: lists the active habits with a tappable
 * check circle for each. The list is backed by [HabitWidgetService] /
 * [HabitWidgetFactory]; row taps come back here as [ACTION_WIDGET_TOGGLE].
 */
class HabitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { id -> renderWidget(context, appWidgetManager, id) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_TOGGLE) {
            val habitId = intent.getStringExtra(ReminderConstants.EXTRA_HABIT_ID) ?: return
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    toggleToday(context, habitId)
                    refresh(context)
                } finally {
                    pending.finish()
                }
            }
        }
    }

    private fun renderWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_habits)

        // Header tap opens the app.
        val openApp = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(context, MainActivity::class.java)
        openApp.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        views.setOnClickPendingIntent(
            R.id.widget_header,
            PendingIntent.getActivity(
                context, 0, openApp,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

        // Back the list with the RemoteViewsService. A unique data URI per widget id
        // stops the framework from caching one adapter across multiple placements.
        val serviceIntent = Intent(context, HabitWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
        views.setRemoteAdapter(R.id.widget_list, serviceIntent)
        views.setEmptyView(R.id.widget_list, R.id.widget_empty)

        // Template that each row's fill-in intent completes with its habit id.
        val toggleTemplate = Intent(context, HabitWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_TOGGLE
        }
        views.setPendingIntentTemplate(
            R.id.widget_list,
            PendingIntent.getBroadcast(
                context, 0, toggleTemplate,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

        appWidgetManager.updateAppWidget(appWidgetId, views)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list)
    }

    private suspend fun toggleToday(context: Context, habitId: String) {
        val repository = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .habitRepository()
        val habit = runCatching { repository.getHabits() }
            .getOrNull()?.firstOrNull { it.id == habitId } ?: return
        val today = LocalDate.now().toString()
        val updated = if (habit.completedDates.contains(today)) {
            habit.completedDates - today
        } else {
            habit.completedDates + today
        }
        repository.updateHabitCompletion(habit.copy(completedDates = updated))
    }

    companion object {
        const val ACTION_WIDGET_TOGGLE =
            "io.github.gaurav95998030.habittracker.ACTION_WIDGET_TOGGLE"

        /** Re-render every placed widget and reload its list — call after data changes. */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(
                ComponentName(context, HabitWidgetProvider::class.java)
            )
            if (ids.isEmpty()) return
            val intent = Intent(context, HabitWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
            manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_list)
        }
    }
}
