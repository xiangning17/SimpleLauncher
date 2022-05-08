package com.xiangning.simplelauncher.ui

import android.view.KeyEvent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by xiangning on 2022/5/6.
 */
open class BaseActivity : AppCompatActivity() {

    override fun onStart() {
        super.onStart()

        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
    }
}