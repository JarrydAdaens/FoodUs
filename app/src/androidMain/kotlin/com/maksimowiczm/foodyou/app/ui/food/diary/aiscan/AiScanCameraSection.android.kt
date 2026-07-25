package com.maksimowiczm.foodyou.app.ui.food.diary.aiscan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import foodyou.app.generated.resources.*
import java.io.ByteArrayOutputStream
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun AiScanCameraSection(
    jpeg: ByteArray?,
    onPhotoCaptured: (ByteArray) -> Unit,
    onDiscardRetry: () -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val takePicture =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) onPhotoCaptured(bitmap.toDownscaledJpeg())
        }

    val requestPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasPermission = granted
            if (granted) takePicture.launch(null)
        }

    // Story 7: the modern photo picker needs no storage permission. The picked image is decoded and
    // fed through the same downscale + preview path as a freshly captured photo.
    val pickFromGallery =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.decodeDownscaledJpeg(context)?.let(onPhotoCaptured)
        }

    fun launchCapture() {
        if (hasPermission) {
            takePicture.launch(null)
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    fun launchGallery() {
        pickFromGallery.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    if (jpeg == null) {
        CaptureSection(
            onCapture = ::launchCapture,
            onPickFromGallery = ::launchGallery,
            modifier = modifier,
        )
    } else {
        PhotoPreview(jpeg = jpeg, onDiscardRetry = onDiscardRetry, modifier = modifier)
    }
}

@Composable
private fun CaptureSection(
    onCapture: () -> Unit,
    onPickFromGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(220.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(onClick = onCapture, modifier = Modifier.weight(1f).fillMaxHeight()) {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.height(48.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.action_take_photo),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        GalleryButton(onClick = onPickFromGallery)
    }
}

@Composable
private fun GalleryButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(onClick = onClick, modifier = modifier.width(96.dp).fillMaxHeight()) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = stringResource(Res.string.action_pick_from_gallery),
                modifier = Modifier.height(32.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.action_pick_from_gallery),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PhotoPreview(
    jpeg: ByteArray,
    onDiscardRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageBitmap =
        remember(jpeg) { BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)?.asImageBitmap() }

    var showDiscardDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedCard(
            onClick = { showDiscardDialog = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable {
                        showDiscardDialog = true
                    },
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.neutral_tap_photo_to_retake),
            style = MaterialTheme.typography.bodySmall,
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onDiscardRetry()
                    }
                ) {
                    Text(stringResource(Res.string.action_discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
            title = { Text(stringResource(Res.string.question_discard_photo)) },
        )
    }
}

/** Decodes a gallery [Uri] into the same downscaled JPEG a captured photo produces, or null. */
private fun Uri.decodeDownscaledJpeg(context: Context): ByteArray? {
    val bitmap =
        context.contentResolver.openInputStream(this)?.use { BitmapFactory.decodeStream(it) }
    return bitmap?.toDownscaledJpeg()
}

/** Downscales so the longest edge is at most [MAX_EDGE] px, then JPEG-encodes at [JPEG_QUALITY]. */
private fun Bitmap.toDownscaledJpeg(): ByteArray {
    val longestEdge = maxOf(width, height)
    val scaled =
        if (longestEdge > MAX_EDGE) {
            val ratio = MAX_EDGE.toFloat() / longestEdge
            Bitmap.createScaledBitmap(
                this,
                (width * ratio).toInt().coerceAtLeast(1),
                (height * ratio).toInt().coerceAtLeast(1),
                true,
            )
        } else {
            this
        }
    return ByteArrayOutputStream().use { stream ->
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
        stream.toByteArray()
    }
}

private const val MAX_EDGE = 1024
private const val JPEG_QUALITY = 85
