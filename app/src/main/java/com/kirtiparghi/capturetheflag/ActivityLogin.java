package com.kirtiparghi.capturetheflag;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class ActivityLogin extends Activity {

    EditText edtEmail, edtPassword;
    Button btnLogin;
    private RadioGroup radioUserTypeGroup;
    private RadioGroup radioPlayerTeamGroup;
    RadioButton radioUserType, radioPlayerTeam;

    String strPlayerTeam, strUserType;

    LinearLayout linearlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        strPlayerTeam = "Team A";
        strUserType = "PLAYER";

        mapContents();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateFields();
            }
        });

        radioUserTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int selectedId=radioUserTypeGroup.getCheckedRadioButtonId();
                radioUserType=(RadioButton)findViewById(selectedId);
                Toast.makeText(ActivityLogin.this,radioUserType.getText(),Toast.LENGTH_SHORT).show();
                if (radioUserType.getText().toString().equals("ADMIN")) {
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
                //Toast.makeText(ActivityLogin.this,radioPlayerTeam.getText(),Toast.LENGTH_SHORT).show();
                if (radioPlayerTeam.getText().toString().equals("Team A")) {
                    strPlayerTeam = "Team A";
                }
                else {
                    strPlayerTeam = "Team B";
                }
            }
        });
    }

    void validateFields() {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (!edtEmail.getText().toString().matches(emailPattern))
        {
            Toast.makeText(getApplicationContext(),"valid email address",Toast.LENGTH_SHORT).show();
        }
        else if (edtPassword.getText().toString().length() < 6) {
            Toast.makeText(getApplicationContext(),"Password must be 6 characters only.",Toast.LENGTH_SHORT).show();
        }
        else {
            if (strUserType == "ADMIN") {
                Intent intent = new Intent(getApplicationContext(), ActivityAdminHome.class);
                startActivity(intent);
            }
            else {
                Intent intent = new Intent(getApplicationContext(), ActivityPlayerHome.class);
                startActivity(intent);
            }
        }
    }

    void mapContents() {
        edtEmail = (EditText) findViewById(R.id.user_email);
        edtPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.login_button);
        radioPlayerTeamGroup = (RadioGroup) findViewById(R.id.radioTeams);
        radioUserTypeGroup =  (RadioGroup) findViewById(R.id.radioPlayers);
        linearlayout = (LinearLayout) findViewById(R.id.linearlayout);
    }
}