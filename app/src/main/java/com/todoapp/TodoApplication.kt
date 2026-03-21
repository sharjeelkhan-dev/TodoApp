package com.todoapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for TodoApp.
 * Annotated with @HiltAndroidApp to trigger Hilt code generation.
 * Implements WorkManager Configuration.Provider for HiltWorker support.
 */
@HiltAndroidApp
class TodoApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Enable Crashlytics collection (disable in debug if desired)
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
    }
}
