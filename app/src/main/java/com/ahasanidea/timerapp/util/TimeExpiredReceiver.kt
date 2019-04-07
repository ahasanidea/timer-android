package com.ahasanidea.timerapp.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ahasanidea.timerapp.TimerActivity

class TimeExpiredReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        NotificationUtil.showTimerExpired(context!!)

        PrefUtil.setTimerState(TimerActivity.TimerState.Stopped, context!!)
        PrefUtil.setAlarmSetTime(0, context)

    }
}