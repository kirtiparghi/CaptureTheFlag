package com.kirtiparghi.capturetheflag;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class ActivityAdminHome extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String TAG = ActivityAdminHome.class.getSimpleName();

    //GOOGLE MAP IMPLEMENTAION VARIABLE
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Location mLastKnownLocation;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 100f;
    private LatLng middle1, middle2, corner1, corner2, corner3, corner4;
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //FIREBASE VARIABLES
    FirebaseDatabase database;
    DatabaseReference root;
    private ChildEventListener mChildEventListener ;

    //PLAYERS LIST
    private ArrayList<Player> listPlayers;
    private ArrayList<Marker> mMarkerArray = new ArrayList<Marker>();

    int isFlagAdded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isFlagAdded = 0;

        listPlayers = new ArrayList<Player>();
        mMarkerArray = new ArrayList<Marker>();

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.layout_admin_home);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fetchPlayersDetails();
    }

    private void fetchPlayersDetails() {
        database = FirebaseDatabase.getInstance();
        root = database.getReference();
        addNewPlayerAddedOrUpdatedListener();
    }

    void addNewPlayerAddedOrUpdatedListener() {
        if (mChildEventListener == null) {

            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Player player = dataSnapshot.getValue(Player.class);
                    //update google map
                    if (!player.getLatitude().equals("")) {
                        listPlayers.add(player);
                    }
                    setPlayersOnMap();
                    Toast.makeText(getApplicationContext(),"Added : " + player.player,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Player player = dataSnapshot.getValue(Player.class);
                    for (int index = 0; index < listPlayers.size(); index++) {
                        Player p = listPlayers.get(index);
                        if (p.player == player.player) {
                            listPlayers.set(index,player);
                        }
                        else {
                            listPlayers.add(player);
                        }
                    }
                    Log.e("ctf","on child changed call");
                    Log.e("ctf",listPlayers.size()+"");
                    //update google map
                    setPlayersOnMap();

                    Toast.makeText(getApplicationContext(),"Changed : " + player.player,Toast.LENGTH_SHORT).show();
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
     *
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    /**
     * Handles a click on the menu option to get a place.
     *
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            SharedPreferences sharedpreferences = getSharedPreferences("ctf", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("email","");
            editor.putString("isPlayer","");
            editor.commit();

            //sukh
            ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(getApplicationContext(), "object_prefs", 0);
            complexPreferences.clearObject();
            complexPreferences.commit();
            Intent intent = new Intent(getApplicationContext(), ActivityLogin.class);
            startActivity(intent);

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
        map.getUiSettings().setZoomControlsEnabled(true);

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        if (mLocationPermissionGranted)  {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true );
        }
    }

    private void setPlayersOnMap() {

        mMap.clear();

        // The distance you want to increase your square (in meters)
        double distance = 3;

        List<LatLng> positions = new ArrayList<>();

        //REMOVE ALL MARKERS
        for (Marker m : mMarkerArray) {
            Log.e("ctf","remove....");
            m.remove();
        }

        if (listPlayers.size() >0) {
            for (int index =0 ; index < listPlayers.size(); index++) {
                Player p = listPlayers.get(index);
                positions.add(new LatLng(Double.parseDouble(p.getLatitude()),Double.parseDouble(p.getLongitude())));
                Toast.makeText(getApplicationContext(), p.getTeam()+"", Toast.LENGTH_SHORT).show();

//            String s = p.getTeam().toString();
//            Log.e("ctf","temmmmmmm " + s);

                if (p.getTeam().toString() != null) {
                    if (p.getTeam().toString().equals("A")) {
                        //ADD MARKERS......
                        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(p.getLatitude()), Double.parseDouble(p.getLongitude()))).icon(BitmapDescriptorFactory.fromResource(R.drawable.red)));
                        mMarkerArray.add(marker);
                    } else {
                        //ADD MARKERS......
                        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(p.getLatitude()), Double.parseDouble(p.getLongitude()))).icon(BitmapDescriptorFactory.fromResource(R.drawable.blue)));
                        mMarkerArray.add(marker);
                    }
                }
            }
        }

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

        drawBounds (finalBounds, Color.RED);

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
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
//                            mMap.moveCamera(CameraUpdateFactory
//                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
//                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
                }
            }
        }
        updateLocationUI();
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
        }
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
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private Location latlngToLocation(LatLng dest) {
        Location loc = new Location("");
        loc.setLatitude(dest.latitude);
        loc.setLongitude(dest.longitude);
        return loc;
    }

    private void drawBounds (LatLngBounds bounds, int color) {
        PolygonOptions polygonOptions =  new PolygonOptions()
                .add(new LatLng(bounds.northeast.latitude, bounds.northeast.longitude))
                .add(new LatLng(bounds.southwest.latitude, bounds.northeast.longitude))
                .add(new LatLng(bounds.southwest.latitude, bounds.southwest.longitude))
                .add(new LatLng(bounds.northeast.latitude, bounds.southwest.longitude))
                .strokeColor(color);


        mMap.addPolygon(polygonOptions);

        Toast.makeText(getApplicationContext(),"draw bounds...",Toast.LENGTH_SHORT).show();
    }

    public void moveCamera(LatLng latLng, Float zoom) {

        Log.d(TAG, "moveCamera: Moving the Camera To lat : " + latLng.latitude + "|| Lng : " + latLng.longitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.addMarker(new MarkerOptions().position(latLng));
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
        mMap.addMarker(new MarkerOptions().position(getFlagCoordinate(corner1, middle1)).title("Flag").icon(BitmapDescriptorFactory.fromResource(R.drawable.flag_icon)));
        mMap.addMarker(new MarkerOptions().position(getFlagCoordinate(corner3, middle2)).title("Flag").icon(BitmapDescriptorFactory.fromResource(R.drawable.flag_icon)));
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


        double flagLatitude = min + (max - min) * 0.3;

        if(flagLatitude < max && flagLatitude > min){

            return flagLatitude;

        }else{

            return getRandomeCoordinate(min, max);

        }

    }

}