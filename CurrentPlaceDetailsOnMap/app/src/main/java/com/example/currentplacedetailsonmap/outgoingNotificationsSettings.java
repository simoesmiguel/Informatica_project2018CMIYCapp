package com.example.currentplacedetailsonmap;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.someoneelse.library.LocationMethodsAndroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class outgoingNotificationsSettings extends AppCompatActivity {

    private PendingIntent pendingIntent;
    private AlarmManager manager;
    private Button setAlarm,cancelAlarm;
    private ListView currentSharingContacts;
    private ArrayAdapter<String> adapter;
    private LocationMethodsAndroid lm;
    private Map<String,String> name_number;
    private Map<String,String> name_id;
    private ArrayList<String> contacts_array ;
    private   String[] items;
    public static String gpsCoords;

    public static int timeToRepeat;
    public static String destination_id;

    private static final int JOB_ID = 101;
    private JobScheduler jobScheduler;
    private JobInfo jobInfo;



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_notifications_settings);

        if(getIntent().hasExtra("gpsCoords"))
            gpsCoords= getIntent().getExtras().getString("gpsCoords");


        lm= new LocationMethodsAndroid(this);

        contacts_array = new ArrayList<String>();
        name_number = new HashMap<>();
        name_id = new HashMap<>();

        items = new String[]{"15", "30", "60"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);


        // Retrieve a PendingIntent that will perform a broadcast
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);


        currentSharingContacts = (ListView) findViewById(R.id.currentSharingContacts);
        setList();
    }

    public void startAlarm(String timeToRepeat,String destination_id) {
        MyService ms = new MyService();
        Intent i = new Intent(this, MyService.class);
        i.putExtra("timeRepeat",timeToRepeat);
        Log.d("gpsCoords","gpsCoords <<start Alarm>> "+gpsCoords);
        i.putExtra("gpsCoords",gpsCoords);
        i.putExtra("destination_id",destination_id);
        startService(i);

        SendIntentProperly(); // intent to redirect user to OT again
    }


    public void cancelAlarm() {
        jobScheduler.cancel(JOB_ID); //cancels a particular job
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setList(){
        Map<String,String> contacts = lm.getAllContacts();

        for(String id: contacts.keySet()){
            if(id!=lm.getMyPhoneNumber()) {  // just to be sure that user's own contact does not appear in the list view
                String phoneNumber = id;
                String name = getContactName(id, this);
                contacts_array.add(name);
                name_number.put(name, id);
                name_id.put(name, id);
            }
        }

        adapter = new ArrayAdapter<String>(this,R.layout.list_item, contacts_array);
        currentSharingContacts.setAdapter(adapter);
        // ListView on item selected listener.
        currentSharingContacts.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // TODO Auto-generated method stub
                showNotification(name_number.get(contacts_array.get(position)));
            }
        });
    }

    public void showNotification(final String destination_id){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notification");
        builder.setMessage("Share Location every X min");

        final ArrayAdapter<String> adp = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);

        final Spinner sp = new Spinner(this);
        sp.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
        sp.setAdapter(adp);

        builder.setView(sp);

        // Set up the buttons
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //startAlarm(sp.getSelectedItem().toString(),destination_id);
                //NewAlarmReceiver nar = new NewAlarmReceiver(this,1000*600,);
                setNewAlarm(sp.getSelectedItem().toString(),destination_id);


            }
        });


        builder.setNegativeButton("Disable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cancelAlarm();
            }
        });

        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setNewAlarm(String interval,String dest) {

        // save destination_id and coords in sharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("com.example.AlarmSettings", MODE_PRIVATE).edit();
        editor.putString("destination_id",dest );
        editor.putString("gpsCoords",gpsCoords);
        editor.commit();

        ComponentName componentName = new ComponentName(this,MJobScheduler.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID,componentName);


        builder.setPeriodic(Integer.parseInt(interval)*60000);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setPersisted(true); // this job exists even after the system reboot

        jobInfo= builder.build();

        jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        jobScheduler.schedule(jobInfo); // schedules the job
        Toast.makeText(this, "Job scheduled", Toast.LENGTH_LONG).show(); // For example


        /*

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this,AlarmReceiver.class);
        intent.putExtra("destination_id",dest);
        intent.putExtra("gpsCoords",gpsCoords);
        PendingIntent pi = PendingIntent.getBroadcast(this,0,intent,0);


        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, 60);
        final long afterXSeconds = c.getTimeInMillis();

        am.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),afterXSeconds,pi);
        //am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,afterXSeconds,pi);
        */
    }


    public String getContactName( String phoneNumber, Context context)
    {
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName="";
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }
        //Toast.makeText(this,"name:  "+contactName,Toast.LENGTH_LONG).show();
        return contactName;
    }


    public void SendIntentProperly(){
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(sendIntent, 0);
        boolean isIntentSafe = activities.size() > 0;
            System.out.println("isIntentSafe : "+isIntentSafe );
            System.out.println("lista de atividades "+activities.toString());
        // Start an activity if it's safe
            if (isIntentSafe) {
            for (ResolveInfo activity : activities){
                if(activity.activityInfo.packageName.contains("owntracks")){
                    sendIntent.setPackage(activity.activityInfo.packageName);
                    sendIntent.putExtra("<CMIYC>goBackAfterSetGeoHashNotifications","goBackAfterSetGeoHashNotifications");
                    startActivity(sendIntent);
                }
            }
        }

    }
}