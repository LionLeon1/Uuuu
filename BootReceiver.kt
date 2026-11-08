package com.synthesisco.frases
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        WidgetHelper.updateAllWidgets(context)
        WidgetHelper.scheduleMidnightUpdate(context)
    }
}