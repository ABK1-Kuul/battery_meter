package io.github.ifa.glancewidget.debug

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Process
import android.util.Log
import java.io.File
import java.util.Date
import kotlin.system.exitProcess

object CrashHandler {
    const val EXTRA_TRACE = "crash_trace"
    private const val MAX_REPORT_LENGTH = 60_000

    fun isCrashProcess(): Boolean {
        return runCatching {
            File("/proc/self/cmdline").readText().trim('\u0000').endsWith(":crash")
        }.getOrDefault(false)
    }

    fun install(app: Application) {
        val isDebuggable = app.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (!isDebuggable || isCrashProcess()) return
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val launched = runCatching {
                val intent = Intent(app, CrashActivity::class.java)
                    .putExtra(EXTRA_TRACE, buildReport(thread, throwable))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                app.startActivity(intent)
            }.isSuccess
            Log.e("CrashHandler", "Uncaught exception on ${thread.name}", throwable)
            if (launched) {
                Process.killProcess(Process.myPid())
                exitProcess(10)
            } else {
                previous?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun buildReport(thread: Thread, throwable: Throwable): String {
        val header = buildString {
            appendLine("Time: ${Date()}")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Thread: ${thread.name}")
            appendLine()
        }
        return (header + Log.getStackTraceString(throwable)).take(MAX_REPORT_LENGTH)
    }
}
