package com.ylabz.aifitmeal

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ylabz.aifitmeal.data.HealthConnectManager
import com.ylabz.aifitmeal.ui.component.FoodPic
import com.ylabz.aifitmeal.ui.component.exercisesession.ExerciseSessionScreen
import com.ylabz.aifitmeal.ui.component.exercisesession.ExerciseSessionViewModel
import com.ylabz.aifitmeal.ui.component.exercisesession.ExerciseSessionViewModelFactory


@Composable
fun AIFitMealApp(healthConnectManager: HealthConnectManager, bitmap : MutableState<Bitmap?>) {
    val availability by healthConnectManager.availability
    val viewModel: ExerciseSessionViewModel = viewModel(
        factory = ExerciseSessionViewModelFactory(
            healthConnectManager = healthConnectManager
        )
    )

    val permissionsGranted by viewModel.permissionsGranted
    val permissions = viewModel.permissions

    val totalCalories by viewModel.totalCalories.collectAsState()


    val onPermissionsResult = { viewModel.initialLoad() }
    val permissionsLauncher =
        rememberLauncherForActivityResult(viewModel.permissionsLauncher) {
            onPermissionsResult()
        }
    Column {
        Text("Calories ${totalCalories}")
        ExerciseSessionScreen(
            permissionsGranted = permissionsGranted,
            permissions = permissions,
            uiState = viewModel.uiState,
            readCalories = {
                viewModel.getTotalCaloriesBurnedToday()
            },
            onPermissionsResult = {
                viewModel.initialLoad()
            },
            onPermissionsLaunch = { values ->
                permissionsLauncher.launch(values)
            }
        )
        FoodPic(bitmap)
    }
}
