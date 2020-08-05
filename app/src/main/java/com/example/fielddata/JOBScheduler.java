package com.example.fielddata;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class JOBScheduler extends JobService {
    private static final String TAG = "Job started";
    private boolean jobCancelled = false;

    static String B_x,B_y,B_z,B_net;
    static String data = new String();
    static String cpuloadstr;
    String fieldcomponent = "TimeStamp,B_x,B_y,B_z,B_net,CPU_load";


    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        Log.d(TAG,"Recording the field values started");

        data = data.concat(fieldcomponent+"\n");
        recording(jobParameters);

        return true;
    }



    private void recording(final JobParameters jobParameters){

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (!jobCancelled){
                    try {
                        Thread.sleep(20);

                        List<String> bB = MainActivity.fieldValues();
                        B_x = bB.get(0);
                        B_y = bB.get(1);
                        B_z = bB.get(2);
                        B_net = bB.get(3);

                        float[] cores = CpuInfo.getCoresUsage();
                        cpuloadstr = CpuInfo.getCpuUsage(cores) + "%";
                        String current_time_stamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date());

                        // Log.d(TAG,"CPU: "+ );

                        data=data.concat(current_time_stamp + "," + B_x + "," +B_y + "," + B_z + "," + B_net +","+ cpuloadstr+"\n");
//                         Log.d(TAG, "B: "+B_x+" "+B_y+" "+B_z);
//                         Log.d(TAG, current_time_stamp);
//                         Log.d(TAG,"DATA: "+data);


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }

                Log.d(TAG, "Recording field values done");
                jobFinished(jobParameters,false);
            }
        }).start();
    }

    public static String getData(){
        return data;
    }


    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job cancelled");
        jobCancelled = true;
        return true;
    }
}
