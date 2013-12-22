package com.markslaboratory.eightbitbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.*;

import java.util.Arrays;

public class MainActivity extends Activity {

    ImageView bitBox;

    ImageButton start;
    ImageButton select;
    ImageButton b;
    ImageButton a;

    ImageButton up;
    ImageButton down;
    ImageButton left;
    ImageButton right;

    SeekBar redSeeker;
    SeekBar greenSeeker;
    SeekBar blueSeekr;

    String TAG = "8BitActivity";

    // Our bluetooth service
    BluetoothService myService;
    ServiceConnection mConnection;
    boolean isBound;

    SharedPreferences myPreferences;


    MarioMusic marioMusic;

    String MAC;

    NfcAdapter nfcAdapter;


    boolean code[] = new boolean[7];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter!= null && !nfcAdapter.isEnabled()) {
            genericDialog("NFC isn't enabled... \n You might want to turn it on...", R.drawable.not_connected);

        }
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            genericDialog("Bluetooth isn't enabled... \n You might want to turn it on...", R.drawable.not_connected);
        }

        // We need to bind to our service, so lets go ahead and do that
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                isBound = true;
                marioMusic = new MarioMusic(myService);

                getActionBar().setTitle(getTitle() + " | P1 Press Start");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBound = false;
                genericDialog("Service disconnected!", R.drawable.ic_launcher);
            }
        };

        myService = new BluetoothService();
        isBound = false;
        // Don't forget we need to decalre the Service (and Bluetooth permission) in the manifest file.
        Intent myIntent = new Intent(getApplicationContext(), BluetoothService.class);
        bindService(myIntent, mConnection, Context.BIND_AUTO_CREATE);
        // Our service should bind now; We will see the dialogs above whe it's all ready;


        start = (ImageButton)findViewById(R.id.btnStart);
        select = (ImageButton)findViewById(R.id.btnSelect);
        b = (ImageButton)findViewById(R.id.btnB);
        a = (ImageButton)findViewById(R.id.btnA);

        up = (ImageButton)findViewById(R.id.dUp);
        down = (ImageButton)findViewById(R.id.dDown);
        left = (ImageButton)findViewById(R.id.dLeft);
        right = (ImageButton)findViewById(R.id.dRight);

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeSequence(0);
            }
        });

        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeSequence(1);
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeSequence(2);
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeSequence(3);
            }
        });

        bitBox = (ImageView)findViewById(R.id.ivBox);
        bitBox.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SharedPreferences.Editor editor = myPreferences.edit();
                editor.clear();
                editor.commit();
                Intent setup = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(setup);
                return false;
            }
        });
        bitBox.setBackgroundColor(Color.BLUE);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (codeSequence(6)) {
                    resetCode();
                    codeValidated();
                    return;
                }
                myService.btConnect(MAC, new BluetoothService.BluetoothConnectCallback() {
                    @Override
                    public void doOnConnect() {
                        getActionBar().setIcon(R.drawable.connected);
                        getActionBar().setTitle(getTitle() + " Test");
                        genericDialog("Connected!", R.drawable.connected);
                        setSeekerStatus(true);
                        setBitBoxLEDs();
                    }

                    @Override
                    public void doOnConnectionFailed() {
                        genericDialog("Connection Failed!", R.drawable.not_connected);
//                        setSeekerStatus(false);
                    }

                    @Override
                    public void doOnDisconnect() {
                    }
                });
            }
        });

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myService.btDisconnect(new BluetoothService.BluetoothConnectCallback() {
                    @Override
                    public void doOnConnect() {

                    }

                    @Override
                    public void doOnConnectionFailed() {

                    }

                    @Override
                    public void doOnDisconnect() {
                        getActionBar().setIcon(R.drawable.not_connected);
                        genericDialog("Disconnected!", R.drawable.not_connected);
//                        setSeekerStatus(false);
                    }
                });
            }
        });

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (codeSequence(4)) {
                    return;
                }
                if (myService.isConnected()) {
                    marioMusic.playOverworld(MainActivity.this);
                }
            }
        });

        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (codeSequence(5)) {
                    return;
                }
                if (myService.isConnected()) {
                    marioMusic.playUnderworld(MainActivity.this);
                }
            }
        });



        redSeeker = (SeekBar)findViewById(R.id.seekRed);
        redSeeker.setMax(255);
        redSeeker.setProgress(0);
        redSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                byte[]setLevel = {BoxConstants.RED[0], (byte) progress};
                myService.writeData(setLevel);
                setBitBoxColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                setBitBoxLEDs();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        greenSeeker = (SeekBar)findViewById(R.id.seekGreen);
        greenSeeker.setMax(255);
        greenSeeker.setProgress(0);
        greenSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                byte[]setLevel = {BoxConstants.GREEN[0], (byte) progress};
                myService.writeData(setLevel);
                setBitBoxColor();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                setBitBoxLEDs();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        blueSeekr = (SeekBar)findViewById(R.id.seekBlue);
        blueSeekr.setMax(255);
        blueSeekr.setProgress(0);
        blueSeekr.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                byte[]setLevel = {BoxConstants.BLUE[0], (byte) progress};
                myService.writeData(setLevel);
                setBitBoxColor();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                setBitBoxLEDs();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        setSeekerStatus(true);

        setBitBoxColor();

    }


    @Override
    protected void onResume() {
        super.onResume();

        // Listen for NFC events
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] filters = new IntentFilter[] { tagDetected };

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, null);

        // We always need a MAC
        MAC = myPreferences.getString(BoxConstants.STORED_MAC, "");
        if (MAC.equals("")) {
            Intent setup = new Intent(this, SetupActivity.class);
            startActivity(setup);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop listening
        nfcAdapter.disableForegroundDispatch(this);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myService.isConnected()) {
            myService.btDisconnect(new BluetoothService.BluetoothConnectCallback() {
                @Override
                public void doOnConnect() {

                }

                @Override
                public void doOnConnectionFailed() {

                }

                @Override
                public void doOnDisconnect() {

                }
            });
        }
        if (isBound) {
            unbindService(mConnection);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag.getId() == null) {
            Toast.makeText(this,"Null ID for NFC tag!", Toast.LENGTH_SHORT).show();
            return;
        }
        String tagString = Arrays.toString(tag.getId());
        String nfcString = myPreferences.getString(BoxConstants.STORED_NFC, BoxConstants.NO_NFC);
        if (tagString.equals(nfcString)) {
            Toast.makeText(this,"Setting favorite color!", Toast.LENGTH_SHORT).show();
            FavoriteHelper favoriteHelper = new FavoriteHelper();
            favoriteHelper.execute();
        }
        else {
            Toast.makeText(this,"Unrecognized NFC tag!", Toast.LENGTH_SHORT).show();
        }
    }

    public void genericDialog(String msg, int iconDrawable) {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("A Wild Message Appears!");
        builder.setIcon(iconDrawable);
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    /*
    Used to set all 3 LEDS at once
     */
    public void setBitBoxLEDs() {
        int red = redSeeker.getProgress();
        int green = greenSeeker.getProgress();
        int blue = blueSeekr.getProgress();

        byte[]setRedLevel = {BoxConstants.RED[0], (byte) red};
        byte[]setGreenLevel = {BoxConstants.GREEN[0], (byte) green};
        byte[]setBlueLevel = {BoxConstants.BLUE[0], (byte) blue};
        myService.writeData(setRedLevel);
        myService.writeData(setGreenLevel);
        myService.writeData(setBlueLevel);

    }

    public void setBitBoxColor() {
        int red = redSeeker.getProgress();
        int green = greenSeeker.getProgress();
        int blue = blueSeekr.getProgress();

        int bgColor = Color.argb(255, red, green, blue);

        bitBox.setBackgroundColor(bgColor);

    }

    public void setSeekerStatus(boolean status) {
        redSeeker.setEnabled(status);
        greenSeeker.setEnabled(status);
        blueSeekr.setEnabled(status);
    }


    boolean codeSequence(int btn) {
        // Check for out of range
        // Should only be given 0-6 (code.length should be 7)
        if (btn > code.length -1 || btn < 0) {
            resetCode();
            return false;
        }

        if (btn == 0) {
            resetCode();
            code[0] = true;
            return true;
        }

        // This will fire if the previous value is true
        // And reset the whole array otherwise
        if (code[btn - 1]) {
            code[btn] = true;
            return true;
        }
        else {
            resetCode();
            return false;
        }
    }

    void codeValidated() {

        SharedPreferences.Editor editor = myPreferences.edit();
        editor.putBoolean(BoxConstants.HAS_FAVORITE, true);
        editor.putInt(BoxConstants.FAVORITE_RED, redSeeker.getProgress());
        editor.putInt(BoxConstants.FAVORITE_GREEN, greenSeeker.getProgress());
        editor.putInt(BoxConstants.FAVORITE_BLUE, blueSeekr.getProgress());
        editor.commit();
        String msg = "";
        msg += "You've entered the secret code!\n\n";
        msg += "I have saved the current LED settings as your new favorite color!\n\n";
        msg += "Now whenever you tap the NFC part of your 8BitBox, I will set the LEDs to your favorite color.\n\n";
        msg += "If you're not connected, I'll even connect, change the color, and then disconnect again. What a " +
                "deal!\n\n";
        msg += "Enter the code again any time to set a new favorite color!";
        genericDialog(msg, R.drawable.ic_launcher);
    }

    void resetCode() {
        for (int i = 0; i < code.length; i++) {
            code[i] = false;
        }
    }

    class FavoriteHelper extends AsyncTask<Void, Void, Void> {

        int red;
        int green;
        int blue;
        int bgColor;
        boolean hasResponded;
        boolean didConnect;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            red = myPreferences.getInt(BoxConstants.FAVORITE_RED, 0);
            green = myPreferences.getInt(BoxConstants.FAVORITE_GREEN, 0);
            blue = myPreferences.getInt(BoxConstants.FAVORITE_BLUE, 0);
            bgColor = Color.argb(255, red, green, blue);

            bitBox.setBackgroundColor(bgColor);
            redSeeker.setProgress(red);
            greenSeeker.setProgress(green);
            blueSeekr.setProgress(blue);

            hasResponded = false;
            didConnect = false;

        }

        @Override
        protected Void doInBackground(Void... params) {

            if (myService.isConnected()) {
                didConnect = true;
                restoreFavorite();
            }
            else {

                BluetoothService.BluetoothConnectCallback myCallback = new BluetoothService.BluetoothConnectCallback() {
                    @Override
                    public void doOnConnect() {
                        restoreFavorite();
                        didConnect = true;
                        myService.btDisconnect(this);
                    }

                    @Override
                    public void doOnConnectionFailed() {

                    }

                    @Override
                    public void doOnDisconnect() {
                        hasResponded = true;
                    }
                };

                myService.btConnect(MAC, myCallback);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!didConnect) {
                genericDialog("Didn't connect!", R.drawable.not_connected);
            }
        }

        void restoreFavorite() {
            byte[]setRedLevel = {BoxConstants.RED[0], (byte) red};
            byte[]setGreenLevel = {BoxConstants.GREEN[0], (byte) green};
            byte[]setBlueLevel = {BoxConstants.BLUE[0], (byte) blue};
            myService.writeData(setRedLevel);
            myService.writeData(setGreenLevel);
            myService.writeData(setBlueLevel);
        }
    }
}

