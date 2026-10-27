package com.maksimowiczm.foodyou.food

import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Rect
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.test.core.app.launchActivity
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.ui.developer.inspection.*
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.ScanTestDriver
import com.maksimowiczm.foodyou.app.ui.food.library.PersonalLibraryScreen
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.RecipeIngredient
import com.maksimowiczm.foodyou.food.domain.repository.*
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.koin.core.context.GlobalContext
import java.util.concurrent.atomic.AtomicReference

@SdkSuppress(minSdkVersion = 31)
class PersonalLibraryInspectionTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var decor: View? = null
    private val driver = ScanTestDriver(instrumentation, { decor }, { instrumentation.targetContext.packageName }, { null })
    @Before fun prepare() = prepareSyntheticLibraryHost()

    @Test fun titleTabsQueryRowAndCreationExportTheirActualSourceWithoutActivatingActions() {
        val products = GlobalContext.get().get<ProductRepository>()
        val recipes = GlobalContext.get().get<RecipeRepository>()
        val name = "AAA Synthetic library inspector Recipe"
        runBlocking {
            if(recipes.observeLibraryNames().first().none { it.name == name }) {
                val id = products.insertProduct("AAA Synthetic inspector ingredient", null, null, null, false, null, null,
                    ProductSource(ProductSource.Type.User), NutritionFacts(energy = NutrientValue.Complete(120.0)))
                recipes.insertRecipe(name, 2, null, false, listOf(RecipeIngredient(products.observeProduct(id).first()!!, Measurement.Gram(100.0))))
            }
        }
        val inspector = AtomicReference<UiInspectorState?>()
        var creates = 0
        val info = instrumentation.uiAutomation.serviceInfo
        val flags = info.flags
        instrumentation.uiAutomation.serviceInfo = info.apply { this.flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS }
        try {
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activity.setContent { UiInspectionContent(true, true, {}) {
                        val current = LocalUiInspector.current
                        SideEffect { inspector.set(current) }
                        MaterialTheme { PersonalLibraryScreen({}, { creates++ }, { creates++ }, {}, {}, {}) }
                    } }
                }
                val labels = listOf(driver.label(Res.string.library_title), driver.label(Res.string.library_recipes),
                    driver.label(Res.string.library_search), name, driver.label(Res.string.library_create_recipe))
                val exported = mutableListOf<String>()
                labels.forEach { label ->
                    val bounds = Rect().also(driver.findText(label)::getBoundsInScreen)
                    val before = AtomicReference<Any?>()
                    instrumentation.runOnMainSync { before.set(inspector.get()?.snapshot) }
                    val down = SystemClock.uptimeMillis()
                    fun event(action: Int) = scenario.onActivity { activity ->
                        val origin = IntArray(2).also(activity.window.decorView::getLocationOnScreen)
                        MotionEvent.obtain(down, SystemClock.uptimeMillis(), action,
                            bounds.exactCenterX() - origin[0], bounds.exactCenterY() - origin[1], 0).also {
                            activity.dispatchTouchEvent(it); it.recycle()
                        }
                    }
                    event(MotionEvent.ACTION_DOWN)
                    SystemClock.sleep(3350)
                    event(MotionEvent.ACTION_UP)
                    driver.await("Actual library source selection changes") {
                        var changed = false
                        instrumentation.runOnMainSync { changed = inspector.get()?.snapshot != null && inspector.get()?.snapshot != before.get() }
                        changed
                    }
                    var xml = ""
                    instrumentation.runOnMainSync { xml = inspector.get()!!.snapshot!!.copyText(true) }
                    assertTrue(xml.contains("/food/library/PersonalLibraryScreen.kt"))
                    assertTrue(xml.contains("LibraryContent.") || xml.contains("LibraryRow."))
                    exported += xml
                }
                assertEquals(0, creates)
                java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "story38-library-inspection.xml")
                    .writeText(exported.joinToString("\n"))
            }
        } finally {
            decor = null
            instrumentation.uiAutomation.serviceInfo = info.apply { this.flags = flags }
        }
    }
}
