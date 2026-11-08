package com.synthesisco.frases
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import org.json.JSONArray
import java.util.Calendar

object WidgetHelper {
    private const val PREFS_NAME = "SynthesiscoWidgetPrefs"
    private const val KEY_OPACITY_PERCENT = "opacity_percent_"
    private const val DEFAULT_OPACITY_PERCENT = 45

    fun saveOpacityPercent(context: Context, widgetId: Int, percent: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_OPACITY_PERCENT + widgetId, percent).putInt(KEY_OPACITY_PERCENT + "global", percent).apply()
    }

    fun getOpacityPercent(context: Context, widgetId: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val globalDefault = prefs.getInt(KEY_OPACITY_PERCENT + "global", DEFAULT_OPACITY_PERCENT)
        return prefs.getInt(KEY_OPACITY_PERCENT + widgetId, globalDefault)
    }

    fun getPhraseForToday(context: Context): String {
        return try {
            val jsonStr = context.assets.open("frases.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonStr)
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) - 1
            val index = dayOfYear.coerceAtLeast(0) % array.length()
            array.getString(index)
        } catch (e: Exception) {
            "God always has a plan"
        }
    }

    fun getMidnightElapsedRealtimeBase(): Long {
        val now = System.currentTimeMillis()
        val midnight = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0); add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
        val millisUntilMidnight = (midnight - now).coerceAtLeast(0L)
        return SystemClock.elapsedRealtime() + millisUntilMidnight
    }

    fun updateSingleWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        val phrase = getPhraseForToday(context)
        views.setTextViewText(R.id.widget_phrase, phrase)
        val targetBase = getMidnightElapsedRealtimeBase()
        views.setChronometerCountDown(R.id.widget_countdown, true)
        views.setChronometer(R.id.widget_countdown, targetBase, null, true)
        val percent = getOpacityPercent(context, widgetId).coerceIn(0, 100)
        val alpha255 = ((percent / 100.0f) * 255).toInt()
        views.setInt(R.id.widget_bg_image, "setImageAlpha", alpha255)
        appWidgetManager.updateAppWidget(widgetId, views)
    }

    fun updateAllWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val squareIds = manager.getAppWidgetIds(ComponentName(context, SquarePhraseWidget::class.java))
        val rectIds = manager.getAppWidgetIds(ComponentName(context, RectanglePhraseWidget::class.java))
        for (id in squareIds) updateSingleWidget(context, manager, id)
        for (id in rectIds) updateSingleWidget(context, manager, id)
    }

    fun scheduleMidnightUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SquarePhraseWidget::class.java).apply { action = "com.synthesisco.frases.ACTION_MIDNIGHT_UPDATE" }
        val pendingIntent = PendingIntent.getBroadcast(context, 999, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val midnight = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 1); set(Calendar.MILLISECOND, 0); add(Calendar.DAY_OF_YEAR, 1) }
        alarmManager.setInexactRepeating(AlarmManager.RTC, midnight.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
    }
}