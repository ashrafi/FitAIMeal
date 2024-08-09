/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ylabz.aifitmeal.ui.component.exercisesession

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.ExerciseSessionRecord
import java.util.UUID

/**
 * Shows a list of [ExerciseSessionRecord]s from today.
 */
@Composable
fun ExerciseSessionScreen(
    permissions: Set<String>,
    permissionsGranted: Boolean,
    uiState: ExerciseSessionViewModel.UiState,
    readCalories: () -> Unit = {},
    calorieData: MutableState<String?>,
    onPermissionsResult: () -> Unit = {},
    onPermissionsLaunch: (Set<String>) -> Unit = {},
) {
    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

    LaunchedEffect(uiState) {
        if (uiState is ExerciseSessionViewModel.UiState.Uninitialized) {
            onPermissionsResult()
        }
        if (uiState is ExerciseSessionViewModel.UiState.Error && errorId.value != uiState.uuid) {
            Log.d("ExerciseSessionScreen", "Error advice: ${uiState.exception}")
            errorId.value = uiState.uuid
        }
    }

    if (uiState != ExerciseSessionViewModel.UiState.Uninitialized) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp) // Reduced padding
        ) {
            if (!permissionsGranted) {
                ElevatedButton(
                    onClick = { onPermissionsLaunch(permissions) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp), // Reduced padding
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    contentPadding = PaddingValues(8.dp) // Reduced content padding
                ) {
                    Icon(
                        Icons.Default.RestaurantMenu,
                        contentDescription = "Request Permissions",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(18.dp) // Smaller icon
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Reduced spacing
                    Text(
                        text = "Request Permissions",
                        style = MaterialTheme.typography.bodyMedium.copy( // Smaller text style
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp), // Reduced padding
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ElevatedButton(
                        onClick = { readCalories() },
                        modifier = Modifier
                            .height(40.dp) // Reduced button height
                            .padding(end = 8.dp), // Reduced padding
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        contentPadding = PaddingValues(8.dp) // Reduced content padding
                    ) {
                        Icon(
                            Icons.Default.LocalDining,
                            contentDescription = "Check Calories",
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(18.dp) // Smaller icon
                        )
                        Spacer(modifier = Modifier.width(4.dp)) // Reduced spacing
                        Text(
                            text = "Check Calories",
                            style = MaterialTheme.typography.bodyMedium.copy( // Smaller text style
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        )
                    }
                    Text(
                        text = "Total Calories: ${calorieData.value}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp) // Reduced padding
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewExerciseSessionScreen() {
    ExerciseSessionScreen(
        permissions = setOf("permission1", "permission2"),
        permissionsGranted = false,
        uiState = ExerciseSessionViewModel.UiState.Done,
        readCalories = { /* Mocked readCalories function */ },
        calorieData = rememberSaveable { mutableStateOf("1232.07") },
        onPermissionsResult = { /* Mocked onPermissionsResult function */ },
        onPermissionsLaunch = { /* Mocked onPermissionsLaunch function */ }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewExerciseSessionScreenTrue() {
    ExerciseSessionScreen(
        permissions = setOf("permission1", "permission2"),
        permissionsGranted = true,
        uiState = ExerciseSessionViewModel.UiState.Done,
        readCalories = { /* Mocked readCalories function */ },
        calorieData = rememberSaveable { mutableStateOf("1232.07") },
        onPermissionsResult = { /* Mocked onPermissionsResult function */ },
        onPermissionsLaunch = { /* Mocked onPermissionsLaunch function */ }
    )
}
