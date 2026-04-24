# Custom Keyboard ProGuard Rules

# Keep all keyboard classes
-keep class com.customkeyboard.** { *; }

# Keep InputMethodService
-keep class * extends android.inputmethodservice.InputMethodService { *; }

# Keep SpeechRecognizer
-keep class android.speech.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
