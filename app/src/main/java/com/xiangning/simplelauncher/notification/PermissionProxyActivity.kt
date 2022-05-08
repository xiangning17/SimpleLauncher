package com.xiangning.simplelauncher.notification

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class PermissionProxyActivity : AppCompatActivity() {

    private val REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isServiceEnabled()) {
            finish()
            return
        }

        requestPermission()
    }


    /**
     * 是否启用通知监听服务
     */
    fun isServiceEnabled(): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(this)
        return packageNames.contains(packageName)
    }

    /**
     * 切换通知监听器服务
     *
     * @param enable
     */
    fun toggleNotificationListenerService(enable: Boolean) {
        val pm = packageManager
        if (enable) {
            pm.setComponentEnabledSetting(
                ComponentName(applicationContext, NotificationService::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
            )
        } else {
            pm.setComponentEnabledSetting(
                ComponentName(applicationContext, NotificationService::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
        }
    }

    /**
     * 请求权限
     */
    fun requestPermission() {
        if (!isServiceEnabled()) {
            startActivityForResult(
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"),
                REQUEST_CODE, null
            )
        } else {
            showMsg("通知服务已开启")
            toggleNotificationListenerService(true)
        }
    }

    fun showMsg(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (isServiceEnabled()) {
                showMsg("通知服务已开启")
                toggleNotificationListenerService(true)
            } else {
                showMsg("通知服务未开启")
                toggleNotificationListenerService(false)
            }
            finish()
        }
    }

}