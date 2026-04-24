package com.customkeyboard;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
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
import android.view.MotionEvent;
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
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;

    private LinearLayout keyboardContainer;
    private LinearLayout toolbarLayout;
    private LinearLayout keyboardContent;
    private TextView voiceStatusText;
    private Vibrator vibrator;
    private float density;

    // Colors
    private int bgColor, keyColor, keyTextColor, accentColor, keyBorderColor, toolbarBg;

    // Key sizing
    private int keyHeightPx;
    private int keyMinWidthPx;

    // Stored reference for reliability
    private InputConnection cachedIC;
    private EditorInfo cachedEditorInfo;

    // Key view tracking for shift refresh
    private final List<TextView> letterKeys = new ArrayList<>();

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

    // ==================== LIFECYCLE ====================

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = new KeyboardPrefs(this);
        clipboardHelper = new ClipboardHelper(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        density = getResources().getDisplayMetrics().density;
        loadTheme();
    }

    @Override
    public View onCreateInputView() {
        loadTheme();
        letterKeys.clear();

        keyboardContainer = new LinearLayout(this);
        keyboardContainer.setOrientation(LinearLayout.VERTICAL);
        keyboardContainer.setBackgroundColor(bgColor);
        keyboardContainer.setFocusable(true);
        keyboardContainer.setFocusableInTouchMode(true);
        keyboardContainer.setClickable(true);

        // Toolbar
        buildToolbar();
        keyboardContainer.addView(toolbarLayout);

        // Keyboard content area
        keyboardContent = new LinearLayout(this);
        keyboardContent.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams kcLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
        keyboardContent.setLayoutParams(kcLp);
        keyboardContainer.addView(keyboardContent);

        showKeyboard(MODE_QWERTY);
        return keyboardContainer;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        cachedEditorInfo = attribute;
        cachedIC = getCurrentInputConnection();

        if (clipboardHelper != null) {
            clipboardHelper.startListening(null);
        }
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
        cachedIC = null;
        cachedEditorInfo = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        if (clipboardHelper != null) {
            clipboardHelper.stopListening();
        }
    }

    // ==================== INPUT CONNECTION ====================

    private InputConnection getIC() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) ic = cachedIC;
        return ic;
    }

    private void commitText(String text) {
        commitText(text, true);
    }

    private void commitText(String text, boolean haptic) {
        InputConnection ic = getIC();
        if (ic != null) {
            ic.commitText(text, 1);
            if (haptic) performHaptic();
        }
    }

    // ==================== THEME ====================

    private void loadTheme() {
        float scale = prefs.getHeightScale();
        keyHeightPx = (int)(54 * density * scale);
        keyMinWidthPx = (int)(32 * density);

        int themeId = prefs.getTheme();
        switch (themeId) {
            case 1: // Light
                bgColor = Color.parseColor("#F0F0F0");
                keyColor = Color.parseColor("#FFFFFF");
                keyTextColor = Color.parseColor("#1A1A1A");
                accentColor = Color.parseColor("#E94560");
                keyBorderColor = Color.parseColor("#CCCCCC");
                toolbarBg = Color.parseColor("#E0E0E0");
                break;
            case 2: // AMOLED
                bgColor = Color.parseColor("#000000");
                keyColor = Color.parseColor("#111111");
                keyTextColor = Color.parseColor("#EEEEEE");
                accentColor = Color.parseColor("#E94560");
                keyBorderColor = Color.parseColor("#222222");
                toolbarBg = Color.parseColor("#050505");
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
        toolbarLayout.setPadding(dp(4), dp(4), dp(4), dp(4));

        // Wrap in HorizontalScrollView for small screens
        HorizontalScrollView toolbarScroll = new HorizontalScrollView(this);
        toolbarScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout toolbarInner = new LinearLayout(this);
        toolbarInner.setOrientation(LinearLayout.HORIZONTAL);
        toolbarInner.setGravity(Gravity.CENTER_VERTICAL);

        if (prefs.isClipboardEnabled()) {
            toolbarInner.addView(tb("📋", v -> showKeyboard(MODE_CLIPBOARD)));
        }
        if (prefs.isVoiceEnabled()) {
            toolbarInner.addView(tb("🎤", v -> toggleVoiceInput()));
        }

        // Voice status
        voiceStatusText = new TextView(this);
        voiceStatusText.setTextSize(10);
        voiceStatusText.setTextColor(Color.parseColor("#AAAAAA"));
        voiceStatusText.setVisibility(View.GONE);
        voiceStatusText.setPadding(dp(4), 0, dp(4), 0);
        toolbarInner.addView(voiceStatusText);

        if (prefs.isBanglaTranslationEnabled()) {
            int mode = prefs.getTranslationMode();
            String label = mode == 1 ? "BN→EN" : mode == 2 ? "EN→BN" : "🌐";
            toolbarInner.addView(tb(label, v -> cycleTranslationMode()));
        }

        toolbarInner.addView(tb("😊", v -> showKeyboard(MODE_EMOJI)));
        toolbarInner.addView(tb("123", v -> showKeyboard(MODE_NUMBERS)));
        toolbarInner.addView(tb("#+=", v -> showKeyboard(MODE_SYMBOLS)));
        toolbarInner.addView(tb("⚙️", v -> openSettings()));
        toolbarInner.addView(tb("⌨️", v -> {
            try {
                android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showInputMethodPicker();
            } catch (Exception ignored) {}
        }));

        toolbarScroll.addView(toolbarInner);
        toolbarLayout.addView(toolbarScroll);
    }

    private TextView tb(String text, View.OnClickListener listener) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(13);
        btn.setTextColor(accentColor);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(10), dp(6), dp(10), dp(6));
        btn.setOnClickListener(listener);
        btn.setBackground(roundedBg(keyColor, dp(6)));
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
            case MODE_QWERTY:   buildQwerty(); break;
            case MODE_SYMBOLS:  buildSymbols(); break;
            case MODE_NUMBERS:  buildNumbers(); break;
            case MODE_EMOJI:    buildEmoji(); break;
            case MODE_CLIPBOARD: buildClipboard(); break;
        }
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
                    case "⇧":  row.addView(makeKey("⇧", Type.SHIFT, null)); break;
                    case "⌫":  row.addView(makeKey("⌫", Type.DELETE, null)); break;
                    case "↵":  row.addView(makeKey("↵", Type.ENTER, null)); break;
                    case "space": row.addView(makeSpaceKey()); break;
                    case "123": row.addView(makeKey("123", Type.MODE_NUM, null)); break;
                    case "🌐": row.addView(makeKey("🌐", Type.TRANSLATE, null)); break;
                    default:   row.addView(makeLetterKey(k)); break;
                }
            }
            keyboardContent.addView(row);
        }
    }

    // ==================== SYMBOLS ====================

    private void buildSymbols() {
        String[][] rows = {
            {"1","2","3","4","5","6","7","8","9","0"},
            {"@","#","$","%","&","-","_","(",")","/"},
            {"*","\"","'",";",":","!","?","~","`","⌫"},
            {"ABC",",",".","+","=","space","…","—","↵"}
        };
        buildGenericKeyboard(rows);
    }

    // ==================== NUMBERS ====================

    private void buildNumbers() {
        String[][] rows = {
            {"1","2","3"},
            {"4","5","6"},
            {"7","8","9"},
            {".","0","⌫"},
            {"ABC","◂","▸","space","↵"}
        };
        buildGenericKeyboard(rows);
    }

    private void buildGenericKeyboard(String[][] rows) {
        for (String[] rowKeys : rows) {
            LinearLayout row = makeRow();
            for (String k : rowKeys) {
                switch (k) {
                    case "⌫":  row.addView(makeKey("⌫", Type.DELETE, null)); break;
                    case "↵":  row.addView(makeKey("↵", Type.ENTER, null)); break;
                    case "space": row.addView(makeSpaceKey()); break;
                    case "ABC": row.addView(makeKey("ABC", Type.MODE_QWERTY, null)); break;
                    case "◂":  row.addView(makeKey("◂", Type.DPAD_LEFT, null)); break;
                    case "▸":  row.addView(makeKey("▸", Type.DPAD_RIGHT, null)); break;
                    default:   row.addView(makeCharKey(k)); break;
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
        String[] labels = {"😀", "👋", "❤️", "⚽", "ABC"};

        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            TextView tab = new TextView(this);
            tab.setText(labels[i]);
            tab.setTextSize(16);
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(dp(14), dp(8), dp(14), dp(8));
            if (i == 4) {
                tab.setTextColor(accentColor);
                tab.setTextSize(13);
            }
            tab.setOnClickListener(v -> {
                if (idx == 4) showKeyboard(MODE_QWERTY);
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
            btn.setOnClickListener(v -> commitText(emoji));

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

        List<ClipData.Item> history = clipboardHelper.getHistory();

        if (history.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No clipboard history yet.\nCopied text will appear here.");
            empty.setTextColor(Color.parseColor("#888888"));
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
                ClipData.Item item = history.get(i);
                CharSequence text = item.getText();
                if (text == null) continue;

                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(dp(10), dp(8), dp(10), dp(8));
                itemLayout.setBackground(roundedBg(keyColor, dp(8)));
                LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                itemLp.setMargins(0, 0, 0, dp(6));
                itemLayout.setLayoutParams(itemLp);

                TextView clipText = new TextView(this);
                String display = text.toString();
                if (display.length() > 120) display = display.substring(0, 120) + "…";
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
                final String clipContent = text.toString();
                pasteBtn.setOnClickListener(v -> commitText(clipContent));
                itemLayout.addView(pasteBtn);

                list.addView(itemLayout);
            }
            scroll.addView(list);
            keyboardContent.addView(scroll);
        }

        // Back button row
        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setPadding(dp(8), dp(4), dp(8), dp(4));
        bottomRow.addView(makeKey("ABC", Type.MODE_QWERTY, null));
        keyboardContent.addView(bottomRow);
    }

    // ==================== KEY CREATION ====================

    private enum Type {
        LETTER, CHAR, SHIFT, DELETE, ENTER, SPACE,
        MODE_QWERTY, MODE_NUM, DPAD_LEFT, DPAD_RIGHT, TRANSLATE
    }

    private LinearLayout makeRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
        lp.setMargins(dp(2), dp(1), dp(2), dp(1));
        row.setLayoutParams(lp);
        return row;
    }

    private TextView makeLetterKey(String letter) {
        TextView key = new TextView(this);
        key.setText(isCaps ? letter.toUpperCase(Locale.getDefault()) : letter);
        key.setTextSize(20);
        key.setTextColor(keyTextColor);
        key.setGravity(Gravity.CENTER);
        key.setBackground(roundedBg(keyColor, dp(6)));
        key.setMinimumWidth(keyMinWidthPx);
        key.setMinimumHeight(keyHeightPx);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(dp(2), 0, dp(2), 0);
        key.setLayoutParams(lp);

        letterKeys.add(key);

        key.setOnClickListener(v -> {
            String toCommit = isCaps ? letter.toUpperCase(Locale.getDefault()) : letter;
            int transMode = prefs.getTranslationMode();
            if (transMode != 0) {
                toCommit = BanglaTranslator.translateWord(toCommit, transMode);
            }
            commitText(toCommit);

            // Auto-undo shift after one keypress
            if (isCaps && !isShiftLocked) {
                isCaps = false;
                updateShiftState();
            }
        });

        key.setOnLongClickListener(v -> {
            String lp2 = getLongPressChar(letter);
            if (lp2 != null) commitText(lp2);
            return true;
        });

        return key;
    }

    private TextView makeCharKey(String ch) {
        TextView key = new TextView(this);
        key.setText(ch);
        key.setTextSize(18);
        key.setTextColor(keyTextColor);
        key.setGravity(Gravity.CENTER);
        key.setBackground(roundedBg(keyColor, dp(6)));
        key.setMinimumWidth(keyMinWidthPx);
        key.setMinimumHeight(keyHeightPx);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(dp(2), 0, dp(2), 0);
        key.setLayoutParams(lp);

        key.setOnClickListener(v -> commitText(ch));
        return key;
    }

    private TextView makeKey(String label, Type type, View.OnClickListener custom) {
        TextView key = new TextView(this);
        key.setText(label);
        key.setTextSize(15);
        key.setGravity(Gravity.CENTER);
        key.setMinimumHeight(keyHeightPx);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(dp(2), 0, dp(2), 0);
        key.setLayoutParams(lp);

        switch (type) {
            case SHIFT:
                key.setTextColor(isCaps ? accentColor : Color.WHITE);
                key.setBackground(roundedBg(isCaps ? accentColor : Color.parseColor("#2A2A4A"), dp(6)));
                key.setOnClickListener(v -> {
                    if (isCaps && !isShiftLocked) {
                        isShiftLocked = true;
                    } else if (isShiftLocked) {
                        isCaps = false;
                        isShiftLocked = false;
                    } else {
                        isCaps = true;
                    }
                    updateShiftState();
                });
                break;
            case DELETE:
                key.setTextColor(Color.WHITE);
                key.setBackground(roundedBg(accentColor, dp(6)));
                key.setOnClickListener(v -> {
                    InputConnection ic = getIC();
                    if (ic != null) {
                        ic.deleteSurroundingText(1, 0);
                        performHaptic();
                    }
                });
                // Long press repeat
                key.setOnLongClickListener(v -> {
                    Handler h = new Handler(Looper.getMainLooper());
                    Runnable repeat = new Runnable() {
                        @Override public void run() {
                            InputConnection ic = getIC();
                            if (ic != null) ic.deleteSurroundingText(1, 0);
                            h.postDelayed(this, 80);
                        }
                    };
                    h.postDelayed(repeat, 400);
                    key.setOnTouchListener((vv, event) -> {
                        if (event.getAction() == MotionEvent.ACTION_UP ||
                            event.getAction() == MotionEvent.ACTION_CANCEL) {
                            h.removeCallbacksAndMessages(null);
                        }
                        return false;
                    });
                    return true;
                });
                break;
            case ENTER:
                key.setTextColor(Color.WHITE);
                key.setBackground(roundedBg(accentColor, dp(6)));
                key.setOnClickListener(v -> {
                    InputConnection ic = getIC();
                    if (ic == null) return;
                    EditorInfo ei = cachedEditorInfo != null ? cachedEditorInfo : getCurrentInputEditorInfo();
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
                            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                        }
                    } else {
                        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                    }
                    performHaptic();
                });
                break;
            case MODE_QWERTY:
                key.setTextColor(accentColor);
                key.setBackground(roundedBg(Color.parseColor("#2A2A4A"), dp(6)));
                key.setOnClickListener(v -> showKeyboard(MODE_QWERTY));
                break;
            case MODE_NUM:
                key.setTextColor(accentColor);
                key.setBackground(roundedBg(Color.parseColor("#2A2A4A"), dp(6)));
                key.setOnClickListener(v -> showKeyboard(MODE_NUMBERS));
                break;
            case TRANSLATE:
                key.setTextColor(accentColor);
                key.setBackground(roundedBg(Color.parseColor("#2A2A4A"), dp(6)));
                key.setOnClickListener(v -> cycleTranslationMode());
                break;
            case DPAD_LEFT:
                key.setTextColor(Color.WHITE);
                key.setBackground(roundedBg(Color.parseColor("#2A2A4A"), dp(6)));
                key.setOnClickListener(v -> {
                    InputConnection ic = getIC();
                    if (ic != null) {
                        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));
                        performHaptic();
                    }
                });
                break;
            case DPAD_RIGHT:
                key.setTextColor(Color.WHITE);
                key.setBackground(roundedBg(Color.parseColor("#2A2A4A"), dp(6)));
                key.setOnClickListener(v -> {
                    InputConnection ic = getIC();
                    if (ic != null) {
                        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
                        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
                        performHaptic();
                    }
                });
                break;
        }

        if (custom != null) {
            key.setOnClickListener(custom);
        }

        return key;
    }

    private TextView makeSpaceKey() {
        TextView key = new TextView(this);

        int transMode = prefs.getTranslationMode();
        if (transMode == 1) key.setText("🌐 Bangla → English");
        else if (transMode == 2) key.setText("🌐 English → Bangla");
        else key.setText("Space");

        key.setTextSize(12);
        key.setTextColor(Color.parseColor("#AAAAAA"));
        key.setGravity(Gravity.CENTER);
        key.setBackground(roundedBg(keyColor, dp(6)));
        key.setMinimumHeight(keyHeightPx);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3);
        lp.setMargins(dp(2), 0, dp(2), 0);
        key.setLayoutParams(lp);

        key.setOnClickListener(v -> commitText(" "));
        return key;
    }

    // ==================== SHIFT STATE ====================

    private void updateShiftState() {
        // Update letter key labels without rebuilding the whole keyboard
        for (TextView key : letterKeys) {
            String text = key.getText().toString();
            if (text.length() == 1 && Character.isLetter(text.charAt(0))) {
                key.setText(isCaps ? text.toUpperCase(Locale.getDefault()) : text.toLowerCase(Locale.getDefault()));
            }
        }

        // Update shift key appearance
        updateShiftKeyVisual();
    }

    private void updateShiftKeyVisual() {
        if (keyboardContent.getChildCount() >= 3) {
            View thirdRow = keyboardContent.getChildAt(2);
            if (thirdRow instanceof LinearLayout) {
                View firstKey = ((LinearLayout) thirdRow).getChildAt(0);
                if (firstKey instanceof TextView) {
                    TextView shiftKey = (TextView) firstKey;
                    if (shiftKey.getText().toString().equals("⇧")) {
                        shiftKey.setTextColor(isCaps ? accentColor : Color.WHITE);
                        shiftKey.setBackground(roundedBg(isCaps ? accentColor : Color.parseColor("#2A2A4A"), dp(6)));
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
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            showToast("Voice recognition not available on this device");
            return;
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
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
                        case SpeechRecognizer.ERROR_PERMISSION: msg = "Mic permission denied"; break;
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
                        commitText(text + " ", false); // no haptic for voice
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
            isListening = true;
        } catch (Exception e) {
            showToast("Could not start voice input: " + e.getMessage());
        }
    }

    private void stopVoiceInput() {
        if (speechRecognizer != null) {
            try {
                speechRecognizer.stopListening();
                speechRecognizer.destroy();
            } catch (Exception ignored) {}
            speechRecognizer = null;
        }
        isListening = false;
        if (voiceStatusText != null) voiceStatusText.setVisibility(View.GONE);
    }

    // ==================== TRANSLATION ====================

    private void cycleTranslationMode() {
        int current = prefs.getTranslationMode();
        int next = (current + 1) % 3;
        prefs.setTranslationMode(next);
        String[] labels = {"Translation OFF", "Bangla → English", "English → Bangla"};
        showToast(labels[next]);
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
                    vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(15);
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
