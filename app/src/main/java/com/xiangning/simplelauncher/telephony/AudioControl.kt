package com.xiangning.simplelauncher.telephony

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.xiangning.simplelauncher.utils.ShellUtils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

@SuppressLint("StaticFieldLeak")
object AudioControl {

    private lateinit var context: Context
    private lateinit var audioManager: AudioManager
    private lateinit var activityManager: ActivityManager

    private val MAX_VOL_VOICE_CALL by lazy { audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) }
    private val MAX_VOL_RING by lazy { audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) }

    fun init(context: Context) {
        this.context = context.applicationContext
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    private var isHandleSpeaker = false

    //打开扬声器
    fun openSpeaker() {
        if (audioManager.isSpeakerphoneOn || isHandleSpeaker) {
            return
        }

        isHandleSpeaker = true
        audioManager.mode = AudioManager.MODE_IN_CALL
        //设置为true，打开扬声器
        audioManager.isSpeakerphoneOn = true

        Observable.fromCallable {
            val dump =
                ShellUtils.execCommand("dumpsys activity a | grep \"mResumedActivity:\"", true)
            if (dump.successMsg?.contains("com.android.incallui/.InCallActivity") != true) {
                return@fromCallable
            }

            Log.e("AudioControl", "openSpeaker")
            ShellUtils.execCommand("input tap 135 1208", true)
        }
            .subscribeOn(Schedulers.io())
            .doOnTerminate { isHandleSpeaker = false }
            .subscribe()
    }

    //关闭扬声器
    fun closeSpeaker() {
//        audioManager.mode = AudioManager.MODE_NORMAL
//        //设置为false，关闭已经打开的扬声器
//        audioManager.isSpeakerphoneOn = false
    }

    fun setCallVolumeMax() {
        audioManager.setStreamVolume(
            AudioManager.STREAM_VOICE_CALL,
            MAX_VOL_VOICE_CALL,
            AudioManager.STREAM_VOICE_CALL
        )
    }

    fun setRingVolumeMax() {
        audioManager.setStreamVolume(
            AudioManager.STREAM_RING,
            MAX_VOL_RING,
            AudioManager.STREAM_RING
        )
    }
}