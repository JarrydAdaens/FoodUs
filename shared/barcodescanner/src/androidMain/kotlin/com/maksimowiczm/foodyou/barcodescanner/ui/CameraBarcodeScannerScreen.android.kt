package com.maksimowiczm.foodyou.barcodescanner.ui

import com.maksimowiczm.foodyou.barcodescanner.ui.BarcodeUiSourceFile as UiSourceFile
import com.maksimowiczm.foodyou.barcodescanner.ui.barcodeUiName as uiName
import com.maksimowiczm.foodyou.barcodescanner.ui.BarcodeUiSourceScope as UiSourceScope

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import foodyou.app.generated.resources.*
import foodyou.app.generated.resources.Res
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun CameraBarcodeScannerScreen(
    onBarcodeScan: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier,
) {
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val activity = LocalActivity.current

    var requestInSettings by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
            isGranted ->
            if (
                activity != null &&
                    !isGranted &&
                    !shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
            ) {
                requestInSettings = true
            }
        }

    val context = LocalContext.current
    if (requestInSettings && !permissionState.status.isGranted) {
        RedirectToSettingsAlertDialog(
            onDismissRequest = { requestInSettings = false },
            onConfirm = {
                redirectToSettings(context)
                requestInSettings = false
            }, modifier = Modifier.uiName("CameraBarcodeScannerScreen.RedirectToSettingsAlertDialog.1", "com.maksimowiczm.foodyou.barcodescanner.ui.RedirectToSettingsAlertDialog", uiInspectionSource, "CameraBarcodeScannerScreen")
        )
    }

    val hapticFeedback = LocalHapticFeedback.current

    Box(modifier = (modifier).uiName("CameraBarcodeScannerScreen.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "CameraBarcodeScannerScreen")) {
        Box(modifier = (Modifier.safeGesturesPadding().align(Alignment.TopEnd).zIndex(1f)).uiName("CameraBarcodeScannerScreen.Box.2", "androidx.compose.foundation.layout.Box", uiInspectionSource, "CameraBarcodeScannerScreen")) {
            FilledIconButton(onClick = onClose, shapes = IconButtonDefaults.shapes(), modifier = Modifier.uiName("CameraBarcodeScannerScreen.FilledIconButton.1", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "CameraBarcodeScannerScreen")) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.action_close), modifier = Modifier.uiName("CameraBarcodeScannerScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "CameraBarcodeScannerScreen")
                )
            }
        }

        if (permissionState.status.isGranted) {
            UiSourceScope(uiInspectionSource.element("CameraBarcodeScannerScreen.ZxingCameraBarcodeScannerScreen.1", "com.maksimowiczm.foodyou.barcodescanner.ui.ZxingCameraBarcodeScannerScreen", "CameraBarcodeScannerScreen")) { ZxingCameraBarcodeScannerScreen(
                onBarcodeScan = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                    onBarcodeScan(it)
                },
                modifier = Modifier,
            ) }
        } else {
            UiSourceScope(uiInspectionSource.element("CameraBarcodeScannerScreen.RequestCameraPermissionScreen.1", "com.maksimowiczm.foodyou.barcodescanner.ui.RequestCameraPermissionScreen", "CameraBarcodeScannerScreen")) { RequestCameraPermissionScreen(
                onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                shouldShowRationale = permissionState.status.shouldShowRationale,
            ) }
        }
    }
}

@Composable
private fun RequestCameraPermissionScreen(
    onRequest: () -> Unit,
    shouldShowRationale: Boolean,
    modifier: Modifier = Modifier,
) {
    val text =
        if (shouldShowRationale) {
            stringResource(Res.string.neutral_barcode_scanner_camera_request_rationale)
        } else {
            stringResource(Res.string.neutral_barcode_scanner_camera_request)
        }

    Surface(modifier = (modifier).uiName("RequestCameraPermissionScreen.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "RequestCameraPermissionScreen"), onClick = onRequest) {
        Column(
            modifier = (Modifier.fillMaxSize().safeContentPadding()).uiName("RequestCameraPermissionScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "RequestCameraPermissionScreen"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge, modifier = Modifier.uiName("RequestCameraPermissionScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "RequestCameraPermissionScreen")
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.action_tap_to_allow_access),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium, modifier = Modifier.uiName("RequestCameraPermissionScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "RequestCameraPermissionScreen")
            )
        }
    }
}

@Composable
private fun RedirectToSettingsAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    UiSourceScope(uiInspectionSource.element("RedirectToSettingsAlertDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "RedirectToSettingsAlertDialog")) { AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.uiName("RedirectToSettingsAlertDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "RedirectToSettingsAlertDialog")) {
                Text(stringResource(Res.string.action_go_to_settings), modifier = Modifier.uiName("RedirectToSettingsAlertDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "RedirectToSettingsAlertDialog"))
            }
        },
        modifier = (modifier).uiName("RedirectToSettingsAlertDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "RedirectToSettingsAlertDialog"),
        dismissButton = {
            TextButton(onClick = onDismissRequest, modifier = Modifier.uiName("RedirectToSettingsAlertDialog.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "RedirectToSettingsAlertDialog")) {
                Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("RedirectToSettingsAlertDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "RedirectToSettingsAlertDialog"))
            }
        },
        title = { Text(stringResource(Res.string.headline_permission_required), modifier = Modifier.uiName("RedirectToSettingsAlertDialog.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "RedirectToSettingsAlertDialog")) },
        text = {
            Text(
                stringResource(
                    Res.string.neutral_barcode_scanner_camera_request_redirect_to_settings
                ), modifier = Modifier.uiName("RedirectToSettingsAlertDialog.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "RedirectToSettingsAlertDialog")
            )
        },
    ) }
}

private fun redirectToSettings(context: Context) {
    val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    context.startActivity(intent)
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.barcodescanner.ui",
    "shared/barcodescanner/src/androidMain/kotlin/com/maksimowiczm/foodyou/barcodescanner/ui/CameraBarcodeScannerScreen.android.kt",
)
