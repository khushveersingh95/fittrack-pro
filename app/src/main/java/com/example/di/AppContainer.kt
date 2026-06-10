package com.example.di

import android.content.Context
import com.example.data.FitTrackDatabase
import com.example.data.FitTrackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AppContainer(private val context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database: FitTrackDatabase by lazy {
        FitTrackDatabase.getDatabase(context, applicationScope)
    }

    val repository: FitTrackRepository by lazy {
        FitTrackRepository(database.fitTrackDao())
    }
}
