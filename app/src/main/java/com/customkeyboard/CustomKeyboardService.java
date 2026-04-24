package com.customkeyboard;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;

    private LinearLayout keyboardContainer;
    private LinearLayout toolbarLayout;
    private LinearLayout keyboardContent;
    private TextView voiceStatusText;
    private Vibrator vibrator;

    // Colors
    private int bgColor, keyColor, keyTextColor, accentColor, keyBorderColor, toolbarBg;

    // Key dimensions
    private int keyHeight;
    private float heightScale;

    // Emoji data
    private static final String[] EMOJI_SMILEYS = {
        "😀","😃","😄","😁","😆","😅","🤣","😂","🙂","🙃",
        "😉","😊","😇","🥰","😍","🤩","😘","😗","😚","😙",
        "🥲","😋","😛","😜","🤪","😝","🤑","🤗","🤭","🫢",
        "🤫","🤔","🫡","🤐","🤨","😐","😑","😶","🫥","😏",
        "😒","🙄","😬","🤥","😌","😔","😪","🤤","😴","😷",
        "🤒","🤕","🤢","🤮","🥵","🥶","🥴","😵","🤯","🤠",
        "🥳","🥸","😎","🤓","🧐","😕","🫤","😟","🙁","☹️",
        "😮","😯","😲","😳","🥺","🥹","😦","😧","😨","😰",
        "😥","😢","😭","😱","😖","😣","😞","😓","😩","😫",
        "🥱","😤","😡","😠","🤬","😈","👿","💀","☠️","💩"
    };

    private static final String[] EMOJI_GESTURES = {
        "👋","🤚","🖐️","✋","🖖","🫱","🫲","🫳","🫴","👌",
        "🤌","🤏","✌️","🤞","🫰","🤟","🤘","🤙","👈","👉",
        "👆","🖕","👇","☝️","🫵","👍","👎","✊","👊","🤛",
        "🤜","👏","🙌","🫶","👐","🤲","🤝","🙏","✍️","💪"
    };

    private static final String[] EMOJI_HEARTS = {
        "❤️","🧡","💛","💚","💙","💜","🖤","🤍","🤎","💔",
        "❤️‍🔥","❤️‍🩹","❣️","💕","💞","💓","💗","💖","💘","💝",
        "💟","☮️","✝️","☪️","🕉️","☸️","🪯","✡️","🔯","🕎",
        "☯️","☦️","🛐","⛎","♈","♉","♊","♋","♌","♍"
    };

    private static final String[] EMOJI_OBJECTS = {
        "⚽","🏀","🏈","⚾","🥎","🎾","🏐","🏉","🥏","🎱",
        "🪀","🏓","🏸","🏒","🏑","🥍","🏏","🪃","🥅","⛳",
        "🪁","🏹","🎣","🤿","🥊","🥋","🎽","🛹","🛼","🛷",
        "⛸️","🥌","🎿","⛷️","🏂","🪂","🏋️","🤼","🤸","⛹️",
        "🎯","🎳","🎮","🕹️","🎰","🎲","🧩","🎭","🎨","🎬",
        "🎤","🎧","🎼","🎹","🥁","🪘","🎷","🎺","🪗","🎸"
    };

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = new KeyboardPrefs(this);
        clipboardHelper = new ClipboardHelper(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        loadTheme();
    }

    @Override
    public View onCreateInputView() {
        loadTheme();
        keyboardContainer = new LinearLayout(this);
        keyboardContainer.setOrientation(LinearLayout.VERTICAL);
        keyboardContainer.setBackgroundColor(bgColor);

        // Toolbar
        buildToolbar();
        keyboardContainer.addView(toolbarLayout);

        // Keyboard content
        keyboardContent = new LinearLayout(this);
        keyboardContent.setOrientation(LinearLayout.VERTICAL);
        keyboardContainer.addView(keyboardContent);

        // Load default keyboard
        showKeyboard(MODE_QWERTY);

        return keyboardContainer;
    }

    private void loadTheme() {
        heightScale = prefs.getHeightScale();
        keyHeight = (int)(52 * getResources().getDisplayMetrics().density * heightScale);

        int themeId = prefs.getTheme();
        switch (themeId) {
            case 1: // Light
                bgColor = Color.parseColor("#F5F5F5");
                keyColor = Color.parseColor("#FFFFFF");
                keyTextColor = Color.parseColor("#1a1a1a");
                accentColor = Color.parseColor("#E94560");
                keyBorderColor = Color.parseColor("#DDDDDD");
                toolbarBg = Color.parseColor("#E8E8E8");
                break;
            case 2: // AMOLED
                bgColor = Color.parseColor("#000000");
                keyColor = Color.parseColor("#1A1A1A");
                keyTextColor = Color.parseColor("#FFFFFF");
                accentColor = Color.parseColor("#E94560");
                keyBorderColor = Color.parseColor("#333333");
                toolbarBg = Color.parseColor("#0A0A0A");
                break;
            case 3: // Blue
                bgColor = Color.parseColor("#0D1B2A");
                keyColor = Color.parseColor("#1B2838");
                keyTextColor = Color.parseColor("#E0E0E0");
                accentColor = Color.parseColor("#4FC3F7");
                keyBorderColor = Color.parseColor("#2A3F55");
                toolbarBg = Color.parseColor("#0A1520");
                break;
            default: // Dark
                bgColor = Color.parseColor("#1A1A2E");
                keyColor = Color.parseColor("#16213E");
                keyTextColor = Color.parseColor("#FFFFFF");
                accentColor = Color.parseColor("#E94560");
                keyBorderColor = Color.parseColor("#0F3460");
                toolbarBg = Color.parseColor("#0F3460");
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

        // Clipboard button
        if (prefs.isClipboardEnabled()) {
            toolbarLayout.addView(createToolbarButton("📋", "Clipboard", v -> showKeyboard(MODE_CLIPBOARD)));
        }

        // Voice button
        if (prefs.isVoiceEnabled()) {
            toolbarLayout.addView(createToolbarButton("🎤", "Voice", v -> toggleVoiceInput()));
        }

        // Voice status text
        voiceStatusText = new TextView(this);
        voiceStatusText.setTextSize(11);
        voiceStatusText.setTextColor(Color.parseColor("#AAAAAA"));
        voiceStatusText.setVisibility(View.GONE);
        voiceStatusText.setPadding(dp(4), 0, dp(4), 0);
        LinearLayout.LayoutParams voiceLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        voiceStatusText.setLayoutParams(voiceLp);
        toolbarLayout.addView(voiceStatusText);

        // Translation toggle
        if (prefs.isBanglaTranslationEnabled()) {
            int mode = prefs.getTranslationMode();
            String label = mode == 1 ? "BN→EN" : mode == 2 ? "EN→BN" : "🌐";
            toolbarLayout.addView(createToolbarButton(label, "Translate", v -> cycleTranslationMode()));
        }

        // Emoji button
        toolbarLayout.addView(createToolbarButton("😊", "Emoji", v -> showKeyboard(MODE_EMOJI)));

        // Numbers button
        toolbarLayout.addView(createToolbarButton("123", "Numbers", v -> showKeyboard(MODE_NUMBERS)));

        // Symbols button
        toolbarLayout.addView(createToolbarButton("#+=", "Symbols", v -> showKeyboard(MODE_SYMBOLS)));

        // Settings button
        toolbarLayout.addView(createToolbarButton("⚙️", "Settings", v -> openSettings()));

        // Keyboard switch button
        toolbarLayout.addView(createToolbarButton("⌨️", "Switch", v -> {
            try {
                android.view.inputmethod.InputMethodManager imm = 
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showInputMethodPicker();
            } catch (Exception e) { /* ignore */ }
        }));
    }

    private TextView createToolbarButton(String text, String contentDesc, View.OnClickListener listener) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(14);
        btn.setTextColor(accentColor);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(8), dp(4), dp(8), dp(4));
        btn.setContentDescription(contentDesc);
        btn.setOnClickListener(listener);
        btn.setBackground(createRoundedBg(keyColor, dp(6)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(2), 0, dp(2), 0);
        btn.setLayoutParams(lp);
        return btn;
    }


    // ==================== KEYBOARD LAYOUTS ====================

    private void showKeyboard(int mode) {
        currentMode = mode;
        keyboardContent.removeAllViews();

        switch (mode) {
            case MODE_QWERTY:
                buildQwertyKeyboard();
                break;
            case MODE_SYMBOLS:
                buildSymbolsKeyboard();
                break;
            case MODE_NUMBERS:
                buildNumbersKeyboard();
                break;
            case MODE_EMOJI:
                buildEmojiKeyboard();
                break;
            case MODE_CLIPBOARD:
                buildClipboardKeyboard();
                break;
        }
    }

    private void buildQwertyKeyboard() {
        String[][] rows = {
            {"q","w","e","r","t","y","u","i","o","p"},
            {"a","s","d","f","g","h","j","k","l"},
            {"⇧","z","x","c","v","b","n","m","⌫"},
            {"123",",","🌐","space",".","↵"}
        };

        for (int r = 0; r < rows.length; r++) {
            LinearLayout row = createKeyRow();
            for (int i = 0; i < rows[r].length; i++) {
                String key = rows[r][i];
                switch (key) {
                    case "⇧":
                        row.addView(createSpecialKey("⇧", KeyEvent.KEYCODE_SHIFT_LEFT, getShiftBg()));
                        break;
                    case "⌫":
                        row.addView(createSpecialKey("⌫", KeyEvent.KEYCODE_DEL, getAccentBg()));
                        break;
                    case "↵":
                        row.addView(createSpecialKey("↵", KeyEvent.KEYCODE_ENTER, getAccentBg()));
                        break;
                    case "space":
                        row.addView(createSpaceKey());
                        break;
                    case "123":
                        row.addView(createSpecialKey("123", -100, getSecondaryBg(), v -> showKeyboard(MODE_NUMBERS)));
                        break;
                    case "🌐":
                        row.addView(createSpecialKey("🌐", -200, getSecondaryBg(), v -> cycleTranslationMode()));
                        break;
                    default:
                        row.addView(createLetterKey(key));
                        break;
                }
            }
            keyboardContent.addView(row);
        }
    }

    private void buildSymbolsKeyboard() {
        String[][] rows = {
            {"1","2","3","4","5","6","7","8","9","0"},
            {"@","#","$","%","&","-","_","(",")","/"},
            {"*","\"","'",";",":","!","?","~","`","⌫"},
            {"ABC",",",".","+","=","space","…","—","↵"}
        };

        for (String[] rowKeys : rows) {
            LinearLayout row = createKeyRow();
            for (String key : rowKeys) {
                switch (key) {
                    case "⌫":
                        row.addView(createSpecialKey("⌫", KeyEvent.KEYCODE_DEL, getAccentBg()));
                        break;
                    case "↵":
                        row.addView(createSpecialKey("↵", KeyEvent.KEYCODE_ENTER, getAccentBg()));
                        break;
                    case "space":
                        row.addView(createSpaceKey());
                        break;
                    case "ABC":
                        row.addView(createSpecialKey("ABC", -100, getSecondaryBg(), v -> showKeyboard(MODE_QWERTY)));
                        break;
                    default:
                        row.addView(createSymbolKey(key));
                        break;
                }
            }
            keyboardContent.addView(row);
        }
    }

    private void buildNumbersKeyboard() {
        String[][] rows = {
            {"1","2","3"},
            {"4","5","6"},
            {"7","8","9"},
            {".","0","⌫"},
            {"ABC","◂","▸","space","↵"}
        };

        for (String[] rowKeys : rows) {
            LinearLayout row = createKeyRow();
            for (String key : rowKeys) {
                switch (key) {
                    case "⌫":
                        row.addView(createSpecialKey("⌫", KeyEvent.KEYCODE_DEL, getAccentBg()));
                        break;
                    case "↵":
                        row.addView(createSpecialKey("↵", KeyEvent.KEYCODE_ENTER, getAccentBg()));
                        break;
                    case "space":
                        row.addView(createSpaceKey());
                        break;
                    case "ABC":
                        row.addView(createSpecialKey("ABC", -100, getSecondaryBg(), v -> showKeyboard(MODE_QWERTY)));
                        break;
                    case "◂":
                        row.addView(createSpecialKey("◂", KeyEvent.KEYCODE_DPAD_LEFT, getSecondaryBg()));
                        break;
                    case "▸":
                        row.addView(createSpecialKey("▸", KeyEvent.KEYCODE_DPAD_RIGHT, getSecondaryBg()));
                        break;
                    default:
                        row.addView(createSymbolKey(key));
                        break;
                }
            }
            keyboardContent.addView(row);
        }
    }

    private void buildEmojiKeyboard() {
        // Category tabs
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setBackgroundColor(toolbarBg);
        tabs.setPadding(dp(4), dp(2), dp(4), dp(2));

        String[] categories = {"😀", "👋", "❤️", "⚽"};
        String[][] emojiData = {EMOJI_SMILEYS, EMOJI_GESTURES, EMOJI_HEARTS, EMOJI_OBJECTS};

        for (int i = 0; i < categories.length; i++) {
            final int idx = i;
            TextView tab = new TextView(this);
            tab.setText(categories[i]);
            tab.setTextSize(18);
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(dp(12), dp(6), dp(12), dp(6));
            tab.setOnClickListener(v -> showEmojiCategory(emojiData[idx]));
            tabs.addView(tab);
        }

        // Back to QWERTY
        TextView backTab = new TextView(this);
        backTab.setText("ABC");
        backTab.setTextSize(14);
        backTab.setTextColor(accentColor);
        backTab.setGravity(Gravity.CENTER);
        backTab.setPadding(dp(12), dp(6), dp(12), dp(6));
        backTab.setOnClickListener(v -> showKeyboard(MODE_QWERTY));
        tabs.addView(backTab);

        keyboardContent.addView(tabs);

        // Default: smileys
        showEmojiCategory(EMOJI_SMILEYS);
    }

    private void showEmojiCategory(String[] emojis) {
        // Remove old emoji grid (keep tabs)
        while (keyboardContent.getChildCount() > 1) {
            keyboardContent.removeViewAt(1);
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(8);
        grid.setPadding(dp(4), dp(4), dp(4), dp(4));

        for (String emoji : emojis) {
            TextView emojiBtn = new TextView(this);
            emojiBtn.setText(emoji);
            emojiBtn.setTextSize(22);
            emojiBtn.setGravity(Gravity.CENTER);
            int pad = dp(6);
            emojiBtn.setPadding(pad, pad, pad, pad);
            emojiBtn.setOnClickListener(v -> {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) {
                    ic.commitText(emoji, 1);
                    performHaptic();
                }
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1);
            params.setMargins(dp(2), dp(2), dp(2), dp(2));
            emojiBtn.setLayoutParams(params);
            grid.addView(emojiBtn);
        }

        scroll.addView(grid);
        keyboardContent.addView(scroll);
    }

    private void buildClipboardKeyboard() {
        keyboardContent.removeAllViews();

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(dp(8), dp(8), dp(8), dp(4));
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
        clearBtn.setOnClickListener(v -> {
            clipboardHelper.clearHistory();
            showKeyboard(MODE_CLIPBOARD);
        });
        header.addView(clearBtn);

        keyboardContent.addView(header);

        // Clipboard items
        List<ClipData.Item> history = clipboardHelper.getHistory();

        if (history.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No clipboard history yet.\nCopied text will appear here.");
            empty.setTextColor(Color.parseColor("#888888"));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(16), dp(24), dp(16), dp(24));
            keyboardContent.addView(empty);
        } else {
            ScrollView scroll = new ScrollView(this);
            LinearLayout list = new LinearLayout(this);
            list.setOrientation(LinearLayout.VERTICAL);
            list.setPadding(dp(4), 0, dp(4), dp(4));

            for (int i = 0; i < Math.min(history.size(), 10); i++) {
                ClipData.Item item = history.get(i);
                CharSequence text = item.getText();
                if (text == null) continue;

                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(dp(8), dp(6), dp(8), dp(6));
                itemLayout.setBackground(createRoundedBg(keyColor, dp(6)));
                LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                itemLp.setMargins(0, 0, 0, dp(4));
                itemLayout.setLayoutParams(itemLp);

                TextView clipText = new TextView(this);
                String display = text.toString();
                if (display.length() > 100) display = display.substring(0, 100) + "…";
                clipText.setText(display);
                clipText.setTextColor(keyTextColor);
                clipText.setTextSize(13);
                clipText.setMaxLines(2);
                clipText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                itemLayout.addView(clipText);

                TextView pasteBtn = new TextView(this);
                pasteBtn.setText("Paste");
                pasteBtn.setTextColor(accentColor);
                pasteBtn.setTextSize(12);
                pasteBtn.setPadding(dp(8), 0, 0, 0);
                final String clipContent = text.toString();
                pasteBtn.setOnClickListener(v -> {
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null) {
                        ic.commitText(clipContent, 1);
                        performHaptic();
                    }
                });
                itemLayout.addView(pasteBtn);

                list.addView(itemLayout);
            }

            scroll.addView(list);
            keyboardContent.addView(scroll);
        }

        // Back button
        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setPadding(dp(4), dp(4), dp(4), dp(4));
        bottomRow.addView(createSpecialKey("ABC", -100, getSecondaryBg(), v -> showKeyboard(MODE_QWERTY)));
        LinearLayout.LayoutParams spacer = new LinearLayout.LayoutParams(0, 1, 1);
        bottomRow.addView(new View(this), spacer);
        keyboardContent.addView(bottomRow);
    }

    // ==================== KEY CREATION HELPERS ====================

    private LinearLayout createKeyRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        row.setLayoutParams(lp);
        return row;
    }

    private TextView createLetterKey(String letter) {
        TextView key = new TextView(this);
        String display = isCaps ? letter.toUpperCase() : letter;
        key.setText(display);
        key.setTextSize(20);
        key.setTextColor(keyTextColor);
        key.setGravity(Gravity.CENTER);
        key.setBackground(createRoundedBg(keyColor, dp(5)));
        key.setHeight(keyHeight);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(dp(2), 0, dp(2), 0);
        key.setLayoutParams(lp);

        key.setOnClickListener(v -> {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                String toCommit = isCaps ? letter.toUpperCase() : letter;

                // Apply translation if enabled
                int transMode = prefs.getTranslationMode();
                if (transMode != 0) {
                    toCommit = BanglaTranslator.translateWord(toCommit, transMode);
                }

                ic.commitText(toCommit, 1);
                performHaptic();

                // Auto-undo shift after one keypress
                if (isCaps && !isShiftLocked) {
                    isCaps = false;
                    refreshQwertyKeys();
                }
            }
        });

        key.setOnLongClickListener(v -> {
            // Long press for numbers/symbols on letter keys
            String longPress = getLongPressChar(letter);
            if (longPress != null) {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) {
                    ic.commitText(longPress, 1);
                    performHaptic();
                }
            }
            return true;
        });

        return key;
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

    private TextView createSymbolKey(String symbol) {
        TextView key = new TextView(this);
        key.setText(symbol);
        key.setTextSize(18);
        key.setTextColor(keyTextColor);
        key.setGravity(Gravity.CENTER);
        key.setBackground(createRoundedBg(keyColor, dp(5)));
        key.setHeight(keyHeight);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(dp(2), 0, dp(2), 0);
        key.setLayoutParams(lp);

        key.setOnClickListener(v -> {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.commitText(symbol, 1);
                performHaptic();
            }
        });

        return key;
    }

    private TextView createSpecialKey(String label, int code, GradientDrawable bg) {
        return createSpecialKey(label, code, bg, null);
    }

    private TextView createSpecialKey(String label, int code, GradientDrawable bg, View.OnClickListener customListener) {
        TextView key = new TextView(this);
        key.setText(label);
        key.setTextSize(16);
        key.setTextColor(code == KeyEvent.KEYCODE_SHIFT_LEFT && isCaps ? accentColor : Color.WHITE);
        key.setGravity(Gravity.CENTER);
        key.setBackground(bg);
        key.setHeight(keyHeight);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(dp(2), 0, dp(2), 0);
        key.setLayoutParams(lp);

        if (customListener != null) {
            key.setOnClickListener(customListener);
        } else {
            key.setOnClickListener(v -> handleSpecialKey(code));
        }

        return key;
    }

    private TextView createSpaceKey() {
        TextView key = new TextView(this);

        // Show translation mode indicator on space bar
        int transMode = prefs.getTranslationMode();
        if (transMode == 1) key.setText("🌐 Bangla → English");
        else if (transMode == 2) key.setText("🌐 English → Bangla");
        else key.setText("Space");

        key.setTextSize(13);
        key.setTextColor(Color.parseColor("#AAAAAA"));
        key.setGravity(Gravity.CENTER);
        key.setBackground(createRoundedBg(keyColor, dp(5)));
        key.setHeight(keyHeight);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3);
        lp.setMargins(dp(2), 0, dp(2), 0);
        key.setLayoutParams(lp);

        key.setOnClickListener(v -> {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.commitText(" ", 1);
                performHaptic();
            }
        });

        return key;
    }

    // ==================== KEY HANDLING ====================

    private void handleSpecialKey(int code) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        performHaptic();

        switch (code) {
            case KeyEvent.KEYCODE_DEL:
                ic.deleteSurroundingText(1, 0);
                break;
            case KeyEvent.KEYCODE_ENTER:
                EditorInfo ei = getCurrentInputEditorInfo();
                if (ei != null) {
                    int action = ei.imeOptions & EditorInfo.IME_MASK_ACTION;
                    if (action == EditorInfo.IME_ACTION_GO ||
                        action == EditorInfo.IME_ACTION_SEARCH ||
                        action == EditorInfo.IME_ACTION_SEND ||
                        action == EditorInfo.IME_ACTION_NEXT ||
                        action == EditorInfo.IME_ACTION_DONE) {
                        ic.performEditorAction(action);
                    } else {
                        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    }
                }
                break;
            case KeyEvent.KEYCODE_SHIFT_LEFT:
                if (isCaps && !isShiftLocked) {
                    isShiftLocked = true;
                } else if (isShiftLocked) {
                    isCaps = false;
                    isShiftLocked = false;
                } else {
                    isCaps = true;
                }
                refreshQwertyKeys();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
                break;
        }
    }

    private void refreshQwertyKeys() {
        if (currentMode == MODE_QWERTY) {
            showKeyboard(MODE_QWERTY);
        }
    }

    // ==================== VOICE INPUT ====================

    private void toggleVoiceInput() {
        if (isListening) {
            stopVoiceInput();
        } else {
            startVoiceInput();
        }
    }

    private void startVoiceInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            showToast("Voice recognition not available");
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                voiceStatusText.setText("Listening...");
                voiceStatusText.setVisibility(View.VISIBLE);
            }
            @Override public void onBeginningOfSpeech() {
                voiceStatusText.setText("🎤 Speaking...");
            }
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {
                voiceStatusText.setText("Processing...");
            }
            @Override public void onError(int error) {
                String msg;
                switch (error) {
                    case SpeechRecognizer.ERROR_NO_MATCH: msg = "No speech recognized"; break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: msg = "Speech timeout"; break;
                    case SpeechRecognizer.ERROR_AUDIO: msg = "Audio error"; break;
                    case SpeechRecognizer.ERROR_NETWORK: msg = "Network error"; break;
                    default: msg = "Error: " + error;
                }
                voiceStatusText.setText(msg);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    voiceStatusText.setVisibility(View.GONE);
                    isListening = false;
                }, 2000);
            }
            @Override public void onResults(android.os.Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0);
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null) {
                        ic.commitText(text + " ", 1);
                    }
                    voiceStatusText.setText("✓ " + text);
                }
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    voiceStatusText.setVisibility(View.GONE);
                    isListening = false;
                }, 1500);
            }
            @Override public void onPartialResults(android.os.Bundle partialResults) {
                ArrayList<String> partial = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (partial != null && !partial.isEmpty()) {
                    voiceStatusText.setText("🎤 " + partial.get(0));
                }
            }
            @Override public void onEvent(int eventType, android.os.Bundle params) {}
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        try {
            speechRecognizer.startListening(intent);
            isListening = true;
        } catch (Exception e) {
            showToast("Could not start voice input");
        }
    }

    private void stopVoiceInput() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        isListening = false;
        voiceStatusText.setVisibility(View.GONE);
    }

    // ==================== TRANSLATION ====================

    private void cycleTranslationMode() {
        int current = prefs.getTranslationMode();
        int next = (current + 1) % 3; // 0 → 1 → 2 → 0
        prefs.setTranslationMode(next);

        String[] labels = {"Translation OFF", "Bangla → English", "English → Bangla"};
        showToast(labels[next]);

        // Rebuild toolbar to update label
        rebuildToolbar();
    }

    private void rebuildToolbar() {
        if (keyboardContainer != null) {
            keyboardContainer.removeView(toolbarLayout);
            buildToolbar();
            keyboardContainer.addView(toolbarLayout, 0);
        }
    }

    // ==================== SETTINGS ====================

    private void openSettings() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // ==================== UTILITIES ====================

    private GradientDrawable createRoundedBg(int color, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(radius);
        bg.setStroke(1, keyBorderColor);
        return bg;
    }

    private GradientDrawable getAccentBg() {
        return createRoundedBg(accentColor, dp(5));
    }

    private GradientDrawable getSecondaryBg() {
        return createRoundedBg(Color.parseColor("#2A2A4A"), dp(5));
    }

    private GradientDrawable getShiftBg() {
        int color = isCaps ? accentColor : Color.parseColor("#2A2A4A");
        return createRoundedBg(color, dp(5));
    }

    private int dp(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }

    private void performHaptic() {
        if (prefs.isVibrateEnabled() && vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(20);
            }
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (clipboardHelper != null) {
            clipboardHelper.stopListening();
        }
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        if (clipboardHelper != null) {
            clipboardHelper.startListening(text -> {
                // Clipboard changed — could show notification
            });
        }
    }
}
