package com.example.fitnesstracker

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri

object FitnessDataProvider {
    private const val AUTHORITY = "com.example.fitnesstracker.provider"
    private val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/fitness_data")

    fun getFitnessData(contentResolver: ContentResolver): FitnessData {
        val projection = arrayOf("steps", "calories", "active_time")
        var cursor: Cursor? = null
        
        try {
            cursor = contentResolver.query(CONTENT_URI, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val steps = cursor.getInt(cursor.getColumnIndexOrThrow("steps"))
                val calories = cursor.getInt(cursor.getColumnIndexOrThrow("calories"))
                val activeTime = cursor.getInt(cursor.getColumnIndexOrThrow("active_time"))
                return FitnessData(steps, calories, activeTime)
            }
        } finally {
            cursor?.close()
        }
        
        return FitnessData(0, 0, 0) // Return default values if data is not available
    }
}

data class FitnessData(
    val steps: Int,
    val caloriesBurned: Int,
    val activeTimeMinutes: Int
)