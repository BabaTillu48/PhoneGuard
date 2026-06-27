package com.phoneguard.blocker;

import android.app.Application;
import android.os.Environment;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

public class PhoneGuardApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Thread.DefaultUncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                StringWriter sw = new StringWriter();
                throwable.printStackTrace(new PrintWriter(sw));
                String errorMsg = sw.toString();

                File downloads = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "PhoneGuard_crash.txt"
                );
                FileWriter fw = new FileWriter(downloads);
                fw.write(errorMsg);
                fw.close();
            } catch (Exception e) {
            }
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
    }
}
