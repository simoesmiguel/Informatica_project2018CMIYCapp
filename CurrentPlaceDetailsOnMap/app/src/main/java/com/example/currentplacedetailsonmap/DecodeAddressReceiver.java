package com.example.currentplacedetailsonmap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;

import com.example.someoneelse.library.LocationMethodsAndroid;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class DecodeAddressReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {
        LocationMethodsAndroid lm = new LocationMethodsAndroid(context);

        if(intent.hasExtra("address")){
            try {
                System.out.println("Receiving address to decipher ===========================");
                String decodedAddress = lm.recvAndDecipher(intent.getExtras().getString("address"));
                System.out.println("<Decoded Address Receiver> decodedAddress: "+decodedAddress);
                JSONObject jo = new JSONObject(decodedAddress);
                String name = null;
                Map<String,String> contacts = lm.getAllContacts();
                for(String mobile_number: contacts.keySet()) {
                    if (mobile_number.equals(jo.getString("user_id"))) { name  = getContactName(mobile_number, context);
                    }
                }

                String [] extras = {"decodedAddress@"+decodedAddress ,"user_id@"+name};
                sendHidenIntentProperly(context,extras);

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else if(intent.hasExtra("isInGeohash")){
            System.out.println("Receiving geoHash to decipher ===========================");
            try {
                System.out.println(intent.getExtras().getString("isInGeohash"));
                String decodedAddress = lm.recvAndDecipher(intent.getExtras().getString("isInGeohash"));
                JSONObject jo = new JSONObject(decodedAddress);
                String geohash = jo.getString("geohash");
                String id = jo.getString("user_id");

                String name = null;
                Map<String,String> contacts = lm.getAllContacts();
                for(String mobile_number: contacts.keySet()) {
                    System.out.println("MOBILE_NUMBER :"+mobile_number);
                    System.out.println("CONTACT NAME : "+getContactName(mobile_number, context));
                    if (mobile_number.equals(id)) { name  = getContactName(mobile_number, context);
                    }
                }
                boolean b=LocationMethodsAndroid.inGeohash(intent.getExtras().getDoubleArray("coords"), geohash);
                String [] extras = {"inGeohash@"+String.valueOf(b),"user_id@"+name};
                sendHidenIntentProperly(context, extras);

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if(intent.hasExtra("getMobileNumber")){
            String myPhoneNumber = lm.getMyPhoneNumber();
            if(myPhoneNumber!="")
                sendHidenIntentProperly(context,new String [] {"myPhoneNumber@"+myPhoneNumber});
        }else if(intent.hasExtra("meetingPoint")) {
            try {
                System.out.println("Receiving meetingPoint to decipher ===========================");
                String decodedMeetingPoint = lm.recvAndDecipher(intent.getExtras().getString("meetingPoint"));
                System.out.println("<Decoded Address Receiver> meetingPoint: " + decodedMeetingPoint);
                JSONObject jo = new JSONObject(decodedMeetingPoint);
                String name = null;
                Map<String, String> contacts = lm.getAllContacts();
                for (String mobile_number : contacts.keySet()) {
                    if (mobile_number.equals(jo.getString("user_id"))) {
                        name = getContactName(mobile_number, context);
                    }
                }

                String[] extras = {"decodedMeetingPoint@" + decodedMeetingPoint, "user_id@" + name};
                sendHidenIntentProperly(context, extras);

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if(intent.hasExtra("getAllMeetingPoints")){
            try {
                System.out.println("<DecodeAddressReceiver> GETTING ALL MEETING POINTS");
                Map<String, double[]> map =lm.getAllMeetingCoords();
                Map<String, double[]> map2 =lm.getAllZones();
                System.out.println("<Decode ADDRESS RECEIVER> PRINTING ALL GEOFENCES "+map2);

               // startSpecificAppProperly(context,"AllMeetingCoords", new HashMap<String, double[]>(map));

                Intent intentMeetingPointAndZones = new Intent(Intent.ACTION_SEND);
                intentMeetingPointAndZones.setType("text/plain");

                PackageManager packageManager = context.getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(intentMeetingPointAndZones, 0);
                boolean isIntentSafe = activities.size() > 0;
                System.out.println("isIntentSafe : "+isIntentSafe );
                System.out.println("lista de atividades "+activities.toString());
                // Start an activity if it's safe
                if (isIntentSafe) {
                    for (ResolveInfo activity : activities){
                        if(activity.activityInfo.packageName.contains("owntracks")){
                            intentMeetingPointAndZones.setPackage(activity.activityInfo.packageName);
                            intentMeetingPointAndZones.putExtra("AllMeetingCoords",new HashMap<String, double[]>(map));
                            intentMeetingPointAndZones.putExtra("AllZones",new HashMap<String, double[]>(map2));
                            context.startActivity(intentMeetingPointAndZones);
                        }
                    }
                }


            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }else if(intent.hasExtra("justSaveMeetingPoint")){
            String meetingPoinData = intent.getExtras().getString("justSaveMeetingPoint");
            try {
                lm.saveMeetingCoords(meetingPoinData.split(",")[0],new double[]{Double.parseDouble(meetingPoinData.split(",")[1]),Double.parseDouble(meetingPoinData.split(",")[2])});
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }else if(intent.hasExtra("deleteMeetingPoint")){
            try {
                Map<String, double[]> map =lm.getAllMeetingCoords();
                System.out.println("<DECODE ADDRESS RECEIVER> ALL COORDS: \n"+map);
                System.out.println("<DECODE ADDRESS RECEIVER> DELETING MEETING POINTS");
                lm.deleteMeetingCoords(intent.getExtras().getString("deleteMeetingPoint"));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


    }

    public void sendHidenIntentProperly(Context context,String [] extras){  //hided intent
        Intent intent2 = new Intent(Intent.ACTION_VIEW);
        intent2.setType("text/plain");

        System.out.println("<DECODE ADDRESS RECEIVER> "+ Arrays.toString(extras));

        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent2, 0);
        boolean isIntentSafe = activities.size() > 0;
        //System.out.println("isIntentSafe : "+isIntentSafe );
        //System.out.println("lista de atividades "+activities.toString());

        // Start an activity if it's safe
        if (isIntentSafe) {
            for (ResolveInfo activity : activities){
                if(activity.activityInfo.packageName.contains("owntracks")){
                    intent2.setData(Uri.parse("receiver://123"));
                    intent2.setPackage(activity.activityInfo.packageName);

                    for(String s: extras){
                        String[] a = s.split("@");
                        intent2.putExtra(a[0],a[1]);
                    }
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    System.out.println("<AlarmReceiver> decodedAddress: "+intent2.getExtras());
                    context.sendBroadcast(intent2);
                }
            }
        }
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

}
