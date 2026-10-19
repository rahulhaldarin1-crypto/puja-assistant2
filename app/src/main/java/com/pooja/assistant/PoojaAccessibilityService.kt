package com.pooja.assistant

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class PoojaAccessibilityService : AccessibilityService() {

    companion object {
        var instance: PoojaAccessibilityService? = null
        var currentScreenText: String = ""
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode = rootInActiveWindow ?: return
        val sb = StringBuilder()
        extractAllText(rootNode, sb)
        currentScreenText = sb.toString().take(1500) // এআই বিশ্লেষণের জন্য প্রথম ১৫০০ অক্ষর নেওয়া হলো
    }

    private fun extractAllText(node: AccessibilityNodeInfo?, sb: StringBuilder) {
        if (node == null) return
        node.text?.let {
            if (it.isNotBlank()) {
                sb.append(it).append(" | ")
            }
        }
        for (i in 0 until node.childCount) {
            extractAllText(node.getChild(i), sb)
        }
    }

    fun openApplication(packageName: String): Boolean {
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun onInterrupt() {
        instance = null
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
