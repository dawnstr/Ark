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

        val line1 = if (micGranted) "[OK] Microphone" else "[ ] Microphone"
        val line2 = if (overlayGranted) "[OK] Draw over apps" else "[ ] Draw over apps"
        val line3 = if (accessibilityGranted) "[OK] Accessibility service" else "[ ] Accessibility service"
        statusText.text = line1 + "\n" + line2 + "\n" + line3
    }
