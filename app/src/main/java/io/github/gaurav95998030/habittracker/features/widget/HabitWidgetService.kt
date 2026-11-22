package io.github.gaurav95998030.habittracker.features.widget

import android.content.Intent
import android.widget.RemoteViewsService

/** Hosts the [HabitWidgetFactory] that supplies the widget list's rows. */
class HabitWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        HabitWidgetFactory(applicationContext)
}
