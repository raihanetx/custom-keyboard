package com.customkeyboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Custom key view with proper press feedback, accessibility, and touch handling.
 * Supports pressed state animation, long-press repeat, and haptic feedback.
 */
public class KeyView extends TextView {

    public interface OnKeyActionListener {
        void onKeyPressed(String label);
        void onKeyLongPressed(String label);
    }

    private String keyLabel;
    private int normalColor;
    private int pressedColor;
    private int textColor;
    private int borderColor;
    private int cornerRadius;
    private boolean isRepeatable = false;
    private boolean isPressed = false;
    private OnKeyActionListener listener;
    private Vibrator vibrator;
    private boolean vibrateEnabled = true;

    // Long-press repeat
    private Runnable repeatRunnable;
    private android.os.Handler repeatHandler;
    private static final int REPEAT_DELAY = 400;
    private static final int REPEAT_INTERVAL = 70;

    public KeyView(Context context, String label, int normalColor, int pressedColor,
                   int textColor, int borderColor, int cornerRadius) {
        super(context);
        this.keyLabel = label;
        this.normalColor = normalColor;
        this.pressedColor = pressedColor;
        this.textColor = textColor;
        this.borderColor = borderColor;
        this.cornerRadius = cornerRadius;
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.repeatHandler = new android.os.Handler(android.os.Looper.getMainLooper());

        setText(label);
        setTextColor(textColor);
        setGravity(Gravity.CENTER);
        setClickable(true);
        setFocusable(true);

        // Accessibility
        setContentDescription(getContentDescriptionForLabel(label));

        // Background with pressed state
        setBackground(createStateBackground());
    }

    public void setOnKeyActionListener(OnKeyActionListener listener) {
        this.listener = listener;
    }

    public void setRepeatable(boolean repeatable) {
        this.isRepeatable = repeatable;
    }

    public void setVibrateEnabled(boolean enabled) {
        this.vibrateEnabled = enabled;
    }

    public void updateLabel(String newLabel) {
        this.keyLabel = newLabel;
        setText(newLabel);
        setContentDescription(getContentDescriptionForLabel(newLabel));
    }

    public void updateColors(int normal, int pressed, int text) {
        this.normalColor = normal;
        this.pressedColor = pressed;
        this.textColor = text;
        setTextColor(text);
        setBackground(createStateBackground());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);
                isPressed = true;
                performHaptic();

                if (isRepeatable) {
                    repeatRunnable = () -> {
                        if (isPressed && listener != null) {
                            listener.onKeyPressed(keyLabel);
                            repeatHandler.postDelayed(repeatRunnable, REPEAT_INTERVAL);
                        }
                    };
                    repeatHandler.postDelayed(repeatRunnable, REPEAT_DELAY);
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (isPressed) {
                    setPressed(false);
                    isPressed = false;
                    repeatHandler.removeCallbacksAndMessages(null);
                    if (listener != null) {
                        listener.onKeyPressed(keyLabel);
                    }
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                isPressed = false;
                repeatHandler.removeCallbacksAndMessages(null);
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performLongClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            super.performLongClick();
        }
        if (listener != null) {
            listener.onKeyLongPressed(keyLabel);
        }
        return true;
    }

    private void performHaptic() {
        if (vibrateEnabled && vibrator != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(12);
                }
            } catch (Exception ignored) {}
        }
    }

    private StateListDrawable createStateBackground() {
        StateListDrawable states = new StateListDrawable();

        // Pressed state
        GradientDrawable pressed = new GradientDrawable();
        pressed.setColor(pressedColor);
        pressed.setCornerRadius(cornerRadius);
        pressed.setStroke(1, borderColor);
        states.addState(new int[]{android.R.attr.state_pressed}, pressed);

        // Normal state
        GradientDrawable normal = new GradientDrawable();
        normal.setColor(normalColor);
        normal.setCornerRadius(cornerRadius);
        normal.setStroke(1, borderColor);
        states.addState(new int[]{}, normal);

        return states;
    }

    private String getContentDescriptionForLabel(String label) {
        if (label == null) return "";
        switch (label) {
            case "⇧": return "Shift";
            case "⌫": return "Backspace";
            case "↵": return "Enter";
            case "space": case "Space": return "Space";
            case "123": return "Numbers";
            case "#+=": return "Symbols";
            case "🌐": return "Translation";
            case "ABC": return "Back to letters";
            case "◂": return "Move cursor left";
            case "▸": return "Move cursor right";
            default: return label.length() == 1 ? label : label;
        }
    }

    public static int darkenColor(int color, float factor) {
        int r = (int)(Color.red(color) * factor);
        int g = (int)(Color.green(color) * factor);
        int b = (int)(Color.blue(color) * factor);
        return Color.rgb(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }
}
