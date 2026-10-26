package com.maksimowiczm.foodyou.app.infrastructure.android
import androidx.compose.ui.Modifier

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import android.content.Intent
import android.os.Bundle
import com.maksimowiczm.foodyou.app.ui.FoodYouApp

class MainActivity : FoodYouAbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UiSourceScope(uiInspectionSource.element("MainActivity.onCreate.FoodYouApp.1", "com.maksimowiczm.foodyou.app.ui.FoodYouApp", "MainActivity.onCreate")) { FoodYouApp(
                onDatabaseBackup = {
                    val intent =
                        Intent(this, DeveloperActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }

                    startActivity(intent)
                }
            ) }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.infrastructure.android",
    "app/src/androidMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/android/MainActivity.kt",
)
