package heroes.bachelorprojectapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button bluetoothButton;
    private Button bumpGaugeButton;
    private Button bumpListenerButton;
    private Button activityAccelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothButton     = (Button) findViewById(R.id.bluetooth);
        bumpGaugeButton     = (Button) findViewById(R.id.bumpGauge);
        bumpListenerButton  = (Button) findViewById(R.id.listenForBumps);
        activityAccelerometer = (Button) findViewById(R.id.activityAccelerometer);

        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, BluetoothActivity.class);
                MainActivity.this.startActivity(i);
            }
        });

        bumpGaugeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, BumpGaugeActivity.class);
                MainActivity.this.startActivity(i);
            }
        });

        bumpListenerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, BumpListenerActivity.class);
                MainActivity.this.startActivity(i);
            }
        });

        activityAccelerometer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ActivityAccelerometer.class);
                startActivity(i);
            }
        });
    }
}
