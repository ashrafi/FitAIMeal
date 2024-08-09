package com.ylabz.aifitmeal

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ylabz.aifitmeal.data.HealthConnectManager
import com.ylabz.aifitmeal.ui.component.exercisesession.ExerciseSessionScreen
import com.ylabz.aifitmeal.ui.component.exercisesession.ExerciseSessionViewModel
import com.ylabz.aifitmeal.ui.component.exercisesession.ExerciseSessionViewModelFactory


@Composable
fun ExerciseData(
    healthConnectManager: HealthConnectManager,
    calorieData: MutableState<String?>,
) {
    val availability by healthConnectManager.availability
    val viewModel: ExerciseSessionViewModel = viewModel(
        factory = ExerciseSessionViewModelFactory(
            healthConnectManager = healthConnectManager
        )
    )
    val permissionsGranted by viewModel.permissionsGranted
    val permissions = viewModel.permissions
    calorieData.value = viewModel.totalCalories.collectAsState().value


    val onPermissionsResult = { viewModel.initialLoad() }
    val permissionsLauncher =
        rememberLauncherForActivityResult(viewModel.permissionsLauncher) {
            onPermissionsResult()
        }
    Column {
        ExerciseSessionScreen(
            permissionsGranted = permissionsGranted,
            permissions = permissions,
            uiState = viewModel.uiState,
            readCalories = {
                viewModel.getTotalCaloriesBurnedToday()
            },
            calorieData = calorieData,
            onPermissionsResult = {
                viewModel.initialLoad()
            },
            onPermissionsLaunch = { values ->
                permissionsLauncher.launch(values)
            }
        )
    }
}
