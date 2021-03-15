package com.rteenagers.parrot;

// This class just makes a dictionary for the ban values. It is its own class for the sake of cleanliness
// Note: the last point on both hashes are missing because they are special cases

import java.util.Dictionary;
import java.util.Hashtable;

public class Utils {
    public static Dictionary<Integer, String> banValues = new Hashtable<>();
    public static Dictionary<Integer, String> muteValues = new Hashtable<>();
    public static Dictionary<String, Integer> decayValues = new Hashtable<>();

    public static void createHashes() {
        banValues.put(1, "1d");
        banValues.put(2, "2d");
        banValues.put(3, "3d");
        banValues.put(4, "4d");
        banValues.put(5, "5d");
        banValues.put(6, "6d");
        banValues.put(7, "7d");
        banValues.put(8, "10d");
        banValues.put(9, "14d");

        muteValues.put(1, "15m");
        muteValues.put(2, "30m");
        muteValues.put(3, "45m");
        muteValues.put(4, "60m");
        muteValues.put(5, "1d");
        muteValues.put(6, "3d");
        muteValues.put(7, "5d");

        // Int is the amount of days required to decay
        decayValues.put("bans", 60);
        decayValues.put("mutes", 30);
    }

    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }
}
