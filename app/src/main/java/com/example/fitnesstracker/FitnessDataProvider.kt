package com.example.fitnesstracker

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.util.Log

object FitnessDataProvider {
    private const val AUTHORITY = "com.example.fitnesstracker.provider"
    private val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/fitness_data")
    private const val TAG = "FitnessDataProvider"

    fun getFitnessData(contentResolver: ContentResolver): FitnessData {
        Log.d(TAG, "getFitnessData: Attempting to retrieve fitness data")
        val projection = arrayOf("steps", "calories", "active_time")
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(CONTENT_URI, projection, null, null, null)
            Log.d(TAG, "getFitnessData: Query executed")
            if (cursor != null && cursor.moveToFirst()) {
                val steps = cursor.getInt(cursor.getColumnIndexOrThrow("steps"))
                val calories = cursor.getInt(cursor.getColumnIndexOrThrow("calories"))
                val activeTime = cursor.getInt(cursor.getColumnIndexOrThrow("active_time"))
                val fitnessData = FitnessData(steps, calories, activeTime)
                Log.d(TAG, "getFitnessData: Retrieved data: $fitnessData")
                return fitnessData
            } else {
                Log.w(TAG, "getFitnessData: Cursor is null or empty")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getFitnessData: Error retrieving data", e)
        } finally {
            cursor?.close()
            Log.d(TAG, "getFitnessData: Cursor closed")
        }
        Log.d(TAG, "getFitnessData: Returning default values")
        return FitnessData(0, 0, 0) // Return default values if data is not available
    }
}

data class FitnessData(
    val steps: Int,
    val caloriesBurned: Int,
    val activeTimeMinutes: Int
) {
    override fun toString(): String {
        return "FitnessData(steps=$steps, caloriesBurned=$caloriesBurned, activeTimeMinutes=$activeTimeMinutes)"
    }
}