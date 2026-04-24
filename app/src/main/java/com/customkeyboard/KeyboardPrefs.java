package com.customkeyboard;

import android.content.Context;
import android.content.SharedPreferences;

public class KeyboardPrefs {
    private static final String PREFS = "keyboard_prefs";
    private static final String KEY_VOICE_ENABLED = "voice_enabled";
    private static final String KEY_CLIPBOARD_ENABLED = "clipboard_enabled";
    private static final String KEY_CLIPBOARD_HISTORY = "clipboard_history_enabled";
    private static final String KEY_BANGLA_TRANSLATION = "bangla_translation";
    private static final String KEY_TRANSLATION_MODE = "translation_mode";
    private static final String KEY_VIBRATE = "vibrate";
    private static final String KEY_POPUP = "popup";
    private static final String KEY_THEME = "theme";
    private static final String KEY_AUTO_CAP = "auto_capitalize";
    private static final String KEY_HEIGHT_SCALE = "height_scale";
    private static final String KEY_SUGGESTIONS = "suggestions_enabled";
    private static final String KEY_GEMMA_API_KEY = "gemma_api_key";
    private static final String KEY_USE_GEMMA_VOICE = "use_gemma_voice";
    private static final String KEY_GROQ_API_KEY = "groq_api_key";

    // Per-language voice engine: 0=Android, 1=Groq Whisper
    private static final String KEY_EN_VOICE_ENGINE = "en_voice_engine";
    private static final String KEY_BN_VOICE_ENGINE = "bn_voice_engine";

    private final SharedPreferences prefs;

    public KeyboardPrefs(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isVoiceEnabled() { return prefs.getBoolean(KEY_VOICE_ENABLED, true); }
    public void setVoiceEnabled(boolean v) { prefs.edit().putBoolean(KEY_VOICE_ENABLED, v).apply(); }

    public boolean isClipboardEnabled() { return prefs.getBoolean(KEY_CLIPBOARD_ENABLED, true); }
    public void setClipboardEnabled(boolean v) { prefs.edit().putBoolean(KEY_CLIPBOARD_ENABLED, v).apply(); }

    public boolean isClipboardHistoryEnabled() { return prefs.getBoolean(KEY_CLIPBOARD_HISTORY, true); }
    public void setClipboardHistoryEnabled(boolean v) { prefs.edit().putBoolean(KEY_CLIPBOARD_HISTORY, v).apply(); }

    public boolean isBanglaTranslationEnabled() { return prefs.getBoolean(KEY_BANGLA_TRANSLATION, false); }
    public void setBanglaTranslationEnabled(boolean v) { prefs.edit().putBoolean(KEY_BANGLA_TRANSLATION, v).apply(); }

    public int getTranslationMode() { return prefs.getInt(KEY_TRANSLATION_MODE, 0); }
    public void setTranslationMode(int mode) { prefs.edit().putInt(KEY_TRANSLATION_MODE, mode).apply(); }

    public boolean isVibrateEnabled() { return prefs.getBoolean(KEY_VIBRATE, true); }
    public void setVibrateEnabled(boolean v) { prefs.edit().putBoolean(KEY_VIBRATE, v).apply(); }

    public boolean isPopupEnabled() { return prefs.getBoolean(KEY_POPUP, true); }
    public void setPopupEnabled(boolean v) { prefs.edit().putBoolean(KEY_POPUP, v).apply(); }

    public int getTheme() { return prefs.getInt(KEY_THEME, 0); }
    public void setTheme(int theme) { prefs.edit().putInt(KEY_THEME, theme).apply(); }

    public boolean isAutoCapitalize() { return prefs.getBoolean(KEY_AUTO_CAP, true); }
    public void setAutoCapitalize(boolean v) { prefs.edit().putBoolean(KEY_AUTO_CAP, v).apply(); }

    public boolean isSuggestionsEnabled() { return prefs.getBoolean(KEY_SUGGESTIONS, true); }
    public void setSuggestionsEnabled(boolean v) { prefs.edit().putBoolean(KEY_SUGGESTIONS, v).apply(); }

    public float getHeightScale() { return prefs.getFloat(KEY_HEIGHT_SCALE, 1.0f); }
    public void setHeightScale(float scale) { prefs.edit().putFloat(KEY_HEIGHT_SCALE, scale).apply(); }

    // Gemma
    public String getGemmaApiKey() { return prefs.getString(KEY_GEMMA_API_KEY, ""); }
    public void setGemmaApiKey(String key) { prefs.edit().putString(KEY_GEMMA_API_KEY, key).apply(); }
    public boolean isGemmaVoiceEnabled() { return prefs.getBoolean(KEY_USE_GEMMA_VOICE, false); }
    public void setGemmaVoiceEnabled(boolean v) { prefs.edit().putBoolean(KEY_USE_GEMMA_VOICE, v).apply(); }

    // Groq Whisper
    public String getGroqApiKey() { return prefs.getString(KEY_GROQ_API_KEY, ""); }
    public void setGroqApiKey(String key) { prefs.edit().putString(KEY_GROQ_API_KEY, key).apply(); }

    // Per-language voice engine: 0=Android, 1=Groq Whisper
    public int getEnVoiceEngine() { return prefs.getInt(KEY_EN_VOICE_ENGINE, 0); }
    public void setEnVoiceEngine(int engine) { prefs.edit().putInt(KEY_EN_VOICE_ENGINE, engine).apply(); }

    public int getBnVoiceEngine() { return prefs.getInt(KEY_BN_VOICE_ENGINE, 0); }
    public void setBnVoiceEngine(int engine) { prefs.edit().putInt(KEY_BN_VOICE_ENGINE, engine).apply(); }

    /**
     * Detect current input language from the active editor's subtype locale.
     * Returns "bn" for Bangla, "en" for everything else.
     */
    public String detectLanguage(android.view.inputmethod.EditorInfo ei) {
        if (ei != null) {
            String locale = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                android.os.LocaleList locales = ei.getHintLocales();
                if (locales != null && locales.size() > 0) {
                    locale = locales.get(0).getLanguage();
                }
            }
            if (locale == null && ei.inputType != 0) {
                // Fallback: check imeOptions or extras
                locale = "";
            }
            if ("bn".equals(locale) || "bn_BD".equals(locale) || "bn_IN".equals(locale)) {
                return "bn";
            }
        }
        return "en";
    }

    /**
     * Get voice engine for the given language code ("en" or "bn").
     */
    public int getVoiceEngineForLang(String lang) {
        if ("bn".equals(lang)) return getBnVoiceEngine();
        return getEnVoiceEngine();
    }
}
