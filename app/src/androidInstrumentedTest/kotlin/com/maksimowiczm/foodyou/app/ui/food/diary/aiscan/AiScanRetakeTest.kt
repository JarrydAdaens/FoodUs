package com.maksimowiczm.foodyou.app.ui.food.diary.aiscan

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.launchActivity
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimate
import com.maksimowiczm.foodyou.ai.domain.AiFoodScanner
import com.maksimowiczm.foodyou.ai.domain.AiScanResult
import com.maksimowiczm.foodyou.ai.domain.AiSettings
import com.maksimowiczm.foodyou.ai.domain.ObserveAiConfigured
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.ScanTestDriver
import com.maksimowiczm.foodyou.common.config.AppConfig
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.food.search.domain.FoodSearchUseCase
import foodyou.app.generated.resources.*
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test
import org.koin.core.context.GlobalContext

/** Actual composable, scanner state and accessibility actions; system camera/picker and permission
 * UI are simulated by a private registry/context. No hardware capture or owner content is used.
 */
@SdkSuppress(minSdkVersion = 30)
class AiScanRetakeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var decor: View? = null
    private var activityPackage = ""
    private val driver = ScanTestDriver(instrumentation, { decor }, { activityPackage }, { null })
    private lateinit var registry: ControlledRegistry
    private lateinit var permissionContext: CameraPermissionContext
    private lateinit var model: AiScanViewModel
    private var capturedCount = 0

    @Test fun previewLaunchesOnceRetainsReviewOnCancelAndReplacesOnlyOnSuccess() = withPhoto {
        val original = currentState()
        val originalJpeg = requireNotNull(original.jpeg)
        retake()
        assertEquals(listOf(Acquisition.Camera), launches())
        assertRetained(original, 0)
        completeCamera(null)
        assertRetained(original, 0)

        retake()
        assertEquals(listOf(Acquisition.Camera, Acquisition.Camera), launches())
        assertRetained(original, 0)
        val replacement = syntheticBitmap(Color.RED, 1600, 800)
        try {
            completeCamera(replacement)
            assertReplacement(originalJpeg, count = 1, width = 1024, height = 512)
        } finally {
            replacement.recycle()
        }
    }

    @Test fun permissionIsCheckedAtEachTapAndDenialOrRequestErrorRetainsReview() = withPhoto {
        val original = currentState()
        // Change after composition without recomposing: a remembered permission flag is stale.
        driver.onMain { permissionContext.granted = false }
        retake()
        assertEquals(listOf(Acquisition.Permission), launches())
        assertRetained(original, 0)
        driver.onMain { registry.completePermission(false) }
        assertRetained(original, 0)

        driver.onMain { registry.nextLaunchFailure = SecurityException("Synthetic permission launch") }
        retake()
        assertEquals(listOf(Acquisition.Permission, Acquisition.Permission), launches())
        assertRetained(original, 0)

        retake()
        driver.onMain { registry.completePermission(true) }
        assertEquals(listOf(Acquisition.Permission, Acquisition.Permission, Acquisition.Permission,
            Acquisition.Camera), launches())
        assertRetained(original, 0)
        completeCamera(null)
        assertRetained(original, 0)

        // Permission becoming granted must also be observed without rebuilding the composable.
        driver.onMain { permissionContext.granted = true }
        retake()
        assertEquals(Acquisition.Camera, launches().last())
        assertEquals(3, launches().count { it == Acquisition.Permission })
        val replacement = syntheticBitmap(Color.GREEN)
        try {
            completeCamera(replacement)
            assertReplacement(requireNotNull(original.jpeg), 1, 24, 24)
        } finally {
            replacement.recycle()
        }
    }

    @Test fun cameraLaunchAndEncodingErrorsKeepReviewAndAllowRetry() = withPhoto {
        val original = currentState()
        listOf(ActivityNotFoundException("Synthetic unavailable camera"),
            SecurityException("Synthetic camera launch")).forEachIndexed { index, failure ->
            driver.onMain { registry.nextLaunchFailure = failure }
            retake()
            assertEquals(index + 1, launches().size)
            assertRetained(original, 0)
        }
        retake()
        val invalid = syntheticBitmap(Color.RED).apply { recycle() }
        completeCamera(invalid)
        assertRetained(original, 0)
        retake()
        val replacement = syntheticBitmap(Color.GREEN)
        try {
            completeCamera(replacement)
            assertReplacement(requireNotNull(original.jpeg), 1, 24, 24)
            assertEquals(List(4) { Acquisition.Camera }, launches())
        } finally {
            replacement.recycle()
        }
    }

    @Test fun galleryOriginPreviewRetakesThroughCameraAndRetainsGalleryPhotoOnCancel() =
        withPhoto(initialPhoto = false) {
            driver.clickText(driver.label(Res.string.action_pick_from_gallery))
            assertEquals(listOf(Acquisition.Gallery), launches())
            val galleryFile = File.createTempFile("synthetic-retake-", ".jpg", requireNotNull(decor).context.cacheDir)
            try {
                galleryFile.writeBytes(syntheticJpeg(Color.BLUE))
                driver.onMain { registry.completeGallery(Uri.fromFile(galleryFile)) }
                driver.await("synthetic gallery photo acquired") { captureCount() == 1 }
                assertTrue(currentState() is AiScanUiState.Captured)
                // Retain an actual reviewed state over the gallery-acquired JPEG as well.
                driver.onMain { model.submit() }
                driver.await("synthetic gallery review") { currentState() is AiScanUiState.Result }
                val original = currentState()
                retake()
                assertEquals(listOf(Acquisition.Gallery, Acquisition.Camera), launches())
                assertRetained(original, 1)
                completeCamera(null)
                assertRetained(original, 1)
                retake()
                val replacement = syntheticBitmap(Color.RED)
                try {
                    completeCamera(replacement)
                    assertReplacement(requireNotNull(original.jpeg), 2, 24, 24)
                    assertEquals(listOf(Acquisition.Gallery, Acquisition.Camera, Acquisition.Camera), launches())
                } finally {
                    replacement.recycle()
                }
            } finally {
                assertTrue("Delete only this generated gallery fixture", galleryFile.delete())
            }
        }

    private fun retake() {
        val label = driver.label(Res.string.neutral_tap_photo_to_retake)
        // Compose can expose its Button role separately from the described actionable View.
        // Locate the actual preview description, then its click target rather than the role node.
        var root = driver.findText(label)
        while (root.parent != null) root = root.parent
        val described = driver.descendants(root).firstOrNull {
            it.isVisibleToUser && it.contentDescription?.toString() == label
        }
        assertNotNull("Preview exposes the retake description", described)
        var control = requireNotNull(described)
        while (!control.isClickable && control.parent != null) control = control.parent
        assertTrue("Preview is an enabled accessible action", control.isEnabled && control.isClickable)
        assertTrue("Retake action succeeds", control.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        instrumentation.waitForIdleSync()
        // No confirm dialog or Take Photo step can satisfy the synchronous registry assertions.
    }

    private fun assertRetained(original: AiScanUiState, count: Int) {
        assertSame("No intermediate discard or scanner state reset", original, currentState())
        assertSame("Prior JPEG remains usable", original.jpeg, currentState().jpeg)
        assertEquals("No replacement callback on unsuccessful acquisition", count, captureCount())
        driver.control(driver.label(Res.string.neutral_tap_photo_to_retake))
    }

    private fun assertReplacement(priorJpeg: ByteArray, count: Int, width: Int, height: Int) {
        driver.await("successful replacement callback") { captureCount() == count }
        val current = currentState()
        assertTrue("Successful replacement resets review through production ViewModel", current is AiScanUiState.Captured)
        val jpeg = requireNotNull(current.jpeg)
        assertFalse("Replacement differs from prior photo", priorJpeg.contentEquals(jpeg))
        val decoded = requireNotNull(BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size))
        try {
            assertEquals("Existing downscale width", width, decoded.width)
            assertEquals("Existing downscale height", height, decoded.height)
        } finally {
            decoded.recycle()
        }
        driver.control(driver.label(Res.string.neutral_tap_photo_to_retake))
    }

    private fun completeCamera(bitmap: Bitmap?) = driver.onMain { registry.completeCamera(bitmap) }
    private fun currentState() = driver.onMain { model.state.value }
    private fun captureCount() = driver.onMain { capturedCount }
    private fun launches() = driver.onMain { registry.launches.toList() }

    private fun withPhoto(initialPhoto: Boolean = true, checkFlow: () -> Unit) {
        val applicationKoin = GlobalContext.get()
        val config = object : AppConfig by applicationKoin.get<AppConfig>() {
            override val aiApiKey = ""
            override val aiEndpoint = ""
            override val aiModel = ""
        }
        val settings = object : UserPreferencesRepository<AiSettings> {
            override fun observe() = MutableStateFlow(AiSettings("synthetic-test-key", null, null, null))
            override suspend fun update(transform: AiSettings.() -> AiSettings) =
                error("Retake fixture must not persist settings")
        }
        val scanner = object : AiFoodScanner {
            override suspend fun scan(jpeg: ByteArray, hint: String?) =
                AiScanResult.Success(AiFoodEstimate("Synthetic retake food", calories = 100.0))
        }
        val store = ViewModelStore()
        val automation = instrumentation.uiAutomation
        val originalInfo = automation.serviceInfo
        val originalFlags = originalInfo.flags
        capturedCount = 0
        try {
            automation.serviceInfo = automation.serviceInfo.apply {
                flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            }
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activityPackage = activity.packageName
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    registry = ControlledRegistry()
                    permissionContext = CameraPermissionContext(activity)
                    model = AiScanViewModel(scanner, applicationKoin.get<FoodSearchUseCase>(),
                        ObserveAiConfigured(settings, config))
                    store.put("synthetic-retake", model)
                    if (initialPhoto) {
                        model.onPhotoCaptured(syntheticJpeg(Color.BLUE))
                        model.submit()
                    }
                    val owner = object : ActivityResultRegistryOwner {
                        override val activityResultRegistry = registry
                    }
                    activity.setContent {
                        CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner,
                            LocalContext provides permissionContext) {
                            val state by model.state.collectAsState()
                            MaterialTheme {
                                Column(Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
                                    AiScanCameraSection(
                                        jpeg = state.jpeg,
                                        onPhotoCaptured = { capturedCount++; model.onPhotoCaptured(it) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }
                }
                instrumentation.waitForIdleSync()
                if (initialPhoto) driver.await("synthetic initial review") { currentState() is AiScanUiState.Result }
                checkFlow()
            }
        } finally {
            driver.onMain { store.clear() }
            decor = null
            automation.serviceInfo = originalInfo.apply { flags = originalFlags }
            assertSame("Global application Koin survives", applicationKoin, GlobalContext.get())
        }
    }

    private class CameraPermissionContext(base: Context) : ContextWrapper(base) {
        var granted = true
        override fun checkPermission(permission: String, pid: Int, uid: Int): Int {
            check(permission == Manifest.permission.CAMERA) { "Unexpected fixture permission check" }
            return if (granted) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
        }
    }

    private enum class Acquisition { Camera, Permission, Gallery }

    private class ControlledRegistry : ActivityResultRegistry() {
        val launches = mutableListOf<Acquisition>()
        var nextLaunchFailure: RuntimeException? = null
        private var pending: Pair<Int, Acquisition>? = null

        override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>, input: I,
            options: ActivityOptionsCompat?) {
            val acquisition = when (contract.javaClass) {
                ActivityResultContracts.TakePicturePreview::class.java -> {
                    check(input == null)
                    Acquisition.Camera
                }
                ActivityResultContracts.RequestPermission::class.java -> {
                    check(input == Manifest.permission.CAMERA)
                    Acquisition.Permission
                }
                ActivityResultContracts.PickVisualMedia::class.java -> {
                    check((input as PickVisualMediaRequest).mediaType == ActivityResultContracts.PickVisualMedia.ImageOnly)
                    Acquisition.Gallery
                }
                else -> error("Unexpected acquisition contract")
            }
            check(pending == null) { "Concurrent synthetic acquisition" }
            launches += acquisition
            nextLaunchFailure?.let { nextLaunchFailure = null; throw it }
            pending = requestCode to acquisition
        }

        fun completeCamera(bitmap: Bitmap?) = complete(Acquisition.Camera, bitmap)
        fun completePermission(granted: Boolean) = complete(Acquisition.Permission, granted)
        fun completeGallery(uri: Uri) = complete(Acquisition.Gallery, uri)

        private fun <T> complete(expected: Acquisition, value: T) {
            val (requestCode, acquisition) = requireNotNull(pending)
            check(expected == acquisition)
            pending = null
            check(dispatchResult(requestCode, value)) { "Missing production acquisition callback" }
        }
    }

    private fun syntheticBitmap(color: Int, width: Int = 24, height: Int = 24) =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }

    private fun syntheticJpeg(color: Int): ByteArray {
        val bitmap = syntheticBitmap(color)
        return try {
            ByteArrayOutputStream().use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output))
                output.toByteArray()
            }
        } finally {
            bitmap.recycle()
        }
    }
}
