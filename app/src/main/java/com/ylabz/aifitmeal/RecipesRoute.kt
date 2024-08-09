package com.ylabz.aifitmeal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.ylabz.aifitmeal.data.HealthConnectManager
import com.ylabz.aifitmeal.ui.component.TwoTextAreasTabs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File
import java.io.FileOutputStream

@Composable
fun RecipesRoute(
    recipesViewModel: RecipesViewModel = viewModel(),
    healthConnectManager: HealthConnectManager,
    bitmap: Bitmap?,
    calorieData: String? = null
) {



    val onEvent = recipesViewModel::onEvent
    val mlState by recipesViewModel.recipesUiState.collectAsStateWithLifecycle()
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
    onEvent: (RecipesEvent) -> Unit,
    mealUiState: RecipesUiState,
    bitmap: Bitmap?,
    calorieData: String?,
    healthConnectManager: HealthConnectManager
) {
    when (mealUiState) {
        is RecipesUiState.Loading -> {
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

        is RecipesUiState.Success -> {
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

        is RecipesUiState.Error -> {
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
    onEvent: (RecipesEvent) -> Unit,
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