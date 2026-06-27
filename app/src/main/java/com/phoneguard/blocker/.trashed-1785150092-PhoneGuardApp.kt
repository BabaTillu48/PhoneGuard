package com.phoneguard.blocker

import android.app.Application
import android.os.Environment
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class PhoneGuardApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))

                // Write to public Downloads folder so it's easy to find with any file manager
                val downloads = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "PhoneGuard_crash.txt"
                )
                downloads.writeText(sw.toString())

                // Also write a backup copy inside the app's own folder, in case Downloads write fails
                val backup = File(getExternalFilesDir(null), "PhoneGuard_crash.txt")
                backup.writeText(sw.toString())
            } catch (e: Exception) {
                // if even writing the crash log fails, ignore and let it crash normally
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
