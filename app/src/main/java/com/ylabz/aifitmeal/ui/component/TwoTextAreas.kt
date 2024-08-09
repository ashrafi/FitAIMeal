package com.ylabz.aifitmeal.ui.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.UnfoldLess
import androidx.compose.material.icons.twotone.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ylabz.aifitmeal.RecipesEvent
import com.ylabz.aifitmeal.R
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun TwoTextAreasTabs(
    geminiText: List<String>,
    bitmap: Bitmap?,
    caloriesText: String = "500",
    onEvent: (RecipesEvent) -> Unit,
    errorMessage: String,
    showError: Boolean = false,
    onErrorDismiss: () -> Unit,
    isLoading: Boolean = false
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabNames = listOf("Recipe", "Nutrition")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Enhanced TabRow with Material 3 design
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 4.dp // Adds a visual indicator below the selected tab
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            tabNames.forEachIndexed { index, title ->
                Tab(
                    text = {
                        Text(
                            title,
                            color = if (selectedTabIndex == index)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer,
                            style = if (selectedTabIndex == index)
                                MaterialTheme.typography.titleMedium
                            else
                                MaterialTheme.typography.bodyMedium
                        )
                    },
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                )
            }
        }

        // Error Snackbar
        if (showError) {
            Snackbar(
                action = {
                    TextButton(onClick = onErrorDismiss) {
                        Text(
                            text = "Dismiss",
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = errorMessage,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Loading Indicator
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        // Content Section
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> PromptSection(
                    index = selectedTabIndex,
                    bitmap = bitmap,
                    ansText = geminiText.getOrNull(selectedTabIndex) ?: "",
                    buttonText = "   🍛   🥙   Recipe   🥗   🍱  ",
                    initialPrompt = "Please use the provided image and to the best of your ability create a recipe for a dinner meal around $caloriesText calories. It's OK if not exact but just your best guess.",
                    onEvent = onEvent,
                    onErrorDismiss = onErrorDismiss
                )

                1 -> PromptSection(
                    index = selectedTabIndex,
                    bitmap = bitmap,
                    ansText = geminiText.getOrNull(selectedTabIndex) ?: "",
                    buttonText = "   💪   🫀   Nutrition   🫁   🧠 ",
                    initialPrompt = "You just provided a recipe with all the ingredients. What is the nutritional information of dinner recipe you just provided?  ",
                    onEvent = onEvent,
                    onErrorDismiss = onErrorDismiss
                )
            }
        }
    }
}


@Composable
fun PromptSection(
    index: Int,
    bitmap: Bitmap?,
    ansText: String,
    buttonText: String,
    initialPrompt: String,
    onEvent: (RecipesEvent) -> Unit,
    onErrorDismiss: () -> Unit
) {
    var isPromptVisible by rememberSaveable { mutableStateOf(false) }
    val icon = if (isPromptVisible) Icons.TwoTone.UnfoldLess else Icons.TwoTone.UnfoldMore
    var prompt by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Watch for changes in ansText
    LaunchedEffect(ansText) {
        isLoading = false
    }

    LaunchedEffect(initialPrompt) {
        prompt = "$initialPrompt Thank you for your help!"
    }

    Column {
        if (isPromptVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Image Preview",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextField(
                    value = prompt,
                    label = { Text("Prompt") },
                    onValueChange = { prompt = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Expand/Collapse",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { isPromptVisible = !isPromptVisible }
                    .padding(end = 8.dp)
            )

            Button(
                onClick = {
                    isLoading = true
                    try {
                        if (bitmap != null) {
                            onEvent(RecipesEvent.GenAiChatResponseImg(prompt, bitmap, index))
                        }
                    } catch (e: Exception) {
                        onErrorDismiss()
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = buttonText)
            }
        }

        Column {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            }
            MarkdownText(
                modifier = Modifier.padding(8.dp),
                markdown = ansText,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTwoTextAreasTabs() {
    TwoTextAreasTabs(
        geminiText = listOf("Recipe text", "Nutrition text"),
        bitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888),
        caloriesText = "500",
        onEvent = {},
        errorMessage = "Error occurred",
        showError = true,
        onErrorDismiss = {}
    )
}
