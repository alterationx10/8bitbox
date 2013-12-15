package com.markslaboratory.eightbitbox;


import android.os.AsyncTask;
import android.util.Log;

public class MarioMusic {

    private BluetoothService myBox;

    public MarioMusic(BluetoothService myBox) {
        this.myBox = myBox;
    }

    int underworld[] = {
            Tone.NOTE_C4, Tone.NOTE_C5, Tone.NOTE_A3, Tone.NOTE_A4,
            Tone.NOTE_AS3, Tone.NOTE_AS4, 0,
            0,
            Tone.NOTE_C4, Tone.NOTE_C5, Tone.NOTE_A3, Tone.NOTE_A4,
            Tone.NOTE_AS3, Tone.NOTE_AS4, 0,
            0,
            Tone.NOTE_F3, Tone.NOTE_F4, Tone.NOTE_D3, Tone.NOTE_D4,
            Tone.NOTE_DS3, Tone.NOTE_DS4, 0,
            0,
            Tone.NOTE_F3, Tone.NOTE_F4, Tone.NOTE_D3, Tone.NOTE_D4,
            Tone.NOTE_DS3, Tone.NOTE_DS4, 0,
            0, Tone.NOTE_DS4, Tone.NOTE_CS4, Tone.NOTE_D4,
            Tone.NOTE_CS4, Tone.NOTE_DS4,
            Tone.NOTE_DS4, Tone.NOTE_GS3,
            Tone.NOTE_G3, Tone.NOTE_CS4,
            Tone.NOTE_C4, Tone.NOTE_FS4, Tone.NOTE_F4, Tone.NOTE_E3, Tone.NOTE_AS4, Tone.NOTE_A4,
            Tone.NOTE_GS4, Tone.NOTE_DS4, Tone.NOTE_B3,
            Tone.NOTE_AS3, Tone.NOTE_A3, Tone.NOTE_GS3,
            0, 0, 0
    };

    int underworld_tempo[] = {
            12, 12, 12, 12,
            12, 12, 6,
            3,
            12, 12, 12, 12,
            12, 12, 6,
            3,
            12, 12, 12, 12,
            12, 12, 6,
            3,
            12, 12, 12, 12,
            12, 12, 6,
            6, 18, 18, 18,
            6, 6,
            6, 6,
            6, 6,
            18, 18, 18,18, 18, 18,
            10, 10, 10,
            10, 10, 10,
            3, 3, 3
    };


    public void playUnderworld() {
        Runnable playUnderworldRunnable = new Runnable() {
            @Override
            public void run() {
                for (int i=0; i < underworld.length; i++) {
                    byte[] note = {"Z".getBytes()[0], Tone.toneMSB(underworld[i]), Tone.toneLSB(underworld[i]),
                            (byte) underworld_tempo[i]};
                    myBox.writeData(note);
                    // We need a small delay so we don't overload the Arduino (it will skip notes)
                    // 200 ms seems to be a good number.
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        myBox.commService.execute(new Runnable() {
            @Override
            public void run() {
                for (int i=0; i < underworld.length; i++) {
                    byte[] note = {"Z".getBytes()[0], Tone.toneMSB(underworld[i]), Tone.toneLSB(underworld[i]),
                            (byte) underworld_tempo[i]};
                    myBox.writeData(note);
                    // We need a small delay so we don't overload the Arduino (it will skip notes)
                    // 200 ms seems to be a good number.
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }



}
