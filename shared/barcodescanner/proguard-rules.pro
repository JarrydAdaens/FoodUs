-keep class com.maksimowiczm.foodyou.barcodescanner.ui.CameraBarcodeScannerScreen_androidKt { *; }

# This library is minified before the app consumes it. Preserve its optional public inspection
# bridge at that module boundary; the app's final R8 pass still optimizes the linked application.
-keep class com.maksimowiczm.foodyou.barcodescanner.ui.BarcodeUiSource { public *; }
-keep class com.maksimowiczm.foodyou.barcodescanner.ui.BarcodeUiInspection { public *; }
-keep class com.maksimowiczm.foodyou.barcodescanner.ui.BarcodeUiInspectionKt {
    public static void BarcodeUiInspectionProvider(...);
}
