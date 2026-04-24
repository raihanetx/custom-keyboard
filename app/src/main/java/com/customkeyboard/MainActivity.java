package com.customkeyboard;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.view.View;
import android.graphics.Color;
import android.graphics.Typeface;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private KeyboardPrefs prefs;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new KeyboardPrefs(this);
        buildUI();
    }

    private void buildUI() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#1A1A2E"));

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        main.setPadding(pad, pad, pad, pad);

        // ============ HEADER ============
        addEmoji(main, "⌨️", 48);
        addTitle(main, "Custom Keyboard");
        addSubtitle(main, "A powerful keyboard with voice, clipboard & Bangla translation");

        // ============ STATUS ============
        statusText = new TextView(this);
        statusText.setTextSize(15);
        statusText.setPadding(0, dp(16), 0, dp(16));
        main.addView(statusText);

        // ============ SETUP BUTTONS ============
        addButton(main, "🔧 Enable Keyboard", "#E94560", v -> {
            try {
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(this, "Could not open settings", Toast.LENGTH_SHORT).show();
            }
        });

        addButton(main, "🔄 Switch to Keyboard", "#16213E", v -> {
            try {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showInputMethodPicker();
            } catch (Exception e) {
                Toast.makeText(this, "Could not open keyboard picker", Toast.LENGTH_SHORT).show();
            }
        });

        addDivider(main);

        // ============ FEATURES ============
        addSectionTitle(main, "✨ Features");

        addCheckbox(main, "🎤 Voice Typing", "Use speech-to-text to type by voice",
            prefs.isVoiceEnabled(), prefs::setVoiceEnabled);

        addCheckbox(main, "📋 Clipboard", "Quick paste & clipboard access",
            prefs.isClipboardEnabled(), prefs::setClipboardEnabled);

        addCheckbox(main, "📋 Clipboard History", "Remember copied text for quick access",
            prefs.isClipboardHistoryEnabled(), prefs::setClipboardHistoryEnabled);

        addCheckbox(main, "🇧🇩 Bangla Translation", "Translate between Bangla and English",
            prefs.isBanglaTranslationEnabled(), prefs::setBanglaTranslationEnabled);

        // Translation mode selector
        LinearLayout transModeLayout = new LinearLayout(this);
        transModeLayout.setOrientation(LinearLayout.VERTICAL);
        transModeLayout.setPadding(dp(16), dp(8), dp(8), dp(8));
        transModeLayout.setBackgroundColor(Color.parseColor("#16213E"));

        TextView transLabel = new TextView(this);
        transLabel.setText("Translation Direction:");
        transLabel.setTextColor(Color.parseColor("#CCCCCC"));
        transLabel.setTextSize(13);
        transModeLayout.addView(transLabel);

        RadioGroup transGroup = new RadioGroup(this);
        transGroup.setOrientation(RadioGroup.VERTICAL);

        int idOff = View.generateViewId();
        int idBnEn = View.generateViewId();
        int idEnBn = View.generateViewId();

        RadioButton rbOff = createRadioButton("Off", idOff);
        RadioButton rbBnEn = createRadioButton("Bangla → English (transliterate)", idBnEn);
        RadioButton rbEnBn = createRadioButton("English → Bangla (phonetic)", idEnBn);

        transGroup.addView(rbOff);
        transGroup.addView(rbBnEn);
        transGroup.addView(rbEnBn);

        int currentMode = prefs.getTranslationMode();
        if (currentMode == 0) rbOff.setChecked(true);
        else if (currentMode == 1) rbBnEn.setChecked(true);
        else rbEnBn.setChecked(true);

        transGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == idOff) prefs.setTranslationMode(0);
            else if (checkedId == idBnEn) prefs.setTranslationMode(1);
            else if (checkedId == idEnBn) prefs.setTranslationMode(2);
        });

        transModeLayout.addView(transGroup);
        main.addView(transModeLayout);

        addDivider(main);

        // ============ BEHAVIOR ============
        addSectionTitle(main, "⚙️ Behavior");

        addCheckbox(main, "Auto Capitalize", "Capitalize first letter of sentences",
            prefs.isAutoCapitalize(), prefs::setAutoCapitalize);

        addCheckbox(main, "Word Suggestions", "Show word suggestions while typing",
            prefs.isSuggestionsEnabled(), prefs::setSuggestionsEnabled);

        addCheckbox(main, "Key Vibration", "Vibrate on key press",
            prefs.isVibrateEnabled(), prefs::setVibrateEnabled);

        addCheckbox(main, "Key Sound", "Play sound on key press",
            prefs.isSoundEnabled(), prefs::setSoundEnabled);

        addCheckbox(main, "Key Popup", "Show key popup on press",
            prefs.isPopupEnabled(), prefs::setPopupEnabled);

        addCheckbox(main, "Emoji Row", "Show quick emoji row on main keyboard",
            prefs.isEmojiRowEnabled(), prefs::setEmojiRowEnabled);

        addDivider(main);

        // ============ APPEARANCE ============
        addSectionTitle(main, "🎨 Appearance");

        // Theme selector
        LinearLayout themeLayout = new LinearLayout(this);
        themeLayout.setOrientation(LinearLayout.VERTICAL);
        themeLayout.setPadding(dp(8), dp(8), dp(8), dp(8));

        TextView themeLabel = new TextView(this);
        themeLabel.setText("Theme:");
        themeLabel.setTextColor(Color.parseColor("#CCCCCC"));
        themeLabel.setTextSize(14);
        themeLayout.addView(themeLabel);

        String[] themes = {"🌙 Dark (Default)", "☀️ Light", "🖤 AMOLED Black", "🔵 Blue"};
        int selectedTheme = prefs.getTheme();

        for (int i = 0; i < themes.length; i++) {
            final int themeId = i;
            RadioButton rb = createRadioButton(themes[i], i);
            rb.setChecked(i == selectedTheme);
            rb.setOnCheckedChangeListener((btn, checked) -> {
                if (checked) prefs.setTheme(themeId);
            });
            themeLayout.addView(rb);
        }
        main.addView(themeLayout);

        // Keyboard height
        LinearLayout heightLayout = new LinearLayout(this);
        heightLayout.setOrientation(LinearLayout.VERTICAL);
        heightLayout.setPadding(dp(8), dp(16), dp(8), dp(8));

        TextView heightLabel = new TextView(this);
        heightLabel.setText("Keyboard Height: " + (int)(prefs.getHeightScale() * 100) + "%");
        heightLabel.setTextColor(Color.parseColor("#CCCCCC"));
        heightLabel.setTextSize(14);
        heightLayout.addView(heightLabel);

        SeekBar heightSeekBar = new SeekBar(this);
        heightSeekBar.setMax(150); // 50% to 200%
        heightSeekBar.setProgress((int)(prefs.getHeightScale() * 100) - 50);
        heightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float scale = (progress + 50) / 100f;
                heightLabel.setText("Keyboard Height: " + (int)(scale * 100) + "%");
                prefs.setHeightScale(scale);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        heightLayout.addView(heightSeekBar);
        main.addView(heightLayout);

        addDivider(main);

        // ============ ABOUT ============
        addSectionTitle(main, "ℹ️ About");
        addInfoText(main, "Custom Keyboard v2.0\nBuilt with ❤️ for Bangla speakers\n\nFeatures:\n• QWERTY, Symbols, Numbers, Emoji keyboards\n• Voice typing (speech-to-text)\n• Bangla ↔ English translation\n• Clipboard history & quick paste\n• 4 themes (Dark, Light, AMOLED, Blue)\n• Adjustable keyboard height\n• Long-press for numbers & symbols\n• Haptic feedback\n• Auto-capitalize\n• Cursor movement arrows");

        scrollView.addView(main);
        setContentView(scrollView);
    }

    // ============ UI HELPERS ============

    private void addEmoji(LinearLayout parent, String emoji, int size) {
        TextView tv = new TextView(this);
        tv.setText(emoji);
        tv.setTextSize(size);
        tv.setPadding(0, dp(8), 0, 0);
        parent.addView(tv);
    }

    private void addTitle(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(26);
        tv.setTextColor(Color.parseColor("#E94560"));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(0, dp(4), 0, dp(4));
        parent.addView(tv);
    }

    private void addSubtitle(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setTextColor(Color.parseColor("#AAAAAA"));
        tv.setPadding(0, 0, 0, dp(8));
        parent.addView(tv);
    }

    private void addSectionTitle(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(18);
        tv.setTextColor(Color.parseColor("#E94560"));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(0, dp(16), 0, dp(8));
        parent.addView(tv);
    }

    private void addDivider(LinearLayout parent) {
        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#2A2A4A"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        lp.setMargins(0, dp(12), 0, dp(12));
        divider.setLayoutParams(lp);
        parent.addView(divider);
    }

    private void addButton(LinearLayout parent, String text, String colorHex, View.OnClickListener listener) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(15);
        btn.setBackgroundColor(Color.parseColor(colorHex));
        btn.setPadding(dp(16), dp(12), dp(16), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(6), 0, dp(6));
        btn.setLayoutParams(lp);
        btn.setOnClickListener(listener);
        parent.addView(btn);
    }

    private void addCheckbox(LinearLayout parent, String title, String desc, boolean checked, 
                             java.util.function.Consumer<Boolean> setter) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(6), dp(8), dp(6));
        layout.setBackgroundColor(Color.parseColor("#16213E"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(3), 0, dp(3));
        layout.setLayoutParams(lp);

        CheckBox cb = new CheckBox(this);
        cb.setText(title);
        cb.setTextColor(Color.WHITE);
        cb.setTextSize(15);
        cb.setChecked(checked);
        cb.setOnCheckedChangeListener((btn, isChecked) -> setter.accept(isChecked));
        layout.addView(cb);

        TextView descTv = new TextView(this);
        descTv.setText(desc);
        descTv.setTextColor(Color.parseColor("#888888"));
        descTv.setTextSize(12);
        descTv.setPadding(dp(36), 0, 0, dp(4));
        layout.addView(descTv);

        parent.addView(layout);
    }

    private RadioButton createRadioButton(String text, int id) {
        RadioButton rb = new RadioButton(this);
        rb.setText(text);
        rb.setTextColor(Color.parseColor("#CCCCCC"));
        rb.setTextSize(13);
        rb.setId(id);
        rb.setPadding(dp(4), dp(2), dp(4), dp(2));
        return rb;
    }

    private void addInfoText(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#888888"));
        tv.setTextSize(13);
        tv.setPadding(dp(8), dp(4), dp(8), dp(4));
        tv.setBackgroundColor(Color.parseColor("#16213E"));
        tv.setPadding(dp(12), dp(12), dp(12), dp(12));
        parent.addView(tv);
    }

    private int dp(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        if (statusText == null) return;

        String enabledIMEs = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS);
        boolean enabled = enabledIMEs != null && enabledIMEs.contains(getPackageName());

        String currentIME = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        boolean isCurrent = currentIME != null && currentIME.contains(getPackageName());

        if (isCurrent) {
            statusText.setText("✅ Keyboard is active and ready!");
            statusText.setTextColor(Color.parseColor("#4CAF50"));
        } else if (enabled) {
            statusText.setText("⚠️ Keyboard is enabled but not selected.\nTap 'Switch to Keyboard' to activate.");
            statusText.setTextColor(Color.parseColor("#FFC107"));
        } else {
            statusText.setText("❌ Keyboard is not enabled.\nTap 'Enable Keyboard' to get started.");
            statusText.setTextColor(Color.parseColor("#FF5252"));
        }
    }
}
