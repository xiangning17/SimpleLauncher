package com.xiangning.simplelauncher.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import com.xiangning.simplelauncher.R
import com.xiangning.simplelauncher.utils.KeyMonitor
import com.xiangning.simplelauncher.utils.ShellUtils
import com.xiangning.simplelauncher.utils.StatusBarHelper
import kotlinx.android.synthetic.main.settings_activity.*
import kotlin.concurrent.thread

class SettingsActivity : BaseActivity() {

    companion object {
        const val KEY_ENABLE = "simple_mode_enable"

        private var sp: SharedPreferences? = null

        var Context.isSimpleLauncherEnable: Boolean
            get() = (sp ?: applicationContext.getSharedPreferences(
                "default",
                Context.MODE_PRIVATE
            )!!)
                .getBoolean(KEY_ENABLE, false)
            set(value) = (sp ?: applicationContext.getSharedPreferences(
                "default",
                Context.MODE_PRIVATE
            )!!)
                .edit().putBoolean(KEY_ENABLE, value).apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        simple_mode.isChecked = isSimpleLauncherEnable

        simple_mode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked == isSimpleLauncherEnable) {
                return@setOnCheckedChangeListener
            }

            isSimpleLauncherEnable = isChecked

            if (isChecked) {
                // 开始拦截按键
                KeyMonitor.start(this)
            } else {
                // 停止拦截按键
                KeyMonitor.stop()
            }

            thread {
                StatusBarHelper.disableStatusBar(this, isChecked)
                if (isChecked) {
                    ShellUtils.execCommand("pm disable com.huawei.android.launcher/.Launcher", true)
                    ShellUtils.execCommand(
                        "pm disable com.huawei.android.launcher/.simpleui.SimpleUILauncher",
                        true
                    )
                } else {
                    ShellUtils.execCommand("pm enable com.huawei.android.launcher/.Launcher", true)
                    ShellUtils.execCommand(
                        "pm enable com.huawei.android.launcher/.simpleui.SimpleUILauncher",
                        true
                    )
                }
            }
        }

        contacts_edit.setOnClickListener {
            val intent = Intent(this, Contacts::class.java)
            intent.putExtra("isEdit", true)
            startActivity(intent)
        }
    }
}