# Proguard & R8 configuration for MFS Fund & Expense Tracker

# Keep Room SQLite schemas and TypeConverters
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Kotlinx Serialization models
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class * {
    <fields>;
}

# Keep project data models
-keep class com.mfstracker.app.data.model.** { *; }
-keep class com.mfstracker.app.data.local.** { *; }

