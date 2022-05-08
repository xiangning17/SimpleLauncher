package com.xiangning.simplelauncher.telephony

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class InCallManager(private val context: Context) {

    private val TAG = "InCallManager"

    private val teleManager = context.applicationContext
        .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val listener = MyPhoneStateListener()

    private val telecomManager by lazy { context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager }

    private var checkStateDisposable: Disposable? = null

    init {
        //注册电话状态监听器
        teleManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    fun destroy() {
        teleManager.listen(listener, PhoneStateListener.LISTEN_NONE)
    }

    internal inner class MyPhoneStateListener : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            super.onCallStateChanged(state, incomingNumber)
            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {
                    // 挂机
                    Log.e(TAG, "onCallStateChanged: 挂机")

                    checkStateDisposable?.dispose()
                    checkStateDisposable = null
                }

                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    Log.e(TAG, "onCallStateChanged: 摘机")

                    //  如果是去電則incomingNumber為""
                    // 【因为此处无法监听去电并且无法去电的电话号码，所以当去电时此处的打入电话号码为null或者""】
                    val incoming = incomingNumber.isNotBlank()
                    Log.e(TAG, "onCallStateChanged: ${if (incoming) "来电" else "去电"}")
                    checkStateDisposable = Observable.interval(2, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnDispose { AudioControl.closeSpeaker() }
                        .subscribe {
                            ensureDialForeground()
                            ensureSpeakerOn()
                            ensureVolumeMax()
                        }

                }
                TelephonyManager.CALL_STATE_RINGING -> {
                    // 响铃
                    Log.e(TAG, "onCallStateChanged: 响铃")
                    AudioControl.setRingVolumeMax()
                }
            }
        }
    }

    private fun ensureDialForeground() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "ensureDialForeground: no permissions")
            return
        }

        if (telecomManager.isInCall) {
            telecomManager.showInCallScreen(true)
        }
    }

    private fun ensureSpeakerOn() {
        AudioControl.openSpeaker()
    }

    private fun ensureVolumeMax() {
        AudioControl.setCallVolumeMax()
    }

    //此自动接听代码来自官方开源Demo http://code.google.com/p/auto-answer/source/detail?r=17 
    @Throws(Exception::class)
    private fun answerPhoneAidl() {
//        val c = Class.forName(teleManager.javaClass.getName())
//        val m = c.getDeclaredMethod("getITelephony")
//        m.isAccessible = true
//        val telephonyService: TelecomManager = m.invoke(teleManager, null as Array<Any?>?) as TelecomManager
//
//        // Silence the ringer and answer the call!
//        telephonyService.silenceRinger()
//        telephonyService.answerRingingCall()
    }

    /**
     * 利用JAVA反射机制调用ITelephony的answerRingingCall()开始通话。
     */
//    private fun StartCall() {
//
//        // 初始化iTelephony
//        val c: Class<TelephonyManager> = TelephonyManager::class.java
//        var getITelephonyMethod: Method? = null
//        try {
//
//            // 获取所有public/private/protected/默认
//
//            // 方法的函数，如果只需要获取public方法，则可以调用getMethod.
//            getITelephonyMethod = c.getDeclaredMethod("getITelephony", *null as Array<Class<*>?>?)
//
//            // 将要执行的方法对象设置是否进行访问检查，也就是说对于public/private/protected/默认
//
//            // 我们是否能够访问。值为 true 则指示反射的对象在使用时应该取消 Java 语言访问检查。值为 false
//
//            // 则指示反射的对象应该实施 Java 语言访问检查。
//            getITelephonyMethod.isAccessible = true
//        } catch (e: SecurityException) {
//            Toast.makeText(context, "安全异常：" + e.message, Toast.LENGTH_SHORT).show()
//        } catch (e: NoSuchMethodException) {
//            Toast.makeText(context, "未找到方法：" + e.message, Toast.LENGTH_SHORT).show()
//        }
//        try {
//            val iTelephony: ITelephony = getITelephonyMethod!!.invoke(
//                teleManager,
//                null as Array<Any?>?
//            ) as ITelephony //停止响铃
//            iTelephony.silenceRinger() //接听来电
//            iTelephony.answerRingingCall()
//        } catch (e: IllegalArgumentException) {
//            Toast.makeText(context, "参数异常：" + e.message, Toast.LENGTH_SHORT).show()
//        } catch (e: IllegalAccessException) {
//            Toast.makeText(context, "进入权限异常：" + e.message, Toast.LENGTH_SHORT).show()
//        } catch (e: InvocationTargetException) {
//            Toast.makeText(context, "目标异常：" + e.message, Toast.LENGTH_SHORT).show()
//        } catch (e: RemoteException) {
//            Toast.makeText(context, "Remote异常：" + e.message, Toast.LENGTH_SHORT).show()
//        }
//    }

}