package com.phoneguard.blocker;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import java.util.Locale;

public class OverlayManager {
    private static View overlayView = null;
    private static TextToSpeech tts = null;
    private static Handler handler = null;
    private static boolean speaking = false;

    public static void showBlockOverlay(final Context context) {
        if (overlayView != null) return;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.overlay_block, null);

        TextView streakText = view.findViewById(R.id.streakText);
        long days = GuardPrefs.getStreakDays(context);
        streakText.setText("Your streak reset at day " + days + ". It restarts after you reboot.");

        final Button closeBtn = view.findViewById(R.id.closeButton);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopSound();
                closeBtn.setText("Sound muted");
                closeBtn.setEnabled(false);
            }
        });

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );

        wm.addView(view, params);
        overlayView = view;

        GuardPrefs.setBlockActive(context, true);
        GuardPrefs.resetStreak(context);
        startSound(context);
    }

    public static void removeOverlayIfAny(Context context) {
        if (overlayView != null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            try {
                wm.removeView(overlayView);
            } catch (Exception e) {
            }
        }
        overlayView = null;
        stopSound();
    }

    private static void startSound(final Context context) {
        if (speaking) return;
        speaking = true;
        handler = new Handler(Looper.getMainLooper());
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.US);
                    speakLoop();
                }
            }
        });
    }

    private static void speakLoop() {
        if (!speaking || tts == null) return;
        tts.speak("Stop. This is not what you want to do.", TextToSpeech.QUEUE_FLUSH, null);
        if (handler != null) {
            handler.postDelayed(new Runnable() {
                public void run() {
                    speakLoop();
                }
            }, 4000);
        }
    }

    private static void stopSound() {
        speaking = false;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (tts != null) {
            tts.stop();
        }
    }

    public static boolean isOverlayShowing() {
        return overlayView != null;
    }
}
