package com.customkeyboard;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomKeyboardService extends InputMethodService {

    private static final int MODE_QWERTY = 0;
    private static final int MODE_SYMBOLS = 1;
    private static final int MODE_NUMBERS = 2;
    private static final int MODE_EMOJI = 3;
    private static final int MODE_CLIPBOARD = 4;

    private int currentMode = MODE_QWERTY;
    private boolean isCaps = false;
    private boolean isShiftLocked = false;

    private KeyboardPrefs prefs;
    private ClipboardHelper clipboardHelper;
    private RecentEmoji recentEmoji;
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private Vibrator vibrator;
    private float density;

    // Gemma voice
    private GemmaVoiceHelper gemmaVoiceHelper;
    private static String sGemmaApiKey = "";

    public static String getGemmaApiKeyStatic() { return sGemmaApiKey; }

    // Views
    private LinearLayout keyboardContainer;
    private LinearLayout toolbarLayout;
    private LinearLayout keyboardContent;
    private SuggestionsBar suggestionsBar;
    private TextView voiceStatusText;

    // Colors
    private int bgColor, keyColor, keyTextColor, accentColor, keyBorderColor, toolbarBg, pressedColor;
    private int specialKeyColor, specialKeyTextColor;

    // Key sizing
    private int keyHeightPx;
    private int keyMinWidthPx;
    private int cornerRadiusPx;

    // Input state
    private InputConnection cachedIC;
    private EditorInfo cachedEditorInfo;
    private StringBuilder currentWord = new StringBuilder();
    private int committedLength = 0; // tracks actual chars committed to editor (differs from currentWord in EN→BN mode)

    // EN→BN translation buffer (accumulates original English chars for word-level matching)
    private StringBuilder translationBuffer = new StringBuilder();
    // FIX: Track the number of Bangla chars actually committed to the editor
    // during EN→BN mode, so backspace can delete the correct amount.
    private int bnCommittedLength = 0;

    // Flag to bypass translation (used for suggestion selection, voice input, clipboard paste)
    private boolean skipTranslation = false;

    // Key tracking for shift
    private final List<KeyView> letterKeys = new ArrayList<>();

    // Emoji data
    private static final String[] EMOJI_SMILEYS = {
        "😀","😃","😄","😁","😆","😅","🤣","😂","🙂","🙃",
        "😉","😊","😇","🥰","😍","🤩","😘","😗","😚","😙",
        "🥲","😋","😛","😜","🤪","😝","🤑","🤗","🤭","🤫",
        "🤔","🤐","🤨","😐","😑","😶","😏","😒","🙄","😬",
        "🤥","😌","😔","😪","🤤","😴","😷","🤒","🤕","🤢",
        "🤮","🥵","🥶","🥴","😵","🤯","🤠","🥳","🥸","😎",
        "🤓","🧐","😕","😟","🙁","😮","😯","😲","😳","🥺",
        "🥹","😦","😧","😨","😰","😥","😢","😭","😱","😖",
        "😣","😞","😓","😩","😫","🥱","😤","😡","😠","🤬",
        "😈","👿","💀","☠️","💩","🤡","👹","👺","👻","👽"
    };
    private static final String[] EMOJI_GESTURES = {
        "👋","🤚","🖐️","✋","🖖","👌","🤌","🤏","✌️","🤞",
        "🤟","🤘","🤙","👈","👉","👆","🖕","👇","☝️","👍",
        "👎","✊","👊","🤛","🤜","👏","🙌","👐","🤲","🤝",
        "🙏","✍️","💪","🦾","🦿","🦵","🦶","👂","🦻","👃"
    };
    private static final String[] EMOJI_HEARTS = {
        "❤️","🧡","💛","💚","💙","💜","🖤","🤍","🤎","💔",
        "❣️","💕","💞","💓","💗","💖","💘","💝","💟","♥️"
    };
    private static final String[] EMOJI_OBJECTS = {
        "⚽","🏀","🏈","⚾","🎾","🏐","🏉","🎱","🏓","🏸",
        "🏒","🏑","🥍","🏏","🥊","🥋","🎯","🎳","🎮","🕹️",
        "🎲","🧩","🎭","🎨","🎬","🎤","🎧","🎼","🎹","🥁",
        "🎷","🎺","🎸","🪗","🎻","🪘","📱","💻","⌨️","🖥️",
        "📷","📹","🎥","📞","☎️","📺","📻","🎙️","⏰","⏳"
    };

    // ==================== LIFECYCLE ====================

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = new KeyboardPrefs(this);
        clipboardHelper = new ClipboardHelper(this);
        recentEmoji = new RecentEmoji(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        density = getResources().getDisplayMetrics().density;
        sGemmaApiKey = prefs.getGemmaApiKey();
        gemmaVoiceHelper = new GemmaVoiceHelper();
        loadTheme();
    }

    @Override
    public View onCreateInputView() {
        loadTheme();
        letterKeys.clear();
        currentWord.setLength(0);
        translationBuffer.setLength(0);
        bnCommittedLength = 0; // FIX: Reset BN tracking on view recreation

        keyboardContainer = new LinearLayout(this);
        keyboardContainer.setOrientation(LinearLayout.VERTICAL);
        keyboardContainer.setBackgroundColor(bgColor);

        // Suggestions bar
        suggestionsBar = new SuggestionsBar(this, accentColor, toolbarBg, keyTextColor);
        suggestionsBar.setOnSuggestionSelectedListener(word -> {
            InputConnection ic = getIC();
            if (ic != null) {
                // BUG FIX: Use committedLength to delete the correct number of chars
                // (in EN→BN mode, committedLength differs from currentWord.length())
                int toDelete = committedLength > 0 ? committedLength : currentWord.length();
                ic.deleteSurroundingText(toDelete, 0);
                skipTranslation = true;
                ic.commitText(word + " ", 1);
                skipTranslation = false;
                currentWord.setLength(0);
                committedLength = 0;
                bnCommittedLength = 0; // FIX: Reset BN tracking
                translationBuffer.setLength(0);
                suggestionsBar.hide();
            }
        });
        keyboardContainer.addView(suggestionsBar);

        // Toolbar
        buildToolbar();
        keyboardContainer.addView(toolbarLayout);

        // Keyboard content
        keyboardContent = new LinearLayout(this);
        keyboardContent.setOrientation(LinearLayout.VERTICAL);
        keyboardContent.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        keyboardContainer.addView(keyboardContent);

        showKeyboard(MODE_QWERTY);
        return keyboardContainer;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        cachedEditorInfo = attribute;
        cachedIC = getCurrentInputConnection();
        sGemmaApiKey = prefs.getGemmaApiKey();
        if (clipboardHelper != null) clipboardHelper.startListening();
    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        cachedIC = getCurrentInputConnection();
        cachedEditorInfo = attribute;
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
        currentWord.setLength(0);
        committedLength = 0;
        bnCommittedLength = 0; // FIX: Clear BN tracking
        translationBuffer.setLength(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            try { speechRecognizer.destroy(); } catch (Exception ignored) {}
            speechRecognizer = null;
        }
        if (gemmaVoiceHelper != null) {
            gemmaVoiceHelper.release(); // FIX: Use release() for full cleanup (stops recording + cancels API calls)
        }
        if (clipboardHelper != null) clipboardHelper.stopListening();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // BUG FIX: Flush any pending translation buffer before rebuilding
        InputConnection ic = getIC();
        if (ic != null && translationBuffer.length() > 0) {
            flushEntireBuffer(ic);
        }
        // Rebuild keyboard on orientation change
        if (keyboardContainer != null) {
            onCreateInputView();
        }
    }

    // ==================== INPUT CONNECTION ====================

    private InputConnection getIC() {
        InputConnection ic = getCurrentInputConnection();
        return ic != null ? ic : cachedIC;
    }

    private void commitText(String text) {
        commitText(text, true);
    }

    private void commitText(String text, boolean haptic) {
        InputConnection ic = getIC();
        if (ic != null) {
            // Apply translation (unless bypassed)
            int transMode = skipTranslation ? 0 : prefs.getTranslationMode();
            String toCommit = text;

            if (transMode == 1) {
                // BN→EN: character-by-character transliteration (works for single chars)
                toCommit = BanglaTranslator.translateWord(text, transMode);
            } else if (transMode == 2) {
                // EN→BN: word-level buffering for multi-character phonetic matching
                if (text.length() == 1 && Character.isLetter(text.charAt(0))) {
                    translationBuffer.append(text.toLowerCase(Locale.getDefault()));
                    // Try to flush matched parts of the buffer
                    String translated = flushTranslationBuffer(ic);
                    if (haptic) performHaptic();
                    // BUG FIX: Track English input for suggestions, not Bangla translation
                    currentWord.append(text.toLowerCase(Locale.getDefault()));
                    committedLength += translated.length();
                    // FIX: Track Bangla chars committed for proper backspace in EN→BN mode
                    bnCommittedLength += translated.length();
                    if (suggestionsBar != null && prefs.isSuggestionsEnabled()) {
                        suggestionsBar.updateSuggestions(currentWord.toString());
                    }
                    return;
                } else {
                    // Non-letter (space, punctuation, etc.) — flush entire buffer first
                    flushEntireBuffer(ic);
                    translationBuffer.setLength(0);
                    bnCommittedLength = 0; // FIX: Reset BN tracking after word boundary
                    // Then commit the non-letter character as-is
                    ic.commitText(text, 1);
                    if (haptic) performHaptic();
                    if (text.equals(" ") || text.equals("\n")) {
                        currentWord.setLength(0);
                        committedLength = 0;
                        if (suggestionsBar != null) suggestionsBar.hide();
                    }
                    return;
                }
            }

            // Auto-capitalize: if at start of sentence, force uppercase on first letter
            if (toCommit.length() == 1 && Character.isLetter(toCommit.charAt(0)) && prefs.isAutoCapitalize()) {
                if (isAtSentenceStart(ic)) {
                    toCommit = toCommit.toUpperCase(Locale.getDefault());
                }
            }

            ic.commitText(toCommit, 1);
            if (haptic) performHaptic();

            // Track current word for suggestions
            if (text.length() == 1 && Character.isLetter(text.charAt(0))) {
                currentWord.append(toCommit);
                if (suggestionsBar != null && prefs.isSuggestionsEnabled()) {
                    suggestionsBar.updateSuggestions(currentWord.toString());
                }
            } else if (text.equals(" ") || text.equals("\n")) {
                currentWord.setLength(0);
                committedLength = 0;
                bnCommittedLength = 0; // FIX: Reset BN tracking on word boundary
                if (suggestionsBar != null) suggestionsBar.hide();
            }
        }
    }

    /**
     * Flush translation buffer by greedily matching the longest prefix.
     * Returns the flushed (translated) text.
     */
    private String flushTranslationBuffer(InputConnection ic) {
        StringBuilder output = new StringBuilder();
        while (translationBuffer.length() > 0) {
            // Check if entire remaining buffer matches
            String full = translationBuffer.toString();
            if (BanglaTranslator.containsKey(full)) {
                // Check if it's a prefix of a longer key — if so, keep buffering
                if (BanglaTranslator.isPrefixOfLongerKey(full)) {
                    break; // wait for more input
                }
                output.append(BanglaTranslator.getTranslation(full));
                translationBuffer.setLength(0);
                break;
            }

            // Try to find the longest matching prefix (3, 2, 1 chars)
            boolean matched = false;
            for (int len = Math.min(3, translationBuffer.length()); len >= 1; len--) {
                String sub = translationBuffer.substring(0, len);
                if (BanglaTranslator.containsKey(sub)) {
                    // Check if this is a prefix of a longer key — keep buffering if so
                    if (len < translationBuffer.length() || BanglaTranslator.isPrefixOfLongerKey(sub)) {
                        // Don't flush yet, might form a longer match with upcoming chars
                        // But only if the remaining buffer (after this sub) hasn't diverged
                        if (len == translationBuffer.length() && BanglaTranslator.isPrefixOfLongerKey(sub)) {
                            break; // wait for more input
                        }
                    }
                    output.append(BanglaTranslator.getTranslation(sub));
                    translationBuffer.delete(0, len);
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                // No match for first char, output as-is
                output.append(translationBuffer.charAt(0));
                translationBuffer.deleteCharAt(0);
            }
        }

        if (output.length() > 0) {
            ic.commitText(output.toString(), 1);
        }
        return output.toString();
    }

    /**
     * Flush entire translation buffer as a word-level translation.
     * Used when space/punctuation is pressed.
     */
    private void flushEntireBuffer(InputConnection ic) {
        if (translationBuffer.length() == 0) return;

        String word = translationBuffer.toString();
        // Try whole-word match first
        if (BanglaTranslator.containsKey(word)) {
            ic.commitText(BanglaTranslator.getTranslation(word), 1);
        } else {
            // Fall back to character-by-character
            ic.commitText(BanglaTranslator.englishToBangla(word), 1);
        }
        translationBuffer.setLength(0);
    }

    /**
     * Check if the cursor is at the start of a sentence.
     */
    private boolean isAtSentenceStart(InputConnection ic) {
        CharSequence before = ic.getTextBeforeCursor(2, 0);
        if (before == null || before.length() == 0) return true; // start of field
        if (before.length() == 1) {
            char c = before.charAt(0);
            return c == '\n'; // start of line
        }
        // Check for sentence-ending patterns: ". ", "? ", "! ", or newline
        char last = before.charAt(before.length() - 1);
        char prev = before.charAt(before.length() - 2);
        return (prev == '.' && last == ' ') ||
               (prev == '?' && last == ' ') ||
               (prev == '!' && last == ' ') ||
               last == '\n';
    }

    // ==================== THEME ====================

    private void loadTheme() {
        float scale = prefs.getHeightScale();
        keyHeightPx = (int)(48 * density * scale);
        keyMinWidthPx = (int)(28 * density);
        cornerRadiusPx = (int)(8 * density);

        int themeId = prefs.getTheme();
        switch (themeId) {
            case 1: // Light
                bgColor = Color.parseColor("#F5F5F5");
                keyColor = Color.parseColor("#FFFFFF");
                pressedColor = Color.parseColor("#E0E0E0");
                keyTextColor = Color.parseColor("#212121");
                accentColor = Color.parseColor("#5B6EF5");
                keyBorderColor = Color.parseColor("#E8E8E8");
                toolbarBg = Color.parseColor("#FAFAFA");
                specialKeyColor = Color.parseColor("#EEEEEE");
                specialKeyTextColor = Color.parseColor("#616161");
                break;
            case 2: // AMOLED
                bgColor = Color.parseColor("#000000");
                keyColor = Color.parseColor("#141414");
                pressedColor = Color.parseColor("#2A2A2A");
                keyTextColor = Color.parseColor("#F0F0F0");
                accentColor = Color.parseColor("#7C8AFF");
                keyBorderColor = Color.parseColor("#1A1A1A");
                toolbarBg = Color.parseColor("#0A0A0A");
                specialKeyColor = Color.parseColor("#1A1A1A");
                specialKeyTextColor = Color.parseColor("#909090");
                break;
            case 3: // Blue
                bgColor = Color.parseColor("#0D1B2A");
                keyColor = Color.parseColor("#1B2838");
                pressedColor = Color.parseColor("#2A3F55");
                keyTextColor = Color.parseColor("#E8EDF2");
                accentColor = Color.parseColor("#64B5F6");
                keyBorderColor = Color.parseColor("#1E3045");
                toolbarBg = Color.parseColor("#0A1520");
                specialKeyColor = Color.parseColor("#162530");
                specialKeyTextColor = Color.parseColor("#8EAFC0");
                break;
            default: // Dark (Modern)
                bgColor = Color.parseColor("#1C1C1E");
                keyColor = Color.parseColor("#2C2C2E");
                pressedColor = Color.parseColor("#3A3A3C");
                keyTextColor = Color.parseColor("#F2F2F7");
                accentColor = Color.parseColor("#5B6EF5");
                keyBorderColor = Color.parseColor("#323234");
                toolbarBg = Color.parseColor("#1C1C1E");
                specialKeyColor = Color.parseColor("#323234");
                specialKeyTextColor = Color.parseColor("#A0A0A5");
                break;
        }
    }

    // ==================== TOOLBAR ====================

    private void buildToolbar() {
        toolbarLayout = new LinearLayout(this);
        toolbarLayout.setOrientation(LinearLayout.HORIZONTAL);
        toolbarLayout.setBackgroundColor(toolbarBg);
        toolbarLayout.setGravity(Gravity.CENTER_VERTICAL);
        toolbarLayout.setPadding(dp(6), dp(4), dp(6), dp(4));

        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.HORIZONTAL);
        inner.setGravity(Gravity.CENTER_VERTICAL);

        if (prefs.isClipboardEnabled()) {
            inner.addView(toolbarBtn("📋", v -> showKeyboard(MODE_CLIPBOARD)));
        }
        if (prefs.isVoiceEnabled()) {
            inner.addView(toolbarBtn("🎤", v -> toggleVoiceInput()));
        }

        voiceStatusText = new TextView(this);
        voiceStatusText.setTextSize(10);
        voiceStatusText.setTextColor(specialKeyTextColor);
        voiceStatusText.setVisibility(View.GONE);
        voiceStatusText.setPadding(dp(4), 0, dp(4), 0);
        inner.addView(voiceStatusText);

        if (prefs.isBanglaTranslationEnabled()) {
            int mode = prefs.getTranslationMode();
            String label = mode == 1 ? "BN→EN" : mode == 2 ? "EN→BN" : "🌐";
            inner.addView(toolbarBtn(label, v -> cycleTranslationMode()));
        }

        inner.addView(toolbarBtn("😊", v -> showKeyboard(MODE_EMOJI)));
        inner.addView(toolbarBtn("123", v -> showKeyboard(MODE_NUMBERS)));
        inner.addView(toolbarBtn("#+=", v -> showKeyboard(MODE_SYMBOLS)));
        inner.addView(toolbarBtn("⚙️", v -> openSettings()));
        inner.addView(toolbarBtn("⌨️", v -> {
            try {
                android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showInputMethodPicker();
            } catch (Exception ignored) {}
        }));

        scroll.addView(inner);
        toolbarLayout.addView(scroll);
    }

    private TextView toolbarBtn(String text, View.OnClickListener listener) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(14);
        btn.setTextColor(specialKeyTextColor);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(10), dp(6), dp(10), dp(6));
        btn.setOnClickListener(listener);
        btn.setBackground(roundedBg(specialKeyColor, cornerRadiusPx));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(2), 0, dp(2), 0);
        btn.setLayoutParams(lp);
        return btn;
    }

    // ==================== KEYBOARD MODES ====================

    private void showKeyboard(int mode) {
        currentMode = mode;
        keyboardContent.removeAllViews();
        letterKeys.clear();

        switch (mode) {
            case MODE_QWERTY: buildQwerty(); break;
            case MODE_SYMBOLS: buildSymbols(); break;
            case MODE_NUMBERS: buildNumbers(); break;
            case MODE_EMOJI: buildEmoji(); break;
            case MODE_CLIPBOARD: buildClipboard(); break;
        }
    }

    // ==================== KEY CREATION ====================

    private KeyView makeKey(String label, int normalColor, int textColor, boolean isSpecial) {
        KeyView key = new KeyView(this, label, normalColor, pressedColor, textColor, keyBorderColor, cornerRadiusPx);
        key.setTextSize(isSpecial ? 14 : 18);
        key.setTypeface(isSpecial ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);
        key.setMinimumHeight(keyHeightPx);
        key.setMinimumWidth(keyMinWidthPx);
        key.setVibrateEnabled(prefs.isVibrateEnabled());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(dp(1), dp(1), dp(1), dp(1));
        key.setLayoutParams(lp);

        return key;
    }

    private KeyView makeLetterKey(String letter) {
        String display = isCaps ? letter.toUpperCase(Locale.getDefault()) : letter;
        KeyView key = makeKey(display, keyColor, keyTextColor, false);
        letterKeys.add(key);

        key.setOnKeyActionListener(new KeyView.OnKeyActionListener() {
            @Override
            public void onKeyPressed(String label) {
                String toCommit = isCaps ? letter.toUpperCase(Locale.getDefault()) : letter;
                commitText(toCommit);
                if (isCaps && !isShiftLocked) {
                    isCaps = false;
                    updateShiftState();
                }
            }
            @Override
            public void onKeyLongPressed(String label) {
                String lp = getLongPressChar(letter);
                if (lp != null) commitText(lp);
            }
        });

        return key;
    }

    private KeyView makeCharKey(String ch) {
        KeyView key = makeKey(ch, keyColor, keyTextColor, false);
        key.setTextSize(18);
        key.setOnKeyActionListener(new KeyView.OnKeyActionListener() {
            @Override public void onKeyPressed(String label) { commitText(ch); }
            @Override public void onKeyLongPressed(String label) {}
        });
        return key;
    }

    private LinearLayout makeRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        row.setPadding(dp(1), dp(1), dp(1), dp(1));
        return row;
    }

    // ==================== QWERTY ====================

    private void buildQwerty() {
        String[][] rows = {
            {"q","w","e","r","t","y","u","i","o","p"},
            {"a","s","d","f","g","h","j","k","l"},
            {"⇧","z","x","c","v","b","n","m","⌫"},
            {"123",",","🌐","space",".","↵"}
        };

        for (String[] rowKeys : rows) {
            LinearLayout row = makeRow();
            for (String k : rowKeys) {
                switch (k) {
                    case "⇧": {
                        KeyView key = makeKey("⇧", specialKeyColor, specialKeyTextColor, true);
                        key.updateColors(isCaps ? accentColor : specialKeyColor,
                                        pressedColor, isCaps ? Color.WHITE : specialKeyTextColor);
                        key.setOnKeyActionListener(new KeyView.OnKeyActionListener() {
                            @Override public void onKeyPressed(String label) {
                                if (isCaps && !isShiftLocked) isShiftLocked = true;
                                else if (isShiftLocked) { isCaps = false; isShiftLocked = false; }
                                else isCaps = true;
                                updateShiftState();
                            }
                            @Override public void onKeyLongPressed(String label) {
                                isCaps = true; isShiftLocked = true; updateShiftState();
                            }
                        });
                        row.addView(key);
                        break;
                    }
                    case "⌫": {
                        KeyView key = makeKey("⌫", specialKeyColor, accentColor, true);
                        key.setRepeatable(true);
                        key.setOnKeyActionListener(new KeyView.OnKeyActionListener() {
                            @Override public void onKeyPressed(String label) {
                                InputConnection ic = getIC();
                                if (ic == null) return;

                                // FIX: In EN→BN mode, delete the last committed Bangla char(s)
                                // which may differ from the English buffer length
                                int transMode = skipTranslation ? 0 : prefs.getTranslationMode();
                                if (transMode == 2 && translationBuffer.length() > 0) {
                                    // EN→BN mode with pending buffer
                                    if (bnCommittedLength > 0) {
                                        // Delete the last Bangla character from editor
                                        ic.deleteSurroundingText(1, 0);
                                        bnCommittedLength--;
                                    }
                                    // Also remove last English char from buffer
                                    if (translationBuffer.length() > 0) {
                                        translationBuffer.setLength(translationBuffer.length() - 1);
                                    }
                                } else {
                                    ic.deleteSurroundingText(1, 0);
                                }

                                if (currentWord.length() > 0) {
                                    currentWord.setLength(currentWord.length() - 1);
                                    if (suggestionsBar != null && prefs.isSuggestionsEnabled()) {
                                        suggestionsBar.updateSuggestions(currentWord.toString());
                                    }
                                }
                            }
                            @Override public void onKeyLongPressed(String label) {}
                        });
                        row.addView(key);
                        break;
                    }
                    case "↵": {
                        KeyView key = makeKey("↵", accentColor, Color.WHITE, true);
                        key.setOnKeyActionListener(new KeyView.OnKeyActionListener() {
                            @Override public void onKeyPressed(String label) {
                                InputConnection ic = getIC();
                                if (ic == null) return;
                                if (translationBuffer.length() > 0) {
                                    flushEntireBuffer(ic);
                                }
                                EditorInfo ei = cachedEditorInfo != null ? cachedEditorInfo : getCurrentInputEditorInfo();
                                if (ei != null) {
                                    int action = ei.imeOptions & EditorInfo.IME_MASK_ACTION;
                                    if (action == EditorInfo.IME_ACTION_GO || action == EditorInfo.IME_ACTION_SEARCH ||
                                        action == EditorInfo.IME_ACTION_SEND || action == EditorInfo.IME_ACTION_NEXT ||
                                        action == EditorInfo.IME_ACTION_DONE) {
                                        ic.performEditorAction(action);
                                    } else {
                                        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                                        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                                    }
                                } else {
                                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                                }
                                currentWord.setLength(0);
                                bnCommittedLength = 0; // FIX: Reset BN tracking
                                if (suggestionsBar != null) suggestionsBar.hide();
                            }
                            @Override public void onKeyLongPressed(String label) {}
                        });
                        row.addView(key);
                        break;
                    }
                    case "space": {
                        KeyView key = makeKey("Space", keyColor, specialKeyTextColor, false);
                        key.setTextSize(11);
                        key.setTypeface(Typeface.DEFAULT);
                        key.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3));
                        key.setOnKeyActionListener(new KeyView.OnKeyActionListener() {
                            @Override public void onKeyPressed(String label) {
                                commitText(" ");
                            }
                            @Override public void onKeyLongPressed(String label) {}
                        });
                        row.addView(key);
                        break;
                    }
                    case "123": {
                        KeyView key = makeKey("123", specialKeyColor, specialKeyTextColor, true);
                        key.setTextSize(12);
                        key.setOnKeyActionListener(new KeyView.OnKeyActionListener() {
                            @Override public void onKeyPressed(String label) { showKeyboard(MODE_NUMBERS); }
                            @Override public void onKeyLongPressed(String label) {}
                        });
                        row.addView(key);
                        break;
                    }
                    case "🌐": {
                        KeyView key = makeKey("🌐", specialKeyColor, specialKeyTextColor, true);
                        key.setOnKeyActionListener(new KeyView.OnKeyActionListener() {
                            @Override public void onKeyPressed(String label) { cycleTranslationMode(); }
                            @Override public void onKeyLongPressed(String label) {}
                        });
                        row.addView(key);
                        break;
                    }
                    default:
                        row.addView(makeLetterKey(k));
                        break;
                }
            }
            keyboardContent.addView(row);
        }
    }

    // ==================== SYMBOLS / NUMBERS ====================

    private void buildSymbols() {
        String[][] rows = {
            {"1","2","3","4","5","6","7","8","9","0"},
            {"@","#","$","%","&","-","_","(",")","/"},
            {"*","\"","'",";",":","!","?","~","`","⌫"},
            {"ABC",",",".","+","=","space","…","—","↵"}
        };
        buildGenericKeyboard(rows);
    }

    private void buildNumbers() {
        String[][] rows = {
            {"1","2","3"},{"4","5","6"},{"7","8","9"},
            {".","0","⌫"},{"ABC","◂","▸","space","↵"}
        };
        buildGenericKeyboard(rows);
    }

    private void buildGenericKeyboard(String[][] rows) {
        for (String[] rowKeys : rows) {
            LinearLayout row = makeRow();
            for (String k : rowKeys) {
                switch (k) {
                    case "⌫": {
                        KeyView key = makeKey("⌫", specialKeyColor, accentColor, true);
                        key.setRepeatable(true);
                        key.setOnKeyActionListener(new KeyView.OnKeyActionListener() {
                            @Override public void onKeyPressed(String label) {
                                InputConnection ic = getIC();
                                if (ic == null) return;

                                // FIX: Same EN→BN backspace fix as QWERTY mode
                                int transMode = skipTranslation ? 0 : prefs.getTranslationMode();
                                if (transMode == 2 && translationBuffer.length() > 0) {
                                    if (bnCommittedLength > 0) {
                                        ic.deleteSurroundingText(1, 0);
                                        bnCommittedLength--;
                                    }
                                    if (translationBuffer.length() > 0) {
                                        translationBuffer.setLength(translationBuffer.length() - 1);
                                    }
                                } else {
                                    ic.deleteSurroundingText(1, 0);
                                }

                                if (currentWord.length() > 0) {
                                    currentWord.setLength(currentWord.length() - 1);
                                }
                            }
                            @Override public void onKeyLongPressed(String label) {}
                        });
                        row.addView(key);
                        break;
                    }
                    case "↵": {
                        KeyView key = makeKey("↵", accentColor, Color.WHITE, true);
                        key.setOnKeyActionListener(new KeyView.OnKeyActionListener() {
                            @Override public void onKeyPressed(String label) {
                                InputConnection ic = getIC();
                                if (ic == null) return;
                                // FIX: Flush translation buffer before enter (same as QWERTY)
                                if (translationBuffer.length() > 0) {
                                    flushEntireBuffer(ic);
                                }
                                // FIX: Handle IME actions (GO, SEARCH, SEND, etc.)
                                // instead of always sending raw ENTER key event
                                EditorInfo ei = cachedEditorInfo != null ? cachedEditorInfo : getCurrentInputEditorInfo();
                                if (ei != null) {
                                    int action = ei.imeOptions & EditorInfo.IME_MASK_ACTION;
                                    if (action == EditorInfo.IME_ACTION_GO || action == EditorInfo.IME_ACTION_SEARCH ||
                                        action == EditorInfo.IME_ACTION_SEND || action == EditorInfo.IME_ACTION_NEXT ||
                                        action == EditorInfo.IME_ACTION_DONE) {
                                        ic.performEditorAction(action);
                                        currentWord.setLength(0);
                                        bnCommittedLength = 0;
                                        if (suggestionsBar != null) suggestionsBar.hide();
                                        return;
                                    }
                                }
                                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                                currentWord.setLength(0);
                                bnCommittedLength = 0;
                                if (suggestionsBar != null) suggestionsBar.hide();
                            }
                            @Override public void onKeyLongPressed(String label) {}
                        });
                        row.addView(key);
                        break;
                    }
                    case "space": {
                        KeyView key = makeKey("Space", keyColor, specialKeyTextColor, false);
                        key.setTextSize(11);
                        key.setTypeface(Typeface.DEFAULT);
                        key.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3));
                        key.setOnKeyActionListener(new KeyView.OnKeyActionListener() {
                            @Override public void onKeyPressed(String label) { commitText(" "); }
                            @Override public void onKeyLongPressed(String label) {}
                        });
                        row.addView(key);
                        break;
                    }
                    case "ABC": {
                        KeyView key = makeKey("ABC", specialKeyColor, specialKeyTextColor, true);
                        key.setTextSize(12);
                        key.setOnKeyActionListener(new KeyView.OnKeyActionListener() {
                            @Override public void onKeyPressed(String label) { showKeyboard(MODE_QWERTY); }
                            @Override public void onKeyLongPressed(String label) {}
                        });
                        row.addView(key);
                        break;
                    }
                    case "◂": {
                        KeyView key = makeKey("◂", specialKeyColor, specialKeyTextColor, true);
                        key.setRepeatable(true);
                        key.setOnKeyActionListener(new KeyView.OnKeyActionListener() {
                            @Override public void onKeyPressed(String label) {
                                InputConnection ic = getIC();
                                if (ic != null) {
                                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));
                                }
                            }
                            @Override public void onKeyLongPressed(String label) {}
                        });
                        row.addView(key);
                        break;
                    }
                    case "▸": {
                        KeyView key = makeKey("▸", specialKeyColor, specialKeyTextColor, true);
                        key.setRepeatable(true);
                        key.setOnKeyActionListener(new KeyView.OnKeyActionListener() {
                            @Override public void onKeyPressed(String label) {
                                InputConnection ic = getIC();
                                if (ic != null) {
                                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
                                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
                                }
                            }
                            @Override public void onKeyLongPressed(String label) {}
                        });
                        row.addView(key);
                        break;
                    }
                    default:
                        row.addView(makeCharKey(k));
                        break;
                }
            }
            keyboardContent.addView(row);
        }
    }

    // ==================== EMOJI ====================

    private void buildEmoji() {
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setBackgroundColor(toolbarBg);
        tabs.setPadding(dp(4), dp(4), dp(4), dp(4));

        String[][] emojiSets = {EMOJI_SMILEYS, EMOJI_GESTURES, EMOJI_HEARTS, EMOJI_OBJECTS};
        String[] labels = {"😀", "👋", "❤️", "⚽", "🕐", "ABC"};

        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            TextView tab = new TextView(this);
            tab.setText(labels[i]);
            tab.setTextSize(i == 5 ? 13 : 16);
            if (i == 5) tab.setTextColor(accentColor);
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(dp(14), dp(8), dp(14), dp(8));
            tab.setOnClickListener(v -> {
                if (idx == 5) showKeyboard(MODE_QWERTY);
                else if (idx == 4) showEmojiGrid(recentEmoji.getRecentArray());
                else showEmojiGrid(emojiSets[idx]);
            });
            tabs.addView(tab);
        }
        keyboardContent.addView(tabs);
        showEmojiGrid(EMOJI_SMILEYS);
    }

    private void showEmojiGrid(String[] emojis) {
        while (keyboardContent.getChildCount() > 1) {
            keyboardContent.removeViewAt(1);
        }

        if (emojis.length == 0) {
            TextView empty = new TextView(this);
            empty.setText("No recent emojis yet.\nEmojis you use will appear here.");
            empty.setTextColor(Color.parseColor("#8E8E93"));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(16), dp(32), dp(16), dp(32));
            keyboardContent.addView(empty);
            return;
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(8);
        grid.setPadding(dp(4), dp(4), dp(4), dp(4));

        for (String emoji : emojis) {
            TextView btn = new TextView(this);
            btn.setText(emoji);
            btn.setTextSize(24);
            btn.setGravity(Gravity.CENTER);
            int p = dp(6);
            btn.setPadding(p, p, p, p);
            btn.setOnClickListener(v -> {
                commitText(emoji);
                recentEmoji.add(emoji);
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1);
            params.setMargins(dp(2), dp(2), dp(2), dp(2));
            btn.setLayoutParams(params);
            grid.addView(btn);
        }

        scroll.addView(grid);
        keyboardContent.addView(scroll);
    }

    // ==================== CLIPBOARD ====================

    private void buildClipboard() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(dp(12), dp(8), dp(12), dp(8));
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("📋 Clipboard History");
        title.setTextColor(accentColor);
        title.setTextSize(14);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        header.addView(title);

        TextView clearBtn = new TextView(this);
        clearBtn.setText("Clear");
        clearBtn.setTextColor(Color.parseColor("#FF6B6B"));
        clearBtn.setTextSize(12);
        clearBtn.setPadding(dp(8), 0, 0, 0);
        clearBtn.setOnClickListener(v -> {
            clipboardHelper.clearHistory();
            showKeyboard(MODE_CLIPBOARD);
        });
        header.addView(clearBtn);
        keyboardContent.addView(header);

        List<String> history = clipboardHelper.getHistory();

        if (history.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No clipboard history yet.\nCopied text will appear here.");
            empty.setTextColor(Color.parseColor("#8E8E93"));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(16), dp(32), dp(16), dp(32));
            keyboardContent.addView(empty);
        } else {
            ScrollView scroll = new ScrollView(this);
            scroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
            LinearLayout list = new LinearLayout(this);
            list.setOrientation(LinearLayout.VERTICAL);
            list.setPadding(dp(8), 0, dp(8), dp(4));

            for (int i = 0; i < Math.min(history.size(), 15); i++) {
                String text = history.get(i);
                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(dp(10), dp(8), dp(10), dp(8));
                itemLayout.setBackground(roundedBg(keyColor, dp(8)));
                LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                itemLp.setMargins(0, 0, 0, dp(6));
                itemLayout.setLayoutParams(itemLp);

                TextView clipText = new TextView(this);
                String display = text.length() > 120 ? text.substring(0, 120) + "…" : text;
                clipText.setText(display);
                clipText.setTextColor(keyTextColor);
                clipText.setTextSize(13);
                clipText.setMaxLines(3);
                clipText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                itemLayout.addView(clipText);

                TextView pasteBtn = new TextView(this);
                pasteBtn.setText(" Paste ");
                pasteBtn.setTextColor(accentColor);
                pasteBtn.setTextSize(12);
                pasteBtn.setTypeface(null, Typeface.BOLD);
                pasteBtn.setPadding(dp(8), 0, 0, 0);
                final String clipContent = text;
                pasteBtn.setOnClickListener(v -> {
                    skipTranslation = true;
                    commitText(clipContent);
                    skipTranslation = false;
                });
                itemLayout.addView(pasteBtn);

                list.addView(itemLayout);
            }
            scroll.addView(list);
            keyboardContent.addView(scroll);
        }

        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setPadding(dp(8), dp(4), dp(8), dp(4));
        KeyView backKey = makeKey("ABC", specialKeyColor, specialKeyTextColor, true);
        backKey.setTextSize(13);
        backKey.setOnKeyActionListener(new KeyView.OnKeyActionListener() {
            @Override public void onKeyPressed(String label) { showKeyboard(MODE_QWERTY); }
            @Override public void onKeyLongPressed(String label) {}
        });
        bottomRow.addView(backKey);
        keyboardContent.addView(bottomRow);
    }

    // ==================== SHIFT STATE ====================

    private void updateShiftState() {
        for (KeyView key : letterKeys) {
            String text = key.getText().toString();
            if (text.length() == 1 && Character.isLetter(text.charAt(0))) {
                key.updateLabel(isCaps ? text.toUpperCase(Locale.getDefault()) : text.toLowerCase(Locale.getDefault()));
            }
        }
        updateShiftKeyVisual();
    }

    private void updateShiftKeyVisual() {
        if (keyboardContent.getChildCount() >= 3) {
            View thirdRow = keyboardContent.getChildAt(2);
            if (thirdRow instanceof LinearLayout) {
                View firstKey = ((LinearLayout) thirdRow).getChildAt(0);
                if (firstKey instanceof KeyView) {
                    KeyView shiftKey = (KeyView) firstKey;
                    if (shiftKey.getText().toString().equals("⇧")) {
                        shiftKey.updateColors(
                            isCaps ? accentColor : specialKeyColor,
                            pressedColor,
                            isCaps ? Color.WHITE : specialKeyTextColor
                        );
                    }
                }
            }
        }
    }

    // ==================== VOICE INPUT ====================

    private void toggleVoiceInput() {
        if (isListening) stopVoiceInput();
        else startVoiceInput();
    }

    private void startVoiceInput() {
        // FIX: Set isListening synchronously BEFORE any async operations
        // to prevent double-starts from rapid tapping
        isListening = true;

        // Use Gemma API if enabled and key is set
        if (prefs.isGemmaVoiceEnabled() && gemmaVoiceHelper != null) {
            String apiKey = prefs.getGemmaApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                isListening = false; // FIX: Reset on early exit
                showToast("Set API key in keyboard settings first");
                return;
            }
            sGemmaApiKey = apiKey;

            gemmaVoiceHelper.setCallback(new GemmaVoiceHelper.VoiceCallback() {
                @Override
                public void onTranscription(String text) {
                    skipTranslation = true;
                    commitText(text + " ", false);
                    skipTranslation = false;
                    if (voiceStatusText != null) {
                        voiceStatusText.setText("✓ " + text);
                    }
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (voiceStatusText != null) voiceStatusText.setVisibility(View.GONE);
                        isListening = false;
                    }, 1500);
                }

                @Override
                public void onError(String message) {
                    if (voiceStatusText != null) voiceStatusText.setText(message);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (voiceStatusText != null) voiceStatusText.setVisibility(View.GONE);
                        isListening = false;
                    }, 2500);
                }

                @Override
                public void onRecordingStateChanged(boolean recording) {
                    if (recording) {
                        if (voiceStatusText != null) {
                            voiceStatusText.setText("🎤 Listening (Gemma)...");
                            voiceStatusText.setVisibility(View.VISIBLE);
                        }
                        // FIX: isListening is already set synchronously in startVoiceInput()
                    }
                }
            });

            gemmaVoiceHelper.startRecording();
            return;
        }

        // Fallback: Android SpeechRecognizer
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            isListening = false; // FIX: Reset on early exit
            showToast("Voice recognition not available");
            return;
        }

        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            }
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle params) {
                    if (voiceStatusText != null) {
                        voiceStatusText.setText("Listening...");
                        voiceStatusText.setVisibility(View.VISIBLE);
                    }
                }
                @Override public void onBeginningOfSpeech() {
                    if (voiceStatusText != null) voiceStatusText.setText("🎤 Speaking...");
                }
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() {
                    if (voiceStatusText != null) voiceStatusText.setText("Processing...");
                }
                @Override public void onError(int error) {
                    String msg;
                    switch (error) {
                        case SpeechRecognizer.ERROR_NO_MATCH: msg = "No speech recognized"; break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: msg = "Timeout"; break;
                        case SpeechRecognizer.ERROR_AUDIO: msg = "Audio error"; break;
                        case SpeechRecognizer.ERROR_NETWORK: msg = "Network error"; break;
                        default: msg = "Error " + error;
                    }
                    if (voiceStatusText != null) voiceStatusText.setText(msg);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (voiceStatusText != null) voiceStatusText.setVisibility(View.GONE);
                        isListening = false;
                    }, 2000);
                }
                @Override public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String text = matches.get(0);
                        skipTranslation = true;
                        commitText(text + " ", false);
                        skipTranslation = false;
                        if (voiceStatusText != null) voiceStatusText.setText("✓ " + text);
                    }
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (voiceStatusText != null) voiceStatusText.setVisibility(View.GONE);
                        isListening = false;
                    }, 1500);
                }
                @Override public void onPartialResults(Bundle partialResults) {
                    ArrayList<String> partial = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (partial != null && !partial.isEmpty() && voiceStatusText != null) {
                        voiceStatusText.setText("🎤 " + partial.get(0));
                    }
                }
                @Override public void onEvent(int eventType, Bundle params) {}
            });

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            speechRecognizer.startListening(intent);
            // FIX: isListening already set synchronously at top of method
        } catch (Exception e) {
            isListening = false; // FIX: Reset on error
            showToast("Could not start voice input");
        }
    }

    private void stopVoiceInput() {
        // Stop Gemma recording if active
        if (gemmaVoiceHelper != null && gemmaVoiceHelper.isRecording()) {
            gemmaVoiceHelper.stopRecording();
            if (voiceStatusText != null) voiceStatusText.setText("Processing...");
            // isListening will be cleared by the callback
            return;
        }
        // Stop Android SpeechRecognizer
        if (speechRecognizer != null) {
            try { speechRecognizer.stopListening(); } catch (Exception ignored) {}
        }
        isListening = false;
        if (voiceStatusText != null) voiceStatusText.setVisibility(View.GONE);
    }

    // ==================== TRANSLATION ====================

    private void cycleTranslationMode() {
        int current = prefs.getTranslationMode();
        int next = (current + 1) % 3;
        prefs.setTranslationMode(next);
        translationBuffer.setLength(0); // Clear buffer on mode change
        String[] labels = {"Translation OFF", "Bangla → English", "English → Bangla"};
        showToast(labels[next]);
        rebuildToolbar();
    }

    private void rebuildToolbar() {
        if (keyboardContainer != null) {
            keyboardContainer.removeView(toolbarLayout);
            buildToolbar();
            // Insert toolbar after suggestions bar (index 1)
            keyboardContainer.addView(toolbarLayout, 1);
        }
    }

    // ==================== SETTINGS ====================

    private void openSettings() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // ==================== UTILITIES ====================

    private GradientDrawable roundedBg(int color, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(radius);
        bg.setStroke(1, keyBorderColor);
        return bg;
    }

    private int dp(int dp) {
        return (int)(dp * density);
    }

    private void performHaptic() {
        if (prefs.isVibrateEnabled() && vibrator != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(12);
                }
            } catch (Exception ignored) {}
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private String getLongPressChar(String letter) {
        switch (letter) {
            case "q": return "1"; case "w": return "2"; case "e": return "3";
            case "r": return "4"; case "t": return "5"; case "y": return "6";
            case "u": return "7"; case "i": return "8"; case "o": return "9";
            case "p": return "0";
            case "a": return "@"; case "s": return "#"; case "d": return "$";
            case "f": return "%"; case "g": return "&"; case "h": return "-";
            case "j": return "+"; case "k": return "("; case "l": return ")";
            case "z": return "*"; case "x": return "\""; case "c": return "'";
            case "v": return ":"; case "b": return "!"; case "n": return "?";
            case "m": return "/";
            default: return null;
        }
    }
}
