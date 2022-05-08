package com.xiangning.simplelauncher

import android.app.Application
import com.xiangning.simplelauncher.telephony.AudioControl
import com.xiangning.simplelauncher.telephony.InCallManager

/**
 * Created by xiangning on 2022/5/8.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化音频管理
        AudioControl.init(this)
        // 初始化电话监听
        InCallManager(this)
    }
}