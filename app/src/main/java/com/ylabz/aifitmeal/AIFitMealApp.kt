package com.ylabz.aifitmeal

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ylabz.aifitmeal.data.HealthConnectManager
import com.ylabz.aifitmeal.ui.component.FoodPic
import com.ylabz.aifitmeal.ui.component.exercisesession.ExerciseSessionScreen
import com.ylabz.aifitmeal.ui.component.exercisesession.ExerciseSessionViewModel
import com.ylabz.aifitmeal.ui.component.exercisesession.ExerciseSessionViewModelFactory


@Composable
fun AIFitMealApp(
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
        Text("Calories ${calorieData.value}")
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
    }
}
