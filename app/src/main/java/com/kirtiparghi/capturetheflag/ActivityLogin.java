package com.kirtiparghi.capturetheflag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;


public class ActivityLogin extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    FirebaseDatabase database;
    DatabaseReference root;
    private ChildEventListener mChildEventListener ;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;

    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private LocationManager locationManager;

    EditText edtEmail, edtPassword;
    Button btnLogin;
    private RadioGroup radioUserTypeGroup;
    private RadioGroup radioPlayerTeamGroup;
    RadioButton radioUserType, radioPlayerTeam;
    FirebaseDatabase db;

    String strPlayerTeam, strUserType;
    LinearLayout linearlayout;

    String lat, lng;

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        startLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mLocation == null){
            startLocationUpdates();
        }
        if (mLocation != null) {

            // mLatitudeTextView.setText(String.valueOf(mLocation.getLatitude()));
            //mLongitudeTextView.setText(String.valueOf(mLocation.getLongitude()));
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("ctf", "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("ctf", "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    @Override
    public void onLocationChanged(Location location) {

        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());

        lat = String.valueOf(location.getLatitude());
        lng = String.valueOf(location.getLongitude());

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    int teamACount = 0,teamBCount = 0;
    void addListener() {

//        FirebaseDatabase.getInstance().getReference().child("Player")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                    }
//                    @Override
//                    public void onCancelled (DatabaseError databaseError){
//                    }
//                });

        if (mChildEventListener == null) {

            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    Toast.makeText(getApplicationContext(),"listener called....",Toast.LENGTH_LONG).show();

                        Player user = dataSnapshot.getValue(Player.class);
                       Log.e("ctf",dataSnapshot.getValue(Player.class).toString());

//                        if (user.getTeam().equals("A")) {
//                            teamACount++;
//                        }
//                        else if (user.getTeam().equals("B")) {
//                            teamBCount++;
//                        }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        database = FirebaseDatabase.getInstance();
        root = database.getReference();

        addListener();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        strPlayerTeam = "A";
        strUserType = "";


        /* Fetching all data from field*/

        edtEmail = (EditText) findViewById(R.id.user_email);


        edtPassword = (EditText) findViewById(R.id.password);


        btnLogin = (Button) findViewById(R.id.login_button);
        radioPlayerTeamGroup = (RadioGroup) findViewById(R.id.radioTeams);
        radioUserTypeGroup =  (RadioGroup) findViewById(R.id.radioPlayers);
        linearlayout = (LinearLayout) findViewById(R.id.linearlayout);

        // setup the firebase variables
        db = FirebaseDatabase.getInstance();
        root = db.getReference();

        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                final String username = edtEmail.getText().toString().trim();
                final String password = edtPassword.getText().toString().trim();

                validateFields();

                Log.d("err"  , username);
                Log.d("err"  , password);

                if (username.isEmpty()) {
                    // if message is blank, then quit
                    Toast.makeText(ActivityLogin.this, "Please Enter an Email",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.isEmpty()) {
                    // if message is blank, then quit
                    Toast.makeText(ActivityLogin.this, "Please Enter an Password",Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(getApplicationContext(),username,Toast.LENGTH_SHORT).show();
                if (username.equals("Admin@gmail.com")) {
                    Log.e("ctf","inside if....");
                    SharedPreferences sharedpreferences = getSharedPreferences("ctf", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("email","admin@gmail.com");
                    editor.putString("isPlayer","false");
                    editor.commit();
                    Intent intent = new Intent(getApplicationContext(), ActivityAdminHome.class);
                    startActivity(intent);
                }
                else {
                    Log.e("ctf","inside else....");

                    SharedPreferences sharedpreferences = getSharedPreferences("ctf", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("email",edtEmail.getText().toString());
                    editor.putString("isPlayer","true");
                    editor.commit();

                    performLoginOrAccountCreation(username,password,strPlayerTeam);

//                    String id = root.child("Player").push().getKey();
//
//                    Player newPlayer = new Player(id,edtEmail.getText().toString(),password,strPlayerTeam,lat,lng,"false");
//
//                    //store in pref
//                    ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(getApplicationContext(), "object_prefs", 0);
//                    complexPreferences.putObject("currentplayer", newPlayer);
//                    complexPreferences.commit();
//
//                    if (newPlayer.passcode.equals(password)) {
//                        Toast.makeText(getApplicationContext(), "User Found"+newPlayer.getPlayer(), Toast.LENGTH_LONG).show();
//
//                        if (teamACount >= 5 || teamBCount >= 5) {
//                            Toast.makeText(getApplicationContext(), "Team size is 5 so you can not join this game!!!", Toast.LENGTH_LONG).show();
//                            return;
//                        }
//
//                        Intent intent = new Intent(getApplicationContext(), ActivityPlayerHome.class);
//                        startActivity(intent);
//
//                    } else {
//                        Toast.makeText(getApplicationContext(), "Password is wrong", Toast.LENGTH_LONG).show();
//                    }

                }
            }
        });

        radioUserTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int selectedId=radioUserTypeGroup.getCheckedRadioButtonId();
                radioUserType=(RadioButton)findViewById(selectedId);
                Toast.makeText(ActivityLogin.this,radioUserType.getText(),Toast.LENGTH_SHORT).show();
                if (radioUserType.getText().toString().equals("ADMIN"))
                {
                    strUserType = "ADMIN";
                    linearlayout.setVisibility(View.GONE);
                }
                else {
                    strUserType = "PLAYER";
                    linearlayout.setVisibility(View.VISIBLE);
                }
                edtEmail.setText("");
                edtPassword.setText("");
            }
        });

        radioPlayerTeamGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int selectedId=radioPlayerTeamGroup.getCheckedRadioButtonId();
                radioPlayerTeam=(RadioButton)findViewById(selectedId);
                Toast.makeText(ActivityLogin.this,radioPlayerTeam.getText(),Toast.LENGTH_SHORT).show();
                if (radioPlayerTeam.getText().toString().equals("Team A")) {
                    strPlayerTeam = "A";
                }
                else {
                    strPlayerTeam = "B";
                }
            }
        });
    }

    private void validateFields() {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (!edtEmail.getText().toString().matches(emailPattern))
        {
            Toast.makeText(getApplicationContext(),"Please Enter a valid email address",Toast.LENGTH_SHORT).show();
        }
        else if (edtPassword.getText().length() < 6) {
            Toast.makeText(getApplicationContext(),"Please Enter passwod atleast 6 digits long!!!",Toast.LENGTH_SHORT).show();
        }
    }

    private void performLoginOrAccountCreation(final String email, final String password,final String strPlayerTeam)
    {
        Query query = root.child("Player").orderByChild("player").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(getApplicationContext(), "object_prefs", 0);
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0

                    for (DataSnapshot player : dataSnapshot.getChildren()) {
                        // do something with the individual "issues"
                        Player currentPlayer = player.getValue(Player.class);

                        //store in pref
                        complexPreferences.putObject("currentplayer", currentPlayer);
                        complexPreferences.commit();

                        if (currentPlayer.passcode.equals(password)) {
                            Toast.makeText(getApplicationContext(), "User Found"+currentPlayer.getPlayer(), Toast.LENGTH_LONG).show();

                            if (teamACount >= 5 || teamBCount >= 5) {
                                Toast.makeText(getApplicationContext(), "Team size is 5 so you can not join this game!!!", Toast.LENGTH_LONG).show();
                                return;
                            }

                            Intent intent = new Intent(getApplicationContext(), ActivityPlayerHome.class);
                            startActivity(intent);

                        } else {
                            Toast.makeText(getApplicationContext(), "Password is wrong", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "User not found, Creating new User", Toast.LENGTH_LONG).show();
                    String id = root.child("Player").push().getKey();

                    Player newPlayer = new Player(id,email,password,strPlayerTeam,lng,lat,"false");

                    Toast.makeText(getApplicationContext(),newPlayer.getPlayer() +"" + newPlayer.getTeam(),Toast.LENGTH_SHORT).show();

                    root.child("Player").child(id).setValue(newPlayer);

                    //store in pref
                    complexPreferences.putObject("currentplayer", newPlayer);
                    complexPreferences.commit();

                    Toast.makeText(getApplicationContext(), "New Player Added "+email, Toast.LENGTH_LONG).show();

//                    Intent intent = new Intent(getApplicationContext(), ActivityPlayerHome.class);
//                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}