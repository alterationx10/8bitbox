package com.markslaboratory.eightbitbox;


public class BoxConstants {

    // These are commands sent to the Arduino
    static byte[] PLAY_OVERWORLD    = "A".getBytes();
    static byte[] PLAY_UNDERWORLD   = "B".getBytes();
    static byte[] RED_ON            = "C".getBytes();
    static byte[] GREEN_ON          = "D".getBytes();
    static byte[] BLUE_ON           = "E".getBytes();
    static byte[] ALL_ON            = "F".getBytes();
    static byte[] ALL_OFF           = "G".getBytes();


    static byte[] JODY_NFC = {0x72, 0x02, 0x27, (byte) 0xca};
    static String JODY_MAC = "00:18:96:B0:06:DB";

    static byte[] MARC_NFC = {0x62, 0x74, 0x26, (byte) 0xca};
    static String MARC_MAC = "00:18:96:B0:07:82";

    static byte[] MARK_NFC = {0x02, 0x4e, 0x25, (byte) 0xca};
    static String MARK_MAC = "00:18:96:B0:07:66";
}
