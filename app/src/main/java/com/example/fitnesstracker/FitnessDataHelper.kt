package com.example.fitnesstracker

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class FitnessDataHelper(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("FitnessData", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "FitnessDataHelper"
    }

    fun getFitnessData(): FitnessData {
        val steps = sharedPreferences.getInt("steps", 0)
        val calories = sharedPreferences.getInt("calories", 0)
        val activeTime = sharedPreferences.getInt("active_time", 0)
        val fitnessData = FitnessData(steps, calories, activeTime)
        Log.d(TAG, "getFitnessData: Retrieved data: $fitnessData")
        return fitnessData
    }

    fun updateFitnessData(fitnessData: FitnessData) {
        Log.d(TAG, "updateFitnessData: Updating data to: $fitnessData")
        sharedPreferences.edit().apply {
            putInt("steps", fitnessData.steps)
            putInt("calories", fitnessData.caloriesBurned)
            putInt("active_time", fitnessData.activeTimeMinutes)
            apply()
        }
        Log.d(TAG, "updateFitnessData: Data updated successfully")
    }
}