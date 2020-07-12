package com.example.fielddata;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity<thread> extends AppCompatActivity implements SensorEventListener {

    TextView textView9, textView12, textView13, textView14;
    Button button1, button2;
    private static SensorManager sensorManager;
    private Sensor sensor;
    private static final String TAG = "Main Activity";
    private static final int JobID=101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView9 = (TextView) findViewById(R.id.textView9);
        textView12 = (TextView) findViewById(R.id.textView12);
        textView13 = (TextView) findViewById(R.id.textView13);
        textView14 = (TextView) findViewById(R.id.textView14);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(sensor!=null){
            sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
        else{
            Toast.makeText(this,"Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
    }

    static String B_net;
    static String B_x;
    static String B_y;
    static String B_z;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        float Mag_x =Math.round(sensorEvent.values[0]);
        float Mag_y =Math.round(sensorEvent.values[1]);
        float Mag_z =Math.round(sensorEvent.values[2]);

        double Mag_norm = Math.sqrt((Mag_x*Mag_x)+(Mag_y*Mag_y)+(Mag_z*Mag_z));

        B_net = String.format("%.0f",Mag_norm);
        B_x =String.format("%.0f",Mag_x);
        B_y =String.format("%.0f",Mag_y);
        B_z =String.format("%.0f",Mag_z);

        textView9.setText(B_net);
        textView12.setText("X: "+B_x);
        textView13.setText("Y: "+B_y);
        textView14.setText("Z: "+B_z);

    }

    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public static List<String> fieldValues() {
        List<String> bValues = new ArrayList<>();
        bValues.add(B_x);
        bValues.add(B_y);
        bValues.add(B_z);
        bValues.add(B_net);
        return bValues;
    }

    public void load_data(View view) {

        button1 = (Button) findViewById(R.id.button1);
        button1.setEnabled(false);

        // Intent intent = new Intent(MainActivity.this, JOBScheduler.class);

        /*
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("BX", B_x);
        bundle.putString("BY", B_y);
        bundle.putString("BZ", B_z);
        bundle.putString("BNET",B_net);
        */

        // intent.putExtra("BUNDLE", bundle);
        // startActivity(intent);

        ComponentName componentName = new ComponentName(this,JOBScheduler.class);
        JobInfo info = new JobInfo.Builder(JobID,componentName).setPersisted(true).build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);

        if(resultCode==JobScheduler.RESULT_SUCCESS){
            Log.d(TAG, "Job Scheduled");
        }
        else{
            Log.d(TAG,"Job Scheduling failed");
        }

        // toExit = false;
        // t.start();
        /*
        String current_time_stamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Timestamp(System.currentTimeMillis()));
        data.append(current_time_stamp+","+B_x+","+B_y+","+B_z+","+B_net+"\n");
        */
    }

    public void export_data(View view) {
        try{

            //dataRecorder.stop();
            // toExit=true;
            // t.interrupt();

            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.cancel(JobID);
            Log.d(TAG, "Job Cancelled");

            button1 = (Button) findViewById(R.id.button1);
            button1.setEnabled(true);

            String datafinal=JOBScheduler.getData();
            Log.d(TAG,"FINAL DATA: "+datafinal);

            FileOutputStream out =openFileOutput("data.csv",Context.MODE_PRIVATE);
            out.write((datafinal).getBytes());
            out.close();

            Context context = getApplicationContext();
            File filelocation = new File(getFilesDir(),"data.csv");
            Uri path = FileProvider.getUriForFile(context,"com.example.fielddata.fileprovider", filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT,"Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent,"Send E-mail"));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /*
    volatile boolean toExit=false;
    final Thread t = new Thread(new Runnable() {

        @Override
        public void run() {
            while(!toExit){
                try {


                    long subtract_time = SystemClock.currentThreadTimeMillis()%20;
                    Thread.sleep(20);


                    Date date = Calendar.getInstance().getTime();
                    DateFormat basic_date = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.SSS");
                    String strDate = basic_date.format(date);

                    // format(new Timestamp(System.currentTimeMillis()));
                    String current_time_stamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date());


                    data.append(current_time_stamp+","+B_x+","+B_y+","+B_z+","+B_net+"\n");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    */


}
