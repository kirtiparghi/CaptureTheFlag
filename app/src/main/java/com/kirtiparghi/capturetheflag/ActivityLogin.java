package com.kirtiparghi.capturetheflag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

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

public class ActivityLogin extends Activity
{
    EditText edtEmail, edtPassword;
    Button btnLogin;
    private RadioGroup radioUserTypeGroup;
    private RadioGroup radioPlayerTeamGroup;
    RadioButton radioUserType, radioPlayerTeam;
    FirebaseDatabase db;
    DatabaseReference root;
    public FirebaseAuth mAuth;
    String strPlayerTeam, strUserType;
    LinearLayout linearlayout;
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(ActivityLogin.this, "Player is "+user.getEmail(),Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(ActivityLogin.this, "No Player",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        strPlayerTeam = "";
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

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final String username = edtEmail.getText().toString().trim();
                final String password = edtPassword.getText().toString().trim();

             //   validateFields();
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
                if (username.equals("admin@gmail.com")) {
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
                    strPlayerTeam = "Team A";
                }
                else {
                    strPlayerTeam = "Team B";
                }
            }
        });
    }

    private void performLoginOrAccountCreation(final String email, final String password,final String strPlayerTeam)
    {
        Query query = root.child("Player").orderByChild("player").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0

                    for (DataSnapshot player : dataSnapshot.getChildren()) {
                        // do something with the individual "issues"
                        Player currentPlayer = player.getValue(Player.class);

                        if (currentPlayer.passcode.equals(password)) {
                            Toast.makeText(getApplicationContext(), "User Found"+currentPlayer.getPlayer(), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(), ActivityPlayerHome.class);
                            intent.putExtra("player",currentPlayer);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "Password is wrong", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "User not found, Creating new User", Toast.LENGTH_LONG).show();
                    String id = root.child("Player").push().getKey();
                    Player newPlayer = new Player(id,email,password,strPlayerTeam,"","");
                    root.child("Player").child(id).setValue(newPlayer);
                    Toast.makeText(getApplicationContext(), "New Player Added "+email, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), ActivityPlayerHome.class);
                   intent.putExtra("player",  newPlayer);
                    startActivity(intent);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}