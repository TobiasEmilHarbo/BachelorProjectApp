package heroes.bachelorprojectapp;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Tobias on 24-11-2015.
 */
public class ActivityAccelerometer extends Activity implements SensorEventListener
{
    private static final String DEBUGTAG = "ActivityAccelerometer";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    final private static int STILL      = 0;
    final private static int MOVING     = 1;

    //private int levelOfComfort = 0;

    private ArrayList<String[]> activityLog = new ArrayList<String[]>();
    //private ArrayList<String[]> activityDiffLog = new ArrayList<String[]>();
    private ArrayList<String[]> evaluatedLog = new ArrayList<String[]>();

    private int lengthOfLogEvaluation = 120; //seconds

    private TextView activityLogView;
    private TextView comfortFactorView;
    private TextView percentageOfStillView;

    private int logDelayCount = 0;
    private int logDivideFactor = 20;

    private Bluetooth bluetooth;
    private Timer timer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        activityLogView         = (TextView) findViewById(R.id.msg);
        comfortFactorView       = (TextView) findViewById(R.id.comfortFactor);
        percentageOfStillView   = (TextView) findViewById(R.id.percentageStill);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        bluetooth = new Bluetooth(this);
        bluetooth.registerReceiver();

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int lvlOfComfort = getLevelOfComfort();

                            try {
                                String[] latestLog = evaluatedLog.get(evaluatedLog.size() - 1);
                                String[] secondLatestLog = evaluatedLog.get(evaluatedLog.size() - 2);

                                int latestLogType = Integer.parseInt(latestLog[1]);
                                int secondLatestLogType = Integer.parseInt(secondLatestLog[1]);

                                if (
                                        (lvlOfComfort == 5
                                                && latestLogType == STILL
                                                && secondLatestLogType == STILL)
                                                ||
                                                (lvlOfComfort == 1
                                                        && latestLogType == MOVING
                                                        && secondLatestLogType == MOVING)) {
                                    lvlOfComfort = 3;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                //ignore
                            }

                            //String[] lastLog = evaluatedLog.get(0);

                            //int activityType = Integer.parseInt(lastLog[1]);
                            //activityLogView.setText(getActivityType(activityType));

                            comfortFactorView.setText(Integer.toString(lvlOfComfort));
                            bluetooth.transmit(Integer.toString(lvlOfComfort));
                        } catch (IndexOutOfBoundsException e) {
                            //ignore
                        }
                    }
                });
            }
        }, 0, 500);
    }

    protected void onResume()
    {
        super.onResume();
    }

    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        bluetooth.transmit("0");
        bluetooth.unregisterReceiver();
        timer.cancel();
        //mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent e)
    {
        final SensorEvent event = e;
        //Right in here is where you put code to read the current sensor values and
        //update any views you might have that are displaying the sensor information
        //You'd get accelerometer values like this:
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;

        if (logDelayCount == logDivideFactor)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    float mSensorX, mSensorY, mSensorZ;

                    mSensorX = event.values[0];
                    mSensorY = event.values[1];
                    mSensorZ = event.values[2];

                    logData(mSensorX, mSensorY, mSensorZ);
                    trimLogAndCalcDiff();

                    //String[] oldestLog = activityLog.get(activityLog.size() - 1);

                    //long now = System.currentTimeMillis();
                    //long ageOfLog = (now - Long.parseLong(oldestLog[0]))/1000;

                    //Log.d(DEBUGTAG, "" + oldestLog[1]);

                    //Log.d(DEBUGTAG, "Size: " + activityLog.size());
                    //Log.d(DEBUGTAG, "Age of log: " + ageOfLog);

                    //Log.d(DEBUGTAG, "log: "+now);

                    logDelayCount = 0;
                }
            });
        }
        else
        {
            logDelayCount++;
        }
    }

    private void logData(float x, float y, float z)
    {
        Long timeStamp = System.currentTimeMillis();

        activityLog.add(0, //add item to the top of list
                new String[]{
                        Long.toString(timeStamp),
                        Float.toString(x),
                        Float.toString(y),
                        Float.toString(z)
                }
        );
    }

    private void trimLogAndCalcDiff()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                evaluatedLog.clear();

                ArrayList<String[]> trimmedLog = new ArrayList<String[]>();
                //ArrayList<String[]> diffLog = new ArrayList<String[]>();

                long now = System.currentTimeMillis();

                String v = "";

                for (int i = 0; i < activityLog.size(); i++) {
                    String[] log = activityLog.get(i);

                    long logStamp = Long.parseLong(log[0]);
                    long ageOfLog = (now - logStamp) / 1000;

                    trimmedLog.add(log);

                    if (ageOfLog > lengthOfLogEvaluation) break;

                    try {
                        String[] thisLog = activityLog.get(i);
                        String[] nextLog = activityLog.get(i + 1);

                        float thisX = Float.parseFloat(thisLog[1]) + 100;
                        float thisY = Float.parseFloat(thisLog[2]) + 100;
                        float thisZ = Float.parseFloat(thisLog[3]) + 100;

                        float nextX = Float.parseFloat(nextLog[1]) + 100;
                        float nextY = Float.parseFloat(nextLog[2]) + 100;
                        float nextZ = Float.parseFloat(nextLog[3]) + 100;

                        float diffX = Math.abs(thisX - nextX);
                        float diffY = Math.abs(thisY - nextY);
                        float diffZ = Math.abs(thisZ - nextZ);

                        /*diffLog.add(
                                new String[]{
                                        thisLog[1],
                                        Float.toString(diffX),
                                        Float.toString(diffY),
                                        Float.toString(diffZ)
                                }
                        );*/

                        int activity = STILL;

                        if ((diffX >= 0.9 && diffY >= 0.9)
                        || (diffY >= 0.9 && diffZ >= 0.9)
                        || (diffZ >= 0.9 && diffX >= 0.9)) {
                            activity = MOVING;
                        }

                        v = v + getActivityType(activity) + "\n";

                        activityLogView.setText(v);

                        evaluatedLog.add(0, //add item to the top of list
                            new String[]{
                                Long.toString(logStamp),
                                Integer.toString(activity)
                            });

                    } catch (IndexOutOfBoundsException e) {
                        //ignore
                    }

                    activityLogView.setText(v);
                }

                //activityDiffLog = diffLog;
                activityLog = trimmedLog;
            }
        });
    }

    private int getLevelOfComfort()
    {
        int numberOfMoving = 0;
        double percentageStill = 0;

        for(int i = 0; i < evaluatedLog.size(); i++)
        {
            try{

                String[] log1 = evaluatedLog.get(i);
                String[] log2 = evaluatedLog.get(i+1);

                int activityType1 = Integer.parseInt(log1[1]);
                int activityType2 = Integer.parseInt(log2[1]);

                if(activityType1 == MOVING && activityType2 == MOVING)
                {
                    numberOfMoving++;
                }
            }
            catch(IndexOutOfBoundsException e)
            {
                //ignore
            }
        }
        
/*        for (String[] log : evaluatedLog)
        {
            int activity = Integer.parseInt(log[1]);
            if(activity == STILL)
            {
                numberOfStills++;
            }
        }
*/
        double sizeOfLog = evaluatedLog.size();

        //Log.d("numOfStills ", ""+numberOfStills);
        //Log.d("num og logs ", ""+sizeOfLog);

        if(sizeOfLog > 0)
        {
            percentageStill = 100.0-((numberOfMoving*100)/sizeOfLog);
        }

        percentageOfStillView.setText(percentageStill+"% still");

        if(percentageStill > 80) return 1;
        //if(percentageStill > 75) return 2;
        //if(percentageStill > 25) return 4;
        if(percentageStill < 60) return 5;
        else return 3;
    }

    private String getActivityType(int activity)
    {
        String t = null;
        switch (activity)
        {
            case 0:
                t = "STILL";
                break;

            case 1:
                t = "MOVING";
                break;
        }

        return t;
    }
