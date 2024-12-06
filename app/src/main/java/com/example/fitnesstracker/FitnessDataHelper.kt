package com.example.fitnesstracker

import android.content.Context
import android.content.SharedPreferences

class FitnessDataHelper(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("FitnessData", Context.MODE_PRIVATE)

    fun getFitnessData(): FitnessData {
        val steps = sharedPreferences.getInt("steps", 0)
        val calories = sharedPreferences.getInt("calories", 0)
        val activeTime = sharedPreferences.getInt("active_time", 0)
        return FitnessData(steps, calories, activeTime)
    }

    fun updateFitnessData(fitnessData: FitnessData) {
        sharedPreferences.edit().apply {
            putInt("steps", fitnessData.steps)
            putInt("calories", fitnessData.caloriesBurned)
            putInt("active_time", fitnessData.activeTimeMinutes)
            apply()
        }
    }
}