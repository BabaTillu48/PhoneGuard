package com.phoneguard.blocker;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class GuardPrefs {
    private static final String PREF = "phoneguard_prefs";
    private static final String KEY_KEYWORDS = "keywords";
    private static final String KEY_BLOCKED_ACTIVE = "blocked_active";
    private static final String KEY_STREAK_START = "streak_start_millis";

    public static Set<String> getKeywords(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        Set<String> saved = sp.getStringSet(KEY_KEYWORDS, null);
        
        if (saved == null || saved.isEmpty()) {
            Set<String> defaults = new HashSet<>();
            defaults.add("porn");
            defaults.add("xvideos");
            defaults.add("xnxx");
            defaults.add("xhamster");
            defaults.add("pornhub");
            defaults.add("redtube");
            defaults.add("youporn");
            defaults.add("adult");
            defaults.add("sex");
            defaults.add("nude");
            defaults.add("18+");
            return defaults;
        }
        return saved;
    }

    public static void saveKeywords(Context ctx, String raw) {
        Set<String> set = new HashSet<>();
        for (String line : raw.split("\n")) {
            String trimmed = line.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                set.add(trimmed);
            }
        }
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putStringSet(KEY_KEYWORDS, set).apply();
    }

    public static boolean isBlockActive(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_BLOCKED_ACTIVE, false);
    }

    public static void setBlockActive(Context ctx, boolean active) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_BLOCKED_ACTIVE, active).apply();
    }

    public static void clearBlockOnBoot(Context ctx) {
        setBlockActive(ctx, false);
    }

    public static long getStreakStart(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        long start = sp.getLong(KEY_STREAK_START, 0L);
        if (start == 0L) {
            start = System.currentTimeMillis();
            sp.edit().putLong(KEY_STREAK_START, start).apply();
        }
        return start;
    }

    public static void resetStreak(Context ctx) {
        long now = System.currentTimeMillis();
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putLong(KEY_STREAK_START, now).apply();
    }

    public static long getStreakDays(Context ctx) {
        long start = getStreakStart(ctx);
        long diff = System.currentTimeMillis() - start;
        return TimeUnit.MILLISECONDS.toDays(diff);
    }

    public static int getGoalDays() {
        return 30;
    }
}
