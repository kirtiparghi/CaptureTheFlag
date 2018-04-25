package com.kirtiparghi.capturetheflag;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class ActivitySplash extends Activity {

    TextView txtViewAppName;
    WebView view;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.layout_splash);

        mapContents();

        txtViewAppName.setTypeface(null, Typeface.BOLD);

        InputStream stream = null;
        try {
            stream = getAssets().open("flag_animated.gif");
        } catch (IOException e) {
            e.printStackTrace();
        }

        view = (WebView) findViewById(R.id.view);
        view.loadUrl("file:///android_asset/flag_animated.gif");
        view.setBackgroundColor(getResources().getColor(R.color.themecolor));

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {

                SharedPreferences sharedpreferences = getSharedPreferences("ctf", Context.MODE_PRIVATE);
                if (sharedpreferences.getString("email","").equals("admin@gmail.com")) {
                    Intent main = new Intent(ActivitySplash.this,
                            ActivityAdminHome.class);
                    startActivity(main);
                }
                else if (sharedpreferences.getString("isPlayer","").equals("true")) {

                    Intent main = new Intent(ActivitySplash.this,
                            ActivityPlayerHome.class);

                    startActivity(main);
                }
                else {
                        Intent main = new Intent(ActivitySplash.this,
                                ActivityLogin.class);
                        startActivity(main);
                }
                finish();
            }
        }, 3000);
    }

    void mapContents() {
        txtViewAppName = (TextView) findViewById(R.id.txtViewAppName);
    }
}