package com.example.fitnesstracker

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            val binder = service as StepCounterService.LocalBinder
            stepCounterService = binder.getService()
            isBound = true
            updateStepCount()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    private val stepUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == StepCounterService.ACTION_STEPS_UPDATED) {
                val steps = intent.getIntExtra(StepCounterService.EXTRA_STEPS, 0)
                updateStepCount(steps)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setClickListeners()
        startAndBindStepCounterService()
        registerStepUpdateReceiver()

        // Initialize with default value
        updateStepCount(0)
    }

    override fun onResume() {
        super.onResume()
        loadFitnessData()
    }

    override fun onDestroy() {
        super.onDestroy()
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
    }

    private fun setClickListeners() {
//        cvWorkout.setOnClickListener { startActivity(Intent(this, WorkoutActivity::class.java)) }
//        cvGoals.setOnClickListener { startActivity(Intent(this, GoalsActivity::class.java)) }
//        cvStatistics.setOnClickListener { startActivity(Intent(this, StatisticsActivity::class.java)) }
//        cvSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }

    private fun startAndBindStepCounterService() {
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
        val filter = IntentFilter(StepCounterService.ACTION_STEPS_UPDATED)
        registerReceiver(stepUpdateReceiver, filter)
    }

    private fun updateStepCount() {
        stepCounterService?.let { service ->
            updateStepCount(service.getStepCount())
        }
    }

    private fun updateStepCount(steps: Int) {
        tvStepCount.text = "$steps steps"
    }

    private fun loadFitnessData() {
        lifecycleScope.launch {
            val fitnessData = withContext(Dispatchers.IO) {
                FitnessDataProvider.getFitnessData(contentResolver)
            }
            updateUI(fitnessData)
        }
    }

    private fun updateUI(fitnessData: FitnessData) {
        tvCaloriesBurned.text = "${fitnessData.caloriesBurned} kcal"
        tvActiveTime.text = formatActiveTime(fitnessData.activeTimeMinutes)

        // Update progress towards goals
        updateGoalProgress(fitnessData)
    }

    private fun updateGoalProgress(fitnessData: FitnessData) {
        // TODO: Implement progress bar or other visual representation of goal progress
    }

    private fun formatActiveTime(minutes: Int): String {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return "${hours}h ${remainingMinutes}m"
    }
}