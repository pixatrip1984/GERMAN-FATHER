package com.pixatrip1984.germanfather.alarm

import android.app.Activity
import android.app.Application
import android.os.Bundle

class GermanFatherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AlarmRuntime.coordinator(this).reschedule()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                AlarmRuntime.coordinator(this@GermanFatherApplication).reschedule()
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }
}
