package com.kirtiparghi.capturetheflag;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class ActivityPlayerHome extends AppCompatActivity
        implements OnMapReadyCallback, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    int isYardDraw = 0;

    private static final String TAG = com.kirtiparghi.capturetheflag.ActivityPlayerHome.class.getSimpleName();
    private GoogleMap mMap;
    Double lat = 0.0, lon = 0.0;

    //FIREBASE VARIABLES
    FirebaseDatabase database;
    DatabaseReference root;
    private ChildEventListener mChildEventListener ;

    //PLAYERS LIST
    private ArrayList<Player> listPlayers;
    private ArrayList<Marker> mMarkerArray = new ArrayList<Marker>();

    private LatLng middle1, middle2, corner1, corner2, corner3, corner4;

    private LatLng playerLatLng;

    int isFlagAdded;

    Player player;

    Marker flagAMarker, flagBMarker;
    LatLng flagALatLng, flagBLatLng;
    LocationRequest mLocationRequest;
    private static final long INTERVAL = 5000;
    private static final long FASTEST_INTERVAL = 5000;

    GoogleApiClient mGoogleApiClient;

    private boolean mLocationPermissionGranted;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        setContentView(R.layout.layout_player_home);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        isFlagAdded = 0;

        listPlayers = new ArrayList<Player>();
        mMarkerArray = new ArrayList<Marker>();

        fetchPlayersDetails();

        getCurrentPlayerFromPref();

        listPlayers.add(player);

       // Log.e("ctf","team is : "  + player.team);
    }

    private void getCurrentPlayerFromPref() {
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(getApplicationContext(), "object_prefs", 0);
        player = complexPreferences.getObject("currentplayer", Player.class);
    }

    private void fetchPlayersDetails() {
        database = FirebaseDatabase.getInstance();
        root = database.getReference();
        addNewPlayerAddedOrUpdatedListener();
    }

    private void setPlayersOnMap() {

        mMap.clear();

        // The distance you want to increase your square (in meters)
        double distance = 3;

        List<LatLng> positions = new ArrayList<>();

        //REMOVE ALL MARKERS
        for (Marker m : mMarkerArray) {
           // Log.e("ctf","remove....");
            m.remove();
        }

        for (int index =0 ; index < listPlayers.size(); index++) {
            Player p = listPlayers.get(index);
            positions.add(new LatLng(Double.parseDouble(p.getLatitude()),Double.parseDouble(p.getLongitude())));

            if (p.getTeam().equals("A")) {
                Log.e("ctf","have flag --> " +p.getHaveFlag().toString());
//                if (p.getHaveFlag().equals("true")) {
//                    //ADD MARKERS......
//                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(p.getLatitude()),Double.parseDouble(p.getLongitude()))).icon(BitmapDescriptorFactory.fromResource(R.drawable.withflag)));
//                    mMarkerArray.add(marker);
//                    Log.e("ctf","Add Marker...."+p.player);
//                }
//                else {
//                    //ADD MARKERS......
//                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(p.getLatitude()),Double.parseDouble(p.getLongitude()))).icon(BitmapDescriptorFactory.fromResource(R.drawable.red)));
//                    mMarkerArray.add(marker);
//                    Log.e("ctf","Add Marker...."+p.player);
//                }
                Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(p.getLatitude()),Double.parseDouble(p.getLongitude()))).icon(BitmapDescriptorFactory.fromResource(R.drawable.red)));
                mMarkerArray.add(marker);
                Log.e("ctf","Add Marker...."+p.player);
            }
            else if (p.team.equals("B")) {
//                if (p.getHaveFlag().equals("true")) {
//                    //ADD MARKERS......
//                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(p.getLatitude()),Double.parseDouble(p.getLongitude()))).icon(BitmapDescriptorFactory.fromResource(R.drawable.withflag)));
//                    mMarkerArray.add(marker);
//                    Log.e("ctf","Add Marker...."+p.player);
//                }
//                else {
//                    //ADD MARKERS......
//                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(p.getLatitude()),Double.parseDouble(p.getLongitude()))).icon(BitmapDescriptorFactory.fromResource(R.drawable.blue)));
//                    mMarkerArray.add(marker);
//                    Log.e("ctf","Add Marker else...."+p.player);
//                }
                Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(p.getLatitude()),Double.parseDouble(p.getLongitude()))).icon(BitmapDescriptorFactory.fromResource(R.drawable.blue)));
                mMarkerArray.add(marker);
                Log.e("ctf","Add Marker else...."+p.player);

            }
        }

