package com.maksimowiczm.foodyou.barcodescanner.ui

import com.maksimowiczm.foodyou.barcodescanner.ui.BarcodeUiSourceFile as UiSourceFile
import com.maksimowiczm.foodyou.barcodescanner.ui.barcodeUiName as uiName

import android.view.View
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource as androidStringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.google.zxing.client.android.R as zxingR
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.CompoundBarcodeView
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.camera.CameraSettings
import com.maksimowiczm.foodyou.barcodescanner.R
import com.maksimowiczm.foodyou.barcodescanner.databinding.CameraBarcodeLayoutBinding
import foodyou.app.generated.resources.*
import foodyou.app.generated.resources.Res
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun ZxingCameraBarcodeScannerScreen(
    onBarcodeScan: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var barcodeView by remember { mutableStateOf<CompoundBarcodeView?>(null) }
    var torchOn by rememberSaveable { mutableStateOf(false) }

    val latestOnBarcodeScanned by rememberUpdatedState(onBarcodeScan)
    DisposableEffect(barcodeView) {
        barcodeView?.resume()

        val callback = BarcodeCallback { result -> result?.let { latestOnBarcodeScanned(it.text) } }

        barcodeView?.decodeSingle(callback)

        val torchListener =
            object : DecoratedBarcodeView.TorchListener {
                override fun onTorchOn() {
                    torchOn = true
                }

                override fun onTorchOff() {
                    torchOn = false
                }
            }

        barcodeView?.setTorchListener(torchListener)

        barcodeView?.setStatusText("")

        onDispose { barcodeView?.pause() }
    }

    val resetFocus: suspend () -> Unit =
        remember(barcodeView) {
            {
                val view = barcodeView
                if (view != null) {
                    view.cameraSettings =
                        CameraSettings().apply { focusMode = CameraSettings.FocusMode.CONTINUOUS }
                    delay(5_000)
                    view.cameraSettings =
                        CameraSettings().apply { focusMode = CameraSettings.FocusMode.AUTO }
                }
            }
        }
    LaunchedEffect(resetFocus) { resetFocus() }

    Box((modifier).uiName("ZxingCameraBarcodeScannerScreen.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "ZxingCameraBarcodeScannerScreen")) {
        AndroidView(
            factory = { context -> View.inflate(context, R.layout.camera_barcode_layout, null) },
            update = { view ->
                val binding = CameraBarcodeLayoutBinding.bind(view)
                barcodeView = binding.barcodeView
            }, modifier = Modifier.uiName("ZxingCameraBarcodeScannerScreen.AndroidView.1", "androidx.compose.ui.viewinterop.AndroidView", uiInspectionSource, "ZxingCameraBarcodeScannerScreen")
        )

        Column(
            modifier = (Modifier.safeGesturesPadding().align(Alignment.BottomCenter).zIndex(1f)).uiName("ZxingCameraBarcodeScannerScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ZxingCameraBarcodeScannerScreen"),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FlashlightButton(
                enabled = torchOn,
                onClick = {
                    if (torchOn) {
                        barcodeView?.setTorchOff()
                    } else {
                        barcodeView?.setTorchOn()
                    }
                }, modifier = Modifier.uiName("ZxingCameraBarcodeScannerScreen.FlashlightButton.1", "com.maksimowiczm.foodyou.barcodescanner.ui.FlashlightButton", uiInspectionSource, "ZxingCameraBarcodeScannerScreen")
            )
            Surface(
                color = MaterialTheme.colorScheme.scrim,
                modifier = (Modifier.navigationBarsPadding().alpha(.5f)).uiName("ZxingCameraBarcodeScannerScreen.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "ZxingCameraBarcodeScannerScreen"),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = androidStringResource(zxingR.string.zxing_msg_default_status),
                    modifier = (Modifier.padding(8.dp)).uiName("ZxingCameraBarcodeScannerScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ZxingCameraBarcodeScannerScreen"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FlashlightButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val background by
        animateColorAsState(
            if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        )
    val content by
        animateColorAsState(
            if (enabled) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
        )

    FilledIconButton(
        onClick = onClick,
        shapes = IconButtonDefaults.shapes(),
        modifier = (modifier).uiName("FlashlightButton.FilledIconButton.1", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "FlashlightButton"),
        colors =
            IconButtonDefaults.filledIconButtonColors(
                containerColor = background,
                contentColor = content,
            ),
    ) {
        Icon(
            imageVector = if (enabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
            contentDescription =
                if (enabled) stringResource(Res.string.action_disable_camera_flash)
                else stringResource(Res.string.action_enable_camera_flash), modifier = Modifier.uiName("FlashlightButton.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "FlashlightButton")
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.barcodescanner.ui",
    "shared/barcodescanner/src/androidMain/kotlin/com/maksimowiczm/foodyou/barcodescanner/ui/ZxingCameraBarcodeScannerScreen.kt",
)
