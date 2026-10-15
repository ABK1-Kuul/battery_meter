package io.github.ifa.glancewidget

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.ifa.glancewidget.debug.CrashHandler

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        CrashHandler.install(this)
        if (CrashHandler.isCrashProcess()) return
        super.onCreate()
    }
}
