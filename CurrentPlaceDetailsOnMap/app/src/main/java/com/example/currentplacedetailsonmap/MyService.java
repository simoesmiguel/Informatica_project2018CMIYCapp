package com.example.currentplacedetailsonmap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service {

    public static String gpsCoordenadas = "";
    public static String timeToRepeat ="";
    public static String destination_id ="";

    AlarmReceiver alarm = new AlarmReceiver();
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("coordenadas","coordenadas "+intent.getExtras().getString("gpsCoords"));
        this.timeToRepeat = intent.getExtras().getString("timeRepeat");
        this.gpsCoordenadas = intent.getExtras().getString("gpsCoords");
        this.destination_id = intent.getExtras().getString("destination_id");
        //alarm.setAlarm(this);
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        Log.d("coordenadas","coordenadasss "+intent.getExtras().getString("gpsCoords"));
        //alarm.setAlarm(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
