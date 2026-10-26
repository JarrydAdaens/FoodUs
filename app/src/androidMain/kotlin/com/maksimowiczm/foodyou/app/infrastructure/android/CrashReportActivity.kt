package com.maksimowiczm.foodyou.app.infrastructure.android
import androidx.compose.ui.Modifier

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import android.os.Bundle
import com.maksimowiczm.foodyou.app.ui.crash.CrashReportScreen
import com.maksimowiczm.foodyou.app.ui.theme.FoodYouTheme
import com.maksimowiczm.foodyou.common.config.AppConfig
import org.koin.android.ext.android.get

class CrashReportActivity : FoodYouAbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val errorMessage = intent.getStringExtra("report").toString()
        val appConfig: AppConfig = get()

        setContent {
            FoodYouTheme {
                UiSourceScope(uiInspectionSource.element("CrashReportActivity.onCreate.CrashReportScreen.1", "com.maksimowiczm.foodyou.app.ui.crash.CrashReportScreen", "CrashReportActivity.onCreate")) { CrashReportScreen(
                    message = errorMessage,
                    issueTrackerUrl = appConfig.issueTrackerUri,
                ) }
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.infrastructure.android",
    "app/src/androidMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/android/CrashReportActivity.kt",
)
