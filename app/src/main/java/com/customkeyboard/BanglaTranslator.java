package com.customkeyboard;

import java.util.HashMap;
import java.util.Map;

public class BanglaTranslator {
    private static final Map<Character, String> banglaToEnglish = new HashMap<>();
    private static final Map<String, String> englishToBangla = new HashMap<>();

    static {
        // Vowels
        banglaToEnglish.put('অ', "o"); banglaToEnglish.put('আ', "a"); banglaToEnglish.put('ই', "i");
        banglaToEnglish.put('ঈ', "i"); banglaToEnglish.put('উ', "u"); banglaToEnglish.put('ঊ', "u");
        banglaToEnglish.put('ঋ', "ri"); banglaToEnglish.put('এ', "e"); banglaToEnglish.put('ঐ', "oi");
        banglaToEnglish.put('ও', "o"); banglaToEnglish.put('ঔ', "ou");

        // Consonants
        banglaToEnglish.put('ক', "k"); banglaToEnglish.put('খ', "kh"); banglaToEnglish.put('গ', "g");
        banglaToEnglish.put('ঘ', "gh"); banglaToEnglish.put('ঙ', "ng"); banglaToEnglish.put('চ', "ch");
        banglaToEnglish.put('ছ', "chh"); banglaToEnglish.put('জ', "j"); banglaToEnglish.put('ঝ', "jh");
        banglaToEnglish.put('ঞ', "ny"); banglaToEnglish.put('ট', "t"); banglaToEnglish.put('ঠ', "th");
        banglaToEnglish.put('ড', "d"); banglaToEnglish.put('ঢ', "dh"); banglaToEnglish.put('ণ', "n");
        banglaToEnglish.put('ত', "t"); banglaToEnglish.put('থ', "th"); banglaToEnglish.put('দ', "d");
        banglaToEnglish.put('ধ', "dh"); banglaToEnglish.put('ন', "n"); banglaToEnglish.put('প', "p");
        banglaToEnglish.put('ফ', "ph"); banglaToEnglish.put('ব', "b"); banglaToEnglish.put('ভ', "bh");
        banglaToEnglish.put('ম', "m"); banglaToEnglish.put('য', "j"); banglaToEnglish.put('র', "r");
        banglaToEnglish.put('ল', "l"); banglaToEnglish.put('শ', "sh"); banglaToEnglish.put('ষ', "sh");
        banglaToEnglish.put('স', "s"); banglaToEnglish.put('হ', "h"); banglaToEnglish.put('ড়', "r");
        banglaToEnglish.put('ঢ়', "rh"); banglaToEnglish.put('য়', "y");

        // Vowel signs (kar)
        banglaToEnglish.put('া', "a"); banglaToEnglish.put('ি', "i"); banglaToEnglish.put('ী', "i");
        banglaToEnglish.put('ু', "u"); banglaToEnglish.put('ূ', "u"); banglaToEnglish.put('ৃ', "ri");
        banglaToEnglish.put('ে', "e"); banglaToEnglish.put('ৈ', "oi"); banglaToEnglish.put('ো', "o");
        banglaToEnglish.put('ৌ', "ou");

        // Special
        banglaToEnglish.put('্', "");  // Hasant
        banglaToEnglish.put('ং', "ng"); banglaToEnglish.put('ঃ', "h"); banglaToEnglish.put('ঁ', "n");
        banglaToEnglish.put('০', "0"); banglaToEnglish.put('১', "1"); banglaToEnglish.put('২', "2");
        banglaToEnglish.put('৩', "3"); banglaToEnglish.put('৪', "4"); banglaToEnglish.put('৫', "5");
        banglaToEnglish.put('৬', "6"); banglaToEnglish.put('৭', "7"); banglaToEnglish.put('৮', "8");
        banglaToEnglish.put('৯', "9");

        // Common English to Bangla phonetic mappings
        englishToBangla.put("k", "ক"); englishToBangla.put("kh", "খ"); englishToBangla.put("g", "গ");
        englishToBangla.put("gh", "ঘ"); englishToBangla.put("ng", "ঙ"); englishToBangla.put("c", "চ");
        englishToBangla.put("ch", "চ"); englishToBangla.put("chh", "ছ"); englishToBangla.put("j", "জ");
        englishToBangla.put("jh", "ঝ"); englishToBangla.put("ny", "ঞ"); englishToBangla.put("t", "ত");
        englishToBangla.put("th", "থ"); englishToBangla.put("d", "দ"); englishToBangla.put("dh", "ধ");
        englishToBangla.put("n", "ন"); englishToBangla.put("p", "প"); englishToBangla.put("ph", "ফ");
        englishToBangla.put("b", "ব"); englishToBangla.put("bh", "ভ"); englishToBangla.put("m", "ম");
        englishToBangla.put("r", "র"); englishToBangla.put("l", "ল"); englishToBangla.put("sh", "শ");
        englishToBangla.put("s", "স"); englishToBangla.put("h", "হ"); englishToBangla.put("y", "য়");
        englishToBangla.put("a", "আ"); englishToBangla.put("i", "ই"); englishToBangla.put("u", "উ");
        englishToBangla.put("e", "এ"); englishToBangla.put("o", "ও");

        // Common words
        englishToBangla.put("ami", "আমি"); englishToBangla.put("tumi", "তুমি");
        englishToBangla.put("apni", "আপনি"); englishToBangla.put("bhalo", "ভালো");
        englishToBangla.put("kemon", "কেমন"); englishToBangla.put("ache", "আছে");
        englishToBangla.put("na", "না"); englishToBangla.put("ha", "হ্যা");
        englishToBangla.put("khub", "খুব"); englishToBangla.put("valo", "ভালো");
        englishToBangla.put("dhonnobad", "ধন্যবাদ"); englishToBangla.put("salam", "সালাম");
        englishToBangla.put("ki", "কি"); englishToBangla.put("keno", "কেন");
        englishToBangla.put("kothay", "কোথায়"); englishToBangla.put("kokhon", "কখন");
        englishToBangla.put("kichu", "কিছু"); englishToBangla.put("onek", "অনেক");
        englishToBangla.put("baba", "বাবা"); englishToBangla.put("ma", "মা");
        englishToBangla.put("bhai", "ভাই"); englishToBangla.put("bon", "বোন");
        englishToBangla.put("bari", "বাড়ি"); englishToBangla.put("pani", "পানি");
        englishToBangla.put("khabar", "খাবার"); englishToBangla.put("bhat", "ভাত");
        englishToBangla.put("mach", "মাছ"); englishToBangla.put("mangsho", "মাংস");
        englishToBangla.put("torkari", "তরকারি"); englishToBangla.put("cha", "চা");
        englishToBangla.put("coffee", "কফি"); englishToBangla.put("school", "স্কুল");
        englishToBangla.put("college", "কলেজ"); englishToBangla.put("office", "অফিস");
        englishToBangla.put("kaaj", "কাজ"); englishToBangla.put("ghum", "ঘুম");
        englishToBangla.put("jal", "জল"); englishToBangla.put("gaan", "গান");
        englishToBangla.put("boi", "বই"); englishToBangla.put("chobi", "ছবি");
        englishToBangla.put("phone", "ফোন"); englishToBangla.put("kotha", "কথা");
    }

