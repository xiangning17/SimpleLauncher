package com.xiangning.simplelauncher.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import com.xiangning.simplelauncher.R
import com.xiangning.simplelauncher.utils.ShellUtils
import com.xiangning.simplelauncher.utils.StatusBarHelper
import kotlinx.android.synthetic.main.settings_activity.*
import kotlin.concurrent.thread

class SettingsActivity : BaseActivity() {

    companion object {
        const val KEY_ENABLE = "simple_mode_enable"
    }

    private var sp: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        sp = getSharedPreferences("default", Context.MODE_PRIVATE)
        simple_mode.isChecked = sp!!.getBoolean(KEY_ENABLE, false)

        simple_mode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked == sp!!.getBoolean(KEY_ENABLE, false)) {
                return@setOnCheckedChangeListener
            }

            sp!!.edit().putBoolean(KEY_ENABLE, isChecked).apply()
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