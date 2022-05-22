package com.smarttoolfactory.composecolordetector

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.google.modernstorage.photopicker.PhotoPicker

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun ImageSelectionButton(onImageSelected: (ImageBitmap) -> Unit) {
    val context = LocalContext.current

    val photoPicker =
        rememberLauncherForActivityResult(PhotoPicker()) { uris ->
            val uri = uris.firstOrNull() ?: return@rememberLauncherForActivityResult

            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(context.contentResolver, uri)
                ) { decoder, info, source ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }

            onImageSelected(bitmap.asImageBitmap())

        }

    FloatingActionButton(
        onClick = {
            photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.IMAGES_ONLY, 1))
        },
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_add_a_photo_24),
            contentDescription = null
        )
    }
}