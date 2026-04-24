package com.customkeyboard;

import android.content.Context;
import android.content.SharedPreferences;

public class KeyboardPrefs {
    private static final String PREFS = "keyboard_prefs";
    private static final String KEY_VOICE_ENABLED = "voice_enabled";
    private static final String KEY_CLIPBOARD_ENABLED = "clipboard_enabled";
    private static final String KEY_CLIPBOARD_HISTORY = "clipboard_history_enabled";
    private static final String KEY_BANGLA_TRANSLATION = "bangla_translation";
    private static final String KEY_TRANSLATION_MODE = "translation_mode"; // 0=off, 1=bangla_to_en, 2=en_to_bangla
    private static final String KEY_VIBRATE = "vibrate";
    private static final String KEY_SOUND = "sound";
    private static final String KEY_POPUP = "popup";
    private static final String KEY_THEME = "theme"; // 0=dark, 1=light, 2=amoled, 3=blue
    private static final String KEY_AUTO_CAP = "auto_capitalize";
    private static final String KEY_SUGGESTIONS = "suggestions";
    private static final String KEY_LONG_PRESS_DURATION = "long_press_duration";
    private static final String KEY_HEIGHT_SCALE = "height_scale";
    private static final String KEY_EMOJI_ROW = "emoji_row";

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

    public boolean isSoundEnabled() { return prefs.getBoolean(KEY_SOUND, false); }
    public void setSoundEnabled(boolean v) { prefs.edit().putBoolean(KEY_SOUND, v).apply(); }

    public boolean isPopupEnabled() { return prefs.getBoolean(KEY_POPUP, true); }
    public void setPopupEnabled(boolean v) { prefs.edit().putBoolean(KEY_POPUP, v).apply(); }

    public int getTheme() { return prefs.getInt(KEY_THEME, 0); }
    public void setTheme(int theme) { prefs.edit().putInt(KEY_THEME, theme).apply(); }

    public boolean isAutoCapitalize() { return prefs.getBoolean(KEY_AUTO_CAP, true); }
    public void setAutoCapitalize(boolean v) { prefs.edit().putBoolean(KEY_AUTO_CAP, v).apply(); }

    public boolean isSuggestionsEnabled() { return prefs.getBoolean(KEY_SUGGESTIONS, true); }
    public void setSuggestionsEnabled(boolean v) { prefs.edit().putBoolean(KEY_SUGGESTIONS, v).apply(); }

    public int getLongPressDuration() { return prefs.getInt(KEY_LONG_PRESS_DURATION, 400); }
    public void setLongPressDuration(int ms) { prefs.edit().putInt(KEY_LONG_PRESS_DURATION, ms).apply(); }

    public float getHeightScale() { return prefs.getFloat(KEY_HEIGHT_SCALE, 1.0f); }
    public void setHeightScale(float scale) { prefs.edit().putFloat(KEY_HEIGHT_SCALE, scale).apply(); }

    public boolean isEmojiRowEnabled() { return prefs.getBoolean(KEY_EMOJI_ROW, true); }
    public void setEmojiRowEnabled(boolean v) { prefs.edit().putBoolean(KEY_EMOJI_ROW, v).apply(); }
}
