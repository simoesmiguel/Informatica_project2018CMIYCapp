package com.example.currentplacedetailsonmap;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.someoneelse.library.LocationMethodsAndroid;

import java.util.ArrayList;
import java.util.List;

public class Notifications extends AppCompatActivity {

    private ListView newNotificationsListView;
    private ListView seenNotificationsListView;
    private ArrayAdapter<String> adapter;
    private LocationMethodsAndroid lm;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        lm= new LocationMethodsAndroid(this);
        Intent intent = getIntent();
        if(intent.hasExtra("decodedMessage")){
            Bundle extras = getIntent().getExtras();
            String decodedMessage = extras.getString("decodedMessage");
            Log.d("a inserir para a db ","a inserir "+decodedMessage );
            lm.insertNewNotification(decodedMessage);
        }else{
            // Do something else
        }

        newNotificationsListView = (ListView) findViewById(R.id.newNotificationsListView);
        seenNotificationsListView = (ListView) findViewById(R.id.SeenNotificationsListView);
        setList_viewNewNotifications();
        setList_viewSeenNotifications();
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setList_viewNewNotifications(){
        final ArrayList<String> newNotifications = lm.getNewNotifications();
        Log.d("Notifications Array ","New Notifications Array :"+newNotifications);
        adapter = new ArrayAdapter<String>(this,R.layout.list_item, newNotifications);
        newNotificationsListView.setAdapter(adapter);
        // ListView on item selected listener.
        newNotificationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // TODO Auto-generated method stub
                showNotification(newNotifications.get(position),true);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setList_viewSeenNotifications(){
        final ArrayList<String> seenNotifications = lm.getSeenNotifications();
        Log.d("Notifications Array ","Seen Notifications Array :"+seenNotifications);
        adapter = new ArrayAdapter<String>(this,R.layout.list_item, seenNotifications);
        seenNotificationsListView.setAdapter(adapter);
        // ListView on item selected listener.
        seenNotificationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // TODO Auto-generated method stub
                showNotification(seenNotifications.get(position),false);
            }
        });
    }


    public void showNotification(final String notification, final Boolean newNotification){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notification");
        builder.setMessage(notification);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(newNotification == true){
                    lm.deleteNewNotification(notification);
                    Log.d("Notification Deleted","Notification Deleted");
                    lm.insertSeenNotification(notification);
                    setList_viewNewNotifications();
                    setList_viewSeenNotifications();
                }
            }
        });
        builder.show();

    }



}
