package com.example.fitnesstracker

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder

class StepCounterService : Service(), SensorEventListener {

    private val binder = LocalBinder()
    private var stepCount = 0
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    inner class LocalBinder : Binder() {
        fun getService(): StepCounterService = this@StepCounterService
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    fun getStepCount(): Int {
        return stepCount
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                stepCount = it.values[0].toInt()
                broadcastStepUpdate()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for step counter
    }

    private fun broadcastStepUpdate() {
        val intent = Intent(ACTION_STEPS_UPDATED)
        intent.putExtra(EXTRA_STEPS, stepCount)
        sendBroadcast(intent)
    }

    companion object {
        const val ACTION_STEPS_UPDATED = "com.example.fitnesstracker.STEPS_UPDATED"
        const val EXTRA_STEPS = "extra_steps"
    }
}