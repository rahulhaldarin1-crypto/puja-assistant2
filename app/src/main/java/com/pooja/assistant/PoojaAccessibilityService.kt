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
        try {
            val rootNode = rootInActiveWindow ?: return
            val sb = StringBuilder()
            extractAllText(rootNode, sb)
            currentScreenText = sb.toString().take(1500)
        } catch (e: Exception) {
            // ক্র্যাশ আটকাতে এক্সেপশন হ্যান্ডেল করা হলো
        }
    }

    private fun extractAllText(node: AccessibilityNodeInfo?, sb: StringBuilder) {
        if (node == null) return
        try {
            node.text?.let {
                if (it.isNotBlank()) {
                    sb.append(it).append(" | ")
                }
            }
            val count = node.childCount
            for (i in 0 until count) {
                val child = try { node.getChild(i) } catch (e: Exception) { null }
                if (child != null) {
                    extractAllText(child, sb)
                }
            }
        } catch (e: Exception) {
            // চাইল্ড নোড পড়ার সময় এরর হলে সার্ভিস ক্র্যাশ করবে না
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
