package com.phoneguard.blocker

import android.content.Context
import java.util.concurrent.TimeUnit

object GuardPrefs {
    private const val PREF = "phoneguard_prefs"
    private const val KEY_KEYWORDS = "keywords"
    private const val KEY_BLOCKED_ACTIVE = "blocked_active"          // resets only on reboot
    private const val KEY_STREAK_START = "streak_start_millis"
    private const val KEY_LAST_RELAPSE = "last_relapse_millis"

    private val DEFAULT_KEYWORDS = setOf(
        "porn", "xvideos", "xnxx", "xhamster", "pornhub", "redtube",
        "youporn", "adult video", "sex video", "nude", "18+", "hentai"
    )

    fun getKeywords(ctx: Context): Set<String> {
        val sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val saved = sp.getStringSet(KEY_KEYWORDS, null)
        return if (saved.isNullOrEmpty()) DEFAULT_KEYWORDS else saved
    }

    fun saveKeywords(ctx: Context, raw: String) {
        val set = raw.split("\n")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_KEYWORDS, set).apply()
    }

    // ----- Block state: once triggered, stays "active" (locked) until device reboot -----
    fun isBlockActive(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean(KEY_BLOCKED_ACTIVE, false)

    fun setBlockActive(ctx: Context, active: Boolean) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_BLOCKED_ACTIVE, active).apply()
    }

    // Called by BootReceiver — clears the "lock" so cat screen can appear fresh next time,
    // but ALSO counts as a relapse-reset point for the streak per user's spec
    // (kitty only goes away on restart -> restart = the moment user "broke and restarted").
    fun clearBlockOnBoot(ctx: Context) {
        setBlockActive(ctx, false)
    }

    // ----- Streak -----
    fun getStreakStart(ctx: Context): Long {
        val sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        var start = sp.getLong(KEY_STREAK_START, 0L)
        if (start == 0L) {
            start = System.currentTimeMillis()
            sp.edit().putLong(KEY_STREAK_START, start).apply()
        }
        return start
    }

    fun resetStreak(ctx: Context) {
        val now = System.currentTimeMillis()
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_STREAK_START, now)
            .putLong(KEY_LAST_RELAPSE, now)
            .apply()
    }

    fun getStreakDays(ctx: Context): Long {
        val start = getStreakStart(ctx)
        val diff = System.currentTimeMillis() - start
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    fun getGoalDays(): Int = 30
}
