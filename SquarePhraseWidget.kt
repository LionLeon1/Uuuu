package com.synthesisco.frases
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

class SquarePhraseWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) { WidgetHelper.updateSingleWidget(context, appWidgetManager, id) }
        WidgetHelper.scheduleMidnightUpdate(context)
    }
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.synthesisco.frases.ACTION_MIDNIGHT_UPDATE") { WidgetHelper.updateAllWidgets(context) }
    }
}