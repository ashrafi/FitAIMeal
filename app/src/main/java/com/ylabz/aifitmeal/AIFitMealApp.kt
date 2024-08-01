package com.ylabz.aifitmeal

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ylabz.aifitmeal.ui.component.exercisesession.ExerciseSessionScreen
import com.ylabz.aifitmeal.data.HealthConnectManager
import com.ylabz.aifitmeal.ui.component.exercisesession.ExerciseSessionViewModel
import com.ylabz.aifitmeal.ui.component.exercisesession.ExerciseSessionViewModelFactory


@Composable
fun AIFitMealApp(healthConnectManager: HealthConnectManager) {
    val availability by healthConnectManager.availability


    val viewModel: ExerciseSessionViewModel = viewModel(
        factory = ExerciseSessionViewModelFactory(
            healthConnectManager = healthConnectManager
        )
    )
    val permissionsGranted by viewModel.permissionsGranted
    val sessionsList by viewModel.sessionsList
    val permissions = viewModel.permissions
    val onPermissionsResult = { viewModel.initialLoad() }
    val permissionsLauncher =
        rememberLauncherForActivityResult(viewModel.permissionsLauncher) {
            onPermissionsResult()
        }
    ExerciseSessionScreen(
        permissionsGranted = permissionsGranted,
        permissions = permissions,
        sessionsList = sessionsList,
        uiState = viewModel.uiState,
        onInsertClick = {
            viewModel.insertExerciseSession()
        },
        onDetailsClick = { uid ->
            //navController.navigate(Screen.ExerciseSessionDetail.route + "/" + uid)
        },
        onError = { exception ->
            //showExceptionSnackbar(scaffoldState, scope, exception)
        },
        onPermissionsResult = {
            viewModel.initialLoad()
        },
        onPermissionsLaunch = { values ->
            permissionsLauncher.launch(values)
        }
    )

}