    public static String banglaToEnglish(String banglaText) {
        StringBuilder result = new StringBuilder();
        for (char c : banglaText.toCharArray()) {
            if (banglaToEnglish.containsKey(c)) {
                result.append(banglaToEnglish.get(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static String englishToBangla(String englishText) {
        String lower = englishText.toLowerCase().trim();
        if (englishToBangla.containsKey(lower)) {
            return englishToBangla.get(lower);
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < lower.length()) {
            boolean matched = false;
            // Try 3-char, 2-char, then 1-char matches (longest first)
            for (int len = 3; len >= 1; len--) {
                if (i + len <= lower.length()) {
                    String sub = lower.substring(i, i + len);
                    if (englishToBangla.containsKey(sub)) {
                        result.append(englishToBangla.get(sub));
                        i += len;
                        matched = true;
                        break;
                    }
                }
            }
            if (!matched) {
                result.append(lower.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    public static String translateWord(String word, int mode) {
        if (mode == 1) return banglaToEnglish(word);
        if (mode == 2) return englishToBangla(word);
        return word;
    }

    public static String translateText(String text, int mode) {
        if (mode == 0 || text == null || text.isEmpty()) return text;
        StringBuilder result = new StringBuilder();
        String[] words = text.split("(?<=\\s)(?=\\S)|(?<=\\S)(?=\\s)");
        for (String word : words) {
            if (word.trim().isEmpty()) {
                result.append(word);
            } else {
                result.append(translateWord(word.trim(), mode));
                if (word.endsWith(" ") || word.endsWith("\n")) result.append(" ");
            }
        }
        return result.toString();
    }
}