/*
    private void evaluateDiffLogFirst()
    {
        int fragmentLength = 5; //sec
        ArrayList<String[]> fragmentLog = new ArrayList<String[]>();

        float stillThreshold = 20; //%

        //ArrayList<String[]> activityDiffLog = this.activityDiffLog;

        int stills = 0;

        long benchTime = 0;

        for (String[] log : activityDiffLog)
        {
            long time = Long.parseLong(log[0]);
            if (benchTime == 0) benchTime = time;

            long ageOfLog = (benchTime - time) / 1000;

            fragmentLog.add(log);

            if (ageOfLog > fragmentLength)
            {
                benchTime = time;

                int moving = 0;
                int lAll = fragmentLog.size();

                for (String[] l : fragmentLog)
                {
                    if(Float.parseFloat(l[1]) > 1 && Float.parseFloat(l[2]) > 1
                    && Float.parseFloat(l[2]) > 1 && Float.parseFloat(l[3]) > 1
                    && Float.parseFloat(l[3]) > 1 && Float.parseFloat(l[1]) > 1)
                    {
                        moving++;
                    }
                }

                float percentForMove = (moving * 100) / lAll;

                if(percentForMove < stillThreshold) stills++;
            }
        }
    }

    private void evaluateDiffLog()
    {
        long fragmentLength = 5;

        long firstFragmentLogTime = 0;

        for (String[] log : activityDiffLog)
        {
            //long time = Long.parseLong(log[0]);
            //if(firstFragmentLogTime == 0) firstFragmentLogTime = time;

            //long ageOfFragment = (firstFragmentLogTime - time) / 1000;

            if((Float.parseFloat(log[1]) >= 1 && Float.parseFloat(log[2]) >= 1)
            || (Float.parseFloat(log[2]) >= 1 && Float.parseFloat(log[3]) >= 1)
            || (Float.parseFloat(log[3]) >= 1 && Float.parseFloat(log[1]) >= 1))
            {
                //moving++;
            }

            //if(ageOfFragment > fragmentLength) firstFragmentLogTime = 0;
            //long ageOfLog = (benchTime - time) / 1000;

            //long ageOfFistLogOfFragment = (benchTime - time) / 1000;
        }
    }*/
}
