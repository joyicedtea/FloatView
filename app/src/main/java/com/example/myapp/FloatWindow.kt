package com.example.myapp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import kotlin.math.abs

class FloatWindow (private var activity: Activity) : View.OnTouchListener {
    private val TAG = "BackDoorShake"
    private lateinit var shakeView: View
    private var mApp: Application
    private lateinit var lp: FrameLayout.LayoutParams
    private var screenWidth: Int = 1080
    private var screenHeight: Int = 1280

    init {
        Log.d("FloatWindow", "--init ")
        mApp = activity.application
        initView()
    }


    private fun initView() {
        Log.d(TAG, "initView")
        mApp.apply {
            lp = FrameLayout.LayoutParams(dp2px(70), dp2px(50))
            lp.leftMargin = FloatWindowManager.leftMargin
            lp.topMargin = FloatWindowManager.topMargin
            lp.gravity = Gravity.LEFT
            shakeView = LayoutInflater.from(this).inflate(R.layout.layout_shake, null)
            screenWidth = resources.displayMetrics.widthPixels
            screenHeight = resources.displayMetrics.heightPixels
            shakeView.setOnTouchListener(this@FloatWindow)
        }
    }
    fun Context.dp2px(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    var isShowing = false
    fun hide() {
        Log.d(TAG, "---hide--isShowing--${isShowing}")
        if (!isShowing) return
        val decorView = activity.window?.decorView as FrameLayout
        decorView.removeView(shakeView)
        isShowing = false
    }

    fun setIsShow(isShow: Boolean) {
        if (isShow) {
            show()
        } else {
            hide()
        }
    }

    fun show() {
        Log.d(TAG, "---show-isShowing--${isShowing}")
        lp.leftMargin = FloatWindowManager.leftMargin
        lp.topMargin = FloatWindowManager.topMargin
        if (isShowing) {
            shakeView.invalidate()
            return
        }
        val decorView = activity.window?.decorView as FrameLayout
        decorView.addView(shakeView, lp)
        isShowing = true
    }

    private var oldX = 0
    private var oldY = 0
    private var isToMove = false
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event == null) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                oldX = event.rawX.toInt()
                oldY = event.rawY.toInt()
                isToMove = false
            }
            MotionEvent.ACTION_MOVE -> {
                val disX = (event.rawX - oldX).toInt()
                val disY = (event.rawY - oldY).toInt()
                if (abs(disX) > 3 || abs(disY) > 3) {
                    lp.leftMargin += disX
                    lp.topMargin += disY
                    checkBoundary()
                    shakeView.layoutParams = lp
                    shakeView.invalidate()
                    oldX = event.rawX.toInt()
                    oldY = event.rawY.toInt()
                    FloatWindowManager.leftMargin = lp.leftMargin
                    FloatWindowManager.topMargin = lp.topMargin
                    isToMove = true
                }

            }
            MotionEvent.ACTION_UP -> {
                if (!isToMove) {
                    //todo 执行点击事件
                    Toast.makeText(activity,"点击了按钮",Toast.LENGTH_LONG).show()
                }

            }
        }
        return false
    }

    fun checkBoundary() {
        lp.leftMargin = when {
            lp.leftMargin < 0 -> 0
            lp.leftMargin > (screenWidth - shakeView.measuredWidth) -> screenWidth - shakeView.measuredWidth
            else -> lp.leftMargin
        }
        lp.topMargin = when {
            lp.topMargin < 0 -> 0
            lp.topMargin > (screenHeight - shakeView.measuredHeight) -> screenHeight - shakeView.measuredHeight
            else -> lp.topMargin
        }
    }
}