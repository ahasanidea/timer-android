package com.ahasanidea.timerapp

import android.os.Bundle
import android.os.CountDownTimer
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.ahasanidea.timerapp.util.PrefUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class TimerActivity : AppCompatActivity() {
    enum class TimerState {
        Stopped, Paused, Running
    }

    private lateinit var timer: CountDownTimer
    private var timerlengthSeconds = 0L
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
        //TODO: remove background timer, hide notification
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
            timerlengthSeconds
        //TODO: change secondsRemaining according to where the background timer stopped

        //resume where we left off
        if (timerState == TimerState.Running)
            startTimer()
        updateButtons()
        UpdateCountdownUI()

    }

    private fun updateButtons() {
        when(timerState){
            TimerState.Running ->{
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
        val minutesUntilFinished=secondsRemaining/60
        val secondsInMinuteUntilFinished=secondsRemaining -minutesUntilFinished
        val secondsStr=secondsInMinuteUntilFinished.toString()
        textView_countdown.text="$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"
        progress_coundown.progress=(timerlengthSeconds - secondsRemaining).toInt()

    }


    private fun setPreviousTimerLength() {
        timerlengthSeconds=PrefUtil.getPreviousTimerLengthSeconds(this)
        progress_coundown.max=timerlengthSeconds.toInt()
    }



    private fun onTimerFinished() {
        timerState = TimerState.Stopped
        //set the length of the timer to be the one set in SettingsActivity
        //if the length was changed when the timer was running
        setNewTimerLength()
        progress_coundown.progress=0
        PrefUtil.setSecondsRemaining(timerlengthSeconds,this)
        secondsRemaining=timerlengthSeconds
        updateButtons()
        UpdateCountdownUI()
    }

    private fun setNewTimerLength() {
        val lengthInMinutes=PrefUtil.getTimerLength(this)
        timerlengthSeconds=(lengthInMinutes * 60L)
        progress_coundown.max=timerlengthSeconds.toInt()
    }

    private fun startTimer(){
     timerState=TimerState.Running
     timer=object :CountDownTimer(secondsRemaining * 1000,1000){
         override fun onFinish() =onTimerFinished()

         override fun onTick(millisUntilFinished: Long) {
             secondsRemaining=millisUntilFinished/1000
             UpdateCountdownUI()
         }

     }.start()
 }

    override fun onPause() {
        super.onPause()
        if (timerState == TimerState.Running) {
            timer.cancel()
            //TODO: start background timer and show notification
        } else if (timerState == TimerState.Paused) {
            //TODO: show notification
        }
        PrefUtil.setPreviousTimerLengthSeconds(timerlengthSeconds, this)
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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
