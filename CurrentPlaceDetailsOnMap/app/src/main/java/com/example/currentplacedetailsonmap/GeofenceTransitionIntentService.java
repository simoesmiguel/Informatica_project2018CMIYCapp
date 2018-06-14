package com.example.currentplacedetailsonmap;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

import android.graphics.Color;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class GeofenceTransitionIntentService extends IntentService {


    public GeofenceTransitionIntentService() {
        super("ReceiveTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent( Intent intent) {
        Log.d("Handeling Intent","<GeofenceTransisitonIntentService> Handeling new intent");
        Log.d("handelingIntent","handelingIntent");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Log.d("handelingIntent 222","handelingIntent 222");

        if (geofencingEvent.hasError()) {

            Log.e(TAG, "GEOFENCE HAS AN ERROR");
            return;
        }
        Log.d("handelingIntent","passou o primeiro if");

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        Log.d("handelingIntent","passou o getGeofenceTransition" );


        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.d("handelingIntent","passou o 2º if" );

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofencesList = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            /*String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );*/

            // Send notification and log the transition details.
            for (Geofence geofence : triggeringGeofencesList){
                sendNotification(geofence.getRequestId());
            }


        } else {
            Toast.makeText(this,"entrou no segundo else", Toast.LENGTH_SHORT).show();


            // Log the error.
            Log.e(TAG, getString(0,
                    geofenceTransition));
        }
    }


    public void sendNotification(String geoFenceDetails){
        long when = System.currentTimeMillis();

        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.cmiyc_icon_notif)
                        .setContentTitle("CMIYC")
                        .setContentText("GEOFENCE DETAILS : ")
                        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                        .setLights(Color.RED, 3000, 3000)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setWhen(when);



        Intent notificationIntent = new Intent(this, MapsActivityCurrentPlace.class);
       // notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        mBuilder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify((int) when, mBuilder.build());
    }


}
