package com.app.toado;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.app.toado.activity.register.PersonalDetailsAct;
import com.app.toado.activity.settings.DistancePreferencesActivity;
 import com.app.toado.services.LocServ;
 import com.app.toado.settings.UserSettingsSharedPref;
import com.app.toado.activity.ToadoBaseActivity;
import com.app.toado.activity.main.MainAct;
import com.app.toado.activity.register.MobileRegisterAct;
import com.app.toado.settings.UserSession;

public class SplashAct extends ToadoBaseActivity {
    Button btnSignin, btnSignup;
    private UserSession session;
    private UserSettingsSharedPref userSettingsSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        session = new UserSession(SplashAct.this);
        userSettingsSharedPref = new UserSettingsSharedPref(SplashAct.this);
        System.out.println("splash activity session data" + session.getUserKey());
        if (session.isolduser() == true) {
            Intent in = new Intent(SplashAct.this, MainAct.class);
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(in);
            Intent in2 = new Intent(this, LocServ.class);
            in2.putExtra("keyval", session.getUserKey());
            System.out.println("starting service from splash act with key" + session.getUserKey());
            startService(in2);
            finish();

        }
        btnSignin = (Button) findViewById(R.id.btnsignin);
        btnSignup = (Button) findViewById(R.id.btnsignup);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashAct.this, MobileRegisterAct.class);
                intent.putExtra("method", "signup");
                startActivity(intent);
            }
        });
        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashAct.this, MobileRegisterAct.class);
                intent.putExtra("method", "signin");
                startActivity(intent);
            }
        });
    }

    public void signinAct(View view) {
        System.out.println("signin act");
    }

    public void singupAct(View view) {
        System.out.println("singup act");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
