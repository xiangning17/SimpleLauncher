package com.xiangning.simplelauncher.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import java.lang.reflect.Method

/**
 * Created by xiangning on 2022/5/6.
 */
object StatusBarHelper {

    private const val TAG = "StatusBarHelper"

    /**
     * 禁用状态栏与导航栏
     *
     * https://stackoverflow.com/questions/29969086/how-to-disable-status-bar-click-and-pull-down-in-android#:~:text=Disable%20Android%20StatusBar%20expand/pull%2Ddown
     *
     * @param context 上下文
     * @param disable 是否禁用
     */
    @SuppressLint("WrongConstant")
    fun disableStatusBar(context: Context, disable: Boolean) {
        Log.d(TAG, "disableStatusBar: $disable")
        // Read from property or pass it in function, whatever works for you!
//        val disable: Boolean =
//            SystemProperties.getBoolean(context, "supercool.status.bar.disable", true)

        val statusBarService = context.applicationContext.getSystemService("statusbar")

        val statusBarManager: Class<*>?
        try {
            statusBarManager = Class.forName("android.app.StatusBarManager")
            try {
                val disableStatusBarFeatures: Method =
                    statusBarManager.getMethod("disable", Int::class.javaPrimitiveType)
                try {
                    disableStatusBarFeatures.isAccessible = true
                    if (disable) {
                        disableStatusBarFeatures.invoke(statusBarService,
                            0x00010000 // View.STATUS_BAR_DISABLE_EXPAND
//                                    or 0x00020000 // View.STATUS_BAR_DISABLE_NOTIFICATION_ICONS
//                                    or 0x00040000 // View.STATUS_BAR_DISABLE_NOTIFICATION_ALERTS
//                                    or 0x00080000 // View.STATUS_BAR_DISABLE_NOTIFICATION_TICKER
//                                    or 0x00100000 // View.STATUS_BAR_DISABLE_SYSTEM_INFO
//                                    or 0x00800000 // View.STATUS_BAR_DISABLE_CLOCK
                                    or 0x01000000 // View.STATUS_BAR_DISABLE_RECENT
                                    or 0x02000000 // View.STATUS_BAR_DISABLE_SEARCH
                        )
                    } else {
                        disableStatusBarFeatures.invoke(statusBarService, 0x00000000)
                    }
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "disableStatusBar: " + e.message, e)
                }
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "disableStatusBar: " + e.message, e)
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "disableStatusBar: " + e.message, e)
        }
    }
}