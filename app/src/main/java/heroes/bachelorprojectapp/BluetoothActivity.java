package heroes.bachelorprojectapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import heroes.bachelorprojectapp.rest.RestRequest;
import heroes.bachelorprojectapp.rest.RestResponse;

/**
 * Created by Tobias on 21-11-2015.
 */
public class BluetoothActivity extends Activity {

    private String DEBUGTAG = "BluetoothActivity";

    Button state1;
    Button state3;
    Button state5;
    Button scanForDevices;

    private Bluetooth bluetooth;
    Button stop;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        scanForDevices = (Button) findViewById(R.id.scanForDevices);
        state1 = (Button) findViewById(R.id.state1);
        state3 = (Button) findViewById(R.id.state3);
        state5 = (Button) findViewById(R.id.state5);
        stop = (Button) findViewById(R.id.stop);

        bluetooth = new Bluetooth(this);
        bluetooth.registerReceiver();

        createOnClickListeners();
        bluetooth.checkBluetoothAdapter();
    }

    public void createOnClickListeners()
    {
        scanForDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth.scanForDevices();
            }
        });

        state1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth.transmit("1");
            }
        });

        state3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth.transmit("3");
            }
        });

        state5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth.transmit("5");
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth.transmit("0");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetooth.transmit("0");
        bluetooth.unregisterReceiver();
    }
}
