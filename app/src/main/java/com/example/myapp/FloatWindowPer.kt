package com.example.myapp

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import kotlin.math.abs

/**
 * 2021年12月15日
 * houliang
 */

class FloatWindowPer : SensorEventListener2, View.OnTouchListener {
    lateinit var mContext: Context
    lateinit var windowManager: WindowManager
    lateinit var windowView: View
    lateinit var lp: WindowManager.LayoutParams
    var screenWidth: Int = 1080
    fun init(context: Application) {
        this.mContext = context
        mContext.apply {
            initWindowManage()
            val manager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            manager.registerListener(
                this@FloatWindowPer,
                manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onFlushCompleted(sensor: Sensor?) {}
    private val TAG = "BackDoorShake"

    private fun initWindowManage() {
        Log.d(TAG, "initWindowManage")
        mContext.apply {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            lp = WindowManager.LayoutParams()
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            lp.format = PixelFormat.RGBA_8888
            lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            lp.gravity = Gravity.LEFT
            lp.width = dp2px(60)
            lp.height = dp2px(50)
            lp.y = 100
            windowView = LayoutInflater.from(this).inflate(R.layout.layout_shake, null)
            windowView.setOnClickListener {
                Log.d(TAG, "btn_shake_onClick")
                Toast.makeText(this,"点击了按钮",Toast.LENGTH_LONG).show()
            }
            windowView.setOnTouchListener(this@FloatWindowPer)
            screenWidth = resources.displayMetrics.widthPixels
        }
    }
    fun Context.dp2px(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
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

    private var isShow = false
    private var oldTime: Long = 0
    private fun doShake() {
        val time = System.currentTimeMillis() - oldTime
        if (time < 500) {
            return
        }
        if (!checkBackDoorPer()) return
        oldTime = System.currentTimeMillis()
        Log.d(TAG, "doShake__${isShow}")
        isShow = if (isShow) {
            windowManager.removeView(windowView)
            false
        } else {
            windowManager.addView(windowView, lp)
            true
        }
    }

    /**
     * 检查申请权限
     */
    private fun checkBackDoorPer(): Boolean {
        if (Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(mContext)) return true
//        var intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
//        intent.data = Uri.parse("package:$mContext.packageName")
//        startActivity(intent)
        Toast.makeText(mContext,"请打开浮层权限",Toast.LENGTH_LONG).show()
        return false
    }

    var oldX = 0
    var oldY = 0
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event == null) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                oldX = event.rawX.toInt()
                oldY = event.rawY.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                //由于lp的xy坐标并非标准的坐标系坐标，所以用相对移动位置来计算
                val disX = (event.rawX - oldX).toInt()
                val disY = (event.rawY - oldY).toInt()
                if (abs(disX) > 3 || abs(disY) > 3) {
                    lp.x += disX
                    lp.y += disY
                    windowManager.updateViewLayout(windowView, lp)
                }
                oldX = event.rawX.toInt()
                oldY = event.rawY.toInt()
            }
            MotionEvent.ACTION_UP -> {
                lp.x = if (lp.x < 500) 0 else screenWidth - windowView.measuredWidth
                windowManager.updateViewLayout(windowView, lp)
            }
        }
        return false
    }


}