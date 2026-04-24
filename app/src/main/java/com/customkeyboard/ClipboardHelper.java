package com.customkeyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class ClipboardHelper {
    private static final int MAX_HISTORY = 20;
    private final ClipboardManager clipboardManager;
    private final List<ClipData.Item> history = new ArrayList<>();
    private ClipboardManager.OnPrimaryClipChangedListener clipListener;

    public interface ClipboardCallback {
        void onClipboardChanged(String text);
    }

    public ClipboardHelper(Context context) {
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public void startListening(ClipboardCallback callback) {
        // Remove old listener first to prevent stacking
        stopListening();
        clipListener = () -> {
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                ClipData.Item item = clip.getItemAt(0);
                CharSequence text = item.getText();
                if (text != null && text.length() > 0) {
                    addToHistory(item);
                    if (callback != null) {
                        callback.onClipboardChanged(text.toString());
                    }
                }
            }
        };
        clipboardManager.addPrimaryClipChangedListener(clipListener);
    }

    public void stopListening() {
        if (clipListener != null) {
            clipboardManager.removePrimaryClipChangedListener(clipListener);
        }
    }

    private void addToHistory(ClipData.Item item) {
        // Remove duplicate
        for (int i = history.size() - 1; i >= 0; i--) {
            CharSequence existing = history.get(i).getText();
            if (existing != null && existing.toString().equals(item.getText().toString())) {
                history.remove(i);
            }
        }
        history.add(0, item);
        if (history.size() > MAX_HISTORY) {
            history.remove(history.size() - 1);
        }
    }

    public void copy(String text) {
        ClipData clip = ClipData.newPlainText("custom_keyboard", text);
        clipboardManager.setPrimaryClip(clip);
    }

    public String paste() {
        ClipData clip = clipboardManager.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            CharSequence text = clip.getItemAt(0).getText();
            return text != null ? text.toString() : "";
        }
        return "";
    }

    public List<ClipData.Item> getHistory() {
        return new ArrayList<>(history);
    }

    public String getHistoryItem(int index) {
        if (index >= 0 && index < history.size()) {
            CharSequence text = history.get(index).getText();
            return text != null ? text.toString() : "";
        }
        return "";
    }

    public void clearHistory() {
        history.clear();
    }

    public boolean hasClipboardContent() {
        return clipboardManager.hasPrimaryClip();
    }
}
