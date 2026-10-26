# AndroidX Test monitor references these compile-time method annotations only.
# Keep this exception in the test APK; shipping app/library R8 rules stay unchanged.
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.MustBeClosed
