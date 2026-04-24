package com.customkeyboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

/**
 * Clean, minimal key view with subtle press feedback.
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
    private boolean hasRepeated = false;
    private boolean longPressFired = false; // FIX: Prevent double long-press
    private OnKeyActionListener listener;
    private Vibrator vibrator;
    private boolean vibrateEnabled = true;

    private Runnable repeatRunnable;
    private Runnable longPressRunnable;
    private final Handler handler;
    // FIX: Use a dedicated token for this key's callbacks instead of
    // removeCallbacksAndMessages(null) which nukes ALL main thread callbacks
    private final Object handlerToken = new Object();
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
        this.handler = new Handler(Looper.getMainLooper());

        setText(label);
        setTextColor(textColor);
        setGravity(Gravity.CENTER);
        setClickable(true);
        setFocusable(true);
        setTypeface(Typeface.DEFAULT);
        setContentDescription(getContentDescriptionForLabel(label));
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
                hasRepeated = false;
                longPressFired = false;
                performHaptic();

                if (isRepeatable) {
                    repeatRunnable = () -> {
                        if (isPressed && listener != null) {
                            hasRepeated = true;
                            listener.onKeyPressed(keyLabel);
                            handler.postDelayed(repeatRunnable, handlerToken, REPEAT_INTERVAL);
                        }
                    };
                    handler.postDelayed(repeatRunnable, handlerToken, REPEAT_DELAY);
                }
                longPressRunnable = () -> {
                    if (isPressed && listener != null) {
                        longPressFired = true;
                        listener.onKeyLongPressed(keyLabel);
                    }
                };
                handler.postDelayed(longPressRunnable, handlerToken, ViewConfiguration.getLongPressTimeout());
                return true;

            case MotionEvent.ACTION_UP:
                if (isPressed) {
                    setPressed(false);
                    isPressed = false;
                    handler.removeCallbacksAndMessages(handlerToken);
                    // Don't call listener here — performClick() handles it
                    // (avoids double-fire since system calls performClick on UP)
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                isPressed = false;
                // FIX: Only remove THIS key's callbacks
                handler.removeCallbacksAndMessages(handlerToken);
                return true;
        }
        return super.onTouchEvent(event);
    }

    // FIX: Override performClick for accessibility services and normal tap handling.
    // The system calls this on ACTION_UP (since onTouchEvent returns true).
    // Accessibility services (TalkBack) also call this directly.
    @Override
    public boolean performClick() {
        super.performClick();
        // Don't fire if the key already repeated (backspace held down) or long-pressed
        if (!hasRepeated && !longPressFired && listener != null) {
            listener.onKeyPressed(keyLabel);
        }
        return true;
    }

    // FIX: Don't fire long-press from performLongClick — it's already handled
    // by the longPressRunnable in onTouchEvent. The system's performLongClick
    // would cause a double-fire.
    @Override
    public boolean performLongClick() {
        super.performLongClick();
        // Long press is handled by longPressRunnable in onTouchEvent
        return true;
    }

    private void performHaptic() {
        if (vibrateEnabled && vibrator != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(10);
                }
            } catch (Exception ignored) {}
        }
    }

    private StateListDrawable createStateBackground() {
        StateListDrawable states = new StateListDrawable();

        GradientDrawable pressed = new GradientDrawable();
        pressed.setColor(pressedColor);
        pressed.setCornerRadius(cornerRadius);
        states.addState(new int[]{android.R.attr.state_pressed}, pressed);

        GradientDrawable normal = new GradientDrawable();
        normal.setColor(normalColor);
        normal.setCornerRadius(cornerRadius);
        if (borderColor != Color.TRANSPARENT) {
            normal.setStroke(1, borderColor);
        }
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
            default: return label;
        }
    }
}
