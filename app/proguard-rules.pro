# ====================================================================
# Reglas de Ofuscación y Conservación R8 / ProGuard para Nierika
# ====================================================================

# 1. Conservar Atributos Esenciales para Stacktraces y Depuración en Producción
-keepattributes Signature, InnerClasses, EnclosingMethod, Deprecated, AnnotationDefault, *Annotation*
-renamesourcefileattribute SourceFile
-keepattributes SourceFile, LineNumberTable

# 2. Enums y Modelos de Dominio (Persistencia DataStore y Preferencias)
-keep enum com.ixeken.nierika.core.domain.model.** { *; }
-keep class com.ixeken.nierika.core.domain.model.** { *; }

# 3. Room Database, Entidades y DAOs
-keep class com.ixeken.nierika.core.database.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }

# 4. Reconocimiento de Texto ML Kit (Google Play Services)
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.vision.** { *; }
-dontwarn com.google.mlkit.**
-keep class * implements com.google.mlkit.vision.text.TextRecognizerOptionsInterface { *; }

# 5. Manipulación de Metadatos EXIF
-keep class androidx.exifinterface.media.ExifInterface { *; }

# 6. Biometría y Seguridad
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# 7. MainActivity Listener para Picture-in-Picture
-keepclassmembers class com.ixeken.nierika.MainActivity {
    public kotlin.jvm.functions.Function0 onUserLeaveHintListener;
    public void setOnUserLeaveHintListener(kotlin.jvm.functions.Function0);
    public kotlin.jvm.functions.Function0 getOnUserLeaveHintListener();
}

