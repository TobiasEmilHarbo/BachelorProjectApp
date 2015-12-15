package heroes.bachelorprojectapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import heroes.bachelorprojectapp.rest.RestRequest;

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
    private Button bluetoothButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        scanForDevices = (Button) findViewById(R.id.scanForDevices);
        state1 = (Button) findViewById(R.id.state1);
        state3 = (Button) findViewById(R.id.state3);
        state5 = (Button) findViewById(R.id.state5);
        bluetoothButton = (Button) findViewById(R.id.testBump);

        bluetooth = new Bluetooth(this);
        bluetooth.registerReceiver();

        createOnClickListeners();
        bluetooth.checkBluetoothAdapter();

        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerBump();
            }
        });
    }

    public void registerBump()
    {
        final long unixTime = System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override
            public void run() {

                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String imei = telephonyManager.getDeviceId();

                RestRequest request = new RestRequest();

                try {
                    List<NameValuePair> parameters = new ArrayList<NameValuePair>();
                    parameters.add(new BasicNameValuePair("imei", imei));
                    parameters.add(new BasicNameValuePair("timestamp", Long.toString(unixTime)));

                    request.post(
                            "http://ttw.idyia.dk/register_bump",
                            parameters
                    );

                    Boolean response = request.getResponse().getBoolean("success");

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetooth.unregisterReceiver();
    }
}
