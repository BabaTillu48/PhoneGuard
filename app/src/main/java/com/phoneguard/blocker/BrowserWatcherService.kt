package com.phoneguard.blocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class BrowserWatcherService : AccessibilityService() {

    // Known browser packages we actively inspect. Service stays idle for everything else.
    private val browserPackages = setOf(
        "com.android.chrome",
        "com.chrome.beta",
        "com.chrome.dev",
        "com.chrome.canary",
        "org.mozilla.firefox",
        "org.mozilla.focus",
        "com.opera.browser",
        "com.opera.browser.beta",
        "com.brave.browser",
        "com.microsoft.emmx",
        "com.sec.android.app.sbrowser",
        "com.android.browser",
        "com.duckduckgo.mobile.android",
        "com.UCMobile.intl",
        "mark.via.gp"
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

        val root = rootInActiveWindow ?: return
        val keywords = GuardPrefs.getKeywords(this)
        if (keywords.isEmpty()) return

        val screenText = StringBuilder()
        collectText(root, screenText, depth = 0, maxNodes = NodeCounter())
        val lowerText = screenText.toString().lowercase()

        val matched = keywords.any { lowerText.contains(it) }

        if (matched) {
            OverlayManager.showBlockOverlay(this)
        }
    }

    /** Small mutable counter so the recursive scan can cap total nodes visited (perf safety). */
    private class NodeCounter(var count: Int = 0)

    /**
     * Walks the visible accessibility tree and collects all visible text
     * (URL bar, page title, on-screen search results text, etc.).
     * This does NOT read keystrokes — it only reads text that is already
     * rendered on screen, and only while a browser app is in the foreground.
     * Capped at ~400 nodes and depth 12 to stay lightweight.
     */
    private fun collectText(
        node: AccessibilityNodeInfo?,
        out: StringBuilder,
        depth: Int,
        maxNodes: NodeCounter
    ) {
        if (node == null || depth > 12 || maxNodes.count > 400) return
        maxNodes.count++

        node.text?.let {
            if (it.isNotEmpty()) {
                out.append(it)
                out.append(' ')
            }
        }
        node.contentDescription?.let {
            if (it.isNotEmpty()) {
                out.append(it)
                out.append(' ')
            }
        }

        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), out, depth + 1, maxNodes)
        }
    }

    override fun onInterrupt() {
        // no-op
    }
}
