package com.victor.ark

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Onboarding screen: walks through the three permissions Ark needs
 * (microphone, draw-over-apps, accessibility service), then lets the
 * user start the floating mic.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    companion object {
        private const val MIC_PERMISSION_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.status_text)

        findViewById<Button>(R.id.btn_mic_permission).setOnClickListener {
            requestMicPermission()
        }
        findViewById<Button>(R.id.btn_overlay_permission).setOnClickListener {
            requestOverlayPermission()
        }
        findViewById<Button>(R.id.btn_accessibility_permission).setOnClickListener {
            openAccessibilitySettings()
        }
        findViewById<Button>(R.id.btn_start_ark).setOnClickListener {
            startArk()
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val micGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        val overlayGranted = Settings.canDrawOverlays(this)
        val accessibilityGranted = isAccessibilityServiceEnabled()

        statusText.text = buildString {
            append(if (micGranted) "✅ Microphone\n" else "⬜ Microphone\n")
            append(if (overlayGranted) "✅ Draw over apps\n" else "⬜ Draw over apps\n")
            append(if (accessibilityGranted) "✅ Accessibility service" else "⬜ Accessibility service")
        }
    }

    private fun requestMicPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.RECORD_AUDIO), MIC_PERMISSION_REQUEST
        )
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            Toast.makeText(this, "Already granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        Toast.makeText(this, "Find and enable 'Ark' in the list", Toast.LENGTH_LONG).show()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedServiceName = "$packageName/${VoiceControlAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            if (colonSplitter.next().equals(expectedServiceName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun startArk() {
        val micGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!micGranted || !Settings.canDrawOverlays(this) || !isAccessibilityServiceEnabled()) {
            Toast.makeText(this, "Grant all three permissions first", Toast.LENGTH_SHORT).show()
            return
        }

        startService(Intent(this, FloatingMicService::class.java))
        Toast.makeText(this, "Ark is running — look for the mic bubble", Toast.LENGTH_SHORT).show()
        moveTaskToBack(true)
    }
}
