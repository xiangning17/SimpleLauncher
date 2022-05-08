package com.xiangning.simplelauncher.notification

import android.annotation.SuppressLint
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit


class NotificationService : NotificationListenerService() {

    companion object {
        var instance: NotificationService? = null
    }

    @SuppressLint("CheckResult")
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.e("NotificationService", "onListenerConnected")
        // 延迟一下再请求
        Observable.timer(1000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                cancelAll()
            }
        instance = this
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?, rankingMap: RankingMap?) {
        super.onNotificationPosted(sbn, rankingMap)
        Log.e("NotificationService", "onNotificationPosted: $sbn")
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification?,
        rankingMap: RankingMap?,
        reason: Int
    ) {
        super.onNotificationRemoved(sbn, rankingMap, reason)
        Log.e("NotificationService", "onNotificationRemoved [$reason]: $sbn")
    }

    fun cancelAll() {
        if (instance == null) {
            return
        }
        Log.e("NotificationService", "cancelAll")
        cancelAllNotifications()
    }

}