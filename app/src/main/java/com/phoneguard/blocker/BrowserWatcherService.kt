package com.phoneguard.blocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class BrowserWatcherService : AccessibilityService() {

    private val browserPackages = setOf(
        "com.android.chrome",
        "com.chrome.beta",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.brave.browser",
        "com.microsoft.emmx",
        "com.sec.android.app.sbrowser",
        "com.duckduckgo.mobile.android"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return
        if (pkg !in browserPackages) return

        if (GuardPrefs.isBlockActive(this) && !OverlayManager.isOverlayShowing()) {
            OverlayManager.showBlockOverlay(this)
            return
        }
        if (OverlayManager.isOverlayShowing()) return

        val root = rootInActiveWindow ?: return
        val keywords = GuardPrefs.getKeywords(this)
        if (keywords.isEmpty()) return

        val text = extractAllText(root)
        val lowerText = text.lowercase()

        val matched = keywords.any { lowerText.contains(it) }
        if (matched) {
            OverlayManager.showBlockOverlay(this)
        }
    }

    private fun extractAllText(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        
        val sb = StringBuilder()
        val visited = mutableSetOf<AccessibilityNodeInfo>()
        
        fun walk(n: AccessibilityNodeInfo?) {
            if (n == null || visited.size > 200) return
            if (visited.contains(n)) return
            visited.add(n)
            
            n.text?.let { if (it.isNotEmpty()) sb.append(it).append(" ") }
            n.contentDescription?.let { if (it.isNotEmpty()) sb.append(it).append(" ") }
            
            for (i in 0 until n.childCount) {
                walk(n.getChild(i))
            }
        }
        
        walk(node)
        return sb.toString()
    }

    override fun onInterrupt() {}
}
