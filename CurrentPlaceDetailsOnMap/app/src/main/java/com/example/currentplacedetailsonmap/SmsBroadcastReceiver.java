package com.example.currentplacedetailsonmap;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.someoneelse.library.LocationMethodsAndroid;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;



public class SmsBroadcastReceiver extends BroadcastReceiver {
    private LocationMethodsAndroid lm;
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        lm = new LocationMethodsAndroid(context);

        if (intentExtras != null) {
            /* Get Messages */
            Object[] sms = (Object[]) intentExtras.get("pdus");
            final SmsMessage[] messages = new SmsMessage[sms.length];


            for (int i = 0; i < sms.length; ++i) {
                /* Parse Each Message */
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);
                String phone = smsMessage.getOriginatingAddress();
                String message = smsMessage.getMessageBody().toString();

                StringBuffer content = new StringBuffer();
                for (int j = 0; j < sms.length; j++) {
                    messages[j] = SmsMessage.createFromPdu((byte[])sms[j]);
                }
                for (SmsMessage m : messages) {
                    content.append(m.getDisplayMessageBody());
                }

                String mySmsText = content.toString();
                try {
                    sendNotification(context, mySmsText, phone);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            //Toast.makeText(context, phone + ": " + message, Toast.LENGTH_SHORT).show();
/*
                Notification notification = new Notification(android.R.drawable.ic_popup_reminder,
                        "CMIYC", System.currentTimeMillis());
                notification.defaults |= Notification.DEFAULT_SOUND;
                notification.defaults |= Notification.DEFAULT_VIBRATE;

*/
            //Intent intent2 = new Intent(context,MapsActivityCurrentPlace.class);
            //context.startActivity(intent2);
            //intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //context.startActivity(intent2);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sendNotification(Context context, String message,String phone) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, ClassNotFoundException {


        //String decodedMessage = lm.recvAndDecipher(message);
        String name="";
        try {
            name = getContactName(phone, context);
        }catch(Exception e){
            e.printStackTrace();
        }
        //Get an instance of NotificationManager//
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.cmiyc_icon_notif)
                        .setContentTitle("CMIYC")
                        .setContentText(new StringBuilder().append(name).append(" sent you a message").toString());

        //long time_notifcation= Calendar.getInstance().getTimeInMillis();

        //decode message
        //lm.recvAndDecipher(message);

        // Gets an instance of the NotificationManager service//

        Intent notificationIntent = new Intent(context, Notifications.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        String decoded_message = new String(Base64.decode(message,Base64.NO_WRAP));
        Log.d("decoded_message ","decoded_message "+decoded_message);
        notificationIntent.putExtra("decodedMessage",name+" shared: "+lm.recvAndDecipher(decoded_message));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(001, mBuilder.build());
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
        Toast.makeText(context,"name:  "+contactName,Toast.LENGTH_LONG).show();
        return contactName;
    }


}