//        if (isYardDraw == 0) {
//            Toast.makeText(getApplicationContext(),"Player size    " + listPlayers.size(), Toast.LENGTH_SHORT).show();
//            if (listPlayers.size() >= 2) {
                // Create a LatLngBounds.Builder and include your positions
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng position : positions) {
                    builder.include(position);
                }

                // Calculate the bounds of the initial positions
                LatLngBounds initialBounds = builder.build();

                // Increase the bounds by the given distance
                // Notice the distance * Math.sqrt(2) to increase the bounds in the directions of northeast and southwest (45 and 225 degrees respectively)
                LatLng targetNorteast = SphericalUtil.computeOffset(initialBounds.northeast, distance * Math.sqrt(15), 0);
                LatLng targetSouthwest = SphericalUtil.computeOffset(initialBounds.southwest, distance * Math.sqrt(15), 0);

                // if (isYardDraw == 0) {
                //if (listPlayers.size() >= 3) {
                CameraPosition googlePlex = CameraPosition.builder()
                        .target(new LatLng(targetNorteast.latitude, targetSouthwest.longitude))
                        .zoom(18)
                        .bearing(0)
                        .tilt(45)
                        .build();

                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(googlePlex));

                // Add the new positions to the bounds
                builder.include(targetNorteast);
                builder.include(targetSouthwest);

                // Calculate the bounds of the final positions
                LatLngBounds finalBounds = builder.build();

                double middleLatitude = (finalBounds.northeast.latitude - finalBounds.southwest.latitude) / 2 + finalBounds.southwest.latitude;

                middle1 = new LatLng(middleLatitude, finalBounds.northeast.longitude);
                middle2 = new LatLng(middleLatitude, finalBounds.southwest.longitude);
                corner1 = new LatLng(finalBounds.northeast.latitude, finalBounds.southwest.longitude);
                corner2 = new LatLng(finalBounds.northeast.latitude, finalBounds.northeast.longitude);
                corner3 = new LatLng(finalBounds.southwest.latitude, finalBounds.northeast.longitude);
                corner4 = new LatLng(finalBounds.southwest.latitude, finalBounds.southwest.longitude);

                double variation = (corner1.longitude - corner2.longitude) * 0.3;

                LatLng jailCorner12 = new LatLng(corner1.latitude, corner1.longitude - variation);
                LatLng jailCorner13 = new LatLng(corner1.latitude - variation, corner1.longitude - variation);
                LatLng jailCorner14 = new LatLng(corner1.latitude - variation, corner1.longitude);

                LatLng jailCorner32 = new LatLng(corner3.latitude, corner3.longitude + variation);
                LatLng jailCorner33 = new LatLng(corner3.latitude + variation, corner3.longitude + variation);
                LatLng jailCorner34 = new LatLng(corner3.latitude + variation, corner3.longitude);

                addJail(corner1, jailCorner12, jailCorner13, jailCorner14);
                addJail(corner3, jailCorner32, jailCorner33, jailCorner34);

                drawBounds(finalBounds, Color.RED);

                mMap.addPolyline(
                        new PolylineOptions().add(
                                middle1,
                                middle2
                        ).width(10).color(Color.BLUE).geodesic(true)
                );

                // ge Flag Coordinate
                //if (isFlagAdded == 0) {
                addFlag();
//            isFlagAdded = 1;
//        }
                //  }
