package com.ruitai.aijiubao.activity.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.ruitai.aijiubao.R;


public class SplashActivity extends Activity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_multi);
       /* findViewById(R.id.btn_enter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SplashActivity.this, BltActivity.class));
                finish();
            }
        });*/


        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, BltActivity.class));
                overridePendingTransition(R.anim.anim_faded_in, R.anim.anim_faded_out);
                finish();
            }
        },3000);
        //TODO Read local data

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
