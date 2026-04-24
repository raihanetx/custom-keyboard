package com.customkeyboard;

import java.util.HashMap;
import java.util.Map;

public class BanglaTranslator {
    private static final Map<String, String> banglaToEnglish = new HashMap<>();
    private static final Map<String, String> englishToBangla = new HashMap<>();

    static {
        // Vowels
        banglaToEnglish.put("\u0985", "o");   // অ
        banglaToEnglish.put("\u0986", "a");   // আ
        banglaToEnglish.put("\u0987", "i");   // ই
        banglaToEnglish.put("\u0988", "i");   // ঈ
        banglaToEnglish.put("\u0989", "u");   // উ
        banglaToEnglish.put("\u098A", "u");   // ঊ
        banglaToEnglish.put("\u098B", "ri");  // ঋ
        banglaToEnglish.put("\u098F", "e");   // এ
        banglaToEnglish.put("\u0990", "oi");  // ঐ
        banglaToEnglish.put("\u0993", "o");   // ও
        banglaToEnglish.put("\u0994", "ou");  // ঔ

        // Consonants
        banglaToEnglish.put("\u0995", "k");   // ক
        banglaToEnglish.put("\u0996", "kh");  // খ
        banglaToEnglish.put("\u0997", "g");   // গ
        banglaToEnglish.put("\u0998", "gh");  // ঘ
        banglaToEnglish.put("\u0999", "ng");  // ঙ
        banglaToEnglish.put("\u099A", "ch");  // চ
        banglaToEnglish.put("\u099B", "chh"); // ছ
        banglaToEnglish.put("\u099C", "j");   // জ
        banglaToEnglish.put("\u099D", "jh");  // ঝ
        banglaToEnglish.put("\u099E", "ny");  // ঞ
        banglaToEnglish.put("\u099F", "t");   // ট
        banglaToEnglish.put("\u09A0", "th");  // ঠ
        banglaToEnglish.put("\u09A1", "d");   // ড
        banglaToEnglish.put("\u09A2", "dh");  // ঢ
        banglaToEnglish.put("\u09A3", "n");   // ণ
        banglaToEnglish.put("\u09A4", "t");   // ত
        banglaToEnglish.put("\u09A5", "th");  // থ
        banglaToEnglish.put("\u09A6", "d");   // দ
        banglaToEnglish.put("\u09A7", "dh");  // ধ
        banglaToEnglish.put("\u09A8", "n");   // ন
        banglaToEnglish.put("\u09AA", "p");   // প
        banglaToEnglish.put("\u09AB", "ph");  // ফ
        banglaToEnglish.put("\u09AC", "b");   // ব
        banglaToEnglish.put("\u09AD", "bh");  // ভ
        banglaToEnglish.put("\u09AE", "m");   // ম
        banglaToEnglish.put("\u09AF", "j");   // য
        banglaToEnglish.put("\u09B0", "r");   // র
        banglaToEnglish.put("\u09B2", "l");   // ল
        banglaToEnglish.put("\u09B6", "sh");  // শ
        banglaToEnglish.put("\u09B7", "sh");  // ষ
        banglaToEnglish.put("\u09B8", "s");   // স
        banglaToEnglish.put("\u09B9", "h");   // হ
        banglaToEnglish.put("\u09DC", "r");   // ড় (single codepoint)
        banglaToEnglish.put("\u09DD", "rh");  // ঢ় (single codepoint)
        banglaToEnglish.put("\u09DF", "y");   // য় (single codepoint)

        // Vowel signs (kar)
        banglaToEnglish.put("\u09BE", "a");   // া
        banglaToEnglish.put("\u09BF", "i");   // ি
        banglaToEnglish.put("\u09C0", "i");   // ী
        banglaToEnglish.put("\u09C1", "u");   // ু
        banglaToEnglish.put("\u09C2", "u");   // ূ
        banglaToEnglish.put("\u09C3", "ri");  // ৃ
        banglaToEnglish.put("\u09C7", "e");   // ে
        banglaToEnglish.put("\u09C8", "oi");  // ৈ
        banglaToEnglish.put("\u09CB", "o");   // ো
        banglaToEnglish.put("\u09CC", "ou");  // ৌ

        // Special
        banglaToEnglish.put("\u09CD", "");    // ্ Hasant
        banglaToEnglish.put("\u0982", "ng");  // ং
        banglaToEnglish.put("\u0983", "h");   // ঃ
        banglaToEnglish.put("\u0981", "n");   // ঁ

        // Digits
        banglaToEnglish.put("\u09E6", "0");   // ০
        banglaToEnglish.put("\u09E7", "1");   // ১
        banglaToEnglish.put("\u09E8", "2");   // ২
        banglaToEnglish.put("\u09E9", "3");   // ৩
        banglaToEnglish.put("\u09EA", "4");   // ৪
        banglaToEnglish.put("\u09EB", "5");   // ৫
        banglaToEnglish.put("\u09EC", "6");   // ৬
        banglaToEnglish.put("\u09ED", "7");   // ৭
        banglaToEnglish.put("\u09EE", "8");   // ৮
        banglaToEnglish.put("\u09EF", "9");   // ৯

        // English to Bangla single characters
        englishToBangla.put("a", "\u0986");   // আ
        englishToBangla.put("b", "\u09AC");   // ব
        englishToBangla.put("c", "\u099A");   // চ
        englishToBangla.put("d", "\u09A6");   // দ
        englishToBangla.put("e", "\u098F");   // এ
        englishToBangla.put("f", "\u09AB");   // ফ
        englishToBangla.put("g", "\u0997");   // গ
        englishToBangla.put("h", "\u09B9");   // হ
        englishToBangla.put("i", "\u0987");   // ই
        englishToBangla.put("j", "\u099C");   // জ
        englishToBangla.put("k", "\u0995");   // ক
        englishToBangla.put("l", "\u09B2");   // ল
        englishToBangla.put("m", "\u09AE");   // ম
        englishToBangla.put("n", "\u09A8");   // ন
        englishToBangla.put("o", "\u0993");   // ও
        englishToBangla.put("p", "\u09AA");   // প
        englishToBangla.put("r", "\u09B0");   // র
        englishToBangla.put("s", "\u09B8");   // স
        englishToBangla.put("t", "\u09A4");   // ত
        englishToBangla.put("u", "\u0989");   // উ
        englishToBangla.put("v", "\u09AD");   // ভ
        englishToBangla.put("w", "\u0993");   // ও
        englishToBangla.put("x", "\u098F\u0995\u09CD\u09B8"); // এক্স
        englishToBangla.put("y", "\u09DF");   // য়
        englishToBangla.put("z", "\u099C");   // জ

        // Two/three char phonetic
        englishToBangla.put("kh", "\u0996");  // খ
        englishToBangla.put("gh", "\u0998");  // ঘ
        englishToBangla.put("ng", "\u0999");  // ঙ
        englishToBangla.put("ch", "\u099A");  // চ
        englishToBangla.put("chh", "\u099B"); // ছ
        englishToBangla.put("jh", "\u099D");  // ঝ
        englishToBangla.put("ny", "\u099E");  // ঞ
        englishToBangla.put("th", "\u09A5");  // থ
        englishToBangla.put("dh", "\u09A7");  // ধ
        englishToBangla.put("ph", "\u09AB");  // ফ
        englishToBangla.put("bh", "\u09AD");  // ভ
        englishToBangla.put("sh", "\u09B6");  // শ
        englishToBangla.put("tt", "\u099F");  // ট
        englishToBangla.put("dd", "\u09A1");  // ড
        englishToBangla.put("nn", "\u09A3");  // ণ

        // Common words — Greetings
        englishToBangla.put("salaam", "\u09B8\u09BE\u09B2\u09BE\u09AE");     // সালাম
        englishToBangla.put("salam", "\u09B8\u09BE\u09B2\u09BE\u09AE");      // সালাম
        englishToBangla.put("namaskar", "\u09A8\u09AE\u09B8\u09CD\u0995\u09BE\u09B0"); // নমস্কার
        englishToBangla.put("dhonnobad", "\u09A7\u09A8\u09CD\u09AF\u09AC\u09BE\u09A6"); // ধন্যবাদ
        englishToBangla.put("thanks", "\u09A7\u09A8\u09CD\u09AF\u09AC\u09BE\u09A6");   // ধন্যবাদ
        englishToBangla.put("thank", "\u09A7\u09A8\u09CD\u09AF\u09AC\u09BE\u09A6");    // ধন্যবাদ
        englishToBangla.put("hello", "\u09B9\u09CD\u09AF\u09BE\u09B2\u09CB");  // হ্যালো
        englishToBangla.put("hi", "\u09B9\u09BE\u0987");                       // হাই
        englishToBangla.put("ha", "\u09B9\u09CD\u09AF\u09BE");                // হ্যা
        englishToBangla.put("yes", "\u09B9\u09CD\u09AF\u09BE");               // হ্যা
        englishToBangla.put("na", "\u09A8\u09BE");                             // না
        englishToBangla.put("no", "\u09A8\u09BE");                             // না
        englishToBangla.put("sorry", "\u09A6\u09C1\u0983\u0996\u09BF\u09A4"); // দুঃখিত
        englishToBangla.put("please", "\u0985\u09A8\u09C1\u0997\u09CD\u09B0\u09B9"); // অনুগ্রহ

        // Pronouns
        englishToBangla.put("ami", "\u0986\u09AE\u09BF");      // আমি
        englishToBangla.put("i", "\u0986\u09AE\u09BF");         // আমি
        englishToBangla.put("tumi", "\u09A4\u09C1\u09AE\u09BF"); // তুমি
        englishToBangla.put("you", "\u09A4\u09C1\u09AE\u09BF");  // তুমি
        englishToBangla.put("apni", "\u0986\u09AA\u09A8\u09BF"); // আপনি
        englishToBangla.put("se", "\u09B8\u09C7");               // সে
        englishToBangla.put("he", "\u09B8\u09C7");               // সে
        englishToBangla.put("she", "\u09B8\u09C7");              // সে
        englishToBangla.put("ora", "\u09A4\u09BE\u09B0\u09BE");  // তারা
        englishToBangla.put("they", "\u09A4\u09BE\u09B0\u09BE"); // তারা
        englishToBangla.put("amra", "\u0986\u09AE\u09B0\u09BE"); // আমরা
        englishToBangla.put("we", "\u0986\u09AE\u09B0\u09BE");   // আমরা
        englishToBangla.put("amake", "\u0986\u09AE\u09BE\u0995\u09C7"); // আমাকে
        englishToBangla.put("tomake", "\u09A4\u09CB\u09AE\u09BE\u0995\u09C7"); // তোমাকে
        englishToBangla.put("amader", "\u0986\u09AE\u09BE\u09A6\u09C7\u09B0"); // আমাদের

        // Common verbs
        englishToBangla.put("koro", "\u0995\u09B0\u09CB");      // করো
        englishToBangla.put("korte", "\u0995\u09B0\u09A4\u09C7"); // করতে
        englishToBangla.put("korbo", "\u0995\u09B0\u09AC\u09CB"); // করবো
        englishToBangla.put("kore", "\u0995\u09B0\u09C7");       // করে
        englishToBangla.put("kora", "\u0995\u09B0\u09BE");       // করা
        englishToBangla.put("hoy", "\u09B9\u09AF\u09BC");        // হয়
        englishToBangla.put("hobe", "\u09B9\u09AC\u09C7");       // হবে
        englishToBangla.put("hole", "\u09B9\u09B2\u09C7");       // হলে
        englishToBangla.put("ache", "\u0986\u099B\u09C7");       // আছে
        englishToBangla.put("chilo", "\u099B\u09BF\u09B2");      // ছিল
        englishToBangla.put("jao", "\u09AF\u09BE\u0993");        // যাও
        englishToBangla.put("jai", "\u09AF\u09BE\u0987");        // যাই
        englishToBangla.put("gelam", "\u0997\u09C7\u09B2\u09BE\u09AE"); // গেলাম
        englishToBangla.put("asho", "\u0986\u09B8\u09CB");       // আসো
        englishToBangla.put("khai", "\u0996\u09BE\u0987");       // খাই
        englishToBangla.put("khao", "\u0996\u09BE\u0993");       // খাও
        englishToBangla.put("khabo", "\u0996\u09BE\u09AC\u09CB"); // খাবো
        englishToBangla.put("bolo", "\u09AC\u09B2\u09CB");       // বলো
        englishToBangla.put("boli", "\u09AC\u09B2\u09BF");       // বলি
        englishToBangla.put("bole", "\u09AC\u09B2\u09C7");       // বলে
        englishToBangla.put("dekho", "\u09A6\u09C7\u0996\u09CB"); // দেখো
        englishToBangla.put("dekhi", "\u09A6\u09C7\u0996\u09BF"); // দেখি
        englishToBangla.put("shuno", "\u09B6\u09C1\u09A8\u09CB"); // শুনো
        englishToBangla.put("shuni", "\u09B6\u09C1\u09A8\u09BF"); // শুনি
        englishToBangla.put("likho", "\u09B2\u09C7\u0996\u09CB"); // লেখো
        englishToBangla.put("likhi", "\u09B2\u09C7\u0996\u09BF"); // লেখি
        englishToBangla.put("poro", "\u09AA\u09DC\u09CB");        // পড়ো
        englishToBangla.put("pori", "\u09AA\u09DC\u09BF");        // পড়ি
        englishToBangla.put("bosi", "\u09AC\u09B8\u09BF");        // বসি
        englishToBangla.put("thako", "\u09A5\u09BE\u0995\u09CB"); // থাকো
        englishToBangla.put("thaki", "\u09A5\u09BE\u0995\u09BF"); // থাকি

        // Adjectives
        englishToBangla.put("bhalo", "\u09AD\u09BE\u09B2\u09CB"); // ভালো
        englishToBangla.put("good", "\u09AD\u09BE\u09B2\u09CB");  // ভালো
        englishToBangla.put("valo", "\u09AD\u09BE\u09B2\u09CB");  // ভালো
        englishToBangla.put("bad", "\u0996\u09BE\u09B0\u09BE\u09AA"); // খারাপ
        englishToBangla.put("boro", "\u09AC\u09DC\u09BE");        // বড়
        englishToBangla.put("big", "\u09AC\u09DC\u09BE");         // বড়
        englishToBangla.put("choto", "\u099B\u09CB\u099F");       // ছোট
        englishToBangla.put("small", "\u099B\u09CB\u099F");       // ছোট
        englishToBangla.put("sundor", "\u09B8\u09C1\u09A8\u09CD\u09A6\u09B0"); // সুন্দর
        englishToBangla.put("beautiful", "\u09B8\u09C1\u09A8\u09CD\u09A6\u09B0"); // সুন্দর
        englishToBangla.put("nice", "\u09B8\u09C1\u09A8\u09CD\u09A6\u09B0");   // সুন্দর
        englishToBangla.put("khub", "\u0996\u09C1\u09AC");        // খুব
        englishToBangla.put("very", "\u0996\u09C1\u09AC");        // খুব
        englishToBangla.put("onek", "\u0985\u09A8\u09C7\u0995");  // অনেক
        englishToBangla.put("much", "\u0985\u09A8\u09C7\u0995");  // অনেক
        englishToBangla.put("notun", "\u09A8\u09A4\u09C1\u09A8"); // নতুন
        englishToBangla.put("new", "\u09A8\u09A4\u09C1\u09A8");   // নতুন
        englishToBangla.put("purono", "\u09AA\u09C1\u09B0\u09CB\u09A8\u09CB"); // পুরোনো
        englishToBangla.put("old", "\u09AA\u09C1\u09B0\u09CB\u09A8\u09CB");   // পুরোনো

        // Questions
        englishToBangla.put("ki", "\u0995\u09BF");                // কি
        englishToBangla.put("what", "\u0995\u09BF");              // কি
        englishToBangla.put("keno", "\u0995\u09C7\u09A8");        // কেন
        englishToBangla.put("why", "\u0995\u09C7\u09A8");         // কেন
        englishToBangla.put("kothay", "\u0995\u09CB\u09A5\u09BE\u09AF\u09BC"); // কোথায়
        englishToBangla.put("where", "\u0995\u09CB\u09A5\u09BE\u09AF\u09BC");  // কোথায়
        englishToBangla.put("kokhon", "\u0995\u0996\u09A8");      // কখন
        englishToBangla.put("when", "\u0995\u0996\u09A8");        // কখন
        englishToBangla.put("kemon", "\u0995\u09C7\u09AE\u09A8"); // কেমন
        englishToBangla.put("how", "\u0995\u09C7\u09AE\u09A8");   // কেমন
        englishToBangla.put("ke", "\u0995\u09C7");                // কে
        englishToBangla.put("who", "\u0995\u09C7");               // কে
        englishToBangla.put("kichu", "\u0995\u09BF\u099B\u09C1"); // কিছু
        englishToBangla.put("some", "\u0995\u09BF\u099B\u09C1");  // কিছু

        // People & Family
        englishToBangla.put("baba", "\u09AC\u09BE\u09AC\u09BE");   // বাবা
        englishToBangla.put("father", "\u09AC\u09BE\u09AC\u09BE");  // বাবা
        englishToBangla.put("ma", "\u09AE\u09BE");                  // মা
        englishToBangla.put("mother", "\u09AE\u09BE");              // মা
        englishToBangla.put("bhai", "\u09AD\u09BE\u0987");          // ভাই
        englishToBangla.put("brother", "\u09AD\u09BE\u0987");       // ভাই
        englishToBangla.put("bon", "\u09AC\u09CB\u09A8");           // বোন
        englishToBangla.put("sister", "\u09AC\u09CB\u09A8");        // বোন
        englishToBangla.put("chele", "\u099B\u09C7\u09B2\u09C7");  // ছেলে
        englishToBangla.put("boy", "\u099B\u09C7\u09B2\u09C7");    // ছেলে
        englishToBangla.put("meye", "\u09AE\u09C7\u09AF\u09BC\u09C7"); // মেয়ে
        englishToBangla.put("girl", "\u09AE\u09C7\u09AF\u09BC\u09C7");  // মেয়ে
        englishToBangla.put("manush", "\u09AE\u09BE\u09A8\u09C1\u09B7"); // মানুষ
        englishToBangla.put("human", "\u09AE\u09BE\u09A8\u09C1\u09B7");  // মানুষ
        englishToBangla.put("bondhu", "\u09AC\u09A8\u09CD\u09A7\u09C1"); // বন্ধু
        englishToBangla.put("friend", "\u09AC\u09A8\u09CD\u09A7\u09C1"); // বন্ধু

        // Places
        englishToBangla.put("bari", "\u09AC\u09BE\u09DC\u09BF");          // বাড়ি
        englishToBangla.put("home", "\u09AC\u09BE\u09DC\u09BF");           // বাড়ি
        englishToBangla.put("school", "\u09B8\u09CD\u0995\u09C1\u09B2");   // স্কুল
        englishToBangla.put("college", "\u0995\u09B2\u09C7\u099C");        // কলেজ
        englishToBangla.put("office", "\u0985\u09AB\u09BF\u09B8");         // অফিস
        englishToBangla.put("bazar", "\u09AC\u09BE\u099C\u09BE\u09B0");   // বাজার
        englishToBangla.put("market", "\u09AC\u09BE\u099C\u09BE\u09B0");  // বাজার
        englishToBangla.put("hospital", "\u09B9\u09BE\u09B8\u09AA\u09BE\u09A4\u09BE\u09B2"); // হাসপাতাল

        // Food & Drink
        englishToBangla.put("pani", "\u09AA\u09BE\u09A8\u09BF");    // পানি
        englishToBangla.put("water", "\u09AA\u09BE\u09A8\u09BF");   // পানি
        englishToBangla.put("cha", "\u099A\u09BE");                  // চা
        englishToBangla.put("tea", "\u099A\u09BE");                  // চা
        englishToBangla.put("coffee", "\u0995\u09AB\u09BF");         // কফি
        englishToBangla.put("bhat", "\u09AD\u09BE\u09A4");           // ভাত
        englishToBangla.put("rice", "\u09AD\u09BE\u09A4");           // ভাত
        englishToBangla.put("mach", "\u09AE\u09BE\u099B");           // মাছ
        englishToBangla.put("fish", "\u09AE\u09BE\u099B");           // মাছ
        englishToBangla.put("mangsho", "\u09AE\u09BE\u0982\u09B8");  // মাংস
        englishToBangla.put("meat", "\u09AE\u09BE\u0982\u09B8");     // মাংস
        englishToBangla.put("torkari", "\u09A4\u09B0\u0995\u09BE\u09B0\u09BF"); // তরকারি
        englishToBangla.put("dal", "\u09A1\u09BE\u09B2");            // ডাল
        englishToBangla.put("khabar", "\u0996\u09BE\u09AC\u09BE\u09B0"); // খাবার
        englishToBangla.put("food", "\u0996\u09BE\u09AC\u09BE\u09B0");   // খাবার
        englishToBangla.put("dim", "\u09A1\u09BF\u09AE");            // ডিম
        englishToBangla.put("egg", "\u09A1\u09BF\u09AE");            // ডিম
        englishToBangla.put("roti", "\u09B0\u09C1\u099F\u09BF");    // রুটি
        englishToBangla.put("bread", "\u09B0\u09C1\u099F\u09BF");   // রুটি
        englishToBangla.put("aam", "\u0986\u09AE");                  // আম
        englishToBangla.put("mango", "\u0986\u09AE");                // আম
        englishToBangla.put("kola", "\u0995\u09B2\u09BE");           // কলা
        englishToBangla.put("banana", "\u0995\u09B2\u09BE");         // কলা

        // Objects & Time
        englishToBangla.put("boi", "\u09AC\u09CB\u0987");           // বই
        englishToBangla.put("book", "\u09AC\u09CB\u0987");          // বই
        englishToBangla.put("phone", "\u09AB\u09CB\u09A8");         // ফোন
        englishToBangla.put("gaan", "\u0997\u09BE\u09A8");          // গান
        englishToBangla.put("song", "\u0997\u09BE\u09A8");          // গান
        englishToBangla.put("kotha", "\u0995\u09A5\u09BE");         // কথা
        englishToBangla.put("word", "\u0995\u09A5\u09BE");          // কথা
        englishToBangla.put("shomoy", "\u09B8\u09AE\u09AF\u09BC");  // সময়
        englishToBangla.put("time", "\u09B8\u09AE\u09AF\u09BC");    // সময়
        englishToBangla.put("din", "\u09A6\u09BF\u09A8");           // দিন
        englishToBangla.put("day", "\u09A6\u09BF\u09A8");           // দিন
        englishToBangla.put("raat", "\u09B0\u09BE\u09A4");          // রাত
        englishToBangla.put("night", "\u09B0\u09BE\u09A4");         // রাত
        englishToBangla.put("shokal", "\u09B8\u0995\u09BE\u09B2");  // সকাল
        englishToBangla.put("morning", "\u09B8\u0995\u09BE\u09B2"); // সকাল
        englishToBangla.put("ghum", "\u0998\u09C1\u09AE");          // ঘুম
        englishToBangla.put("sleep", "\u0998\u09C1\u09AE");         // ঘুম
        englishToBangla.put("kaaj", "\u0995\u09BE\u099C");          // কাজ
        englishToBangla.put("work", "\u0995\u09BE\u099C");          // কাজ
        englishToBangla.put("taka", "\u099F\u09BE\u0995\u09BE");   // টাকা
        englishToBangla.put("money", "\u099F\u09BE\u0995\u09BE");  // টাকা

        // Feelings
        englishToBangla.put("kemon", "\u0995\u09C7\u09AE\u09A8");     // কেমন
        englishToBangla.put("kharap", "\u0996\u09BE\u09B0\u09BE\u09AA"); // খারাপ
        englishToBangla.put("khushi", "\u0996\u09C1\u09B6\u09BF");    // খুশি
        englishToBangla.put("happy", "\u0996\u09C1\u09B6\u09BF");     // খুশি
        englishToBangla.put("dukkhi", "\u09A6\u09C1\u0983\u0996\u09C0"); // দুঃখী
        englishToBangla.put("sad", "\u09A6\u09C1\u0983\u0996\u09C0");    // দুঃখী
        englishToBangla.put("bhalobasha", "\u09AD\u09BE\u09B2\u09CB\u09AC\u09BE\u09B8\u09BE"); // ভালোবাসা
        englishToBangla.put("love", "\u09AD\u09BE\u09B2\u09CB\u09AC\u09BE\u09B8\u09BE");      // ভালোবাসা
        englishToBangla.put("jibon", "\u099C\u09C0\u09AC\u09A8");    // জীবন
        englishToBangla.put("life", "\u099C\u09C0\u09AC\u09A8");     // জীবন
        englishToBangla.put("shanti", "\u09B6\u09BE\u09A8\u09CD\u09A4\u09BF"); // শান্তি
        englishToBangla.put("peace", "\u09B6\u09BE\u09A8\u09CD\u09A4\u09BF");  // শান্তি

        // Actions
        englishToBangla.put("jao", "\u09AF\u09BE\u0993");           // যাও
        englishToBangla.put("go", "\u09AF\u09BE\u0993");             // যাও
        englishToBangla.put("asho", "\u0986\u09B8\u09CB");           // আসো
        englishToBangla.put("come", "\u0986\u09B8\u09CB");           // আসো
        englishToBangla.put("cholo", "\u099A\u09B2\u09CB");          // চলো
        englishToBangla.put("letsgo", "\u099A\u09B2\u09CB");         // চলো
        englishToBangla.put("bolo", "\u09AC\u09B2\u09CB");           // বলো
        englishToBangla.put("say", "\u09AC\u09B2\u09CB");            // বলো
        englishToBangla.put("shono", "\u09B6\u09CB\u09A8\u09CB");    // শোনো
        englishToBangla.put("listen", "\u09B6\u09CB\u09A8\u09CB");   // শোনো
        englishToBangla.put("dekho", "\u09A6\u09C7\u0996\u09CB");    // দেখো
        englishToBangla.put("see", "\u09A6\u09C7\u0996\u09CB");      // দেখো
        englishToBangla.put("help", "\u09B8\u09BE\u09B9\u09BE\u09AF\u09CD\u09AF"); // সাহায্য
        englishToBangla.put("koro", "\u0995\u09B0\u09CB");           // করো
        englishToBangla.put("do", "\u0995\u09B0\u09CB");             // করো

        // Numbers & Common
        englishToBangla.put("ekhon", "\u098F\u0996\u09A8");          // এখন
        englishToBangla.put("now", "\u098F\u0996\u09A8");            // এখন
        englishToBangla.put("pore", "\u09AA\u09B0\u09C7");           // পরে
        englishToBangla.put("later", "\u09AA\u09B0\u09C7");          // পরে
        englishToBangla.put("age", "\u0986\u0997\u09C7");            // আগে
        englishToBangla.put("before", "\u0986\u0997\u09C7");         // আগে
        englishToBangla.put("sob", "\u09B8\u09AC");                  // সব
        englishToBangla.put("all", "\u09B8\u09AC");                  // সব
        englishToBangla.put("ek", "\u098F\u0995");                   // এক
        englishToBangla.put("one", "\u098F\u0995");                  // এক
        englishToBangla.put("dui", "\u09A6\u09C1\u0987");            // দুই
        englishToBangla.put("two", "\u09A6\u09C1\u0987");            // দুই
        englishToBangla.put("tin", "\u09A4\u09BF\u09A8");            // তিন
        englishToBangla.put("three", "\u09A4\u09BF\u09A8");          // তিন

        // Technology
        englishToBangla.put("internet", "\u0987\u09A8\u09CD\u099F\u09BE\u09B0\u09A8\u09C7\u099F"); // ইন্টারনেট
        englishToBangla.put("computer", "\u0995\u09AE\u09CD\u09AA\u09BF\u0989\u099F\u09BE\u09B0");  // কম্পিউটার
        englishToBangla.put("mobile", "\u09AE\u09CB\u09AC\u09BE\u0987\u09B2");  // মোবাইল
        englishToBangla.put("video", "\u09AD\u09BF\u09A1\u09BF\u0993");         // ভিডিও
        englishToBangla.put("message", "\u09AE\u09C7\u09B8\u09C7\u099C");       // মেসেজ
        englishToBangla.put("call", "\u0995\u09B2");                            // কল
    }

    public static String banglaToEnglish(String banglaText) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < banglaText.length()) {
            // Try 2-char match first (for combining characters), then 1-char
            boolean matched = false;
            if (i + 1 < banglaText.length()) {
                String two = banglaText.substring(i, i + 2);
                if (banglaToEnglish.containsKey(two)) {
                    result.append(banglaToEnglish.get(two));
                    i += 2;
                    matched = true;
                }
            }
            if (!matched) {
                String one = banglaText.substring(i, i + 1);
                result.append(banglaToEnglish.getOrDefault(one, one));
                i++;
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
