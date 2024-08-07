package com.ylabz.aifitmeal

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ylabz.aifitmeal.data.HealthConnectManager
import com.ylabz.aifitmeal.ui.component.FoodPic
import com.ylabz.aifitmeal.ui.component.exercisesession.ExerciseSessionViewModel
import com.ylabz.aifitmeal.ui.component.exercisesession.ExerciseSessionViewModelFactory
import com.ylabz.aifitmeal.ui.theme.AIFitMealTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val healthConnectManager by lazy { HealthConnectManager(this) }

        setContent {
            AIFitMealTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val context = LocalContext.current
                    val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.food, null) as BitmapDrawable
                    val bitmap = rememberSaveable { mutableStateOf<Bitmap?>(null) }
                    //bitmap.value = drawable.bitmap.asShared()
                    val calories = rememberSaveable { mutableStateOf<String?>(null) }
                    Column {
                        AIFitMealApp(
                            healthConnectManager = healthConnectManager,
                            calorieData = calories
                        )

                        FoodPic(bitmap)//bitmap = bitmap)
                        // Use the calorieData if it's not null
                        //calorieData?.collectAsState(initial = 0.0)?.value?.let { calories ->
                       Text(
                            text = "Total Calories: ${calories.value}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                        MealAppRoute(
                            healthConnectManager = healthConnectManager,
                            bitmap = bitmap.value,
                            calorieData = calories.value
                        )
                    }
                }
            }
        }
    }
}