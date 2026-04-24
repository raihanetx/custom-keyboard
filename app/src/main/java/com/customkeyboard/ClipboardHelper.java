package com.customkeyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Clipboard manager with persistent history.
 * Stores clipboard entries to SharedPreferences so they survive keyboard restarts.
 */
public class ClipboardHelper {
    private static final int MAX_HISTORY = 30;
    private static final String PREFS = "clipboard_history_prefs";
    private static final String KEY_HISTORY = "clipboard_history";

    private final ClipboardManager clipboardManager;
    private final SharedPreferences prefs;
    private final List<String> history = new ArrayList<>();
    private ClipboardManager.OnPrimaryClipChangedListener clipListener;

    public ClipboardHelper(Context context) {
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        loadHistory();
    }

    public void startListening(Object callback) {
        stopListening();
        clipListener = () -> {
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                ClipData.Item item = clip.getItemAt(0);
                CharSequence text = item.getText();
                if (text != null && text.length() > 0) {
                    addToHistory(text.toString());
                }
            }
        };
        clipboardManager.addPrimaryClipChangedListener(clipListener);
    }

    public void stopListening() {
        if (clipListener != null) {
            clipboardManager.removePrimaryClipChangedListener(clipListener);
            clipListener = null;
        }
    }

    private void addToHistory(String text) {
        if (text == null || text.trim().isEmpty()) return;
        // Remove duplicate
        history.remove(text);
        history.add(0, text);
        while (history.size() > MAX_HISTORY) {
            history.remove(history.size() - 1);
        }
        saveHistory();
    }

    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    public String getHistoryItem(int index) {
        if (index >= 0 && index < history.size()) {
            return history.get(index);
        }
        return "";
    }

    public void clearHistory() {
        history.clear();
        saveHistory();
    }

    public boolean hasClipboardContent() {
        return clipboardManager.hasPrimaryClip();
    }

    private void loadHistory() {
        try {
            String json = prefs.getString(KEY_HISTORY, "[]");
            JSONArray arr = new JSONArray(json);
            history.clear();
            for (int i = 0; i < arr.length(); i++) {
                String item = arr.getString(i);
                if (item != null && !item.isEmpty()) {
                    history.add(item);
                }
            }
        } catch (JSONException e) {
            history.clear();
        }
    }

    private void saveHistory() {
        try {
            JSONArray arr = new JSONArray();
            for (String item : history) {
                arr.put(item);
            }
            prefs.edit().putString(KEY_HISTORY, arr.toString()).apply();
        } catch (Exception ignored) {}
    }
}
