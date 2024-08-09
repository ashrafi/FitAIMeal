package com.ylabz.aifitmeal.ui.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.exifinterface.media.ExifInterface
import com.ylabz.aifitmeal.R
import java.io.File
import java.io.FileOutputStream

@Composable
fun FoodPic(bitmap: MutableState<Bitmap?>) {
    var isImageVisible by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { capturedBitmap ->
        capturedBitmap?.let {
            val rotatedBitmap = rotateBitmapIfRequired(context, it)
            bitmap.value = rotatedBitmap
            isImageVisible = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)//horizontal = 16.dp)
    ) {

        // Spacer(modifier = Modifier.height(16.dp))

        if (bitmap.value == null) {
            Button(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.FoodBank, contentDescription = "Food Icon")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Picture of Fridge")
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    //.padding(8.dp)
            ) {
                if (isImageVisible) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        elevation = CardDefaults.elevatedCardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Image(
                            bitmap = bitmap.value!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        )
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
                if (!isImageVisible) {
                    Text(
                        text = "Fridge Snapshot",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
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
}

private fun rotateBitmapIfRequired(context: Context, bitmap: Bitmap): Bitmap {
    val tempFile = createTempFile(context)
    saveBitmapToFile(bitmap, tempFile)

    val exif = ExifInterface(tempFile.absolutePath)
    var orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    Log.d("FoodPic", "Orientation: $orientation")
    // NOTE : Open ticket with Google about this
    orientation = 6

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
    }

    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    tempFile.delete() // Clean up the temporary file

    return rotatedBitmap
}

private fun createTempFile(context: Context): File {
    return File.createTempFile("temp_image", ".jpg", context.cacheDir).apply {
        deleteOnExit()
    }
}

private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFoodPic() {
    val context = LocalContext.current
    val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.food, null) as BitmapDrawable
    val bitmap = drawable.bitmap

    val bitmapState = remember { mutableStateOf<Bitmap?>(bitmap) }

    FoodPic(bitmap = bitmapState)
}

