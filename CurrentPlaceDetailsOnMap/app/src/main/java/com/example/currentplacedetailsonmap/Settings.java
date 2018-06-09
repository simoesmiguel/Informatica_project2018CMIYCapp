package com.example.currentplacedetailsonmap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class Settings extends AppCompatActivity {

    private Switch incomingNotificationsSwitch, outgoingNotificationsSwitch;
    private Button customizeIncomingButton,customizeOutgoingButton,newZoneButton,editZoneButton;
    private String gpscoords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if(getIntent().hasExtra("gpsCoords"))
            gpscoords= getIntent().getExtras().getString("gpsCoords");

        //get all the switches and buttons
        incomingNotificationsSwitch = (Switch) findViewById(R.id.enableIncomingSwitch);
        outgoingNotificationsSwitch = (Switch) findViewById(R.id.enableOutgoingSwitch);
        customizeIncomingButton = (Button) findViewById(R.id.customizeIncomingButton);
        customizeOutgoingButton = (Button) findViewById(R.id.customizeOutgoingButton);
        newZoneButton = (Button) findViewById(R.id.newZoneButton);
        editZoneButton = (Button) findViewById(R.id.editZoneButton);

        SharedPreferences sharedPrefs = getSharedPreferences("com.example.Settings", MODE_PRIVATE);
        incomingNotificationsSwitch.setChecked(sharedPrefs.getBoolean("incomingNotificationsSwitch", true));
        outgoingNotificationsSwitch.setChecked(sharedPrefs.getBoolean("outgoingNotificationsSwitch", true));


        incomingNotificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(incomingNotificationsSwitch.isChecked()){
                    Toast.makeText(getApplicationContext(),"enabled", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = getSharedPreferences("com.example.Settings", MODE_PRIVATE).edit(); // save switch state
                    editor.putBoolean("incomingNotificationsSwitch", true);
                    editor.commit();
                }else{
                    Toast.makeText(getApplicationContext(),"disabled", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = getSharedPreferences("com.example.Settings", MODE_PRIVATE).edit();
                    editor.putBoolean("incomingNotificationsSwitch", false);
                    editor.commit();
                }

            }
        });
        outgoingNotificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(outgoingNotificationsSwitch.isChecked()){
                    Toast.makeText(getApplicationContext(),"enabled", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = getSharedPreferences("com.example.Settings", MODE_PRIVATE).edit(); // save switch state
                    editor.putBoolean("outgoingNotificationsSwitch", true);
                    editor.commit();
                }else{
                    Toast.makeText(getApplicationContext(),"disabled", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = getSharedPreferences("com.example.Settings", MODE_PRIVATE).edit();
                    editor.putBoolean("outgoingNotificationsSwitch", false);
                    editor.commit();
                }
            }
        });

        customizeIncomingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        customizeOutgoingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToOutgoingNotificationsSettings();
            }
        });
        newZoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        editZoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public void goToOutgoingNotificationsSettings(){
        Intent outgoing_settings = new Intent(this,outgoingNotificationsSettings.class);
        outgoing_settings.putExtra("gpsCoords",gpscoords);
        startActivity(outgoing_settings);
    }


}
