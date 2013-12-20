package com.markslaboratory.eightbitbox;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.*;

import java.util.Arrays;
import java.util.Set;

public class SetupActivity extends Activity {

    String TAG = "8BitSetup";

    BluetoothService myService;
    ServiceConnection mConnection;
    SharedPreferences myPreferences;
    SharedPreferences.Editor myEditor;
    boolean isBound;


    NfcAdapter nfcAdapter;


    Dialog initialDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        myEditor = myPreferences.edit();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // We need to bind to our service, so lets go ahead and do that
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                isBound = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
                builder.setTitle("Set up!");
                builder.setIcon(R.drawable.ic_launcher);
                String msg =
                        "Let's set up your 8BitBox!\n" +
                                "(You will need to have both Bluetooth and NFC turned on)\n\n" +
                                "Please tap your phone next to the NFC enabled spot of your 8BitBox.";
                builder.setMessage(msg);
                builder.setPositiveButton("My 8BitBox doesn't have an NFC Tag", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setupWithoutNFC();
                    }
                });
                initialDialog = builder.create();
                initialDialog.setCanceledOnTouchOutside(false);
                initialDialog.show();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBound = false;
            }
        };


        myService = new BluetoothService();
        isBound = false;
        Intent myIntent = new Intent(getApplicationContext(), BluetoothService.class);
        bindService(myIntent, mConnection, Context.BIND_AUTO_CREATE);

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(mConnection);
        }
    }

    public void genericDialog(String title, String msg) {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
        builder.setTitle(title);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
            }
        });
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (tag.getId() != null) {
            setupWithNFC(tag.getId());
        }
        else {
            Toast.makeText(this,"Tag ID not found! Try again.",Toast.LENGTH_LONG).show();
        }

    }

    void setupWithNFC(byte[] tag) {
        String tagName = Arrays.toString(tag);
        // Store our NFC tag ID
        myEditor.putString(BoxConstants.STORED_NFC, tagName);
        myEditor.commit();
        // Does it match anyone?
        if (tagName.equals(BoxConstants.JODY_NFC_STRING)) {
            setupJody();
            initialDialog.dismiss();
        }
        else if (tagName.equals(BoxConstants.MARC_NFC_STRING)) {
            setupMarc();
            initialDialog.dismiss();
        }
        else if (tagName.equals(BoxConstants.MARK_NFC_STRING)) {
            setupMark();
            initialDialog.dismiss();
        }
        else {
            setupGuest();
            initialDialog.dismiss();
        }

    }

    void setupWithoutNFC() {
        // We assume no NFC functionality, so we show a list of paired BT devices.
        myEditor.putString(BoxConstants.STORED_NFC, BoxConstants.NO_NFC);
        myEditor.commit();
        setupGuest();

    }


    void setupJody() {
        // Store our known mac addresses
        myEditor.putString(BoxConstants.STORED_MAC, BoxConstants.JODY_MAC);
        myEditor.commit();
        String msg =
                "Your 8BitBox is all set up!\n\n" +
                        "If you get hungry, eat something!";
        setupFinishedDialog("Hey Jody!", msg);

    }

    void setupMarc() {
        // Store our known mac addresses
        myEditor.putString(BoxConstants.STORED_MAC, BoxConstants.MARC_MAC);
        myEditor.commit();
        String msg =
                "Your 8BitBox is all set up!";
        setupFinishedDialog("Oh hai, Marc.", msg);

    }

    void setupMark() {
        // Store our known mac addresses
        myEditor.putString(BoxConstants.STORED_MAC, BoxConstants.MARK_MAC);
        myEditor.commit();
        String msg =
                "Beep Bop Boop Beep";
        setupFinishedDialog("01000010", msg);

    }

    void setupGuest() {

        int nDevices = 0;

        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please pick the 8BitBox from your paired Bluetooth devices:");

        // Set up our ListView to hold the items
        final ListView macList = new ListView(this);

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Set up our ArrayAdapter
        final ArrayAdapter<String> macAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1);
        macList.setAdapter(macAdapter);

        // Get the list of paired devices
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        // Add paired devices to the list
        nDevices = pairedDevices.size();
        if (nDevices > 0) {
            for (BluetoothDevice device : pairedDevices) {
                // Add the Name and MAC
                macAdapter.add(device.getName() + "\n" + device.getAddress());
            }

        }


        builder.setView(macList);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        final Dialog finalDialog = dialog;
        macList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int arg2, long arg3) {

                // Once an item is selected...

                // Don't let people click it twice
                macList.setClickable(false);

                // Get the MAC address
                String deviceToConnect = macAdapter.getItem(arg2);
                int MACLength = deviceToConnect.length();
                deviceToConnect = deviceToConnect.substring(MACLength - 17, MACLength);

                // Store it
                myEditor.putString(BoxConstants.STORED_MAC, deviceToConnect);
                myEditor.commit();
                finalDialog.dismiss();


            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // Did we store a MAC
                String testMAC = myPreferences.getString(BoxConstants.STORED_MAC,"");
                String msg = "";
                String title = "";
                if (testMAC.equals("")) {
                    title = "Try Again!";
                    msg = "It seems you didn't store a MAC address. Maybe it wasn't already in your " +
                            "list of paired Bluetooth devices? Please pair it and try again!";
                }
                else {
                    title = "Setup Complete!";
                    msg = "Your 8BitBox should be set up and ready to use!";
                }
                setupFinishedDialog(title, msg);
            }
        });

        dialog.show();
        // Immediately dismiss if there were no paired devices
        if (nDevices == 0) {
            dialog.dismiss();
        }

    }

    void setupFinishedDialog(String title, String msg) {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
        builder.setTitle(title);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SetupActivity.this.finish();
            }
        });
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
