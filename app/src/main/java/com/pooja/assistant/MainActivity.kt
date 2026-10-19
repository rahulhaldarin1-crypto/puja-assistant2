package com.pooja.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnToggleService: Button
    private lateinit var btnAccessibility: Button
    private var isServiceRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        btnToggleService = findViewById(R.id.btnToggleService)
        btnAccessibility = findViewById(R.id.btnAccessibility)

        requestRequiredPermissions()

        btnToggleService.setOnClickListener {
            if (!isServiceRunning) {
                startPoojaService()
            } else {
                stopPoojaService()
            }
        }

        btnAccessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "তালিকা থেকে 'Pooja Assistant' অন করুন", Toast.LENGTH_LONG).show()
        }
    }

    private fun startPoojaService() {
        val intent = Intent(this, PoojaVoiceService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        isServiceRunning = true
        tvStatus.text = "স্ট্যাটাস: পূজা শুনছে ('পূজা' বলে ডাকুন)"
        btnToggleService.text = "পূজা বন্ধ করুন"
    }

    private fun stopPoojaService() {
        val intent = Intent(this, PoojaVoiceService::class.java)
        stopService(intent)
        isServiceRunning = false
        tvStatus.text = "স্ট্যাটাস: সার্ভিস বন্ধ রয়েছে"
        btnToggleService.text = "পূজা চালু করুন"
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, neededPermissions.toTypedArray(), 101)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val overlayIntent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(overlayIntent)
        }
    }
}
