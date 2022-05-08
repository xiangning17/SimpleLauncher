package com.xiangning.simplelauncher

import android.app.Application
import com.xiangning.simplelauncher.telephony.InCallManager

/**
 * Created by xiangning on 2022/5/8.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        InCallManager(this)
    }
}