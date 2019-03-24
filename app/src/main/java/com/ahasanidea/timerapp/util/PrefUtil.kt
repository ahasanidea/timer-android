package com.ahasanidea.timerapp.util

import android.content.Context
import android.preference.PreferenceManager
import com.ahasanidea.timerapp.TimerActivity

class PrefUtil {
    companion object {
        fun getTimerLength(context: Context):Int{
            //placeholder
            return 1
        }
        private const val PREVIOUS_TIMER_LENGTH_SECONDS_ID="com.ahasanidea.timerapp.previous_timer_length_seconds"
        fun getPreviousTimerLengthSeconds(context:Context):Long{
            val preferences=PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID,0)
        }

        fun setPreviousTimerLengthSeconds(seconds:Long, contxt: Context){
           val editor=PreferenceManager.getDefaultSharedPreferences(contxt).edit()
            editor.putLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID,seconds)
            editor.apply()
        }

        private const val TIMER_STATE_ID="com.ahasanidea.timerapp.timer_state"
        fun getTimerState(contxt: Context):TimerActivity.TimerState{
            val preferences=PreferenceManager.getDefaultSharedPreferences(contxt)
            val ordinal=preferences.getInt(TIMER_STATE_ID,0)
            return TimerActivity.TimerState.values()[ordinal]
        }
        fun setTimerState(state:TimerActivity.TimerState,contxt: Context){
            val editor=PreferenceManager.getDefaultSharedPreferences(contxt).edit()
            val ordinal=state.ordinal
            editor.putInt(TIMER_STATE_ID,ordinal)
            editor.apply()

        }
        private const val SECONDS_REMAINING_ID = "com.resocoder.timer.seconds_remaining"
        fun getSecondsRemaining(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(SECONDS_REMAINING_ID, 0)
        }

        fun setSecondsRemaining(seconds: Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(SECONDS_REMAINING_ID, seconds)
            editor.apply()
        }
    }
}