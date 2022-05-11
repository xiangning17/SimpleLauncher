package com.xiangning.simplelauncher.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.xiangning.simplelauncher.ui.SettingsActivity.Companion.isSimpleLauncherEnable

/**
 * Created by xiangning on 2022/5/11.
 * 拦截按键事件
 */
@SuppressLint("StaticFieldLeak")
object KeyMonitor {
    private const val TAG = "KeyMonitor"

    private lateinit var context: Context
    private var monitorView: View? = null

    private var isAdded = false

    fun start(context: Context) {
        if (isAdded || !context.isSimpleLauncherEnable) {
            return
        }
        this.context = context.applicationContext
        addWindow()
    }

    private fun addWindow() {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            width = 1
            height = 1
        }
        val monitorView = View(context).apply {
            setOnKeyListener { v, keyCode, event ->
                // 拦截所有按键事件，“Home"、“Recent”、“Power”按键拦截不到
                Log.e(TAG, "onKeyEvent: $keyCode")
//                when (keyCode) {
//                    KeyEvent.KEYCODE_BACK,
//                    KeyEvent.KEYCODE_MENU,
//                    KeyEvent.KEYCODE_HOME
//                    -> {
//                       true
//                    }
//                }
                true
            }
        }
        wm.addView(monitorView, lp)
        this.monitorView = monitorView
        Log.e(TAG, "add key monitor window success!")
        isAdded = true
    }

    fun stop() {
        if (!isAdded) {
            return
        }

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        monitorView?.let { wm.removeView(it) }
        monitorView = null
        isAdded = false
        Log.e(TAG, "remove monitor window success!")
    }

}