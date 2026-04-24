package com.customkeyboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple word suggestions bar that appears above the keyboard.
 * Shows word completions based on current typed prefix.
 */
public class SuggestionsBar extends LinearLayout {

    public interface OnSuggestionSelectedListener {
        void onSuggestionSelected(String word);
    }

    private OnSuggestionSelectedListener listener;
    private LinearLayout container;
    private int accentColor;
    private int bgColor;
    private int textColor;
    private float density;

    // Basic English word frequency list (top common words)
    private static final String[] COMMON_WORDS = {
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see",
        "other", "than", "then", "now", "look", "only", "come", "its", "over",
        "think", "also", "back", "after", "use", "two", "how", "our", "work",
        "first", "well", "way", "even", "new", "want", "because", "any", "these",
        "give", "day", "most", "us", "is", "am", "are", "was", "were", "been",
        "has", "had", "did", "does", "doing", "done", "should", "may", "might",
        "must", "shall", "can", "could", "would", "need", "dare", "ought",
        "used", "hello", "thanks", "please", "sorry", "yes", "no", "ok",
        "what", "where", "when", "why", "how", "who", "which",
        "good", "bad", "big", "small", "hot", "cold", "old", "new",
        "right", "wrong", "true", "false", "here", "there",
        "going", "doing", "being", "having", "making", "taking",
        "coming", "getting", "saying", "thinking", "knowing",
        "really", "very", "much", "more", "most", "less",
        "always", "never", "sometimes", "often", "usually",
        "today", "tomorrow", "yesterday", "morning", "night",
        "love", "like", "want", "need", "help", "tell"
    };

    public SuggestionsBar(Context context, int accentColor, int bgColor, int textColor) {
        super(context);
        this.accentColor = accentColor;
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.density = context.getResources().getDisplayMetrics().density;

        setOrientation(HORIZONTAL);
        setBackgroundColor(bgColor);
        setPadding(dp(4), dp(2), dp(4), dp(2));
        setGravity(Gravity.CENTER_VERTICAL);
        setMinimumHeight(dp(32));

        HorizontalScrollView scroll = new HorizontalScrollView(context);
        scroll.setHorizontalScrollBarEnabled(false);
        container = new LinearLayout(context);
        container.setOrientation(HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        scroll.addView(container);
        addView(scroll, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        setVisibility(View.GONE);
    }

    public void setOnSuggestionSelectedListener(OnSuggestionSelectedListener listener) {
        this.listener = listener;
    }

    public void updateSuggestions(String currentWord) {
        container.removeAllViews();

        if (currentWord == null || currentWord.length() < 1) {
            setVisibility(View.GONE);
            return;
        }

        List<String> suggestions = getSuggestions(currentWord.toLowerCase());
        if (suggestions.isEmpty()) {
            setVisibility(View.GONE);
            return;
        }

        setVisibility(View.VISIBLE);

        // Add the typed word first (so user can select exactly what they typed)
        addSuggestionChip(currentWord, false);

        for (String word : suggestions) {
            if (!word.equals(currentWord.toLowerCase())) {
                addSuggestionChip(word, true);
            }
        }
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    private void addSuggestionChip(String word, boolean isSuggestion) {
        TextView chip = new TextView(getContext());
        chip.setText(word);
        chip.setTextSize(13);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(12), dp(4), dp(12), dp(4));

        if (isSuggestion) {
            chip.setTextColor(textColor);
            chip.setTypeface(null, Typeface.NORMAL);
        } else {
            chip.setTextColor(accentColor);
            chip.setTypeface(null, Typeface.BOLD);
        }

        chip.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSuggestionSelected(word);
            }
        });

        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(2), 0, dp(2), 0);
        container.addView(chip, lp);
    }

    private List<String> getSuggestions(String prefix) {
        List<String> results = new ArrayList<>();
        for (String word : COMMON_WORDS) {
            if (word.startsWith(prefix) && !word.equals(prefix)) {
                results.add(word);
                if (results.size() >= 8) break;
            }
        }
        return results;
    }

    private int dp(int dp) {
        return (int)(dp * density);
    }
}
