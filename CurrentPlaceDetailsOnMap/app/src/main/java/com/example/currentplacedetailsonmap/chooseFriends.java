package com.example.currentplacedetailsonmap;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.example.someoneelse.library.LocationMethodsAndroid;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.vision.text.Line;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public class chooseFriends extends AppCompatActivity {

    private ArrayList<String> contacts_array ; // array dos contactos (posteriormente vai ser feita uma chamada ao serviço para aceder aos contactos)
    private ArrayList<String> send_to ; // array que tem os contactos selecionados
    private LinearLayout ll;
    private Button b;
    private String button_text;
    private CheckBox cb;
    private Cursor cursor1;
    private Map<String,String> name_number;
    private Map<String,String> name_id;
    private LocationMethodsAndroid lm;
    private String selected_address;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS=0;
    private static final int REQUEST_READ_PHONE_STATE=0;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;


    private static final String TAG = chooseFriends.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private Uri uriContact;
    private String phoneNumber;
    private boolean fromAnotherApp= false;
    private String meetingTag;
    private String geofenceTag;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_friends);
        b = (Button) findViewById(R.id.sendTo); // vou buscar o botao para lhe mudar o texto
        button_text = (String) b.getText();

        Intent intent = getIntent();
        checkExtras(intent);
        showContacts(); // just to get the permission in order to access contacts

        contacts_array = new ArrayList<String>();
        send_to = new ArrayList<String>();
        name_number = new HashMap<>();
        name_id = new HashMap<>();


        try {
            lm= new LocationMethodsAndroid(this);
            // insert contacts into db
            //lm.createConcts();

            //get all contacts from db
            Map<String,String> contacts = lm.getAllContacts();

            for(String id: contacts.keySet()){
                phoneNumber = id;
                String name=showContacts();
                if(name.equals(""))
                    name=showContacts();
            //    if(!phoneNumber.equals(lm.getMyPhoneNumber())) {
                    Log.d("name_id ", name + "_" + id);
                    contacts_array.add(name);
                    name_number.put(name, id);
                    name_id.put(name, id);
            //    }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        setCheckBox();

    }

    public void checkExtras(Intent intent){
        if(intent.hasExtra("decrypted_message") ){
            selected_address= intent.getExtras().getString("decrypted_message");
            if(intent.hasExtra("fromAnotherApp")){
                fromAnotherApp = true;
            }
        }else if(intent.hasExtra("meetingPointTag")){
            // another type of extra
            fromAnotherApp = true;
            meetingTag = intent.getExtras().getString("meetingPointTag");
        }else if(intent.hasExtra("geofenceTag")){
            fromAnotherApp = true;
            geofenceTag = intent.getExtras().getString("geofenceTag");
        }

    }


    public void setCheckBox(){  // fill checkBox with the contacts

        ll = (LinearLayout) findViewById(R.id.checkboxLayout);
        for(int i=0;i<contacts_array.size();i++){

            cb = new CheckBox(getApplicationContext());
            cb.setTextColor(Color.GRAY);
            cb.setId(i);
            cb.setText(contacts_array.get(i));
            cb.setOnClickListener(getOnClickDoSomething(cb));
            ll.addView(cb);
        }
    }




    View.OnClickListener getOnClickDoSomething(final Button button){
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //send_to.add(button.getText().toString());
                button_text += ", "+button.getText().toString();
                b.setText(button_text);
            }
        };

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sendMessage(View view) throws NoSuchPaddingException, ShortBufferException, ClassNotFoundException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {
        //Intent share_current_locat = new Intent(this, nvigationDrawer.class );
        //startActivity(share_current_locat);

        //for(String name: send_to) {
        //    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + name_number.get(name)));
        //    intent.putExtra("sms_body", lm.generateAddressMessage(selected_address, 5, Integer.parseInt(name_id.get(name))));
        //    startActivity(intent);
        //}

        //for(String name: send_to) {
        //    lm.generateAddressMessage("ola estou em Aveiro", 5, Integer.parseInt(name_id.get(name)));
        //    SmsManager.getDefault().sendTextMessage(name_number.get(name), null, "es gay", null, null);
        //}
        String text = b.getText().toString();
        Log.d("button text","button text :"+text);


        String [] allselecteNames = text.split(", ");
        for(int i=1;i<allselecteNames.length;i++) {
            Log.d("send_to","send_to >"+allselecteNames[i]);
            send_to.add(allselecteNames[i]);
        }
        if(fromAnotherApp){
            if(meetingTag!=null){
                //save meetingPoint to database
                lm.saveMeetingCoords(meetingTag.split(",")[0],new double[]{Double.parseDouble(meetingTag.split(",")[1]),Double.parseDouble(meetingTag.split(",")[2])});
                getBacktoTheOtherApp();
            }else if(geofenceTag!=null){
                lm.saveZone(geofenceTag.split(",")[0], new double[]{Double.parseDouble(geofenceTag.split(",")[1]),Double.parseDouble(geofenceTag.split(",")[2])},Double.parseDouble(geofenceTag.split(",")[3]));
                //create Geofence



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
                            sendIntent.putExtra("<CMIYC>goBackAfterSaveGeofence","goBackAfterSaveGeofence");
                            startActivity(sendIntent);
                        }
                    }
                }

            }else
                getBacktoTheOtherApp();
        }else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.SEND_SMS)){

                }else{
                    ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.SEND_SMS},MY_PERMISSIONS_REQUEST_SEND_SMS);
                }
            }else{
                sendFinalMessage();
            }
        }
    }





    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sendFinalMessage(){
        for (String name : send_to) {
            Log.d("name ","name "+name);
            Log.d("name_id","name_id"+name_id.get(name));
            Log.d("name_number","name_number "+name_number.get(name));
            SmsManager smsManager = SmsManager.getDefault();
            try {
                String encryptedMessage = lm.generateAddressMessage(selected_address, Integer.parseInt(lm.getMyPhoneNumber()), Integer.parseInt(name_id.get(name)));
                Log.d("encryptedMessage","encryptedMessage: "+Base64.encodeToString(encryptedMessage.getBytes(),Base64.NO_WRAP));
                //Toast.makeText(getApplicationContext(),"number "+name_number.get(name), Toast.LENGTH_SHORT).show();
                ArrayList<String> parts = smsManager.divideMessage(Base64.encodeToString(encryptedMessage.getBytes(),Base64.NO_WRAP));
                //ArrayList<String> parts = smsManager.divideMessage(encryptedMessage);

                // smsManager.sendTextMessage(name_number.get(name), null,"Pqp esta merda", null, null);
                smsManager.sendMultipartTextMessage(name_number.get(name), null,parts, null, null);
            } catch (Exception e) {
                e.printStackTrace();
                if(e.toString().contains(Manifest.permission.READ_PHONE_STATE) && ContextCompat
                        .checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission
                            .READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);

                } // else it's some other exception
            }
        }
        Toast.makeText(getApplicationContext(), "Sent successfully ", Toast.LENGTH_SHORT).show();

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendFinalMessage();
                } else {
                    Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (true) {
                    return;
                } else {
                    Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }



    private String showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        String name="";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }
        else {
            name = getContactName(phoneNumber, this);
        }
        return name ;
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getBacktoTheOtherApp(){  // goes back to the app which called CMIYC
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");

        HashMap<String [],String> nameMobileNumber_encryptedMessage = new HashMap<>();
        for (String name : send_to) {
            try {
                if(meetingTag!=null){
                    //generateMeetingMessage(double[] dstCoords, String name, int senderId, int destId)
                    String encryptedMessage =
                            lm.generateMeetingMessage(new double[]{Double.parseDouble(meetingTag.split(",")[1]),Double.parseDouble(meetingTag.split(",")[2])},
                            meetingTag.split(",")[0],
                            Integer.parseInt(lm.getMyPhoneNumber()),
                            Integer.parseInt(name_id.get(name))
                            );
                    nameMobileNumber_encryptedMessage.put(new String[]{name, name_id.get(name)}, encryptedMessage);
                }
                else {
                    String encryptedMessage = lm.generateAddressMessage(selected_address, Integer.parseInt(lm.getMyPhoneNumber()), Integer.parseInt(name_id.get(name)));
                    Log.d("encryptedMessage", "encryptedMessage: " + Base64.encodeToString(encryptedMessage.getBytes(), Base64.NO_WRAP));
                    nameMobileNumber_encryptedMessage.put(new String[]{name, name_id.get(name)}, encryptedMessage);
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }

        if(meetingTag!=null) {
            startSpecificAppProperly(sendIntent,"<CMIYC>nameMobileNumber_encryptedMessageMeetingPoint",nameMobileNumber_encryptedMessage);

        }else
            startSpecificAppProperly(sendIntent,"<CMIYC>nameMobileNumber_encryptedMessage",nameMobileNumber_encryptedMessage);



    }

    public void startSpecificAppProperly(Intent intent,String extra,HashMap<String [],String> extraValue){  //st
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;
        System.out.println("isIntentSafe : "+isIntentSafe );
        System.out.println("lista de atividades "+activities.toString());
        // Start an activity if it's safe
        if (isIntentSafe) {
            for (ResolveInfo activity : activities){
                if(activity.activityInfo.packageName.contains("owntracks")){
                    intent.setPackage(activity.activityInfo.packageName);
                    intent.putExtra(extra,extraValue);
                    startActivity(intent);
                }
            }
        }


    }

}





