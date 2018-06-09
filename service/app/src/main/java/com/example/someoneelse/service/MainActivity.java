package com.example.someoneelse.service;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void callService(){
        Intent in = new Intent(this ,com.example.currentplacedetailsonmap.chooseFriends.java);
        startActivity(in);
    }

}
