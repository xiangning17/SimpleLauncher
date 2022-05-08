package com.xiangning.simplelauncher.telephony

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager

@SuppressLint("StaticFieldLeak")
object AudioControl {

    private lateinit var audioManager: AudioManager
    private lateinit var context: Context

    private val MAX_VOL_VOICE_CALL by lazy { audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) }
    private val MAX_VOL_RING by lazy { audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) }

    fun init(context: Context) {
        this.context = context.applicationContext
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    //打开扬声器
    fun openSpeaker() {
        audioManager.mode = AudioManager.MODE_IN_CALL
        //设置为true，打开扬声器
        audioManager.isSpeakerphoneOn = true
    }

    //关闭扬声器
    fun closeSpeaker() {
        audioManager.mode = AudioManager.MODE_NORMAL
        //设置为false，关闭已经打开的扬声器
        audioManager.isSpeakerphoneOn = false
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