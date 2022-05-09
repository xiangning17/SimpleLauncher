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

    private const val TAG = "AudioControl"

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
        if (isHandleSpeaker) {
            return
        }

        // 有耳机时关闭免提
        if (hasOtherOutput()) {
            toggleSpeaker(on = false, hasOtherOutput = true)
            return
        }

        toggleSpeaker(true)
    }

    private fun hasOtherOutput(): Boolean {
        return audioManager.isWiredHeadsetOn || audioManager.isBluetoothScoOn
    }

    private fun toggleSpeaker(on: Boolean, hasOtherOutput: Boolean = false) {
        if (audioManager.isSpeakerphoneOn == on) {
            return
        }

        Log.e(TAG, "toggleSpeaker: $on, hasOtherOutput=$hasOtherOutput")

        isHandleSpeaker = true
        audioManager.isSpeakerphoneOn = on
        audioManager.mode = if (on) AudioManager.MODE_IN_CALL else AudioManager.MODE_NORMAL
        if (on) {
            audioManager.setRouting(
                AudioManager.MODE_IN_CALL,
                AudioManager.ROUTE_SPEAKER,
                AudioManager.ROUTE_SPEAKER
            )
        }

        Observable.fromCallable {
            val dump =
                ShellUtils.execCommand("dumpsys activity a | grep \"mResumedActivity:\"", true)
            if (dump.successMsg?.contains("com.android.incallui/.InCallActivity") != true) {
                return@fromCallable
            }

            Log.e("AudioControl", "toggleSpeaker")
            ShellUtils.execCommand("input tap 135 1208", true)
        }
            .subscribeOn(Schedulers.io())
            .doOnTerminate { isHandleSpeaker = false }
            .subscribe()
    }

    //关闭扬声器
    fun closeSpeaker() {
        toggleSpeaker(false)
    }

    fun setCallVolumeMax() {
        if (audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL) == MAX_VOL_VOICE_CALL) {
            return
        }

        Log.e(TAG, "setCallVolumeMax: $MAX_VOL_VOICE_CALL")
        audioManager.setStreamVolume(
            AudioManager.STREAM_VOICE_CALL,
            MAX_VOL_VOICE_CALL,
            AudioManager.STREAM_VOICE_CALL
        )
    }

    fun setRingVolumeMax() {
        if (audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL) == MAX_VOL_RING) {
            return
        }

        Log.e(TAG, "setRingVolumeMax: $MAX_VOL_RING")
        audioManager.setStreamVolume(
            AudioManager.STREAM_RING,
            MAX_VOL_RING,
            AudioManager.STREAM_RING
        )
    }
}