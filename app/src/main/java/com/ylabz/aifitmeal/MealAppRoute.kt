package com.ylabz.aifitmeal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.ylabz.aifitmeal.data.HealthConnectManager
import com.ylabz.aifitmeal.ui.component.TwoTextAreasTabs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream

@Composable
fun MealAppRoute(
    fixMeViewModel: MealAppViewModel = viewModel(),
    healthConnectManager: HealthConnectManager
) {
    val onEvent = fixMeViewModel::onEvent
    val mlState by fixMeViewModel.mealAppUiState.collectAsStateWithLifecycle()

    MLScreen(
        onEvent = onEvent,
        fixMeUiState = mlState,
        healthConnectManager = healthConnectManager
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
internal fun MLScreen(
    modifier: Modifier = Modifier,
    onEvent: (MLEvent) -> Unit,
    fixMeUiState: MealAppUiState,
    healthConnectManager: HealthConnectManager
) {
    when (fixMeUiState) {
        is MealAppUiState.Loading -> {
            MLContent(
                modifier = modifier,
                onEvent = onEvent,
                result = emptyList(),
                location = null,
                healthConnectManager = healthConnectManager,
                error = null,
                loading = true
            )
        }

        is MealAppUiState.Success -> {
            MLContent(
                modifier = modifier,
                onEvent = onEvent,
                result = fixMeUiState.geminiResponses,
                location = fixMeUiState.currLocation,
                healthConnectManager = healthConnectManager,
                error = null
            )
        }

        is MealAppUiState.Error -> {
            MLContent(
                modifier = modifier,
                onEvent = onEvent,
                result = emptyList(),
                location = null,
                healthConnectManager = healthConnectManager,
                error = fixMeUiState.errorMessage
            )
        }
    }
}

@Composable
fun InitImagePath(context: Context): String {
    //return drawableToFilePath(context, R.drawable.baked_goods_1, "backed_goods")
    return drawableToFilePath(context, R.drawable.baked_goods_1, "my food")
}

fun drawableToFilePath(context: Context, drawableId: Int, fileName: String): String {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    val bitmap = (drawable as BitmapDrawable).bitmap

    val file = File(context.filesDir, fileName)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }

    return file.path
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalCoroutinesApi::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
internal fun MLContent(
    modifier: Modifier = Modifier,
    onEvent: (MLEvent) -> Unit,
    healthConnectManager: HealthConnectManager,
    result: List<String>,
    location: Location?,
    error: String?,
    loading: Boolean = false,
    calorieData: StateFlow<Double>? = null // Add optional parameter for calorieData
) {
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }
    Column(modifier = modifier) {
        AIFitMealApp(healthConnectManager = healthConnectManager, bitmap = bitmap)

        // Use the calorieData if it's not null
        calorieData?.collectAsState(initial = 0.0)?.value?.let { calories ->
            Text(
                text = "Total Calories: $calories",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

        TwoTextAreasTabs(
            geminiText = result,
            bitmap = bitmap,
            caloriesText = 500.toString(),
            onEvent = onEvent,
            errorMessage = error ?: "",
            showError = error != null,
            onErrorDismiss = {}
        )
    }
}
