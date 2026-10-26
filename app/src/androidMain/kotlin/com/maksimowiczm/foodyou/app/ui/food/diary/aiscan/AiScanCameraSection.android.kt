package com.maksimowiczm.foodyou.app.ui.food.diary.aiscan

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import foodyou.app.generated.resources.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun AiScanCameraSection(
    jpeg: ByteArray?,
    onPhotoCaptured: (ByteArray) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current

    val takePicture =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            // Do not replace a usable photo until acquisition and JPEG encoding succeed.
            bitmap?.let { photoAcquisitionOrNull { it.toDownscaledJpeg() } }?.let(onPhotoCaptured)
        }

    val requestPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) photoAcquisitionOrNull { takePicture.launch(null) }
        }

    // Story 7: the modern photo picker needs no storage permission. The picked image is decoded and
    // fed through the same downscale + preview path as a freshly captured photo.
    val pickFromGallery =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { photoAcquisitionOrNull { it.decodeDownscaledJpeg(context) } }
                ?.let(onPhotoCaptured)
        }

    fun launchCapture() {
        photoAcquisitionOrNull {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                takePicture.launch(null)
            } else {
                requestPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    fun launchGallery() {
        photoAcquisitionOrNull {
            pickFromGallery.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    if (jpeg == null) {
        CaptureSection(
            onCapture = ::launchCapture,
            onPickFromGallery = ::launchGallery,
            modifier = (modifier).uiName("AiScanCameraSection.CaptureSection.1", "com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.CaptureSection", uiInspectionSource, "AiScanCameraSection"),
        )
    } else {
        PhotoPreview(jpeg = jpeg, onRetake = ::launchCapture, modifier = (modifier).uiName("AiScanCameraSection.PhotoPreview.1", "com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.PhotoPreview", uiInspectionSource, "AiScanCameraSection"))
    }
}

@Composable
private fun CaptureSection(
    onCapture: () -> Unit,
    onPickFromGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = (modifier.fillMaxWidth().height(220.dp)).uiName("CaptureSection.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "CaptureSection"),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(onClick = onCapture, modifier = (Modifier.weight(1f).fillMaxHeight()).uiName("CaptureSection.Card.1", "androidx.compose.material3.Card", uiInspectionSource, "CaptureSection")) {
            Column(
                modifier = (Modifier.fillMaxWidth().fillMaxHeight().padding(24.dp)).uiName("CaptureSection.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "CaptureSection"),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    modifier = (Modifier.height(48.dp)).uiName("CaptureSection.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "CaptureSection"),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.action_take_photo),
                    style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("CaptureSection.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "CaptureSection")
                )
            }
        }
        GalleryButton(onClick = onPickFromGallery, modifier = Modifier.uiName("CaptureSection.GalleryButton.1", "com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.GalleryButton", uiInspectionSource, "CaptureSection"))
    }
}

@Composable
private fun GalleryButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(onClick = onClick, modifier = (modifier.width(96.dp).fillMaxHeight()).uiName("GalleryButton.OutlinedCard.1", "androidx.compose.material3.OutlinedCard", uiInspectionSource, "GalleryButton")) {
        Column(
            modifier = (Modifier.fillMaxWidth().fillMaxHeight().padding(12.dp)).uiName("GalleryButton.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "GalleryButton"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = stringResource(Res.string.action_pick_from_gallery),
                modifier = (Modifier.height(32.dp)).uiName("GalleryButton.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "GalleryButton"),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.action_pick_from_gallery),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center, modifier = Modifier.uiName("GalleryButton.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "GalleryButton")
            )
        }
    }
}

@Composable
private fun PhotoPreview(
    jpeg: ByteArray,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageBitmap =
        remember(jpeg) { BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)?.asImageBitmap() }

    val retakeLabel = stringResource(Res.string.neutral_tap_photo_to_retake)

    Column(modifier = (modifier).uiName("PhotoPreview.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "PhotoPreview"), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedCard(
            onClick = onRetake,
            modifier = (Modifier.fillMaxWidth().semantics { contentDescription = retakeLabel }).uiName("PhotoPreview.OutlinedCard.1", "androidx.compose.material3.OutlinedCard", uiInspectionSource, "PhotoPreview"),
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    modifier = (Modifier.fillMaxWidth().aspectRatio(1f)).uiName("PhotoPreview.Image.1", "androidx.compose.foundation.Image", uiInspectionSource, "PhotoPreview"),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = retakeLabel,
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("PhotoPreview.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "PhotoPreview")
        )
    }
}

/** External acquisition can fail or become unavailable; keep the current photo for retry. */
private inline fun <T> photoAcquisitionOrNull(acquire: () -> T): T? =
    try {
        acquire()
    } catch (error: Exception) {
        when (error) {
            is CancellationException -> throw error
            is ActivityNotFoundException,
            is SecurityException,
            is IOException,
            is IllegalArgumentException,
            is IllegalStateException -> null
            else -> throw error
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
        check(scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream))
        stream.toByteArray()
    }
}

private const val MAX_EDGE = 1024
private const val JPEG_QUALITY = 85


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.diary.aiscan",
    "app/src/androidMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/aiscan/AiScanCameraSection.android.kt",
)
