package heroes.bachelorprojectapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Tobias on 30-11-2015.
 */
public class Bluetooth {

    private final Activity activity;
    private String DEBUGTAG = "Bluetooth";

    private Context context;
    private AlertDialog.Builder bluetoothDialog;
    private ArrayAdapter<String> bluetoothDeviceNames;
    private BluetoothAdapter BluetoothAdapter;
    private boolean showBluetoothDialog = true;

    static private BluetoothSocket btSocket = null;
    static private OutputStream outStream = null;
    static private InputStream inputStream = null;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static DataReceiveCallback dataReceiveCallback;

    static private BluetoothDevice connectedDevice = null;
    private int reconnectionAttempts = 0;
    private boolean recoverConnectionIfLost = true;

    static Thread receiveThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    public Bluetooth(Activity a)
    {
        activity = a;
        context = a.getApplicationContext();

        bluetoothDialog = new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.stat_sys_data_bluetooth)
                .setTitle("Connect to device")
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showBluetoothDialog = true;
                                dialog.dismiss();
                            }
                        })
                .setPositiveButton("Refresh",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                scanForDevices();
                                showBluetoothDialog = true;
                                dialog.dismiss();
                            }
                        });

        bluetoothDeviceNames = new ArrayAdapter<String>(
                context,
                android.R.layout.select_dialog_item);

        dataReceiveCallback = new DataReceiveCallback() {
            @Override
            public void dataReceived(String data) {
                Log.d(DEBUGTAG, "Bluetooth data: "+data);
            }
        };

//        receiveThread = getReceiverThreadInstance();
    }

    public void setDataReceiveCallback(DataReceiveCallback drc)
    {
        dataReceiveCallback = drc;
    }

    private Thread getReceiverThreadInstance()
    {
        if(receiveThread != null)
        {
            return receiveThread;
        }
        else
        {
            return new Thread(new Runnable()
            {
                public void run()
                {
                    Looper.prepare();
                    while(!Thread.currentThread().isInterrupted() && !stopWorker)
                    {
                        try
                        {
                            int bytesAvailable = inputStream.available();
                            if(bytesAvailable > 0)
                            {
                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);

                                //Log.d(DEBUGTAG, "data data data" + bytesAvailable);

                                String data = "";

                                for(int i=0; i < bytesAvailable; i++)
                                {
                                    //Log.d(DEBUGTAG, "data data data" + bytesAvailable);

                                    byte b = packetBytes[i];
                                    //if(b == delimiter)
                                    //{
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    data += new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    //Log.d(DEBUGTAG, data);
                                    //}
                                    //else
                                    //{
                                    readBuffer[readBufferPosition++] = b;
                                    //}
                                }

                                dataReceiveCallback.dataReceived(data);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            stopWorker = true;
                        }
                    }
                }
            });
        }
    }

    public void transmit(final String data)
    {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        outStream = btSocket.getOutputStream();
                    } catch (IOException e) {
                        Log.d(DEBUGTAG, "Bug BEFORE Sending stuff", e);
                    }

                    byte[] msgBuffer = data.getBytes();

                    try {
                        outStream.write(msgBuffer);
                    } catch (IOException e) {
                        Log.d(DEBUGTAG, "Bug while sending stuff", e);
                    }
                } catch (Exception e) {
                    //Toast.makeText(activity, "Transition failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

/*    public void listenForData()
    {
        try {
            inputStream = btSocket.getInputStream();
            //startListenForData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public void scanForDevices()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                connectedDevice = null;
                recoverConnectionIfLost = false;

                checkBluetoothAdapter();
                BluetoothAdapter.startDiscovery();
                bluetoothDeviceNames.clear();
                devices.clear();

                showBluetoothDialog = true;
            }
        }).start();
    }

    public void checkBluetoothAdapter()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if (BluetoothAdapter == null)
                {
                    Toast.makeText(activity, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
                }
                else if (!BluetoothAdapter.isEnabled())
                {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    activity.startActivityForResult(enableBtIntent, 1);
                }
            }
        }).start();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
            {
                Log.d(DEBUGTAG, "DISCONNECTED");
                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();

                //disconnected();
            }
            if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
            {
                Log.d(DEBUGTAG, "CONNECTED");
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                Toast.makeText(context, "Scanning for bluetooth devices", Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                showBluetoothDialog();
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(null != device.getName())
                {
                    devices.add(device);

                    String name = device.getName();
                    bluetoothDeviceNames.add(name);
                    Log.d(DEBUGTAG, "Found device: " + device.getName());
                }
            }
        }
    };

    private void showBluetoothDialog()
    {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bluetoothDialog.setAdapter(bluetoothDeviceNames,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BluetoothDevice device = devices.get(which);
                                connectToDevice(device);
                            }
                        });

                if (showBluetoothDialog) {
                    showBluetoothDialog = false;
                    bluetoothDialog.show();
                }
            }
        });
    }

    public void startListenForData()
    {
        try {
            inputStream = btSocket.getInputStream();

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            receiveThread = getReceiverThreadInstance();
            receiveThread.start();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void connectToDevice(final BluetoothDevice device)
    {
        try {
            resetConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        connectedDevice = device;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(DEBUGTAG, "Connecting to: " + device);

                recoverConnectionIfLost = true;
                BluetoothAdapter.cancelDiscovery();

                try {
                    btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    btSocket.connect();
                    Toast.makeText(context, "Connected to: " + device.getName(), Toast.LENGTH_SHORT).show();
                    reconnectionAttempts = 0;
                    //listenForData();
                } catch (Exception e) {
                    try {
                        resetConnection();
                    } catch (IOException e2) {
                        Log.d(DEBUGTAG, "Unable to end the connection");
                    }

                    Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show();
                    Log.d(DEBUGTAG, "Socket creation failed");

                    e.printStackTrace();

                    //disconnected();
                }
            }
        });
    }

    private void resetConnection() throws IOException {
        if (inputStream != null) {
            try {inputStream.close();} catch (Exception e) {}
            inputStream = null;
        }

        if (outStream != null) {
            try {outStream.close();} catch (Exception e) {}
            outStream = null;
        }

        if (btSocket != null) {
            btSocket.close();
            btSocket = null;
        }
    }

    private void disconnected()
    {
        if(connectedDevice != null && recoverConnectionIfLost)
        {
            if (reconnectionAttempts < 10)
            {
                reconnectionAttempts++;

                Toast.makeText(context, "Trying to recover connection. Attempt no: "+reconnectionAttempts+"/10", Toast.LENGTH_SHORT).show();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        connectToDevice(connectedDevice);
                    }
                }, 5000);
            }
            else
            {
                recoverConnectionIfLost = false;
                reconnectionAttempts = 0;
            }
        }
    }

    public void unregisterReceiver()
    {
        activity.unregisterReceiver(mReceiver);
    }

    public void registerReceiver()
    {
        BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        activity.registerReceiver(mReceiver, filter);
    }

    public String getBluetoothDeviceName()
    {
        if(connectedDevice != null)
            return connectedDevice.getName();
        else return "No device connected";
    }

    public void stopListeningForData()
    {
        if(receiveThread != null)
        {
            receiveThread.interrupt();
            receiveThread = null;
        }
    }
}

interface DataReceiveCallback
{
    void dataReceived(String data);
}
