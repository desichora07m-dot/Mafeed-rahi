package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.WorkEntry
import com.example.ui.components.AppScreen
import com.example.ui.components.TractorBottomBar
import com.example.ui.components.TractorTopBar
import com.example.ui.screens.AddEditWorkScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RateManagementScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.TractorHisabTheme
import com.example.ui.viewmodel.TractorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TractorHisabTheme {
                TractorApp()
            }
        }
    }
}

@Composable
fun TractorApp(
    viewModel: TractorViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var entryToEdit by remember { mutableStateOf<WorkEntry?>(null) }

    // Handle back button when in a subscreen
    if (currentScreen != AppScreen.HOME) {
        BackHandler {
            if (currentScreen == AppScreen.ADD_WORK && entryToEdit != null) {
                entryToEdit = null
            }
            currentScreen = AppScreen.HOME
        }
    }

    Scaffold(
        topBar = {
            if (currentScreen != AppScreen.ADD_WORK && currentScreen != AppScreen.SETTINGS) {
                TractorTopBar(
                    title = "Tractor Hisab",
                    subtitle = "Work & Customer Accounting",
                    onNavigateSettings = { currentScreen = AppScreen.SETTINGS }
                )
            }
        },
        bottomBar = {
            if (currentScreen != AppScreen.ADD_WORK && currentScreen != AppScreen.SETTINGS) {
                TractorBottomBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { screen ->
                        if (screen == AppScreen.ADD_WORK) {
                            entryToEdit = null
                        }
                        currentScreen = screen
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToAddWork = {
                        entryToEdit = null
                        currentScreen = AppScreen.ADD_WORK
                    },
                    onNavigateToEditWork = { entry ->
                        entryToEdit = entry
                        currentScreen = AppScreen.ADD_WORK
                    }
                )

                AppScreen.ADD_WORK -> AddEditWorkScreen(
                    viewModel = viewModel,
                    existingEntry = entryToEdit,
                    onNavigateBack = {
                        entryToEdit = null
                        currentScreen = AppScreen.HOME
                    }
                )

                AppScreen.CUSTOMERS -> CustomersScreen(
                    viewModel = viewModel
                )

                AppScreen.RATES -> RateManagementScreen(
                    viewModel = viewModel
                )

                AppScreen.REPORTS -> ReportsScreen(
                    viewModel = viewModel
                )

                AppScreen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = AppScreen.HOME }
                )
            }
        }
    }
}
