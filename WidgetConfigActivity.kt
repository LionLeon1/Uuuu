package com.synthesisco.frases
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WidgetConfigActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.activity_widget_config)
        appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        val tvPhrase = findViewById<TextView>(R.id.widget_phrase)
        val chronometer = findViewById<Chronometer>(R.id.widget_countdown)
        val ivBg = findViewById<ImageView>(R.id.widget_bg_image)
        val tvOpacity = findViewById<TextView>(R.id.tv_opacity_label)
        val seekBar = findViewById<SeekBar>(R.id.seekbar_opacity)
        val btnSave = findViewById<Button>(R.id.btn_save_widget)

        tvPhrase.text = WidgetHelper.getPhraseForToday(this)
        chronometer.isCountDown = true
        chronometer.base = WidgetHelper.getMidnightElapsedRealtimeBase()
        chronometer.start()

        val currentPercent = WidgetHelper.getOpacityPercent(this, appWidgetId)
        seekBar.progress = currentPercent
        ivBg.imageAlpha = ((currentPercent / 100f) * 255).toInt()
        tvOpacity.text = "Opacidad: $currentPercent%"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ivBg.imageAlpha = ((progress / 100f) * 255).toInt()
                tvOpacity.text = "Opacidad: $progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnSave.setOnClickListener {
            val chosenPercent = seekBar.progress
            WidgetHelper.saveOpacityPercent(this, appWidgetId, chosenPercent)
            WidgetHelper.updateAllWidgets(this)
            WidgetHelper.scheduleMidnightUpdate(this)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(Activity.RESULT_OK, resultValue)
            }
            finish()
        }
    }
}