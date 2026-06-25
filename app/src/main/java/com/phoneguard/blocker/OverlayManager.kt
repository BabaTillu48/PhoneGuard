package com.phoneguard.blocker

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import java.util.Locale

object OverlayManager {

    private var overlayView: View? = null
    private var tts: TextToSpeech? = null
    private var handler: Handler? = null
    private var speaking = false

    fun showBlockOverlay(context: Context) {
        if (overlayView != null) return // already showing

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.overlay_block, null)

        val streakText = view.findViewById<TextView>(R.id.streakText)
        val days = GuardPrefs.getStreakDays(context)
        streakText.text = "आपकी streak टूट गई थी $days दिन पर। restart करने के बाद नई streak शुरू होगी।"

        val closeBtn = view.findViewById<Button>(R.id.closeButton)
        closeBtn.setOnClickListener {
            stopSound()
            closeBtn.text = "आवाज़ बंद — screen तो restart पर ही जाएगी"
            closeBtn.isEnabled = false
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv().let {
                // we WANT focus so back button doesn't dismiss it trivially, but allow input to our buttons
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            },
            PixelFormat.TRANSLUCENT
        )

        wm.addView(view, params)
        overlayView = view

        GuardPrefs.setBlockActive(context, true)
        GuardPrefs.resetStreak(context) // relapse detected -> streak restarts

        startSound(context)
    }

    fun removeOverlayIfAny(context: Context) {
        overlayView?.let {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            try { wm.removeView(it) } catch (e: Exception) { /* ignore */ }
        }
        overlayView = null
        stopSound()
    }

    private fun startSound(context: Context) {
        if (speaking) return
        speaking = true
        handler = Handler(Looper.getMainLooper())
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("hi", "IN")
                speakLoop()
            }
        }
    }

    private fun speakLoop() {
        if (!speaking) return
        tts?.speak(
            "मत कर लाला, मत कर! यह गंदी चीज़ है।",
            TextToSpeech.QUEUE_FLUSH,
            null,
            "phoneguard_warning"
        )
        handler?.postDelayed({ speakLoop() }, 4000)
    }

    private fun stopSound() {
        speaking = false
        handler?.removeCallbacksAndMessages(null)
        tts?.stop()
    }

    fun isOverlayShowing(): Boolean = overlayView != null
}
