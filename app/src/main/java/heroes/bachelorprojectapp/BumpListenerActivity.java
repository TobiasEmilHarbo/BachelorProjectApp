package heroes.bachelorprojectapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import heroes.bachelorprojectapp.rest.RestRequest;

/**
 * Created by Tobias on 15-12-2015.
 */
public class BumpListenerActivity extends AppCompatActivity
{
    private static final String DEBUGTAG = "BumpListenerActivity";
    private static final String BUMP = "5";
    private Bluetooth bluetooth;
    private TextView artifactId;
    private TextView bumpLogView;
    private final ArrayList<String> bumpLog = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bump_listener);

        bluetooth = new Bluetooth(this);
        bluetooth.registerReceiver();
        bluetooth.checkBluetoothAdapter();

        String deviceName = bluetooth.getBluetoothDeviceName();

        artifactId = (TextView) findViewById(R.id.artifactId);
        bumpLogView = (TextView) findViewById(R.id.bumpLog);

        artifactId.setText(deviceName);

        bluetooth.startListenForData();
        bluetooth.setDataReceiveCallback(new DataReceiveCallback() {
            @Override
            public void dataReceived(String data)
            {
                if(data.equals(BUMP))
                {
                    registerBump();
                    Log.d(DEBUGTAG, data);
                }
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

                    Boolean success = request.getResponse().getBoolean("success");

                    if(success)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bumpLog.add(Long.toString(unixTime));
                                String log = "";
                                for(String time : bumpLog)
                                {
                                    log = "BUMP @ " + time + "\n" + log;
                                }

                                bumpLogView.setText(log);
                            }
                        });
                    }
                    else
                    {
                        final String response = request.getResponse().getString("response");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(DEBUGTAG, "SERVER ERROR :"+response);
                                Toast.makeText(getApplicationContext(), "Server error: "+response, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetooth.unregisterReceiver();
        bluetooth.stopListeningForData();
    }
}
