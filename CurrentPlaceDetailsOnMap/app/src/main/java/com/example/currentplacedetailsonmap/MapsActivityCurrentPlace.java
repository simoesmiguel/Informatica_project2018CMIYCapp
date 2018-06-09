package com.example.currentplacedetailsonmap;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.someoneelse.library.LocationMethodsAndroid;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Channel;

import java.util.*;



/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class MapsActivityCurrentPlace extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String TAG = MapsActivityCurrentPlace.class.getSimpleName();
    private static GoogleMap mMap;
    private CameraPosition mCameraPosition;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;



    private ArrayList<String> likely_places ;
    private ShareCurrentLocation scl;
    private double lat;
    private double lon;


    private  SeekBar simpleSeekBar;
    private int progressChangedValue=0;
    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS =0;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int PERMISSION_REQUEST_CODE = 10;
    private String wantPermission = Manifest.permission.READ_PHONE_STATE;
    Activity activity = MapsActivityCurrentPlace.this;

    private static String selectedMeetingPoint;
    private String  gpsCoords;


    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton addFriend,meetingPoint,notifications,settings,help;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                Toast.makeText(this,"Welcome to CMIYC", Toast.LENGTH_SHORT).show();

            } else if (type.startsWith("image/")) {

            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {

            }
        } else {
            // Handle other intents, such as being started from the home screen
        }




        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // added by me (MMS)
        likely_places = new ArrayList<>();
        scl = new ShareCurrentLocation();


        if(intent.hasExtra("decodedMessage")){
            Bundle extras = getIntent().getExtras();
            String decodedMessage = extras.getString("decodedMessage");
            Toast.makeText(this,decodedMessage,Toast.LENGTH_LONG).show();
        }else if(intent.hasExtra("QrCode")){
            Intent readQr = new Intent(this, QReader.class );
            startActivity(readQr);
        }else if(intent.hasExtra("GPScoords")){
            Bundle extras = getIntent().getExtras();
            gpsCoords = extras.getString("GPScoords");
            //Toast.makeText(this,decodedMessage,Toast.LENGTH_LONG).show();
            try {
                redirectToshareCurrentlocationPage(gpsCoords);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }else if(intent.hasExtra("notifications_settings")){
            Bundle extras = getIntent().getExtras();
            String gpsCoords = extras.getString("notifications_settings");
            redirectToSettingsPage(gpsCoords);
        }else if(intent.hasExtra("shareCoordsOnly")){
            Bundle extras = getIntent().getExtras();
            String gpsCoords = extras.getString("shareCoordsOnly");
            encodeCoordsAndSendBack(gpsCoords);
        }else if(intent.hasExtra("saveMeetingPoint")){
            String meetingPointTag = getIntent().getExtras().getString("saveMeetingPoint");
            redirectToChooseFriendsPage(meetingPointTag);
        }



        // The distance you want to increase your square (in meters)

        simpleSeekBar=(SeekBar)findViewById(R.id.discreteSeekBar); // initiate the Seek bar
        int seekBarValue= simpleSeekBar.getProgress(); // get progress value from the Seek bar
        Log.d("seekBar","currentSeekBarValue "+seekBarValue);

        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChangedValue=i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMap.clear();
                generateGeohashSquare();

            }
        });

        floatingActionMenu = (FloatingActionMenu) findViewById(R.id.floatingActionMenu);
        addFriend = (FloatingActionButton) findViewById(R.id.addFriendFAB);
        meetingPoint = (FloatingActionButton) findViewById(R.id.setupMeetingPointFAB);
        notifications = (FloatingActionButton) findViewById(R.id.notificationsFAB);
        settings = (FloatingActionButton) findViewById(R.id.settingsFAB);
        help = (FloatingActionButton) findViewById(R.id.helpFAB);


        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readQR(view);
            }
        });
        meetingPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seeNotifications();
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSettingsPage();
            }
        });
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


    }
    public void getDistanceAndTimeToAllMeetingPoints(String gpsCoords) throws ClassNotFoundException {

        LocationMethodsAndroid lm = new LocationMethodsAndroid(this);
        //get all meeting points
        Map<String, double[]> map =lm.getAllMeetingCoords();
        GetDirectionsData getDirectionsData = null;
        for (String s : map.keySet()) {
            System.out.println(" MAPP " + s + " : lat:" + map.get(s)[0] + "lon:" + map.get(s)[1]);


            StringBuilder sb = new StringBuilder();
            Object[] dataTransfer = new Object[5];


            sb.append("https://maps.googleapis.com/maps/api/directions/json?");
            sb.append("origin=" + Double.parseDouble(gpsCoords.split(",")[0]) + "," + Double.parseDouble(gpsCoords.split(",")[1]));
            sb.append("&destination=" + map.get(s)[0] + "," + map.get(s)[1]);
            sb.append("&key=" + "AIzaSyBflfO3Bo5efUGcoLHSqh2B3AaPLjnCGVI");

            getDirectionsData = new GetDirectionsData(getApplicationContext());
            dataTransfer[0] = mMap;
            dataTransfer[1] = sb.toString();
            dataTransfer[2] = new LatLng(Double.parseDouble(gpsCoords.split(",")[0]), Double.parseDouble(gpsCoords.split(",")[1]));
            dataTransfer[3] = new LatLng(map.get(s)[0], map.get(s)[1]);
            dataTransfer[4] = s;
            getDirectionsData.execute(dataTransfer);
        }


    }

    public static GoogleMap getMap(){
        return mMap;
    }


    public void checkReadFromContactsPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkReceiveSmsPermission(){
        if(checkSelfPermission( Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            Log.d("receive permission","asking for sms receive permission");
            requestPermissions( new String[]{Manifest.permission.RECEIVE_SMS}, MY_PERMISSIONS_REQUEST_RECEIVE_SMS);
        }
    }


    private void checkPhonestatePermission(){
        if (!checkPermission(wantPermission)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, wantPermission)){
                Toast.makeText(activity, "Phone state permission allows us to get phone number. Please allow it for additional functionality.", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(activity, new String[]{wantPermission},PERMISSION_REQUEST_CODE);
        }


    }

    private boolean checkPermission(String permission){
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ContextCompat.checkSelfPermission(activity, permission);
            if (result == PackageManager.PERMISSION_GRANTED){
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private void drawBounds (LatLngBounds bounds, int color) {
        PolygonOptions polygonOptions =  new PolygonOptions()
                .add(new LatLng(bounds.northeast.latitude, bounds.northeast.longitude))
                .add(new LatLng(bounds.southwest.latitude, bounds.northeast.longitude))
                .add(new LatLng(bounds.southwest.latitude, bounds.southwest.longitude))
                .add(new LatLng(bounds.northeast.latitude, bounds.southwest.longitude))
                .strokeColor(color);

        mMap.addPolygon(polygonOptions);
    }

    public void encodeCoordsAndSendBack(String gpsCoords){
        LocationMethodsAndroid lm= new LocationMethodsAndroid(this);
       // String encodedCoords= lm.generateAddressMessage(gpsCoords,lm.getMyPhoneNumber(), ???);
        Toast.makeText(this,"ainda nao esta implementado", Toast.LENGTH_SHORT);
    }

    public void redirectToChooseFriendsPage(String meetingPointTag){
        Intent settings = new Intent(this, chooseFriends.class);
        settings.putExtra("meetingPointTag",meetingPointTag);
        startActivity(settings);
    }


    public void redirectToshareCurrentlocationPage(String gpsCoords) throws ClassNotFoundException {
        getDistanceAndTimeToAllMeetingPoints(gpsCoords);
        Intent share_current_locat = new Intent(this, ShareCurrentLocation.class );
        share_current_locat.putExtra("gpsCoords",gpsCoords);
        startActivity(share_current_locat);
    }

    /*
    public static void redirectToshareCurrentlocationPageNOW(String gpsCoords) throws ClassNotFoundException {
        Intent share_current_locat = new Intent(this, ShareCurrentLocation.class );
        share_current_locat.putExtra("gpsCoords",gpsCoords);
        startActivity(share_current_locat);
    }
*/



        public void redirectToSettingsPage(String gpsCoords){
        Intent settings = new Intent(this, Settings.class );
        settings.putExtra("gpsCoords",gpsCoords);
        startActivity(settings);
    }


    public void seeNotifications(){
        Intent seeNotifications = new Intent(this, Notifications.class );
        startActivity(seeNotifications);
    }


    public void shareCurrent(View view){
        Intent share_current_locat = new Intent(this, ShareCurrentLocation.class );
        share_current_locat.putExtra("array",likely_places);
        startActivity(share_current_locat);
    }

    public void readQR(View view){
        Intent readQr = new Intent(this, QReader.class );
        startActivity(readQr);
    }

    public void goToSettingsPage(){
        Intent settingsPage = new Intent(this, Settings.class );
        startActivity(settingsPage);
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }



    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        // added by me (MMS)
        addOptionsToScrollView();


    }

    public void generateGeohashSquare()
    {
        // A List of LatLng defining your user's input
        // (Two latLng define a square)
        List<LatLng> positions = new ArrayList<>();
        //positions.add(new LatLng(40.6494140625, -8.701171875));
        //positions.add(new LatLng(40.60546875, -8.6572265625));

        double subtract=0;
        switch(progressChangedValue )
        {
            case 0:
                subtract=0.1;
                break;
            case 1:
                subtract=0.2;
                break;
            case 2:
                subtract=0.3;
                break;
            case 3:
                subtract=0.4;
                break;
            case 4:
                subtract=0.5;
                break;
        }
        Log.d("latitude","subtract : "+subtract);


        double leftTopVertex =lon-subtract;
        double rightBottomVertex= lat-subtract;

        positions.add(new LatLng(lat+subtract,leftTopVertex));
        positions.add(new LatLng(rightBottomVertex,lon+subtract));

        //positions.add(new LatLng(40.6494140625, -8.701171875-subtract));
        //positions.add(new LatLng(40.60546875-subtract, -8.6572265625));

        Log.d("latitude","latitude FML : "+lat);
        Log.d("longitude","longitude FML: "+lon);


// Create a LatLngBounds.Builder and include your positions
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng position : positions) {
            builder.include(position);
        }

        // Calculate the bounds of the final positions
        LatLngBounds finalBounds = builder.build();
        drawBounds (finalBounds, Color.BLUE);

    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                            lat = mLastKnownLocation.getLatitude();
                            lon = mLastKnownLocation.getLongitude();
                            Log.d("latitude","latitude : "+lat);
                            Log.d("longitude","longitude: "+lon);
                            //generate a square in the map
                            generateGeohashSquare();  //40.6271645 -8.651658  - minha casa Aveiro



                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }

    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }

    /**
     * Handles the result of the request for location permissions.
     */
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    checkReceiveSmsPermission(); // check for Receive_SMS Permission
                    return;
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_RECEIVE_SMS:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkReadFromContactsPermission();
                    return;
                }
                break;
            }
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (true) {
                    checkPhonestatePermission();
                    return;
                } else {
                    Toast.makeText(this, "CMIYC is not going run flawlessly until you give permission to read contacts", Toast.LENGTH_SHORT).show();
                }
            }
            case PERMISSION_REQUEST_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Phone number: " + getPhone());
                    //Toast.makeText(activity,"phone number "+getPhone(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity,"Permission Denied. We can't get phone number.", Toast.LENGTH_LONG).show();
                }
                break;
             }
        }
        updateLocationUI();
    }



    private String getPhone() {
        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(activity, wantPermission) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }
        return phoneMgr.getLine1Number();
    }


    // added by me (MMS)
    /**
     * vai ser chamado logo no fim de termos um mapa criado
     */
    public void addOptionsToScrollView(){
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final
            Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                                // Set the count, handling cases where less than 5 entries are returned.
                                int count;
                                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = M_MAX_ENTRIES;
                                }

                                int i = 0;
                                mLikelyPlaceNames = new String[count];
                                mLikelyPlaceAddresses = new String[count];
                                mLikelyPlaceAttributions = new String[count];
                                mLikelyPlaceLatLngs = new LatLng[count];


                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // Build a list of likely places to show the user.
                                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace().getAddress();
                                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace().getAttributions();
                                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();


                                    // added by me (MMS)
                                    likely_places.add((String) placeLikelihood.getPlace().getName()+", "+(String) placeLikelihood.getPlace().getAddress());

                                  //  b= new Button(getApplicationContext());
                                   // b.setText(placeLikelihood.getPlace().getName());
                                   // ll.addView(b);

                                    i++;
                                    if (i > (count - 1)) {
                                        break;
                                    }
                                }

                                // Release the place likelihood buffer, to avoid memory leaks.
                                likelyPlaces.release();

                                // Show a dialog offering the user the list of likely places, and add a
                                // marker at the selected place.

                            } else {
                                Log.e(TAG, "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }




    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final
            Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                                // Set the count, handling cases where less than 5 entries are returned.
                                int count;
                                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = M_MAX_ENTRIES;
                                }

                                int i = 0;
                                mLikelyPlaceNames = new String[count];
                                mLikelyPlaceAddresses = new String[count];
                                mLikelyPlaceAttributions = new String[count];
                                mLikelyPlaceLatLngs = new LatLng[count];


                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // Build a list of likely places to show the user.
                                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                                            .getAddress();
                                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                            .getAttributions();
                                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                                    //text_view.setText("miguel");
                                    //text_view.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
                                    //((LinearLayout) linearLayout).addView(text_view);

                                    i++;
                                    if (i > (count - 1)) {
                                        break;
                                    }
                                }

                                // Release the place likelihood buffer, to avoid memory leaks.
                                likelyPlaces.release();

                                // Show a dialog offering the user the list of likely places, and add a
                                // marker at the selected place.
                                openPlacesDialog();

                            } else {
                                Log.e(TAG, "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                mMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

}
