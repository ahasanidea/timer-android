package com.ahasanidea.timerapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.ahasanidea.timerapp.util.NotificationUtil
import com.ahasanidea.timerapp.util.PrefUtil
import com.ahasanidea.timerapp.util.TimeExpiredReceiver
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*


class TimerActivity : AppCompatActivity() {

    companion object {
        fun setAlarm(context: Context, nowSeconds: Long, secondRemaining: Long): Long {
            val wakeUpTime=(nowSeconds +secondRemaining) * 1000
            val alarmManager=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent=Intent(context,TimeExpiredReceiver::class.java)
            val pendingIntent=PendingIntent.getBroadcast(context,0,intent,0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,wakeUpTime,pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds,context)
            return wakeUpTime
        }
        fun removeAlarm(context: Context){
            val intent=Intent(context, TimeExpiredReceiver::class.java)
            val pendingIntent=PendingIntent.getBroadcast(context,0,intent,0)
            val alarmManager=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0,context)
        }
        val nowSeconds:Long
        get() = Calendar.getInstance().timeInMillis/1000

    }

    enum class TimerState {
        Stopped, Paused, Running
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds = 0L
    private var timerState = TimerState.Stopped

    private var secondsRemaining = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setIcon(R.drawable.ic_timer)
        supportActionBar?.title = "  Timer"

        fab_start.setOnClickListener {
            startTimer()
            timerState = TimerState.Running
            updateButtons()
        }
        fab_pause.setOnClickListener {
            timer.cancel()
            timerState = TimerState.Paused
            updateButtons()
        }
        fab_stop.setOnClickListener {
            timer.cancel()
            onTimerFinished()
        }

    }

    override fun onResume() {
        super.onResume()
        initTimer()

        removeAlarm(this)
        NotificationUtil.hideTimerNotification(this)
    }

    private fun initTimer() {
        timerState = PrefUtil.getTimerState(this)
        //we don't want to change the length of the timer which is already running
        //if the length was changed in settings while it was backgrounded
        if (timerState == TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()
        secondsRemaining = if (timerState == TimerState.Running || timerState == TimerState.Paused)
            PrefUtil.getSecondsRemaining(this)
        else
            timerLengthSeconds
        val alarmSetTime=PrefUtil.getAlarmSetTime(this)
        if(alarmSetTime >0)
            secondsRemaining-= nowSeconds - alarmSetTime

        if (secondsRemaining <=0)
            onTimerFinished()
        if (timerState == TimerState.Running)
            startTimer()
        updateButtons()
        UpdateCountdownUI()

    }

    private fun updateButtons() {
        when (timerState) {
            TimerState.Running -> {
                fab_start.isEnabled = false
                fab_pause.isEnabled = true
                fab_stop.isEnabled = true
            }
            TimerState.Stopped -> {
                fab_start.isEnabled = true
                fab_pause.isEnabled = false
                fab_stop.isEnabled = false
            }
            TimerState.Paused -> {
                fab_start.isEnabled = true
                fab_pause.isEnabled = false
                fab_stop.isEnabled = true
            }
        }
    }

    private fun UpdateCountdownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - (minutesUntilFinished * 60)
        val secondsStr = secondsInMinuteUntilFinished.toString()
        textView_countdown.text =
            "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"
        progress_coundown.progress = (timerLengthSeconds - secondsRemaining).toInt()

    }


    private fun setPreviousTimerLength() {
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
        progress_coundown.max = timerLengthSeconds.toInt()
    }


    private fun onTimerFinished() {
        timerState = TimerState.Stopped
        //set the length of the timer to be the one set in SettingsActivity
        //if the length was changed when the timer was running
        setNewTimerLength()
        progress_coundown.progress = 0
        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds
        updateButtons()
        UpdateCountdownUI()
    }

    private fun setNewTimerLength() {
        val lengthInMinutes = PrefUtil.getTimerLength(this)
        timerLengthSeconds = (lengthInMinutes * 60L)
        progress_coundown.max = timerLengthSeconds.toInt()
    }

    private fun startTimer() {
        timerState = TimerState.Running
        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                UpdateCountdownUI()
            }

        }.start()
    }

    override fun onPause() {
        super.onPause()
        if (timerState == TimerState.Running) {
            timer.cancel()
            val wakeUpTime= setAlarm(this, nowSeconds,secondsRemaining)
            NotificationUtil.showTimerRunning(this,wakeUpTime)

        } else if (timerState == TimerState.Paused) {
            NotificationUtil.showTimerPaused(this)
        }
        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        PrefUtil.setSecondsRemaining(secondsRemaining, this)
        PrefUtil.setTimerState(timerState, this)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent=Intent(this,SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
