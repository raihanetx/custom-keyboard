package com.customkeyboard;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tracks recently used emojis and persists them to SharedPreferences.
 */
public class RecentEmoji {
    private static final String PREFS = "recent_emoji_prefs";
    private static final String KEY_RECENT = "recent_emojis";
    private static final int MAX_RECENT = 24;
    private static final String SEPARATOR = ",";

    private final SharedPreferences prefs;
    private final List<String> recent = new ArrayList<>();

    public RecentEmoji(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        load();
    }

    public void add(String emoji) {
        recent.remove(emoji);
        recent.add(0, emoji);
        while (recent.size() > MAX_RECENT) {
            recent.remove(recent.size() - 1);
        }
        save();
    }

    public List<String> getRecent() {
        return new ArrayList<>(recent);
    }

    public String[] getRecentArray() {
        return recent.toArray(new String[0]);
    }

    public boolean isEmpty() {
        return recent.isEmpty();
    }

    private void load() {
        String saved = prefs.getString(KEY_RECENT, "");
        if (!saved.isEmpty()) {
            recent.clear();
            recent.addAll(Arrays.asList(saved.split(SEPARATOR)));
        }
    }

    private void save() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recent.size(); i++) {
            if (i > 0) sb.append(SEPARATOR);
            sb.append(recent.get(i));
        }
        prefs.edit().putString(KEY_RECENT, sb.toString()).apply();
    }
}
