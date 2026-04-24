package com.customkeyboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

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
        scrollView.setBackgroundColor(Color.parseColor("#1C1C1E"));

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        main.setPadding(pad, pad, pad, pad);

        // ============ HEADER ============
        addEmoji(main, "⌨️", 42);
        addTitle(main, "Custom Keyboard");
        addSubtitle(main, "Voice • Clipboard • Bangla • Themes");

        // ============ STATUS ============
        statusText = new TextView(this);
        statusText.setTextSize(14);
        statusText.setPadding(0, dp(16), 0, dp(16));
        main.addView(statusText);

        // ============ SETUP BUTTONS ============
        addButton(main, "Enable Keyboard", "#5B6EF5", v -> {
            try {
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(this, "Could not open settings", Toast.LENGTH_SHORT).show();
            }
        });

        addButton(main, "Switch to Keyboard", "#2C2C2E", v -> {
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

        addCheckbox(main, "🎤 Voice Typing", "Speech-to-text input via microphone",
            prefs.isVoiceEnabled(), prefs::setVoiceEnabled);

        // Voice Engine — per language
        LinearLayout voiceEngineBox = settingsBox();
        TextView voiceEngineLabel = new TextView(this);
        voiceEngineLabel.setText("🎤 Voice Engine per Language:");
        voiceEngineLabel.setTextColor(Color.parseColor("#F2F2F7"));
        voiceEngineLabel.setTextSize(14);
        voiceEngineLabel.setTypeface(null, Typeface.BOLD);
        voiceEngineLabel.setPadding(dp(8), dp(4), dp(8), dp(4));
        voiceEngineBox.addView(voiceEngineLabel);

        // English engine
        TextView enLabel = new TextView(this);
        enLabel.setText("English voice input:");
        enLabel.setTextColor(Color.parseColor("#CCCCCC"));
        enLabel.setTextSize(13);
        enLabel.setPadding(dp(8), dp(8), dp(8), dp(2));
        voiceEngineBox.addView(enLabel);

        RadioGroup enGroup = new RadioGroup(this);
        enGroup.setOrientation(RadioGroup.HORIZONTAL);
        enGroup.setPadding(dp(4), 0, dp(8), dp(4));
        int idEnAndroid = View.generateViewId();
        int idEnGroq = View.generateViewId();
        RadioButton rbEnAndroid = makeRadio("Android", idEnAndroid);
        RadioButton rbEnGroq = makeRadio("Groq Whisper", idEnGroq);
        enGroup.addView(rbEnAndroid);
        enGroup.addView(rbEnGroq);
        if (prefs.getEnVoiceEngine() == 1) rbEnGroq.setChecked(true);
        else rbEnAndroid.setChecked(true);
        enGroup.setOnCheckedChangeListener((group, checkedId) -> {
            prefs.setEnVoiceEngine(checkedId == idEnGroq ? 1 : 0);
        });
        voiceEngineBox.addView(enGroup);

        // Bangla engine
        TextView bnLabel = new TextView(this);
        bnLabel.setText("Bangla voice input:");
        bnLabel.setTextColor(Color.parseColor("#CCCCCC"));
        bnLabel.setTextSize(13);
        bnLabel.setPadding(dp(8), dp(8), dp(8), dp(2));
        voiceEngineBox.addView(bnLabel);

        RadioGroup bnGroup = new RadioGroup(this);
        bnGroup.setOrientation(RadioGroup.HORIZONTAL);
        bnGroup.setPadding(dp(4), 0, dp(8), dp(4));
        int idBnAndroid = View.generateViewId();
        int idBnGroq = View.generateViewId();
        RadioButton rbBnAndroid = makeRadio("Android", idBnAndroid);
        RadioButton rbBnGroq = makeRadio("Groq Whisper", idBnGroq);
        bnGroup.addView(rbBnAndroid);
        bnGroup.addView(rbBnGroq);
        if (prefs.getBnVoiceEngine() == 1) rbBnGroq.setChecked(true);
        else rbBnAndroid.setChecked(true);
        bnGroup.setOnCheckedChangeListener((group, checkedId) -> {
            prefs.setBnVoiceEngine(checkedId == idBnGroq ? 1 : 0);
        });
        voiceEngineBox.addView(bnGroup);
        main.addView(voiceEngineBox);

        // Groq Whisper API Key
        LinearLayout groqBox = settingsBox();
        TextView groqTitle = new TextView(this);
        groqTitle.setText("⚡ Groq Whisper Settings");
        groqTitle.setTextColor(Color.parseColor("#F2F2F7"));
        groqTitle.setTextSize(13);
        groqTitle.setTypeface(null, Typeface.BOLD);
        groqTitle.setPadding(dp(8), dp(4), dp(8), dp(4));
        groqBox.addView(groqTitle);

        TextView groqApiLabel = new TextView(this);
        groqApiLabel.setText("Groq API Key:");
        groqApiLabel.setTextColor(Color.parseColor("#CCCCCC"));
        groqApiLabel.setTextSize(13);
        groqApiLabel.setPadding(dp(8), dp(4), dp(8), dp(4));
        groqBox.addView(groqApiLabel);

        EditText groqApiInput = new EditText(this);
        groqApiInput.setText(prefs.getGroqApiKey());
        groqApiInput.setTextColor(Color.WHITE);
        groqApiInput.setTextSize(13);
        groqApiInput.setHint("Paste your Groq API key here");
        groqApiInput.setHintTextColor(Color.parseColor("#666666"));
        groqApiInput.setBackgroundColor(Color.parseColor("#1C1C1E"));
        groqApiInput.setPadding(dp(12), dp(8), dp(12), dp(8));
        groqApiInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams groqLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        groqLp.setMargins(dp(8), 0, dp(8), dp(8));
        groqApiInput.setLayoutParams(groqLp);
        groqApiInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                prefs.setGroqApiKey(s.toString().trim());
            }
        });
        groqBox.addView(groqApiInput);

        TextView groqHint = new TextView(this);
        groqHint.setText("Uses whisper-large-v3 • Auto-detects language\nGet key at console.groq.com → API Keys");
        groqHint.setTextColor(Color.parseColor("#666666"));
        groqHint.setTextSize(11);
        groqHint.setPadding(dp(12), 0, dp(8), dp(8));
        groqBox.addView(groqHint);
        main.addView(groqBox);

        // Gemma Voice API Key
        LinearLayout gemmaBox = settingsBox();
        TextView gemmaTitle = new TextView(this);
        gemmaTitle.setText("🧠 Gemma Voice Settings");
        gemmaTitle.setTextColor(Color.parseColor("#F2F2F7"));
        gemmaTitle.setTextSize(13);
        gemmaTitle.setTypeface(null, Typeface.BOLD);
        gemmaTitle.setPadding(dp(8), dp(4), dp(8), dp(4));
        gemmaBox.addView(gemmaTitle);

        TextView apiLabel = new TextView(this);
        apiLabel.setText("Google AI Studio API Key:");
        apiLabel.setTextColor(Color.parseColor("#CCCCCC"));
        apiLabel.setTextSize(13);
        apiLabel.setPadding(dp(8), dp(4), dp(8), dp(4));
        gemmaBox.addView(apiLabel);

        EditText apiInput = new EditText(this);
        apiInput.setText(prefs.getGemmaApiKey());
        apiInput.setTextColor(Color.WHITE);
        apiInput.setTextSize(13);
        apiInput.setHint("Paste your API key here");
        apiInput.setHintTextColor(Color.parseColor("#666666"));
        apiInput.setBackgroundColor(Color.parseColor("#1C1C1E"));
        apiInput.setPadding(dp(12), dp(8), dp(12), dp(8));
        apiInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams apiLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        apiLp.setMargins(dp(8), 0, dp(8), dp(8));
        apiInput.setLayoutParams(apiLp);
        apiInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                prefs.setGemmaApiKey(s.toString().trim());
            }
        });
        gemmaBox.addView(apiInput);

        TextView apiHint = new TextView(this);
        apiHint.setText("Get key at aistudio.google.com → Get API key");
        apiHint.setTextColor(Color.parseColor("#666666"));
        apiHint.setTextSize(11);
        apiHint.setPadding(dp(12), 0, dp(8), dp(8));
        gemmaBox.addView(apiHint);
        main.addView(gemmaBox);

        addCheckbox(main, "📋 Clipboard Access", "Quick paste from toolbar",
            prefs.isClipboardEnabled(), prefs::setClipboardEnabled);

        addCheckbox(main, "📋 Clipboard History", "Remember copied text for quick access",
            prefs.isClipboardHistoryEnabled(), prefs::setClipboardHistoryEnabled);

        addCheckbox(main, "💡 Word Suggestions", "Show word completions while typing",
            prefs.isSuggestionsEnabled(), prefs::setSuggestionsEnabled);

        addCheckbox(main, "🇧🇩 Bangla Translation", "Show translation button on toolbar",
            prefs.isBanglaTranslationEnabled(), prefs::setBanglaTranslationEnabled);

        // Translation mode — uses RadioGroup (properly grouped)
        LinearLayout transBox = settingsBox();
        TextView transLabel = new TextView(this);
        transLabel.setText("Translation Direction:");
        transLabel.setTextColor(Color.parseColor("#CCCCCC"));
        transLabel.setTextSize(14);
        transLabel.setPadding(dp(8), dp(4), dp(8), dp(8));
        transBox.addView(transLabel);

        RadioGroup transGroup = new RadioGroup(this);
        transGroup.setOrientation(RadioGroup.VERTICAL);
        transGroup.setPadding(dp(4), 0, dp(8), dp(8));

        int idOff = View.generateViewId();
        int idBnEn = View.generateViewId();
        int idEnBn = View.generateViewId();

        RadioButton rbOff = makeRadio("Off", idOff);
        RadioButton rbBnEn = makeRadio("Bangla → English (transliterate)", idBnEn);
        RadioButton rbEnBn = makeRadio("English → Bangla (phonetic)", idEnBn);

        transGroup.addView(rbOff);
        transGroup.addView(rbBnEn);
        transGroup.addView(rbEnBn);

        int curMode = prefs.getTranslationMode();
        if (curMode == 0) rbOff.setChecked(true);
        else if (curMode == 1) rbBnEn.setChecked(true);
        else rbEnBn.setChecked(true);

        transGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == idOff) prefs.setTranslationMode(0);
            else if (checkedId == idBnEn) prefs.setTranslationMode(1);
            else if (checkedId == idEnBn) prefs.setTranslationMode(2);
        });

        transBox.addView(transGroup);
        main.addView(transBox);

        addDivider(main);

        // ============ BEHAVIOR ============
        addSectionTitle(main, "⚙️ Behavior");

        addCheckbox(main, "Auto Capitalize", "Capitalize first letter of sentences",
            prefs.isAutoCapitalize(), prefs::setAutoCapitalize);

        addCheckbox(main, "Key Vibration", "Vibrate on key press",
            prefs.isVibrateEnabled(), prefs::setVibrateEnabled);

        addCheckbox(main, "Key Popup", "Show key popup on press",
            prefs.isPopupEnabled(), prefs::setPopupEnabled);

        addDivider(main);

        // ============ APPEARANCE ============
        addSectionTitle(main, "🎨 Appearance");

        // Theme — uses RadioGroup (properly grouped)
        LinearLayout themeBox = settingsBox();
        TextView themeLabel = new TextView(this);
        themeLabel.setText("Theme:");
        themeLabel.setTextColor(Color.parseColor("#CCCCCC"));
        themeLabel.setTextSize(14);
        themeLabel.setPadding(dp(8), dp(4), dp(8), dp(8));
        themeBox.addView(themeLabel);

        RadioGroup themeGroup = new RadioGroup(this);
        themeGroup.setOrientation(RadioGroup.VERTICAL);
        themeGroup.setPadding(dp(4), 0, dp(8), dp(8));

        String[] themeNames = {"🌙 Dark (Default)", "☀️ Light", "🖤 AMOLED Black", "🔵 Blue"};
        int selectedTheme = prefs.getTheme();
        RadioButton[] themeButtons = new RadioButton[themeNames.length];

        for (int i = 0; i < themeNames.length; i++) {
            int id = View.generateViewId();
            themeButtons[i] = makeRadio(themeNames[i], id);
            themeGroup.addView(themeButtons[i]);
        }
        themeButtons[selectedTheme].setChecked(true);

        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            for (int i = 0; i < themeButtons.length; i++) {
                if (themeButtons[i].getId() == checkedId) {
                    prefs.setTheme(i);
                    break;
                }
            }
        });

        themeBox.addView(themeGroup);
        main.addView(themeBox);

        // Keyboard height
        LinearLayout heightBox = settingsBox();
        TextView heightLabel = new TextView(this);
        heightLabel.setText("Keyboard Height: " + (int)(prefs.getHeightScale() * 100) + "%");
        heightLabel.setTextColor(Color.parseColor("#CCCCCC"));
        heightLabel.setTextSize(14);
        heightLabel.setPadding(dp(8), dp(8), dp(8), dp(4));
        heightBox.addView(heightLabel);

        SeekBar heightSeekBar = new SeekBar(this);
        heightSeekBar.setMax(150);
        heightSeekBar.setProgress((int)(prefs.getHeightScale() * 100) - 50);
        heightSeekBar.setPadding(dp(8), 0, dp(8), dp(8));
        heightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float scale = (progress + 50) / 100f;
                heightLabel.setText("Keyboard Height: " + (int)(scale * 100) + "%");
                prefs.setHeightScale(scale);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        heightBox.addView(heightSeekBar);
        main.addView(heightBox);

        addDivider(main);

        // ============ ABOUT ============
        addSectionTitle(main, "ℹ️ About");
        addInfoText(main,
            "Custom Keyboard v2.0\n\n" +
            "Features:\n" +
            "• QWERTY with long-press for numbers & symbols\n" +
            "• Symbols, Numbers, Emoji keyboards\n" +
            "• Voice typing (speech-to-text)\n" +
            "• Bangla ↔ English phonetic translation\n" +
            "• Clipboard history & quick paste\n" +
            "• 4 themes: Dark, Light, AMOLED, Blue\n" +
            "• Adjustable keyboard height\n" +
            "• Haptic feedback\n" +
            "• Auto-capitalize with smart shift\n" +
            "• Cursor movement arrows\n" +
            "• Long-press delete for fast erase");

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
        tv.setTextSize(24);
        tv.setTextColor(Color.parseColor("#5B6EF5"));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(0, dp(4), 0, dp(4));
        parent.addView(tv);
    }

    private void addSubtitle(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(Color.parseColor("#8E8E93"));
        tv.setPadding(0, 0, 0, dp(8));
        parent.addView(tv);
    }

    private void addSectionTitle(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(16);
        tv.setTextColor(Color.parseColor("#F2F2F7"));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(0, dp(16), 0, dp(8));
        parent.addView(tv);
    }

    private void addDivider(LinearLayout parent) {
        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#2C2C2E"));
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

    private LinearLayout settingsBox() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(4), dp(4), dp(4), dp(4));
        box.setBackgroundColor(Color.parseColor("#2C2C2E"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(4), 0, dp(4));
        box.setLayoutParams(lp);
        return box;
    }

    private void addCheckbox(LinearLayout parent, String title, String desc, boolean checked,
                             java.util.function.Consumer<Boolean> setter) {
        LinearLayout box = settingsBox();

        CheckBox cb = new CheckBox(this);
        cb.setText(title);
        cb.setTextColor(Color.parseColor("#F2F2F7"));
        cb.setTextSize(14);
        cb.setChecked(checked);
        cb.setOnCheckedChangeListener((btn, isChecked) -> setter.accept(isChecked));
        cb.setPadding(dp(4), dp(2), dp(4), 0);
        box.addView(cb);

        TextView descTv = new TextView(this);
        descTv.setText(desc);
        descTv.setTextColor(Color.parseColor("#8E8E93"));
        descTv.setTextSize(11);
        descTv.setPadding(dp(36), 0, dp(8), dp(4));
        box.addView(descTv);

        parent.addView(box);
    }

    private RadioButton makeRadio(String text, int id) {
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
        tv.setTextColor(Color.parseColor("#8E8E93"));
        tv.setTextSize(12);
        tv.setBackgroundColor(Color.parseColor("#2C2C2E"));
        tv.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(4), 0, dp(4));
        tv.setLayoutParams(lp);
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

        // Use InputMethodManager instead of Settings.Secure.ENABLED_INPUT_METHODS
        // (blocked for targetSdk >= 34 on Android 14+)
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        boolean enabled = false;
        boolean isCurrent = false;

        if (imm != null) {
            String myId = getPackageName() + "/" + CustomKeyboardService.class.getName();

            // Check if our IME is in the enabled list
            List<InputMethodInfo> enabledImes = imm.getEnabledInputMethodList();
            for (InputMethodInfo imi : enabledImes) {
                if (imi.getId().equals(myId)) {
                    enabled = true;
                    break;
                }
            }

            // Check if we're the current default IME (wrapped in try-catch for API 34+)
            try {
                String currentId = Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
                isCurrent = currentId != null && currentId.equals(myId);
            } catch (SecurityException e) {
                // On API 34+, DEFAULT_INPUT_METHOD may also be unreadable.
                // Fall back: if enabled, assume "enabled but not selected"
                isCurrent = false;
            }
        }

        if (isCurrent) {
            statusText.setText("✅ Keyboard is active and ready!");
            statusText.setTextColor(Color.parseColor("#4CAF50"));
        } else if (enabled) {
            statusText.setText("⚠️ Enabled but not selected.\nTap 'Switch to Keyboard' to activate.");
            statusText.setTextColor(Color.parseColor("#FFC107"));
        } else {
            statusText.setText("❌ Not enabled yet.\nTap 'Enable Keyboard' to get started.");
            statusText.setTextColor(Color.parseColor("#FF5252"));
        }
    }
}
