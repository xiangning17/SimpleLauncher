package com.xiangning.simplelauncher.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import com.tbruyelle.rxpermissions2.RxPermissions
import com.xiangning.simplelauncher.R
import com.xiangning.simplelauncher.calendar.LunarCalendar
import com.xiangning.simplelauncher.entity.WeatherResponse
import com.xiangning.simplelauncher.notification.NotificationService
import com.xiangning.simplelauncher.notification.PermissionProxyActivity
import com.xiangning.simplelauncher.retrofit.RetrofitServiceFactory
import com.xiangning.simplelauncher.retrofit.get
import com.xiangning.simplelauncher.utils.StatusBarHelper
import com.xiangning.simplelauncher.utils.asyncCommand
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_launcher.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


class Launcher : BaseActivity() {

    private val TAG = "SimpleLauncher"

    private val batteryReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val isCharging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0
            val level = 100 * intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) / intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            Log.d(TAG, "onReceive: 电池变化: $isCharging, $level")
            battery?.progress = level
            val tip = SpannableStringBuilder("电量$level")
                .append(" - ")
            when {
                isCharging -> {
                    tip.append("正在充电……")
                }
                level < 30 -> {
                    tip.append("该充电了！")
                }
                else -> {
                    tip.append("电量充足")
                }
            }

            battery_tip.text = tip
        }
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    Log.d(TAG, "onReceive: 亮屏")
                }
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d(TAG, "onReceive: 息屏")

                    // 息屏清除通知
                    NotificationService.instance?.cancelAll()

                    // 清除后台
                    asyncCommand("am kill-all")
                }
            }
        }

    }

    private var clickCount = 0
    private val clearCountTask = Runnable { clickCount = 0 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        startActivity(Intent(this, PermissionProxyActivity::class.java))
        registerReceiver(screenReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })

        val sp = getSharedPreferences("default", Context.MODE_PRIVATE)
        if (sp!!.getBoolean(SettingsActivity.KEY_ENABLE, false)) {
            StatusBarHelper.disableStatusBar(this, true)
        }

        battery_tip.setOnLongClickListener {
            battery_tip?.removeCallbacks(clearCountTask)
            battery_tip?.postDelayed(clearCountTask, 5000)

            clickCount += 1
            if (clickCount >= 3) {
                startActivity(Intent(this, SettingsActivity::class.java))
            }

            return@setOnLongClickListener true
        }

        flashlight.setOnClickListener {
            asyncCommand("am start com.android.systemui/.flashlight.FlashlightActivity")
        }

        contacts.setOnClickListener { startActivity(Intent(this, Contacts::class.java)) }

        lockscreen.setOnClickListener {
            screenOff()
        }

    }

    private fun screenOff() {
        asyncCommand("input keyevent KEYCODE_POWER")
    }

    override fun onStart() {
        super.onStart()
        startTime()
        getWeather()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        RxPermissions(this)
            .request(Manifest.permission.READ_PHONE_STATE)
            .subscribe()

        Log.d(TAG, "onStart")
    }

    override fun onStop() {
        super.onStop()
        time?.removeCallbacks(timeUpdateTask)
        unregisterReceiver(batteryReceiver)
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenReceiver)
    }

    override fun onBackPressed() {
        // ignore
    }

    private var lastStartTime = 0L

    private fun startTime() {
        val calendar = Calendar.getInstance()
        lastStartTime = calendar.timeInMillis

        time?.text = formatTime(calendar)
        updateDate(calendar)
        time?.removeCallbacks(timeUpdateTask)
        time?.postDelayed(timeUpdateTask, (60 - calendar.get(Calendar.SECOND)).toLong() * 1000)
    }

    private val timeUpdateTask = object : Runnable {
        override fun run() {
            val now = Calendar.getInstance()
            time?.text = formatTime(now)
            time?.postDelayed(this, TIME_UPDATE_INTERVEL)

            if (now.timeInMillis - lastStartTime > SCREEN_OFF_TIMEOUT) {
                screenOff()
            }
        }
    }

    private val SCREEN_OFF_TIMEOUT = TimeUnit.MINUTES.toMillis(3)
    private val TIME_UPDATE_INTERVEL = 1000 * 60L
    private val decimalFormat = DecimalFormat("00")
    private val dateFormat = SimpleDateFormat("yyyy年MM月dd日")
    private val weeks = arrayOf("天", "一", "二", "三", "四", "五", "六")
    private fun formatTime(calendar: Calendar): String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        // 更新日期
        if ((hour == 0) and (minutes == 0)) {
            updateDate(calendar)
        }
        return when {
            hour < 3 -> {
                "晚上"
            }
            hour < 7 -> {
                "早上"
            }
            hour < 11 -> {
                "上午"
            }
            hour < 14 -> {
                "中午"
            }
            hour < 19 -> {
                "下午"
            }
            else -> {
                "晚上"
            }
        } + " ${if (hour > 12) hour - 12 else hour}:" + decimalFormat.format(minutes)
    }

    @SuppressLint("SetTextI18n")
    private fun updateDate(calendar: Calendar) {
        // 公历
        date?.text = dateFormat.format(calendar.timeInMillis) +
                "\n星期" + weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        // 计算农历
        tv_lunar?.text = LunarCalendar.getLunarString(calendar)
        // 宜忌
        yiji?.text = LunarCalendar.getyiji(calendar)
    }

    // 请求天气信息2小时时间间隔
    private val GET_WEATHER_INTERVAL = TimeUnit.HOURS.toMillis(2)
    private var mLastUpdateTime = 0L
    @SuppressLint("CheckResult")
    private fun getWeather() {
        if (System.currentTimeMillis() - mLastUpdateTime < GET_WEATHER_INTERVAL) {
            return
        }

        RetrofitServiceFactory.dynamic.get("http://wthrcdn.etouch.cn/weather_mini?city=遂宁", WeatherResponse::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res ->
                if (res?.status == 1000) {
                    val sb = StringBuffer()
                    val data = res.data
                    val today = data?.forecast?.firstOrNull()
                    today?.apply {
                        sb.append("今天：$type ${extractTemp(low)}~${extractTemp(high)}度")
                    }
                    val tomorrow = data?.forecast?.getOrNull(1)
                    tomorrow?.apply {
                        sb.append("\n")
                            .append("明天：$type ${extractTemp(low)}~${extractTemp(high)}度")
                    }

                    weather?.text = sb.toString()
                    mLastUpdateTime = System.currentTimeMillis()
                }
            }, { e ->
                Log.e(TAG, "getWeather: " + e.message, e)
                weather?.text = "未获取到天气数据"
            })
    }

    private fun extractTemp(raw: String) : String {
        val pattern = Pattern.compile(".{2} ([0-9]+).")
        val matcher = pattern.matcher(raw)
        return if (matcher.matches()) {
            matcher.group(1).orEmpty()
        } else {
            ""
        }
    }
}
