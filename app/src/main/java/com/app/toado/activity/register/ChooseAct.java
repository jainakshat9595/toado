package com.app.toado.activity.register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.app.toado.R;
import com.app.toado.helper.RegisterProfileFirebase;
import com.app.toado.settings.UserSession;
import com.app.toado.settings.UserSettingsSharedPref;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import static com.app.toado.helper.ToadoConfig.DBREF_USER_PROFILES;

/**
 * Created by ghanendra on 24/07/2017.
 */

public class ChooseAct extends AppCompatActivity {
    String mobnum;
    private CallbackManager callbackManager;
    private FacebookCallback<LoginResult> callback;
    private LoginButton loginButton;
    private ProgressDialog pd;
    private UserSession sharedpref;
    private UserSettingsSharedPref userSettingsSharedPref;
    String mykey;
    DatabaseReference dbnewusref;
    Button btnmanual;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_registermeth);

        mobnum = getIntent().getStringExtra(MobileRegisterAct.INTENT_PHONENUMBER);

        pd = new ProgressDialog(this);
        pd.setMessage("Please wait.");

        dbnewusref = DBREF_USER_PROFILES.push();
        mykey = dbnewusref.getKey();
        sharedpref = new UserSession(this);
        userSettingsSharedPref = new UserSettingsSharedPref(this);

        loginButton = (LoginButton) findViewById(R.id.login_button);

        callbackManager = CallbackManager.Factory.create();

        btnmanual = (Button) findViewById(R.id.next);

        handleFbLogin();
    }

    public void btnToPersonalDetailsAct(View view) {
        Intent in = new Intent(this, PersonalDetailsAct.class);
        in.putExtra(MobileRegisterAct.INTENT_PHONENUMBER, mobnum);
        startActivity(in);
    }

    private void handleFbLogin() {
        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends", "user_likes", "user_work_history", "user_about_me", "user_education_history"));
        callback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                System.out.println("success fb login" + loginResult.getAccessToken());
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        try {
                            System.out.println("fb object" + object.toString());
                            System.out.println("fb response" + response.toString());
                            getFbImage(loginResult, object);

                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                });
                //Here we put the requested fields to be returned from the JSONObject
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,birthday,email,gender,link,work,education,friends{first_name,last_name}");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                System.out.println("fb login canclled");
            }

            @Override
            public void onError(FacebookException e) {
                System.out.println("fb login error");
                e.printStackTrace();
            }
        };
        loginButton.registerCallback(callbackManager, callback);

    }


    private void getFbImage(LoginResult loginResult, final JSONObject object1) {

        new GraphRequest(loginResult.getAccessToken(),
                "/" + loginResult.getAccessToken().getUserId() + "/picture?redirect=false&type=large", null, HttpMethod.GET, new GraphRequest.Callback() {
            public void onCompleted(GraphResponse response) {
                try {

                    JSONObject jar = response.getJSONObject().getJSONObject("data");
                    String url = jar.getString("url").toString();
                    String name = object1.getString("name").toString();
                    String dob = object1.getString("birthday");
                    String gender = object1.getString("gender");

                    RegisterProfileFirebase.saveDataToFirebase(name, mobnum, gender, dob, url, mykey, sharedpref, userSettingsSharedPref, ChooseAct.this);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            System.out.println("on activity result called for fb");
            callbackManager.onActivityResult(requestCode, resultCode, data);
            pd.show();
            btnmanual.setEnabled(false);
        } else {
            Toast.makeText(this, "Facebook auth failed, please try again.", Toast.LENGTH_SHORT).show();
            pd.dismiss();
            btnmanual.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pd.cancel();
    }
}
