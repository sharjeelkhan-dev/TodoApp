package com.todoapp
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.todoapp.data.worker.SyncWorker
import com.todoapp.domain.repository.AuthRepository
import com.todoapp.presentation.navigation.NavGraph
import com.todoapp.presentation.theme.TodoAppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Main entry point for the TodoApp.
 * Sets up Compose UI, edge-to-edge display, WorkManager periodic sync, and Firebase Analytics.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var analytics: FirebaseAnalytics

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Schedule periodic background sync
        scheduleSyncWorker()

        // Log app open event
        analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)

        setContent {
            val systemDarkMode = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemDarkMode) }
            val navController = rememberNavController()

            TodoAppTheme(darkTheme = isDarkMode) {
                NavGraph(
                    navController = navController,
                    isSignedIn = authRepository.isSignedIn,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { isDarkMode = it }
                )
            }
        }
    }

    /**
     * Schedule a periodic sync worker that runs every 15 minutes
     * when network is available. Uses KEEP policy to avoid duplicate work.
     */
    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    private fun scheduleSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
