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
        banglaToEnglish.put('্', ""); banglaToEnglish.put('ং', "ng"); banglaToEnglish.put('ঃ', "h");
        banglaToEnglish.put('ঁ', "n");
        banglaToEnglish.put('০', "0"); banglaToEnglish.put('১', "1"); banglaToEnglish.put('২', "2");
        banglaToEnglish.put('৩', "3"); banglaToEnglish.put('৪', "4"); banglaToEnglish.put('৫', "5");
        banglaToEnglish.put('৬', "6"); banglaToEnglish.put('৭', "7"); banglaToEnglish.put('৮', "8");
        banglaToEnglish.put('৯', "9");

        // Single character mappings (phonetic)
        englishToBangla.put("a", "আ"); englishToBangla.put("b", "ব"); englishToBangla.put("c", "চ");
        englishToBangla.put("d", "দ"); englishToBangla.put("e", "এ"); englishToBangla.put("f", "ফ");
        englishToBangla.put("g", "গ"); englishToBangla.put("h", "হ"); englishToBangla.put("i", "ই");
        englishToBangla.put("j", "জ"); englishToBangla.put("k", "ক"); englishToBangla.put("l", "ল");
        englishToBangla.put("m", "ম"); englishToBangla.put("n", "ন"); englishToBangla.put("o", "ও");
        englishToBangla.put("p", "প"); englishToBangla.put("r", "র"); englishToBangla.put("s", "স");
        englishToBangla.put("t", "ত"); englishToBangla.put("u", "উ"); englishToBangla.put("v", "ভ");
        englishToBangla.put("w", "ও"); englishToBangla.put("x", "এক্স"); englishToBangla.put("y", "য়");
        englishToBangla.put("z", "জ");

        // Two/three char phonetic
        englishToBangla.put("kh", "খ"); englishToBangla.put("gh", "ঘ"); englishToBangla.put("ng", "ঙ");
        englishToBangla.put("ch", "চ"); englishToBangla.put("chh", "ছ"); englishToBangla.put("jh", "ঝ");
        englishToBangla.put("ny", "ঞ"); englishToBangla.put("th", "থ"); englishToBangla.put("dh", "ধ");
        englishToBangla.put("ph", "ফ"); englishToBangla.put("bh", "ভ"); englishToBangla.put("sh", "শ");
        englishToBangla.put("tt", "ট"); englishToBangla.put("dd", "ড"); englishToBangla.put("nn", "ণ");

        // Common words — Greetings & Basics
        englishToBangla.put("salaam", "সালাম"); englishToBangla.put("salam", "সালাম");
        englishToBangla.put("namaskar", "নমস্কার"); englishToBangla.put("namaste", "নমস্তে");
        englishToBangla.put("dhonnobad", "ধন্যবাদ"); englishToBangla.put("dhonyobad", "ধন্যবাদ");
        englishToBangla.put("thank", "ধন্যবাদ"); englishToBangla.put("thanks", "ধন্যবাদ");
        englishToBangla.put("hello", "হ্যালো"); englishToBangla.put("hi", "হাই");
        englishToBangla.put("ha", "হ্যা"); englishToBangla.put("yes", "হ্যা");
        englishToBangla.put("na", "না"); englishToBangla.put("no", "না");
        englishToBangla.put("sorry", "দুঃখিত"); englishToBangla.put("please", "অনুগ্রহ");

        // Pronouns
        englishToBangla.put("ami", "আমি"); englishToBangla.put("i", "আমি");
        englishToBangla.put("tumi", "তুমি"); englishToBangla.put("you", "তুমি");
        englishToBangla.put("apni", "আপনি"); englishToBangla.put("se", "সে");
        englishToBangla.put("he", "সে"); englishToBangla.put("she", "সে");
        englishToBangla.put("ora", "তারা"); englishToBangla.put("they", "তারা");
        englishToBangla.put("amra", "আমরা"); englishToBangla.put("we", "আমরা");
        englishToBangla.put("amake", "আমাকে"); englishToBangla.put("tomake", "তোমাকে");
        englishToBangla.put("tader", "তাদের"); englishToBangla.put("amader", "আমাদের");

        // Common verbs
        englishToBangla.put("korchilam", "করছিলাম"); englishToBangla.put("koro", "করো");
        englishToBangla.put("korte", "করতে"); englishToBangla.put("korbo", "করবো");
        englishToBangla.put("kore", "করে"); englishToBangla.put("kora", "করা");
        englishToBangla.put("hoy", "হয়"); englishToBangla.put("hoye", "হয়ে");
        englishToBangla.put("hole", "হলে"); englishToBangla.put("hobe", "হবে");
        englishToBangla.put("ache", "আছে"); englishToBangla.put("chilo", "ছিল");
        englishToBangla.put("jao", "যাও"); englishToBangla.put("jai", "যাই");
        englishToBangla.put("jaoya", "যাওয়া"); englishToBangla.put("gelam", "গেলাম");
        englishToBangla.put("esheche", "এসেছে"); englishToBangla.put("asho", "আসো");
        englishToBangla.put("khaoya", "খাওয়া"); englishToBangla.put("khai", "খাই");
        englishToBangla.put("khao", "খাও"); englishToBangla.put("khabo", "খাবো");
        englishToBangla.put("kheyeche", "খেয়েছে"); englishToBangla.put("bolo", "বলো");
        englishToBangla.put("boli", "বলি"); englishToBangla.put("bole", "বলে");
        englishToBangla.put("bolte", "বলতে"); englishToBangla.put("bolo", "বলো");
        englishToBangla.put("dekho", "দেখো"); englishToBangla.put("dekhi", "দেখি");
        englishToBangla.put("dekhte", "দেখতে"); englishToBangla.put("dekhbo", "দেখবো");
        englishToBangla.put("shuno", "শুনো"); englishToBangla.put("shuni", "শুনি");
        englishToBangla.put("likho", "লেখো"); englishToBangla.put("likhi", "লেখি");
        englishToBangla.put("poro", "পড়ো"); englishToBangla.put("pori", "পড়ি");
        englishToBangla.put("boste", "বসতে"); englishToBangla.put("bosi", "বসি");
        englishToBangla.put("thako", "থাকো"); englishToBangla.put("thaki", "থাকি");
        englishToBangla.put("ghumao", "ঘুমাও"); englishToBangla.put("ghumi", "ঘুমি");

        // Adjectives
        englishToBangla.put("bhalo", "ভালো"); englishToBangla.put("good", "ভালো");
        englishToBangla.put("valo", "ভালো"); englishToBangla.put("bad", "খারাপ");
        englishToBangla.put("boro", "বড়"); englishToBangla.put("big", "বড়");
        englishToBangla.put("choto", "ছোট"); englishToBangla.put("small", "ছোট");
        englishToBangla.put("sundor", "সুন্দর"); englishToBangla.put("beautiful", "সুন্দর");
        englishToBangla.put("khub", "খুব"); englishToBangla.put("very", "খুব");
        englishToBangla.put("onek", "অনেক"); englishToBangla.put("much", "অনেক");
        englishToBangla.put("notun", "নতুন"); englishToBangla.put("new", "নতুন");
        englishToBangla.put("purono", "পুরোনো"); englishToBangla.put("old", "পুরোনো");
        englishToBangla.put("shundor", "সুন্দর"); englishToBangla.put("nice", "সুন্দর");

        // Questions
        englishToBangla.put("ki", "কি"); englishToBangla.put("what", "কি");
        englishToBangla.put("keno", "কেন"); englishToBangla.put("why", "কেন");
        englishToBangla.put("kothay", "কোথায়"); englishToBangla.put("where", "কোথায়");
        englishToBangla.put("kokhon", "কখন"); englishToBangla.put("when", "কখন");
        englishToBangla.put("kemon", "কেমন"); englishToBangla.put("how", "কেমন");
        englishToBangla.put("ke", "কে"); englishToBangla.put("who", "কে");
        englishToBangla.put("koto", "কত"); englishToBangla.put("howmuch", "কত");
        englishToBangla.put("kichu", "কিছু"); englishToBangla.put("some", "কিছু");

        // People & Family
        englishToBangla.put("baba", "বাবা"); englishToBangla.put("father", "বাবা");
        englishToBangla.put("ma", "মা"); englishToBangla.put("mother", "মা");
        englishToBangla.put("bhai", "ভাই"); englishToBangla.put("brother", "ভাই");
        englishToBangla.put("bon", "বোন"); englishToBangla.put("sister", "বোন");
        englishToBangla.put("chacha", "চাচা"); englishToBangla.put("uncle", "চাচা");
        englishToBangla.put("khala", "খালা"); englishToBangla.put("aunt", "খালা");
        englishToBangla.put("dada", "দাদা"); englishToBangla.put("grandfather", "দাদা");
        englishToBangla.put("dadi", "দাদি"); englishToBangla.put("grandmother", "দাদি");
        englishToBangla.put("chele", "ছেলে"); englishToBangla.put("boy", "ছেলে");
        englishToBangla.put("meye", "মেয়ে"); englishToBangla.put("girl", "মেয়ে");
        englishToBangla.put("manush", "মানুষ"); englishToBangla.put("human", "মানুষ");
        englishToBangla.put("lok", "লোক"); englishToBangla.put("person", "লোক");
        englishToBangla.put("bondhu", "বন্ধু"); englishToBangla.put("friend", "বন্ধু");

        // Places
        englishToBangla.put("bari", "বাড়ি"); englishToBangla.put("home", "বাড়ি");
        englishToBangla.put("school", "স্কুল"); englishToBangla.put("college", "কলেজ");
        englishToBangla.put("office", "অফিস"); englishToBangla.put("bazar", "বাজার");
        englishToBangla.put("market", "বাজার"); englishToBangla.put("dokan", "দোকান");
        englishToBangla.put("shop", "দোকান"); englishToBangla.put("hospital", "হাসপাতাল");
        englishToBangla.put("road", "রাস্তা"); englishToBangla.put("ghat", "ঘাট");
        englishToBangla.put("nodi", "নদী"); englishToBangla.put("river", "নদী");

        // Food & Drink
        englishToBangla.put("pani", "পানি"); englishToBangla.put("water", "পানি");
        englishToBangla.put("jal", "জল"); englishToBangla.put("cha", "চা");
        englishToBangla.put("tea", "চা"); englishToBangla.put("coffee", "কফি");
        englishToBangla.put("bhat", "ভাত"); englishToBangla.put("rice", "ভাত");
        englishToBangla.put("mach", "মাছ"); englishToBangla.put("fish", "মাছ");
        englishToBangla.put("mangsho", "মাংস"); englishToBangla.put("meat", "মাংস");
        englishToBangla.put("torkari", "তরকারি"); englishToBangla.put("curry", "তরকারি");
        englishToBangla.put("dal", "ডাল"); englishToBangla.put("lentils", "ডাল");
        englishToBangla.put("khabar", "খাবার"); englishToBangla.put("food", "খাবার");
        englishToBangla.put("doi", "দই"); englishToBangla.put("yogurt", "দই");
        englishToBangla.put("dim", "ডিম"); englishToBangla.put("egg", "ডিম");
        englishToBangla.put("roti", "রুটি"); englishToBangla.put("bread", "রুটি");
        englishToBangla.put("phol", "ফল"); englishToBangla.put("fruit", "ফল");
        englishToBangla.put("aam", "আম"); englishToBangla.put("mango", "আম");
        englishToBangla.put("kola", "কলা"); englishToBangla.put("banana", "কলা");

        // Objects
        englishToBangla.put("boi", "বই"); englishToBangla.put("book", "বই");
        englishToBangla.put("chobi", "ছবি"); englishToBangla.put("picture", "ছবি");
        englishToBangla.put("phone", "ফোন"); englishToBangla.put("gaan", "গান");
        englishToBangla.put("song", "গান"); englishToBangla.put("kotha", "কথা");
        englishToBangla.put("word", "কথা"); englishToBangla.put("shomoy", "সময়");
        englishToBangla.put("time", "সময়"); englishToBangla.put("din", "দিন");
        englishToBangla.put("day", "দিন"); englishToBangla.put("raat", "রাত");
        englishToBangla.put("night", "রাত"); englishToBangla.put("shokal", "সকাল");
        englishToBangla.put("morning", "সকাল"); englishToBangla.put("bikel", "বিকেল");
        englishToBangla.put("afternoon", "বিকেল"); englishToBangla.put("ghum", "ঘুম");
        englishToBangla.put("sleep", "ঘুম"); englishToBangla.put("kaaj", "কাজ");
        englishToBangla.put("work", "কাজ"); englishToBangla.put("taka", "টাকা");
        englishToBangla.put("money", "টাকা"); englishToBangla.put("dam", "দাম");
        englishToBangla.put("price", "দাম");

        // States & Feelings
        englishToBangla.put("kemon", "কেমন"); englishToBangla.put("bhalo", "ভালো");
        englishToBangla.put("kharap", "খারাপ"); englishToBangla.put("khushi", "খুশি");
        englishToBangla.put("happy", "খুশি"); englishToBangla.put("dukkhi", "দুঃখী");
        englishToBangla.put("sad", "দুঃখী"); englishToBangla.put("rokkha", "রক্ষা");
        englishToBangla.put("safe", "রক্ষা"); englishToBangla.put("bipod", "বিপদ");
        englishToBangla.put("danger", "বিপদ"); englishToBangla.put("shanti", "শান্তি");
        englishToBangla.put("peace", "শান্তি"); englishToBangla.put("bissash", "বিশ্বাস");
        englishToBangla.put("trust", "বিশ্বাস"); englishToBangla.put("bhalobasha", "ভালোবাসা");
        englishToBangla.put("love", "ভালোবাসা"); englishToBangla.put("jibon", "জীবন");
        englishToBangla.put("life", "জীবন"); englishToBangla.put("mrittu", "মৃত্যু");
        englishToBangla.put("death", "মৃত্যু");

        // Actions
        englishToBangla.put("jao", "যাও"); englishToBangla.put("go", "যাও");
        englishToBangla.put("asho", "আসো"); englishToBangla.put("come", "আসো");
        englishToBangla.put("khaoya", "খাওয়া"); englishToBangla.put("eat", "খাও");
        englishToBangla.put("ghum", "ঘুম"); englishToBangla.put("sleep", "ঘুমাও");
        englishToBangla.put("cholo", "চলো"); englishToBangla.put("letsgo", "চলো");
        englishToBangla.put("bolo", "বলো"); englishToBangla.put("say", "বলো");
        englishToBangla.put("shono", "শোনো"); englishToBangla.put("listen", "শোনো");
        englishToBangla.put("dekho", "দেখো"); englishToBangla.put("see", "দেখো");
        englishToBangla.put("likho", "লেখো"); englishToBangla.put("write", "লেখো");
        englishToBangla.put("poro", "পড়ো"); englishToBangla.put("read", "পড়ো");
        englishToBangla.put("help", "সাহায্য"); englishToBangla.put("shahajjo", "সাহায্য");
        englishToBangla.put("koro", "করো"); englishToBangla.put("do", "করো");

        // Common phrases
        englishToBangla.put("ki", "কি"); englishToBangla.put("khub", "খুব");
        englishToBangla.put("tai", "তাই"); englishToBangla.put("so", "তাই");
        englishToBangla.put("ekhon", "এখন"); englishToBangla.put("now", "এখন");
        englishToBangla.put("pore", "পরে"); englishToBangla.put("later", "পরে");
        englishToBangla.put("age", "আগে"); englishToBangla.put("before", "আগে");
        englishToBangla.put("sob", "সব"); englishToBangla.put("all", "সব");
        englishToBangla.put("kichu", "কিছু"); englishToBangla.put("something", "কিছু");
        englishToBangla.put("keu", "কেউ"); englishToBangla.put("someone", "কেউ");
        englishToBangla.put("kono", "কোনো"); englishToBangla.put("any", "কোনো");
        englishToBangla.put("proti", "প্রতি"); englishToBangla.put("every", "প্রতি");
        englishToBangla.put("ek", "এক"); englishToBangla.put("one", "এক");
        englishToBangla.put("dui", "দুই"); englishToBangla.put("two", "দুই");
        englishToBangla.put("tin", "তিন"); englishToBangla.put("three", "তিন");
        englishToBangla.put("char", "চার"); englishToBangla.put("four", "চার");
        englishToBangla.put("panch", "পাঁচ"); englishToBangla.put("five", "পাঁচ");

        // Technology & Modern
        englishToBangla.put("internet", "ইন্টারনেট"); englishToBangla.put("computer", "কম্পিউটার");
        englishToBangla.put("mobile", "মোবাইল"); englishToBangla.put("app", "অ্যাপ");
        englishToBangla.put("video", "ভিডিও"); englishToBangla.put("photo", "ফটো");
        englishToBangla.put("message", "মেসেজ"); englishToBangla.put("call", "কল");
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
}
