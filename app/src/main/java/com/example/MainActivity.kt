package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.ChecklistRepository
import com.example.data.NotificationScheduler
import com.example.ui.TripAppContent
import com.example.ui.TripViewModel
import com.example.ui.TripViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { ChecklistRepository(database.checklistDao()) }
    private val viewModel: TripViewModel by viewModels {
        TripViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Pre-register and schedule reminders for all activities 15 mins before starting
        NotificationScheduler.scheduleAllTripReminders(this)

        setContent {
            MyApplicationTheme {
                TripAppContent(viewModel = viewModel)
            }
        }
    }
}
