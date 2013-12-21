package com.markslaboratory.eightbitbox;


import android.app.Activity;

/**
 * A Class that plays Mario songs/sounds over the piezo buzzer
 *
 * The overworld and underworld notes/tempo were adapted from the arduino sketch
 * provided by Dipto Pratyaksa at
 * http://www.linuxcircle.com/2013/03/31/playing-mario-bros-tune-with-arduino-and-piezo-buzzer/
 */
public class MarioMusic {

    private BluetoothService myBox;

    public MarioMusic(BluetoothService myBox) {
        this.myBox = myBox;
    }

    private int overworld[] = {
            Tone.NOTE_E7, Tone.NOTE_E7, 0, Tone.NOTE_E7,
            0, Tone.NOTE_C7, Tone.NOTE_E7, 0,
            Tone.NOTE_G7, 0, 0,  0,
            Tone.NOTE_G6, 0, 0, 0,

            Tone.NOTE_C7, 0, 0, Tone.NOTE_G6,
            0, 0, Tone.NOTE_E6, 0,
            0, Tone.NOTE_A6, 0, Tone.NOTE_B6,
            0, Tone.NOTE_AS6, Tone.NOTE_A6, 0,

            Tone.NOTE_G6, Tone.NOTE_E7, Tone.NOTE_G7,
            Tone.NOTE_A7, 0, Tone.NOTE_F7, Tone.NOTE_G7,
            0, Tone.NOTE_E7, 0, Tone.NOTE_C7,
            Tone.NOTE_D7, Tone.NOTE_B6, 0, 0,

            Tone.NOTE_C7, 0, 0, Tone.NOTE_G6,
            0, 0, Tone.NOTE_E6, 0,
            0, Tone.NOTE_A6, 0, Tone.NOTE_B6,
            0, Tone.NOTE_AS6, Tone.NOTE_A6, 0,

            Tone.NOTE_G6, Tone.NOTE_E7, Tone.NOTE_G7,
            Tone.NOTE_A7, 0, Tone.NOTE_F7, Tone.NOTE_G7,
            0, Tone.NOTE_E7, 0, Tone.NOTE_C7,
            Tone.NOTE_D7, Tone.NOTE_B6, 0, 0
    };

    private int overworld_tempo[] = {
            12, 12, 12, 12,
            12, 12, 12, 12,
            12, 12, 12, 12,
            12, 12, 12, 12,

            12, 12, 12, 12,
            12, 12, 12, 12,
            12, 12, 12, 12,
            12, 12, 12, 12,

            9, 9, 9,
            12, 12, 12, 12,
            12, 12, 12, 12,
            12, 12, 12, 12,

            12, 12, 12, 12,
            12, 12, 12, 12,
            12, 12, 12, 12,
            12, 12, 12, 12,

            9, 9, 9,
            12, 12, 12, 12,
            12, 12, 12, 12,
            12, 12, 12, 12,
    };

    private int underworld[] = {
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

    private int underworld_tempo[] = {
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


    public void playUnderworld(final Activity runningActivity) {

        myBox.commService.execute(new Runnable() {
            @Override
            public void run() {
                runningActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runningActivity.getActionBar().setIcon(R.drawable.music);
                    }
                });
                for (int i=0; i < underworld.length; i++) {
                    byte[] note = {BoxConstants.MUSIC[0], Tone.toneMSB(underworld[i]), Tone.toneLSB(underworld[i]),
                            (byte) underworld_tempo[i]};
                    myBox.writeData(note);
                    // For everything but the last note
                    if (i != underworld.length -1) {
                        while(myBox.rawRead() != BoxConstants.MUSIC_RESPONSE[0]) {
                            // Block until we get feedback that we're ready for the next note
                        }
                    }

                }
                runningActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (myBox.isConnected()) {
                            runningActivity.getActionBar().setIcon(R.drawable.connected);
                        }
                        else {
                            runningActivity.getActionBar().setIcon(R.drawable.not_connected);
                        }                    }
                });

            }
        });
    }


    public void playOverworld(final Activity runningActivity) {

        myBox.commService.execute(new Runnable() {
            @Override
            public void run() {
                runningActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runningActivity.getActionBar().setIcon(R.drawable.music);
                    }
                });
                for (int i=0; i < overworld.length; i++) {
                    byte[] note = {BoxConstants.MUSIC[0], Tone.toneMSB(overworld[i]), Tone.toneLSB(overworld[i]),
                            (byte) overworld_tempo[i]};
                    myBox.writeData(note);

                    // For everything but the last note
                    if (i != overworld.length -1) {

                        while(myBox.rawRead() != BoxConstants.MUSIC_RESPONSE[0]) {
                            // Block until we get feedback that we're ready for the next note
                        }
                    }
                }
                runningActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (myBox.isConnected()) {
                            runningActivity.getActionBar().setIcon(R.drawable.connected);
                        }
                        else {
                            runningActivity.getActionBar().setIcon(R.drawable.not_connected);
                        }                    }
                });
            }
        });
    }

}
