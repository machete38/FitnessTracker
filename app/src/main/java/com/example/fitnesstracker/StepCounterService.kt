package com.example.fitnesstracker

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat.requestPermissions

class StepCounterService : Service(), SensorEventListener {
    private val binder = LocalBinder()
    private var stepCount = 0
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    inner class LocalBinder : Binder() {
        fun getService(): StepCounterService = this@StepCounterService
    }
    private lateinit var fitnessDataHelper: FitnessDataHelper

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Service created")
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Log.e(TAG, "Step Counter Sensor not available on this device")
        } else {
            Log.d(TAG, "onCreate: Step sensor available: ${stepSensor != null}")
        }
        fitnessDataHelper = FitnessDataHelper(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Service started")
        stepSensor?.let {
            val registered = sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "onStartCommand: Sensor listener registered: $registered")
            if (!registered) {
                Log.e(TAG, "Failed to register sensor listener. Sensor type: ${it.type}, Name: ${it.name}")
            }
        } ?: Log.e(TAG, "onStartCommand: Step sensor is null")
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind: Service bound")
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Service destroyed")
        sensorManager.unregisterListener(this)
    }

    fun getStepCount(): Int {
        Log.d(TAG, "getStepCount: Current step count: $stepCount")
        return stepCount
    }


    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val newStepCount = it.values[0].toInt()
                Log.d(TAG, "onSensorChanged: New step count: $newStepCount, Old step count: $stepCount")
                if (newStepCount != stepCount) {
                    stepCount = newStepCount
                    // Сохраняем новое значение шагов
                    val currentFitnessData = fitnessDataHelper.getFitnessData()
                    fitnessDataHelper.updateFitnessData(currentFitnessData.copy(steps = stepCount))
                    broadcastStepUpdate()
                } else {
                    Log.d(TAG, "onSensorChanged: Step count unchanged")
                }
            }
        } ?: Log.e(TAG, "onSensorChanged: Received null event")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "onAccuracyChanged: Sensor: ${sensor?.name}, Accuracy: $accuracy")
    }

    private fun broadcastStepUpdate() {
        Log.d(TAG, "broadcastStepUpdate: Broadcasting step count: $stepCount")
        val intent = Intent(ACTION_STEPS_UPDATED)
        intent.putExtra(EXTRA_STEPS, stepCount)
        sendBroadcast(intent)
    }

    companion object {
        private const val TAG = "StepCounterService"
        const val ACTION_STEPS_UPDATED = "com.example.fitnesstracker.STEPS_UPDATED"
        const val EXTRA_STEPS = "extra_steps"
    }
}