package com.phoneguard.blocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class BrowserWatcherService : AccessibilityService() {

    // Known browser packages we actively inspect. Service stays idle for everything else.
    private val browserPackages = setOf(
        "com.android.chrome",
        "com.chrome.beta",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.brave.browser",
        "com.microsoft.emmx",
        "com.sec.android.app.sbrowser",
        "com.android.browser",
        "com.duckduckgo.mobile.android"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return
        if (pkg !in browserPackages) return // ignore everything outside browsers -> saves battery

        // If a block is already active (locked until reboot), keep re-showing it instantly.
        if (GuardPrefs.isBlockActive(this) && !OverlayManager.isOverlayShowing()) {
            OverlayManager.showBlockOverlay(this)
            return
        }
        if (OverlayManager.isOverlayShowing()) return

        val url = extractUrlFromBrowser(rootInActiveWindow) ?: return
        val lowerUrl = url.lowercase()

        val keywords = GuardPrefs.getKeywords(this)
        val matched = keywords.any { lowerUrl.contains(it) }

        if (matched) {
            OverlayManager.showBlockOverlay(this)
        }
    }

    /**
     * Tries to find the address-bar text node of common browsers.
     * This only reads the URL bar's visible text — it does NOT read keystrokes
     * and does NOT function unless a supported browser is in the foreground.
     */
    private fun extractUrlFromBrowser(root: AccessibilityNodeInfo?): String? {
        if (root == null) return null
        val urlBarIds = listOf(
            "com.android.chrome:id/url_bar",
            "org.mozilla.firefox:id/url_bar",
            "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
            "com.opera.browser:id/url_field",
            "com.brave.browser:id/url_bar",
            "com.microsoft.emmx:id/url_bar",
            "com.sec.android.app.sbrowser:id/location_bar_edit_text"
        )
        for (id in urlBarIds) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            if (nodes.isNotEmpty()) {
                val text = nodes[0].text?.toString()
                if (!text.isNullOrEmpty()) return text
            }
        }
        // Fallback: scan all text nodes for anything URL-like (rare path)
        return findUrlLikeText(root, depth = 0)
    }

    private fun findUrlLikeText(node: AccessibilityNodeInfo?, depth: Int): String? {
        if (node == null || depth > 6) return null
        val text = node.text?.toString()
        if (!text.isNullOrEmpty() && (text.contains(".com") || text.contains(".in") || text.contains("http"))) {
            return text
        }
        for (i in 0 until node.childCount) {
            val result = findUrlLikeText(node.getChild(i), depth + 1)
            if (result != null) return result
        }
        return null
    }

    override fun onInterrupt() {
        // no-op
    }
}
