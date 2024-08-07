package com.ylabz.aifitmeal.ui.component

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.ylabz.aifitmeal.R

@Composable
fun FoodPic(bitmap: MutableState<Bitmap?>) {

    // State to manage image visibility
    var isImageVisible by remember { mutableStateOf(true) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { capturedBitmap ->
        if (capturedBitmap != null) {
            val rotatedBitmap = rotateBitmap(capturedBitmap, 90)
            bitmap.value = rotatedBitmap
            isImageVisible = true // Ensure image is visible after capturing
        }
    }

    if (bitmap.value == null) {
        Button(
            onClick = { cameraLauncher.launch(null) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Take Picture of Fridge")
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (isImageVisible) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp), // Adjust height as needed
                    elevation = CardDefaults.elevatedCardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            bitmap = bitmap.value!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Camera, contentDescription = "Retake Picture")
            }

            // Hide/Show button at the bottom left
            FloatingActionButton(
                onClick = { isImageVisible = !isImageVisible },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(
                    imageVector = if (isImageVisible) Icons.Default.Close else Icons.Default.Visibility,
                    contentDescription = if (isImageVisible) "Hide Picture" else "Show Picture"
                )
            }
        }
    }
}


private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

// Helper function to create a sample bitmap for preview
private fun createSampleBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, 100f, 100f, paint)
    return bitmap
}

@Preview(showBackground = true)
@Composable
fun PreviewFoodPic() {
    // Load the drawable resource
    val context = LocalContext.current
    val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.food, null) as BitmapDrawable
    val bitmap = drawable.bitmap

    val bitmapState = remember { mutableStateOf<Bitmap?>(bitmap) }

    FoodPic(bitmap = bitmapState)
}