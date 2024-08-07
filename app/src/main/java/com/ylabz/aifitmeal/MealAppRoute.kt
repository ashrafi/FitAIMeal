package com.ylabz.aifitmeal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.ylabz.aifitmeal.data.HealthConnectManager
import com.ylabz.aifitmeal.ui.component.TwoTextAreasTabs
import com.ylabz.aifitmeal.ui.component.exercisesession.ExerciseSessionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File
import java.io.FileOutputStream

@Composable
fun MealAppRoute(
    mealAppViewModel: MealAppViewModel = viewModel(),
    healthConnectManager: HealthConnectManager,
    bitmap: Bitmap?,
    calorieData: String? = null
) {



    val onEvent = mealAppViewModel::onEvent
    val mlState by mealAppViewModel.mealAppUiState.collectAsStateWithLifecycle()
    MLScreen(
        onEvent = onEvent,
        mealUiState = mlState,
        bitmap = bitmap,
        calorieData = calorieData,
        healthConnectManager = healthConnectManager
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
internal fun MLScreen(
    modifier: Modifier = Modifier,
    onEvent: (MLEvent) -> Unit,
    mealUiState: MealAppUiState,
    bitmap: Bitmap?,
    calorieData: String?,
    healthConnectManager: HealthConnectManager
) {
    when (mealUiState) {
        is MealAppUiState.Loading -> {
            MLContent(
                modifier = modifier,
                onEvent = onEvent,
                result = emptyList(),
                bitmap = bitmap,
                calorieData = null,
                healthConnectManager = healthConnectManager,
                error = null,
                loading = true
            )
        }

        is MealAppUiState.Success -> {
            MLContent(
                modifier = modifier,
                onEvent = onEvent,
                result = mealUiState.geminiResponses,
                bitmap = bitmap,
                calorieData = calorieData,
                healthConnectManager = healthConnectManager,
                error = null
            )
        }

        is MealAppUiState.Error -> {
            MLContent(
                modifier = modifier,
                onEvent = onEvent,
                result = emptyList(),
                bitmap = bitmap,
                calorieData = calorieData,
                healthConnectManager = healthConnectManager,
                error = mealUiState.errorMessage
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
    bitmap : Bitmap?,
    calorieData: String?,
    error: String?,
    loading: Boolean = false,
) {
    Column(modifier = modifier) {

        TwoTextAreasTabs(
            geminiText = result,
            bitmap = bitmap,
            caloriesText = calorieData.toString(),
            onEvent = onEvent,
            errorMessage = error ?: "",
            showError = error != null,
            onErrorDismiss = {}
        )
    }
}


/*@OptIn(ExperimentalPermissionsApi::class, ExperimentalCoroutinesApi::class)
@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun PreviewMLContent() {
    val healthConnectManager = HealthConnectManager(LocalContext.current)
    val result = listOf("Result 1", "Result 2")
    val error: String? = null
    val loading = false
    val calorieData = "250.0"

    MLContent(
        modifier = Modifier,
        onEvent = { /* Mocked event handler */ },
        healthConnectManager = healthConnectManager,
        result = result,
        error = error,
        loading = loading,
        calorieData = calorieData
    )
}*/