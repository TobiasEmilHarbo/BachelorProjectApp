package heroes.bachelorprojectapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import heroes.bachelorprojectapp.rest.RestRequest;
import heroes.bachelorprojectapp.rest.RestResponse;

/**
 * Created by Tobias on 15-12-2015.
 */
public class BumpGaugeActivity extends AppCompatActivity
{
    private static final String DEBUGTAG = "BumpGaugeActivity";
    private TextView gauge;
    private Timer updateGaugeTimer;
    private Button fakeBumpButton;
    private Button resetAllButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bump_gauge);

        gauge = (TextView) findViewById(R.id.bumpGauge);
        fakeBumpButton = (Button) findViewById(R.id.fakeBumpButton);
        resetAllButton = (Button) findViewById(R.id.resetAllButton);

        fakeBumpButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                RestRequest request = new RestRequest();

                try {
                    request.get("http://ttw.idyia.dk/fake_bump");
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        resetAllButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                RestRequest request = new RestRequest();

                try {
                    request.get("http://ttw.idyia.dk/reset_bump_records");
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(updateGaugeTimer == null)
        {
            updateGaugeTimer = new Timer();

            updateGaugeTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateGauge();
                }
            }, 10, 750);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void updateGauge()
    {
        RestRequest request = new RestRequest();

        try {
            request.get("http://ttw.idyia.dk/get_bump_gauge");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        RestResponse response = request.getResponse();

        try {
            final int count = response.getInt("bumpCount");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    gauge.setText(Integer.toString(count));
                    Log.d(DEBUGTAG, "" + count);

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(updateGaugeTimer != null)
        {
            updateGaugeTimer.cancel();
            updateGaugeTimer = null;
        }
    }
}
