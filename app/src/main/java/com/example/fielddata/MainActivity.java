package com.example.fielddata;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView textView9, textView12, textView13, textView14;
    private static SensorManager sensorManager;
    private Sensor sensor;

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

    String B_net,B_x,B_y,B_z;

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

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // String timeStamp;
    // Thread t;
    StringBuilder data = new StringBuilder();
    String fieldcomponent = "B_x,B_y,B_z";
    // data.append(fieldcomponent);
    public void load_data(View view) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        String formatofdate = simpleDateFormat.format(new Date());
        data.append("\n"+formatofdate+","+B_x+","+B_y+","+B_z+","+B_net);
    }

    public void export_data(View view) {
        try{
            FileOutputStream out =openFileOutput("data.csv",Context.MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();

            // Stop the timer
            // t.stop();

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
}
