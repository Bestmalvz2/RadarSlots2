package br.com.radardeslots

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var logView: TextView
    private val projectionRequest = 4102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val padding = (18 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
            setBackgroundColor(0xFF10121B.toInt())
        }

        val title = TextView(this).apply {
            text = "RADAR DE SLOTS"
            textSize = 25f
            setTextColor(0xFFFFD166.toInt())
        }
        status = TextView(this).apply {
            text = "Monitoramento parado"
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, padding, 0, padding)
        }
        val start = Button(this).apply {
            text = "Iniciar captura de tela"
            setOnClickListener {
                val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                startActivityForResult(manager.createScreenCaptureIntent(), projectionRequest)
            }
        }
        val stop = Button(this).apply {
            text = "Parar monitoramento"
            setOnClickListener {
                stopService(Intent(this@MainActivity, CaptureService::class.java))
                status.text = "Monitoramento parado"
            }
        }
        val refresh = Button(this).apply {
            text = "Atualizar registros"
            setOnClickListener { refreshLog() }
        }
        logView = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFFE7E9F2.toInt())
            text = "Nenhum evento registrado."
        }
        val scroll = ScrollView(this).apply { addView(logView) }

        root.addView(title)
        root.addView(status)
        root.addView(start)
        root.addView(stop)
        root.addView(refresh)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        refreshLog()
    }

    @Deprecated("Deprecated by Android, retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == projectionRequest && resultCode == RESULT_OK && data != null) {
            val serviceIntent = Intent(this, CaptureService::class.java).apply {
                putExtra(CaptureService.EXTRA_RESULT_CODE, resultCode)
                putExtra(CaptureService.EXTRA_DATA, data)
            }
            ContextCompat.startForegroundService(this, serviceIntent)
            status.text = "Solicitação de captura enviada. Consulte a notificação."
        } else if (requestCode == projectionRequest) {
            status.text = "Captura não autorizada."
        }
    }

    private fun refreshLog() {
        val prefs = getSharedPreferences(CaptureService.PREFS, MODE_PRIVATE)
        val events = prefs.getString(CaptureService.KEY_EVENTS, "").orEmpty()
        logView.text = if (events.isBlank()) "Nenhum evento registrado ainda." else events
    }

    override fun onResume() {
        super.onResume()
        if (::logView.isInitialized) refreshLog()
    }
}
