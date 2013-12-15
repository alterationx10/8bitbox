package com.markslaboratory.eightbitbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends Activity {

    Button btnConnect;
    Button btnDisconnect;
    Button btnOver;
    Button btnUnder;
    Button btnRed;
    Button btnGreen;
    Button btnBlue;
    Button btnAll;
    Button btnOff;

    String TAG = "8BitActivity";

    // Our bluetooth service
    BluetoothService myService;

    String MAC = "00:18:96:B0:06:DB";

    NfcAdapter nfcAdapter;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            genericDialog("Bluetooth isn't enabled... \n You might want to turn it on...");
        }

        // We need to bind to our service, so lets go ahead and do that
        ServiceConnection mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                genericDialog("Service connected! The app should work now :-)");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                genericDialog("Service disconnected!");
            }
        };

        myService = new BluetoothService();
        // Don't forget we need to decalre the Service (and Bluetooth permission) in the manifest file.
        Intent myIntent = new Intent(getApplicationContext(), BluetoothService.class);
        bindService(myIntent, mConnection, Context.BIND_AUTO_CREATE);
        // Our service should bind now; We will see the dialogs above whe it's all ready;


        btnConnect = (Button)findViewById(R.id.btnConnect);
        btnDisconnect = (Button)findViewById(R.id.btnDisconnect);
        btnOver = (Button)findViewById(R.id.btnOver);
        btnUnder = (Button)findViewById(R.id.btnUnder);
        btnRed = (Button)findViewById(R.id.btnRed);
        btnGreen = (Button)findViewById(R.id.btnGreen);
        btnBlue = (Button)findViewById(R.id.btnBlue);
        btnAll = (Button)findViewById(R.id.btnRGB);
        btnOff = (Button)findViewById(R.id.btnOff);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myService.btConnect(MAC, new BluetoothService.BluetoothConnectCallback() {
                    @Override
                    public void doOnConnect() {
                        genericDialog("Connected!");
                    }

                    @Override
                    public void doOnCOnnectionFailed() {
                        genericDialog("Connection Failed!");
                    }

                    @Override
                    public void doOnDisconnect() {
                    }
                });
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myService.btDisconnect(new BluetoothService.BluetoothConnectCallback() {
                    @Override
                    public void doOnConnect() {

                    }

                    @Override
                    public void doOnCOnnectionFailed() {

                    }

                    @Override
                    public void doOnDisconnect() {
                        genericDialog("Disconnected!");
                    }
                });
            }
        });

        btnOver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myService.writeData(BoxConstants.PLAY_OVERWORLD);
            }
        });

        btnUnder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myService.writeData(BoxConstants.PLAY_UNDERWORLD);
            }
        });

        btnRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myService.writeData(BoxConstants.RED_ON);
            }
        });

        btnGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myService.writeData(BoxConstants.GREEN_ON);
            }
        });

        btnBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myService.writeData(BoxConstants.BLUE_ON);
            }
        });

        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myService.writeData(BoxConstants.ALL_ON);
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myService.writeData(BoxConstants.ALL_OFF);
            }
        });




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
        // Stop listening
        nfcAdapter.disableForegroundDispatch(this);


    }


    @Override
    protected void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        String msg = "Not Found!";
        if (Arrays.equals(tag.getId(), BoxConstants.JODY_NFC)) {
            msg = "Jody";
        } else if (Arrays.equals(tag.getId(), BoxConstants.MARC_NFC)) {
            msg = "Marc";
        } else if (Arrays.equals(tag.getId(), BoxConstants.MARK_NFC)) {
            msg = "Mark";
        }

        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    public void genericDialog(String msg) {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("A Wild Message Appears!");
        builder.setIcon(R.drawable.ic_launcher);
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

}
