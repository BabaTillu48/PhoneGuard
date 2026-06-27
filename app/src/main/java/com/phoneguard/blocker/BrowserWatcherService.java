package com.phoneguard.blocker;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.HashSet;
import java.util.Set;

public class BrowserWatcherService extends AccessibilityService {
    private static final Set<String> browserPackages = new HashSet<String>();

    static {
        browserPackages.add("com.android.chrome");
        browserPackages.add("com.chrome.beta");
        browserPackages.add("org.mozilla.firefox");
        browserPackages.add("com.opera.browser");
        browserPackages.add("com.brave.browser");
        browserPackages.add("com.duckduckgo.mobile.android");
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        String pkg = event.getPackageName() != null ? event.getPackageName().toString() : "";
        if (!browserPackages.contains(pkg)) return;

        if (GuardPrefs.isBlockActive(this) && !OverlayManager.isOverlayShowing()) {
            OverlayManager.showBlockOverlay(this);
            return;
        }
        if (OverlayManager.isOverlayShowing()) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        Set<String> keywords = GuardPrefs.getKeywords(this);
        if (keywords.isEmpty()) return;

        String text = extractAllText(root).toLowerCase();

        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                OverlayManager.showBlockOverlay(this);
                return;
            }
        }
    }

    private String extractAllText(AccessibilityNodeInfo node) {
        if (node == null) return "";
        StringBuilder sb = new StringBuilder();
        Set<AccessibilityNodeInfo> visited = new HashSet<AccessibilityNodeInfo>();
        walk(node, sb, visited);
        return sb.toString();
    }

    private void walk(AccessibilityNodeInfo n, StringBuilder sb, Set<AccessibilityNodeInfo> visited) {
        if (n == null || visited.size() > 200) return;
        if (visited.contains(n)) return;
        visited.add(n);

        if (n.getText() != null && n.getText().length() > 0) {
            sb.append(n.getText()).append(" ");
        }
        if (n.getContentDescription() != null && n.getContentDescription().length() > 0) {
            sb.append(n.getContentDescription()).append(" ");
        }

        for (int i = 0; i < n.getChildCount(); i++) {
            walk(n.getChild(i), sb, visited);
        }
    }

    public void onInterrupt() {
    }
}