//                isYardDraw = 1;
//                //}
//            }
     //   }
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

    private void drawBounds (LatLngBounds bounds, int color) {
        PolygonOptions polygonOptions =  new PolygonOptions()
                .add(new LatLng(bounds.northeast.latitude, bounds.northeast.longitude))
                .add(new LatLng(bounds.southwest.latitude, bounds.northeast.longitude))
                .add(new LatLng(bounds.southwest.latitude, bounds.southwest.longitude))
                .add(new LatLng(bounds.northeast.latitude, bounds.southwest.longitude))
                .strokeColor(color);


        mMap.addPolygon(polygonOptions);
        //Toast.makeText(getApplicationContext(),"draw bounds...",Toast.LENGTH_SHORT).show();
    }

    void addNewPlayerAddedOrUpdatedListener() {
        if (mChildEventListener == null) {

            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    ArrayList<Player> tmpArr = new ArrayList<Player>();
                    tmpArr.add(player);
                    Player playerObj = dataSnapshot.getValue(Player.class);

                    if (!playerObj.team.equals(player.getTeam().toString())) {
                        tmpArr.add(playerObj);
                    }

                    for (int index = 0; index < listPlayers.size(); index++) {
                        Player p = listPlayers.get(index);
                        if (p.player != playerObj.player && (!p.team.equals(player.getTeam().toString()))) {
                            tmpArr.add(p);
                        }
                    }
                    listPlayers = null;
                    listPlayers = tmpArr;

                    Log.e("ctf","on child changed call");
                    Log.e("ctf",listPlayers.size()+"");
                    //update google map
                    if (listPlayers.size() > 0) {
                        setPlayersOnMap();
                    }
                    //Toast.makeText(getApplicationContext(),"Added : " + playerObj.player,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    int isCurrentPlayer;
                    ArrayList<Player> tmpArr = new ArrayList<Player>();

                    Player playerObj = dataSnapshot.getValue(Player.class);
                    if (playerObj.player.equals(player.getPlayer().toString())) {
                        tmpArr.add(playerObj);
                    }

                    for (int index = 0; index < listPlayers.size(); index++) {
                        Player p = listPlayers.get(index);
                        if (p.player != playerObj.player && (!p.team.equals(player.getTeam().toString()))) {
                            tmpArr.add(p);
                        }
                    }
                    listPlayers = null;
                    listPlayers = tmpArr;

                    Log.e("ctf","on child changed call");
                    Log.e("ctf",listPlayers.size()+"");
                    //update google map
                    if (listPlayers.size() > 0) {
                        setPlayersOnMap();
                    }

                   // Toast.makeText(getApplicationContext(),"Changed : " + playerObj.player,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            root.child("Player").addChildEventListener(mChildEventListener);
        }
    }

    private void addPolylineCustom(LatLng latLng1, LatLng latLng2){

        mMap.addPolyline(
                new PolylineOptions().add(
                        latLng1,
                        latLng2
                ).width(10).color(Color.YELLOW).geodesic(true)
        );

    }

    private void addJail(LatLng latLng1, LatLng latLng2, LatLng latLng3, LatLng latLng4){
        addPolylineCustom(latLng2, latLng3);
        addPolylineCustom(latLng4, latLng3);
        addPolylineCustom(latLng1, latLng4);
    }

    private void addFlag(){
        flagAMarker = mMap.addMarker(new MarkerOptions().position(getFlagCoordinate(corner1, middle1)).title("Flag").icon(BitmapDescriptorFactory.fromResource(R.drawable.flag_icon)));
        flagBMarker = mMap.addMarker(new MarkerOptions().position(getFlagCoordinate(corner3, middle2)).title("Flag").icon(BitmapDescriptorFactory.fromResource(R.drawable.flag_icon)));

        flagALatLng = getFlagCoordinate(corner1, middle1);
        flagBLatLng = getFlagCoordinate(corner3, middle2);

        if (player.team.equals("A")) { //CURRENT PLAYER IS OF TEAM A
            flagAMarker.setVisible(true);
            flagBMarker.setVisible(false);
        }
        else { // CURRENT PLAYER IS OF TEAM B
            flagAMarker.setVisible(false);
            flagBMarker.setVisible(true);
        }
    }

    private LatLng getFlagCoordinate(LatLng latLng1, LatLng latLng2){

        double flagLat, flagLng;

        flagLat = flagCoordinatesLatitude(latLng1.latitude, latLng2.latitude);
        flagLng = flagCoordinatesLongitude(latLng1.longitude, latLng2.longitude);

        return new LatLng(flagLat, flagLng);

    }

    private double flagCoordinatesLatitude(double latitude1, double latitude2){

        double minLatitude, maxLatitude;



        if(latitude1 < latitude2){


            minLatitude = latitude1;
            maxLatitude = latitude2;

        }else if(latitude1 > latitude2){

            minLatitude = latitude2;
            maxLatitude = latitude1;


        }else{

            Log.e(TAG, "flagCoordinates: Invalid Corner Coordinate Please manual Check Error. . .");
            return 0;

        }


        return  getRandomeCoordinate(minLatitude, maxLatitude);


    }

    private double flagCoordinatesLongitude(double longitude1, double longitude2){

        double minLongitude, maxLongitude;



        if(longitude1 < longitude2){


            minLongitude = longitude1;
            maxLongitude = longitude2;

        }else if(longitude1 > longitude2){

            minLongitude = longitude2;
            maxLongitude = longitude1;


        }else{

            Log.e(TAG, "flagCoordinates: Invalid Corner Coordinate Please manual Check Error. . .");
            return 0;

        }


        return  getRandomeCoordinate(minLongitude, maxLongitude);


    }

    private double getRandomeCoordinate(double min, double max){

        Random r = new Random();

        double flagLatitude = min + (max - min) * r.nextDouble();

        if(flagLatitude < max && flagLatitude > min){

            return flagLatitude;

        }else{

            return getRandomeCoordinate(min, max);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            SharedPreferences sharedpreferences = getSharedPreferences("ctf", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("email","");
            editor.putString("isPlayer","");
            editor.commit();
            finish();
        }
        return true;
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setBuildingsEnabled(true);
        getLocationPermission();
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.v(TAG, "Status changed: " + s);
    }

    public void onProviderEnabled(String s) {
        Log.e(TAG, "PROVIDER DISABLED: " + s);
    }

    public void onProviderDisabled(String s) {
        Log.e(TAG, "PROVIDER DISABLED: " + s);
    }

    @Override
    public void onStart(){
        super.onStart();
        if(mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connection established. Fetching location ..");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        startLocationUpdates();
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
    }



    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getApplicationContext(),"on location change called",Toast.LENGTH_SHORT).show();
        lat = location.getLatitude();
        lon = location.getLongitude();

        playerLatLng = new LatLng(lat,lon);

        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(getApplicationContext(), "object_prefs", 0);
        player = complexPreferences.getObject("currentplayer", Player.class);

        Toast.makeText(getApplicationContext(),player.getPlayerId()+"",Toast.LENGTH_SHORT).show();

        root.child("Player").child(player.getPlayerId()).child("latitude").setValue(lat + "");
        root.child("Player").child(player.getPlayerId()).child("longitude").setValue(lon + "");

        Toast.makeText(getApplicationContext(),"location changed call",Toast.LENGTH_SHORT).show();

        //prision
        // one player catch other player
        // player is outside the yard

        //get the flag
        //check if player is in his yard
        //hide flag
        //if player with flag in his yard so GAME IS OVER

//        if (isYardDraw == 1) {
//            checkIfPlayerHasFlag();
//        }
    }

    void checkIfPlayerHasFlag() {
        LatLng currentFlagMarker = null;

        if (player.getTeam().equals("A")) {
            currentFlagMarker = new LatLng(flagALatLng.latitude,flagALatLng.longitude);
        }
        else {
            currentFlagMarker = new LatLng(flagBLatLng.latitude,flagBLatLng.longitude);
        }

        Location flagLoc = new Location("");
        flagLoc.setLatitude(currentFlagMarker.latitude);
        flagLoc.setLongitude(currentFlagMarker.longitude);


        Location playerLoc = new Location("");
        flagLoc.setLatitude(playerLatLng.latitude);
        flagLoc.setLongitude(playerLatLng.longitude);

        if(flagLoc.distanceTo(playerLoc) <= 3) {

            Toast.makeText(getApplicationContext(), "Yeeeh.......you got the flag!!!",Toast.LENGTH_SHORT).show();

            root.child("Player").child(player.getPlayerId()).child("haveflag").setValue("true");

            //hide flag;
            if (player.getTeam().equals("A")) {
                flagBMarker.remove();
            }
            else {
                flagAMarker.remove();
            }
        }
    }

    private void updateMap() {
//        Double latitude = lat;
//        Double longitude = lon;
//        LatLng mylocation = new LatLng(latitude, longitude);
//        //mMap.setMyLocationEnabled(true);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(mylocation));
//        mMap.setMaxZoomPreference(18);
//        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        CameraPosition cameraPosition = new CameraPosition.Builder().target(
//                new LatLng(this.lat, this.lon)).zoom(18).tilt(67.5f).bearing(314).build();
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        mMap.addMarker(new MarkerOptions().position(mylocation).title("Team A").icon(BitmapDescriptorFactory.fromResource(R.drawable.girl)));
//        mMap.addMarker(new MarkerOptions().position(boundry1).title("Flag").icon(BitmapDescriptorFactory.fromResource(R.drawable.flag1)));
//        addFencing();
//        mMap.addCircle(circleOptions);
//        updatePlayer(latitude,longitude);
//        mMap.addPolyline(polylineOptions);
    }

    private void addFencing() {
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, LocationAlertIntentService.class);
        return PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest(Geofence geofence) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofence(geofence);
        return builder.build();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
            Toast.makeText(getApplicationContext(), "I AM TRIGGERERED", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "I AM TRIGGERERED not", Toast.LENGTH_SHORT).show();
        }
    }

    protected void startLocationUpdates() {
        if (!mLocationPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                Toast.makeText(getApplicationContext(),"start locaiton updates in if...",Toast.LENGTH_SHORT).show();
                return;
            }

        } else {
//            LocationServices.FusedLocationApi.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, com.kirtiparghi.capturetheflag.ActivityPlayerHome.this);
//            LocationServices.FusedLocationApi.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, com.kirtiparghi.capturetheflag.ActivityPlayerHome.this);
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, ActivityPlayerHome.this);
            Toast.makeText(getApplicationContext(),"start locaiton updates in else...",Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "Location update started ..............: ");
    }

    public String makeURL (double sourcelat, double sourcelog, double destlat, double destlog ){
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString( sourcelog));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString( destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=walking&alternatives=true");
        urlString.append("&key=AIzaSyCNeHVJ89PyygTFC4vQ-xIKgLIP4ZzTFRk");
        return urlString.toString();
    }
}