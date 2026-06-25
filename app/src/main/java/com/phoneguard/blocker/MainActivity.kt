package com.phoneguard.blocker

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var streakDisplay: TextView
    private lateinit var statusText: TextView
    private lateinit var editKeywords: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        streakDisplay = findViewById(R.id.streakDisplay)
        statusText = findViewById(R.id.statusText)
        editKeywords = findViewById(R.id.editKeywords)

        editKeywords.setText(GuardPrefs.getKeywords(this).joinToString("\n"))

        findViewById<Button>(R.id.btnAccessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.btnOverlay).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }

        findViewById<Button>(R.id.btnSaveKeywords).setOnClickListener {
            GuardPrefs.saveKeywords(this, editKeywords.text.toString())
            statusText.text = "Keywords save हो गए ✅"
        }

        findViewById<Button>(R.id.btnTestOverlay).setOnClickListener {
            OverlayManager.showBlockOverlay(this)
        }

        updateStreak()
    }

    override fun onResume() {
        super.onResume()
        updateStreak()
    }

    private fun updateStreak() {
        val days = GuardPrefs.getStreakDays(this)
        val goal = GuardPrefs.getGoalDays()
        streakDisplay.text = "🔥 Streak: $days / $goal दिन"
    }
}
