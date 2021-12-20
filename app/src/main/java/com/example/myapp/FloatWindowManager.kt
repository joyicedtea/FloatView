package com.example.myapp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log

object FloatWindowManager : SensorEventListener2, Application.ActivityLifecycleCallbacks {
    private var shakeViews = LinkedHashMap<Activity,FloatWindow>()
    private var activity:Activity? = null
    var leftMargin = 0
    var topMargin = 0
    fun init(application: Application) {
        Log.d(TAG, "init")
        topMargin = application.resources.displayMetrics.heightPixels / 2
        application.registerActivityLifecycleCallbacks(this)
        val manager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        manager.registerListener(this,
            manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorType = event?.sensor?.type
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            val values: FloatArray? = event.values
            values?.forEach {
                if (it > 20) {
                    doShake()
                    return
                }
            }
        }
    }

    private const val TAG = "BackDoorShakeManager"
    private var isShow = false
    private var oldTime = 0L
    private fun doShake() {
        val time = System.currentTimeMillis() - oldTime
        Log.d(TAG, "doShake__${isShow}")
        if (time < 800) {
            return
        }
        oldTime = System.currentTimeMillis()
        shakeViews.forEach {
            if (activity == it.key) {
                if (isShow) it.value.hide() else it.value.show()
                isShow = !isShow
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onFlushCompleted(sensor: Sensor?) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        this.activity = activity
        if (!shakeViews.containsKey(activity)){
            shakeViews[activity] = FloatWindow(activity)
        }
        shakeViews[activity]?.setIsShow(isShow)
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (shakeViews.containsKey(activity)){
            shakeViews[activity]?.setIsShow(false)
            shakeViews.remove(activity)
        }
        this.activity = null
    }

}