# AEGIS ProGuard Rules
-keep class com.poriot.aegis.data.** { *; }
-keep class com.poriot.aegis.ui.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn kotlinx.coroutines.**