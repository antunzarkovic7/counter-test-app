package com.example.testapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.testapp.MachineDoingPing.CounterState.*
import kotlinx.android.synthetic.main.activity_machine_doing_ping.*
import java.util.*


class MachineDoingPing : AppCompatActivity() {

    private var counterValue: Int = 0
    private var counterState: Int = Running.value

    private var counterTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_machine_doing_ping)

        // data fetch
        val prefs = getSharedPreferences("counterValues", MODE_PRIVATE)
        counterValue = prefs.getInt("counterValue", 0)
        counterState = prefs.getInt("counterState", Running.value)

        startInitTimer()
    }

    override fun onStart() {
        // Used to reinit counter timer after starting app from background
        if (counterState == Running.value) startCounter(false)

        actionBtn.setOnClickListener {
            if (counterTimer != null) {
                counterTimer?.cancel()
                counterTimer = null
                counterState = Paused.value
                actionBtn.text = getString(R.string.resume)
            } else {
                counterState = Running.value
                startCounter(false)
                actionBtn.text = getString(R.string.pause)
            }
        }

        super.onStart()
    }


    private fun startInitTimer() {
        if (counterState == Running.value) startCounter(true)
        Timer().schedule(object : TimerTask() {
            override fun run() {
                this@MachineDoingPing.runOnUiThread {
                    initTV.isVisible = false
                    counterTV.isVisible = true
                    actionBtn.isVisible = true
                    when (counterState) {
                        Running.value -> {
                            actionBtn.text = getString(R.string.pause)
                        }
                        Paused.value -> {
                            actionBtn.text = getString(R.string.resume)
                        }
                    }
                    counterTV.text = counterValue.toString()
                }
            }
        }, 10000)
    }

    private fun startCounter(init: Boolean) {
        // if counter timer already exists
        if (counterTimer != null) return
        val delay = if (init) 13000L else 3000L
        val period = 3000L

        counterTimer = Timer()
        counterTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                counterValue++
                this@MachineDoingPing.runOnUiThread {
                    counterTV.text = counterValue.toString()
                }
            }
        }, delay, period)
    }

    private fun saveCounterValues() {
        val editor = getSharedPreferences("counterValues", MODE_PRIVATE).edit()
        editor.putInt("counterValue", counterValue)
        editor.putInt("counterState", counterState)
        editor.apply()
    }

    enum class CounterState(val value: Int) {
        Paused(0),
        Running(1)
    }

    override fun onStop() {
        saveCounterValues()
        counterTimer?.cancel()
        counterTimer = null
        super.onStop()
    }

    override fun onDestroy() {
        saveCounterValues()
        super.onDestroy()
    }

}