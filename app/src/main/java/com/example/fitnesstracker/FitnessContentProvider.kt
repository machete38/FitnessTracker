package com.example.fitnesstracker

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log

class FitnessContentProvider : ContentProvider() {
    private lateinit var fitnessDataHelper: FitnessDataHelper

    companion object {
        const val AUTHORITY = "com.example.fitnesstracker.provider"
        private const val FITNESS_DATA = 1
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "fitness_data", FITNESS_DATA)
        }
        private const val TAG = "FitnessContentProvider"
    }

    override fun onCreate(): Boolean {
        Log.d(TAG, "onCreate: Initializing FitnessContentProvider")
        fitnessDataHelper = FitnessDataHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        Log.d(TAG, "query: Received query request for URI: $uri")
        return when (uriMatcher.match(uri)) {
            FITNESS_DATA -> {
                val fitnessData = fitnessDataHelper.getFitnessData()
                Log.d(TAG, "query: Retrieved fitness data: $fitnessData")
                val cursor = MatrixCursor(arrayOf("steps", "calories", "active_time"))
                cursor.addRow(arrayOf(fitnessData.steps, fitnessData.caloriesBurned, fitnessData.activeTimeMinutes))
                cursor
            }
            else -> {
                Log.e(TAG, "query: Unknown URI: $uri")
                null
            }
        }
    }

    override fun getType(uri: Uri): String? {
        Log.d(TAG, "getType: Requested type for URI: $uri")
        return when (uriMatcher.match(uri)) {
            FITNESS_DATA -> "vnd.android.cursor.item/vnd.$AUTHORITY.fitness_data"
            else -> {
                Log.e(TAG, "getType: Unknown URI: $uri")
                null
            }
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.e(TAG, "insert: Not implemented")
        throw UnsupportedOperationException("Not implemented")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.e(TAG, "delete: Not implemented")
        throw UnsupportedOperationException("Not implemented")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, "update: Received update request for URI: $uri")
        return when (uriMatcher.match(uri)) {
            FITNESS_DATA -> {
                val steps = values?.getAsInteger("steps") ?: 0
                val calories = values?.getAsInteger("calories") ?: 0
                val activeTime = values?.getAsInteger("active_time") ?: 0
                val fitnessData = FitnessData(steps, calories, activeTime)
                Log.d(TAG, "update: Updating fitness data to: $fitnessData")
                fitnessDataHelper.updateFitnessData(fitnessData)
                context?.contentResolver?.notifyChange(uri, null)
                Log.d(TAG, "update: Notified content resolver of change")
                1
            }
            else -> {
                Log.e(TAG, "update: Unknown URI: $uri")
                0
            }
        }
    }
}