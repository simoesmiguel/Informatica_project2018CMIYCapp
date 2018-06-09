package com.example.currentplacedetailsonmap;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetDirectionsData extends AsyncTask<Object,String,String> {


    GoogleMap mMap;
    String url;
    LatLng startLatLng,endLatLng;
    HttpURLConnection httpURLConnection=null;
    String data = "";
    InputStream inputStream=null;
    Context c;
    Map<String,String[]> dist_time = new HashMap<>();
    String meetingPointName ;

    GetDirectionsData(Context c){this.c=c;}

    @Override
    protected String doInBackground(Object... params) {

        mMap = (GoogleMap)params[0];
        url = (String)params[1];
        startLatLng = (LatLng)params[2];
        endLatLng = (LatLng)params[3];
        meetingPointName= (String) params[4];

        try{
            URL myurl = new URL(url);
            httpURLConnection = (HttpURLConnection) myurl.openConnection();
            httpURLConnection.connect();


            inputStream =httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();
            String line ="";
            while ((line = bufferedReader.readLine())!=null){
                sb.append(line);
            }
            data = sb.toString();
            bufferedReader.close();
            System.out.println(">GETDIRECTIONSDATA >");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



        return data;
    }


    @Override
    protected void onPostExecute(String s) {
        try{
            JSONObject jsonObject = new JSONObject(s);
            System.out.println("JSON OBJECT FROM GOOGLE DIRECTIONS API \n"+jsonObject);

            JSONArray jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");

            String duration = jsonArray.getJSONObject(0).getJSONObject("duration").getString("text");
            String distance = jsonArray.getJSONObject(0).getJSONObject("distance").getString("text");

            dist_time.put(meetingPointName,new String[]{duration,distance});
            ShareCurrentLocation.setMeetingPoints(getMapTimeDistance());
            //MapsActivityCurrentPlace.redirectToshareCurrentlocationPageNOW();


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Map getMapTimeDistance(){
        return dist_time;
    }
}
