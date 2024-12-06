package com.example.fitnesstracker

import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.net.Uri


class MainActivity : AppCompatActivity() {
    private lateinit var tvStepCount: TextView
    private lateinit var tvCaloriesBurned: TextView
    private lateinit var tvActiveTime: TextView
    private lateinit var cvWorkout: CardView
    private lateinit var cvGoals: CardView
    private lateinit var cvStatistics: CardView
    private lateinit var cvSettings: CardView

    private var stepCounterService: StepCounterService? = null
    private var isBound = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected: Service connected")
            val binder = service as StepCounterService.LocalBinder
            stepCounterService = binder.getService()
            isBound = true
            updateStepCount()
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(TAG, "onServiceDisconnected: Service disconnected")
            isBound = false
        }
    }
    private val stepUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == StepCounterService.ACTION_STEPS_UPDATED) {
                val steps = intent.getIntExtra(StepCounterService.EXTRA_STEPS, 0)
                Log.d(TAG, "stepUpdateReceiver: Received step update: $steps")
                updateStepCount(steps)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate: Activity created")

        initializeViews()
        setClickListeners()
        checkAndRequestPermissions()
        startAndBindStepCounterService()
        registerStepUpdateReceiver()
        // Initialize with default value
        updateStepCount(0)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity resumed")
        loadFitnessData()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Activity destroyed")
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        unregisterReceiver(stepUpdateReceiver)
    }

    private fun initializeViews() {
        tvStepCount = findViewById(R.id.tv_step_count)
        tvCaloriesBurned = findViewById(R.id.tv_calories_burned)
        tvActiveTime = findViewById(R.id.tv_active_time)
        cvWorkout = findViewById(R.id.cv_workout)
        cvGoals = findViewById(R.id.cv_goals)
        cvStatistics = findViewById(R.id.cv_statistics)
        cvSettings = findViewById(R.id.cv_settings)
        Log.d(TAG, "initializeViews: Views initialized")
    }

    private fun setClickListeners() {
        // TODO: Implement click listeners
        Log.d(TAG, "setClickListeners: Click listeners set")
    }

    private fun startAndBindStepCounterService() {
        Log.d(TAG, "startAndBindStepCounterService: Starting and binding service")
        // Start the service
        Intent(this, StepCounterService::class.java).also { intent ->
            startService(intent)
        }
        // Bind to the service
        Intent(this, StepCounterService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun registerStepUpdateReceiver() {
        Log.d(TAG, "registerStepUpdateReceiver: Registering step update receiver")
        val filter = IntentFilter(StepCounterService.ACTION_STEPS_UPDATED)
        registerReceiver(stepUpdateReceiver, filter)
    }

    private fun updateStepCount() {
        stepCounterService?.let { service ->
            val steps = service.getStepCount()
            Log.d(TAG, "updateStepCount: Updating step count from service: $steps")
            updateStepCount(steps)
        } ?: Log.e(TAG, "updateStepCount: StepCounterService is null")
    }

    private fun updateStepCount(steps: Int) {
        Log.d(TAG, "updateStepCount: Updating UI with step count: $steps")
        runOnUiThread {
            tvStepCount.text = steps.toString()
            updateGoalProgress()
        }

        // Обновляем данные через ContentProvider
        val values = ContentValues().apply {
            put("steps", steps)
            // Здесь вы можете добавить обновление калорий и активного времени, если это необходимо
        }
        contentResolver.update(Uri.parse("content://${FitnessContentProvider.AUTHORITY}/fitness_data"), values, null, null)
    }

    private fun loadFitnessData() {
        Log.d(TAG, "loadFitnessData: Loading fitness data")
        lifecycleScope.launch {
            val fitnessData = withContext(Dispatchers.IO) {
                FitnessDataProvider.getFitnessData(contentResolver)
            }
            Log.d(TAG, "loadFitnessData: Fitness data loaded: $fitnessData")
            updateUI(fitnessData)
        }
    }

    private fun updateUI(fitnessData: FitnessData) {
        Log.d(TAG, "updateUI: Updating UI with fitness data")
        tvCaloriesBurned.text = "${fitnessData.caloriesBurned} kcal"
        tvActiveTime.text = formatActiveTime(fitnessData.activeTimeMinutes)
        // Update progress towards goals
        updateGoalProgress(fitnessData)
    }

    private fun updateGoalProgress(fitnessData: FitnessData) {
        // TODO: Implement progress bar or other visual representation of goal progress
        Log.d(TAG, "updateGoalProgress: Updating goal progress")
    }
    private fun updateGoalProgress() {
        // TODO: Implement progress bar or other visual representation of goal progress
        Log.d(TAG, "updateGoalProgress: Updating goal progress")
    }

    private fun formatActiveTime(minutes: Int): String {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return "${hours}h ${remainingMinutes}m"
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    REQUEST_ACTIVITY_RECOGNITION_PERMISSION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ACTIVITY_RECOGNITION_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d(TAG, "ACTIVITY_RECOGNITION permission granted")
                    startAndBindStepCounterService()
                } else {
                    Log.e(TAG, "ACTIVITY_RECOGNITION permission denied")
                    // Здесь вы можете показать пользователю объяснение, почему это разрешение необходимо
                }
                return
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 1001
    }
}