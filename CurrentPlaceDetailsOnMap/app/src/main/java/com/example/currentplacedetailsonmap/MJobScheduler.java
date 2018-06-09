package com.example.currentplacedetailsonmap;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.someoneelse.library.LocationMethodsAndroid;

import java.util.List;

import ch.hsr.geohash.GeoHash;

public class MJobScheduler extends JobService{

    private MJobExecutor mJobExecutor;
    private LocationMethodsAndroid lm;


    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        mJobExecutor = new MJobExecutor(){
            @Override
            protected void onPostExecute(String s) {
                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();

                // read destination_id and gpsCoords from sharedPreferences
                SharedPreferences shared = getApplicationContext().getSharedPreferences("com.example.AlarmSettings", getApplicationContext().MODE_PRIVATE);
                String a=null,b = null;
                String gpsCoords = shared.getString("gpsCoords", a);
                String destination_id = shared.getString("destination_id", b);

                System.out.println("dest : "+destination_id +" gps: "+gpsCoords);


                lm = new LocationMethodsAndroid(getApplicationContext());
                String geoHash = null;
                Log.d("gpsCoords ","<AlarmReceiver> gpsCoords "+outgoingNotificationsSettings.gpsCoords);
                try {
                    geoHash= lm.generateGeohashMessage(
                            GeoHash.withBitPrecision(
                                    Double.parseDouble(gpsCoords.split(",")[0]), Double.parseDouble(gpsCoords.split(",")[1])
                                    ,10).toBase32(),Integer.parseInt(lm.getMyPhoneNumber()), Integer.parseInt(destination_id));
                } catch (Exception e) {
                    e.printStackTrace();                                    //Integer.parseInt(intent.getExtras().getString("destination_id"))
                }
                Log.d("geoHashhhhh","<AlarmReceiver> geoHash: "+geoHash);
                Toast.makeText(getApplicationContext(), "CMIYC says: Sending Geohash", Toast.LENGTH_LONG).show(); // For example

                // start activity to ownTracks
                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                intent2.setType("text/plain");

                PackageManager packageManager = getApplicationContext().getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(intent2, 0);
                boolean isIntentSafe = activities.size() > 0;

                System.out.println("<AlarmReceiver> sendingGeoHAsh"+geoHash);
                // Start an activity if it's safe
                if (isIntentSafe) {
                    for (ResolveInfo activity : activities){
                        if(activity.activityInfo.packageName.contains("owntracks")){
                            intent2.setData(Uri.parse("receiver://123"));
                            intent2.setPackage(activity.activityInfo.packageName);
                            intent2.putExtra("<CMIYC>GeoHashInformation",geoHash);
                            intent2.putExtra("<CMIYC>messageId",destination_id);
                            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().sendBroadcast(intent2);
                        }
                    }
                }



                Log.d("sendinggggggg","sendinggggggggggggggggggg");
                jobFinished(jobParameters,false);
            }
        };

        mJobExecutor.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {

        mJobExecutor.cancel(true);

        return false; // return true of want to re schedule the same job again
    }
}
