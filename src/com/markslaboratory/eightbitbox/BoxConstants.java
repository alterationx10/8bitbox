package com.markslaboratory.eightbitbox;


import java.util.Arrays;

public class BoxConstants {

    // These are commands sent to the Arduino
    static byte[] RED = "R".getBytes();
    static byte[] GREEN = "G".getBytes();
    static byte[] BLUE = "B".getBytes();
    static byte[] MUSIC = "Z".getBytes();
    static byte[] MUSIC_RESPONSE = "z".getBytes();


    // Settings constants for 8BitBixes I've made for people
    static byte[] JODY_NFC = {0x72, 0x02, 0x27, (byte) 0xca};
    static String JODY_NFC_STRING = Arrays.toString(JODY_NFC);
    static String JODY_MAC = "00:18:96:B0:06:DB";

    static byte[] MARC_NFC = {0x62, 0x74, 0x26, (byte) 0xca};
    static String MARC_NFC_STRING = Arrays.toString(MARC_NFC);
    static String MARC_MAC = "00:18:96:B0:07:82";

    static byte[] MARK_NFC = {0x02, 0x4e, 0x25, (byte) 0xca};
    static String MARK_NFC_STRING = Arrays.toString(MARK_NFC);
    static String MARK_MAC = "00:18:96:B0:07:66";

    // Shared Preferences Strings
    static String STORED_NFC = "STORED_NFC";
    static String STORED_MAC = "STORED_MAC";
    static String NO_NFC = "NO_NFC";
    static String HAS_FAVORITE = "HAS_FAVORITE";
    static String FAVORITE_RED = "FAVORITE_RED";
    static String FAVORITE_GREEN = "FAVORITE_GREEN";
    static String FAVORITE_BLUE = "FAVORITE_BLUE";
}
