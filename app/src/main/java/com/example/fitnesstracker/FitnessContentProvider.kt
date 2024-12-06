package com.example.fitnesstracker

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class FitnessContentProvider : ContentProvider() {

    private lateinit var fitnessDataHelper: FitnessDataHelper

    companion object {
        const val AUTHORITY = "com.example.fitnesstracker.provider"
        private const val FITNESS_DATA = 1
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "fitness_data", FITNESS_DATA)
        }
    }

    override fun onCreate(): Boolean {
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
        return when (uriMatcher.match(uri)) {
            FITNESS_DATA -> {
                val fitnessData = fitnessDataHelper.getFitnessData()
                val cursor = MatrixCursor(arrayOf("steps", "calories", "active_time"))
                cursor.addRow(arrayOf(fitnessData.steps, fitnessData.caloriesBurned, fitnessData.activeTimeMinutes))
                cursor
            }
            else -> null
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            FITNESS_DATA -> "vnd.android.cursor.item/vnd.$AUTHORITY.fitness_data"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // Не реализовано, так как мы не вставляем новые данные через ContentProvider
        throw UnsupportedOperationException("Not implemented")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        // Не реализовано, так как мы не удаляем данные через ContentProvider
        throw UnsupportedOperationException("Not implemented")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        // Не реализовано, так как мы не обновляем данные через ContentProvider
        throw UnsupportedOperationException("Not implemented")
    }
}