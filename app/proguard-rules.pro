# Keep Room entities and database classes
-keep class androidx.room.** { *; }
-keep class com.example.frauddetector.data.db.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# TODO: tighten rules after integrating real model runtime (e.g., ONNX/TFLite)
