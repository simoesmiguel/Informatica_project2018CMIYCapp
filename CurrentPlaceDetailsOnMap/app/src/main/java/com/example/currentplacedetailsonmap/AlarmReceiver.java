package com.example.currentplacedetailsonmap;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.example.someoneelse.library.LocationMethodsAndroid;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import ch.hsr.geohash.GeoHash;

public class AlarmReceiver extends BroadcastReceiver {

    private LocationMethodsAndroid lm;
    private String timeToRepeat;
    private String gpsCoords = "";
    private String destination_id="";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();


        // read destination_id and gpsCoords from sharedPreferences
        SharedPreferences shared = context.getSharedPreferences("com.example.AlarmSettings", context.MODE_PRIVATE);
        String a=null,b = null;
        String gpsCoords = shared.getString("gpsCoords", a);
        String destination_id = shared.getString("destination_id", b);

        System.out.println("dest : "+destination_id +" gps: "+gpsCoords);


        lm = new LocationMethodsAndroid(context);
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
        Toast.makeText(context, "CMIYC says: Sending Geohash", Toast.LENGTH_LONG).show(); // For example

        // start activity to ownTracks
        Intent intent2 = new Intent(Intent.ACTION_VIEW);
        intent2.setType("text/plain");

        PackageManager packageManager = context.getPackageManager();
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
                    context.sendBroadcast(intent2);
                }
            }
        }
        wl.release();
    }






/*
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setAlarm(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        //am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000*600*Integer.parseInt(MyService.timeToRepeat), pi); // Millisec * Second * Minute  -- repeats every 1 min
        //am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+1000*600*Integer.parseInt(MyService.timeToRepeat),pi);
      //  am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+1000*600,pi);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),1000*600,pi);
        //am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000*600, pi); // Millisec * Second * Minute  -- repeats every 1 min
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
*/
}