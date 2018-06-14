package com.example.currentplacedetailsonmap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;


import com.example.someoneelse.library.LocationMethodsAndroid;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;


public class ShareCurrentLocation extends AppCompatActivity {



    private ScrollView sv;
    private LinearLayout ll;
    private RelativeLayout rl;
    private Button b ;
    private ArrayList<String> likely_places ;
    private Set<String> likely_places_withGranularity ;
    private static ArrayAdapter<String> adapter;
    private ListView list_view1;
    private static ListView list_view2;
    private static String selected_address="";
    private String error_message;
    private boolean fromAnotherApp= false;
    private static Context context;
    private static Set<String> finalSet = new HashSet<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_current_location);

        Intent intent = getIntent();

        list_view1 = (ListView) findViewById(R.id.listView_choose_location);
        list_view2 = (ListView) findViewById(R.id.listView_choose_meetingPoint);
        likely_places = new ArrayList<>();
        likely_places_withGranularity = new HashSet<>();
        context= this;
        checkExtras(intent);

        error_message="you got to choose one likely place";




    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
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
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void checkExtras(Intent intent){
        if(intent.hasExtra("array") ){
            likely_places= intent.getExtras().getStringArrayList("array");

        }else if(intent.hasExtra("gpsCoords")){
            fromAnotherApp = true;
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            String gpscoords= intent.getExtras().getString("gpsCoords");

            try {
                addresses = geocoder.getFromLocation(Double.parseDouble(gpscoords.split(",")[0]),Double.parseDouble(gpscoords.split(",")[1]), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex
                likely_places.add(address);


            } catch (IOException e) {
                e.printStackTrace();
            }

           // String[] a = {"R. Vala 54", "R. Vala 54, 3515-247 Viseu, Portugal", "Quinta Fontinha da Pedra, R. São José 5, 3515-247 Viseu, Portugal"," Transportes António Valente Lopes, Lda"," Rua Campo Da Bola, Moure De Madalena, Viseu", "3515-334 Viseu, Portugal"};
            String [] b ={"Departamento de Biologia da Universidade de Aveiro","Aveiro, Portugal, Universidade de Aveiro","DETI - Departamento de Electrónica","Telecomunicações e Informática"," 3810-193 Aveiro, Portugal","Departamento de Ambiente e Ordenamento, Aveiro",""};

            likely_places = new ArrayList<String>(Arrays.asList(b));



        }else{
            // another type of extra
        }
        setLikely_places_granularity(likely_places);
        setLikely_places();

    }


    public void setLikely_places_granularity(ArrayList<String> lp){
        LocationMethodsAndroid lm = new LocationMethodsAndroid(this);

        //Log.d("location","aqui"+ lista.toString());
       for(String place: lp){
           List<String> lista = lm.getLocationSuggestion(place);
            for(String s: lista){
                likely_places_withGranularity.add(s);

            }

       }
    }

    public static void setMeetingPoints(Map<String, String[]> dist_time){

        for(String s:dist_time.keySet()){  // convert from Set to ArrayList
            System.out.println("MAPA:" + s+" : [Time]"+dist_time.get(s)[0]+"  [Distance]"+dist_time.get(s)[1]);
            String a ="     "+ s+"\n [Time]"+dist_time.get(s)[0]+"  [Distance]"+dist_time.get(s)[1];
            finalSet.add(a);
        }

        final List<String> finalArray = new ArrayList<String>();
        for(String s:finalSet){  // convert from Set to ArrayList
            finalArray.add(s);
        }

        System.out.println("FINAL ARRAY :" +finalArray);

        adapter = new ArrayAdapter<String>(context,R.layout.list_item, finalArray);
        list_view2.setAdapter(adapter);
        // ListView on item selected listener.
        list_view2.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // TODO Auto-generated method stub
                Toast.makeText(context,finalArray.get(position),
                        Toast.LENGTH_SHORT).show();
                selected_address = finalArray.get(position);  // set the variable selected_address to use in the next activity
            }

        });
    }



    public void setLikely_places(){
        final List<String> likely_places_withGranularity_final = new ArrayList<String>();
        for(String s:likely_places_withGranularity){  // convert from Set to ArrayList
            likely_places_withGranularity_final.add(s);
        }

        Comparator<String> temp = new Comparator<String>() {  // sort  likely_places_withGranularity_final by length
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public int compare(String s, String t1) {
                return Integer.compare(t1.length(), s.length());
            }
        };
        Collections.sort(likely_places_withGranularity_final, temp);

        adapter = new ArrayAdapter<String>(this,R.layout.list_item, likely_places_withGranularity_final);
        list_view1.setAdapter(adapter);
        // ListView on item selected listener.
        list_view1.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(),likely_places_withGranularity_final.get(position),
                        Toast.LENGTH_SHORT).show();
                selected_address = likely_places_withGranularity_final.get(position);  // set the variable selected_address to use in the next activity
            }

        });

    }

    public void shareWithFriends(View view) {
        if (selected_address != "") {
            Intent choose_friends = new Intent(getApplicationContext(), chooseFriends.class);
            // encrypt message

            //LocationMethods.generateAddressMessage(selected_address,sender_id, dst_id);
            choose_friends.putExtra("decrypted_message", selected_address);
            if(fromAnotherApp){ // if this activity was called by another app (for instance OwnTracks)
                choose_friends.putExtra("fromAnotherApp","fromAnotherApp");
            }
            Log.d("share","sharing location");
            startActivity(choose_friends);
        }
        else{
            Toast.makeText(getApplicationContext(),error_message,
                    Toast.LENGTH_SHORT).show();
        }
    }










}





