package com.app.toado.activity.register;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberUtils;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.toado.R;
import com.app.toado.activity.main.MainAct;
import com.app.toado.activity.ToadoBaseActivity;
import com.app.toado.helper.CloseKeyboard;
import com.app.toado.helper.MobileNumProcess;
import com.app.toado.helper.MyXMPP2;
import com.app.toado.services.LocServ;
import com.app.toado.settings.UserSession;
import com.app.toado.model.MobileKey;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.sinch.verification.CodeInterceptionException;
import com.sinch.verification.Config;
import com.sinch.verification.InitiationResult;
import com.sinch.verification.InvalidInputException;
import com.sinch.verification.ServiceErrorException;
import com.sinch.verification.SinchVerification;
import com.sinch.verification.Verification;
import com.sinch.verification.VerificationListener;

import java.util.ArrayList;
import java.util.List;

import static com.app.toado.helper.ToadoConfig.DBREF_USER_MOBS;

public class OtpAct extends ToadoBaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "VerificationActivity";

    private static final String APPLICATION_KEY = "30805608-153d-43e1-8e5b-240b1cc185a5";
    private String option;
    private Verification mVerification;
    private boolean mIsSmsVerification;
    private boolean mShouldFallback = true;
    private String mPhoneNumber;
    private Button btnresendotp;
    private static final String[] SMS_PERMISSIONS = {Manifest.permission.INTERNET,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.ACCESS_NETWORK_STATE};
    private static final String[] FLASHCALL_PERMISSIONS = {Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_NETWORK_STATE};
    VerificationListener listener;
    EditText input;
    private MyXMPP2 myxinstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        btnresendotp = (Button) findViewById(R.id.resendotp);

        Intent intent = getIntent();
        if (intent != null) {
            mPhoneNumber = intent.getStringExtra(MobileRegisterAct.INTENT_PHONENUMBER);
            final String method = intent.getStringExtra(MobileRegisterAct.INTENT_METHOD);
            mIsSmsVerification = method.equalsIgnoreCase(MobileRegisterAct.SMS);
            TextView phoneText = (TextView) findViewById(R.id.numberText);
            phoneText.setText(mPhoneNumber);
            System.out.println("mphonenum otpact" + mPhoneNumber);
            option = intent.getStringExtra("option");
            requestPermissions();
        } else {
            Log.e(TAG, "The provided intent is null.");
        }

        System.out.println(mPhoneNumber + " mphone number otp activity" + mIsSmsVerification);
    }

    private void requestPermissions() {
        List<String> missingPermissions;
        String methodText;

        if (mIsSmsVerification) {
            missingPermissions = getMissingPermissions(SMS_PERMISSIONS);
            methodText = "SMS";
        } else {
            missingPermissions = getMissingPermissions(FLASHCALL_PERMISSIONS);
            methodText = "calls";
        }

        if (missingPermissions.isEmpty()) {
            createVerification();
        } else {
            if (needPermissionsRationale(missingPermissions)) {
                Toast.makeText(this, "This application needs permissions to read your " + methodText + " to automatically verify your "
                        + "phone, you may disable the permissions once you have been verified.", Toast.LENGTH_LONG)
                        .show();
            }
            ActivityCompat.requestPermissions(this,
                    missingPermissions.toArray(new String[missingPermissions.size()]),
                    0);
        }
    }

    private void createVerification() {
        // It is important to pass ApplicationContext to the Verification config builder as the
        // verification process might outlive the activity.
        System.out.println(" createverification called enableinputfield set true");
        enableInputField(true);
        Config config = SinchVerification.config()
                .applicationKey(APPLICATION_KEY)
                .context(getApplicationContext())
                .build();
        TextView messageText = (TextView) findViewById(R.id.textView);

        listener = new MyVerificationListener();

        if (mIsSmsVerification) {
            System.out.println(" sms createverification called ");

            messageText.setText(R.string.sending_sms);
            mVerification = SinchVerification.createSmsVerification(config, mPhoneNumber, listener);
            mVerification.initiate();
        } else {
            System.out.println(" call createverification called ");

            messageText.setText(R.string.flashcalling);
            mVerification = SinchVerification.createFlashCallVerification(config, mPhoneNumber, listener);
            mVerification.initiate();
        }

        showProgress();
    }

    private boolean needPermissionsRationale(List<String> permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Proceed with verification after requesting permissions.
        // If the verification SDK fails to intercept the code automatically due to missing permissions,
        // the VerificationListener.onVerificationFailed(1) method will be executed with an instance of
        // CodeInterceptionException. In this case it is still possible to proceed with verification
        // by asking the user to enter the code manually.
        createVerification();
    }

    private List<String> getMissingPermissions(String[] requiredPermissions) {
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        return missingPermissions;
    }

    public void onSubmitClicked(View view) {
        String code = (input).getText().toString();
        if (!code.isEmpty()) {
            if (mVerification != null) {
                System.out.println("input code to send otpact" + code);
                mVerification.verify(code);
                showProgress();
                TextView messageText = (TextView) findViewById(R.id.textView);
                messageText.setText("Verification in progress");
                enableInputField(false);
            }
        }
    }

    public void onResendClicked(View view) {
        System.out.println("resend otp clicked");
        requestPermissions();
        if (mIsSmsVerification)
            Toast.makeText(OtpAct.this, "Sending otp again, please wait.", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(OtpAct.this, "Trying flash call again, please wait.", Toast.LENGTH_SHORT).show();

        if (btnresendotp.isEnabled()) {
            new CountDownTimer(60000, 1000) {

                public void onTick(long millisUntilFinished) {
                    btnresendotp.setText("Try again in : " + millisUntilFinished / 1000 + " seconds.");
                    //here you can have your logic to set text to edittext
                    btnresendotp.setEnabled(false);
                }

                public void onFinish() {
                    if (mIsSmsVerification)
                        btnresendotp.setText("Resend Otp");
                    else
                        btnresendotp.setText("Flash Call again");

                    btnresendotp.setEnabled(true);
                }

            }.start();

        }

    }

    private void enableInputField(boolean enable) {
        View container = findViewById(R.id.inputContainer);
        if (enable) {
            TextView hintText = (TextView) findViewById(R.id.enterToken);
            hintText.setText(mIsSmsVerification ? R.string.sms_enter_code : R.string.flashcall_enter_cli);
            container.setVisibility(View.VISIBLE);
            if (mIsSmsVerification) {
                //sms verification
                input = (EditText) findViewById(R.id.inputCode);
                input.setVisibility(View.VISIBLE);
            } else {
                //flashcall
                input = (EditText) findViewById(R.id.inputCodeCall);
                input.setVisibility(View.VISIBLE);
            }

            final CloseKeyboard cb = new CloseKeyboard();
            input.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    System.out.println("on editor action mob register act");
                    return cb.closeKeyb2(input, OtpAct.this, v, actionId, event);
                }

            });

            if (mIsSmsVerification)
                btnresendotp.setText("Resend Otp");
            else
                btnresendotp.setText("Flash Call again");
        } else {
            container.setVisibility(View.GONE);
        }
    }

    private void hideProgressAndShowMessage(int message) {
        hideProgress();
        TextView messageText = (TextView) findViewById(R.id.textView);
        messageText.setText(message);
    }

    private void hideProgress() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressIndicator);
        progressBar.setVisibility(View.INVISIBLE);
        TextView progressText = (TextView) findViewById(R.id.progressText);
        progressText.setVisibility(View.INVISIBLE);
    }

    private void showProgress() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressIndicator);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void showCompleted() {
        ImageView checkMark = (ImageView) findViewById(R.id.checkmarkImage);
        checkMark.setVisibility(View.VISIBLE);
    }

    class MyVerificationListener implements VerificationListener {

        @Override
        public void onInitiated(InitiationResult result) {
            Log.d(TAG, "Initialized!");
            showProgress();
        }

        @Override
        public void onInitiationFailed(Exception exception) {
            Log.e(TAG, "Verification initialization failed: " + exception.getMessage());
            hideProgressAndShowMessage(R.string.failed);

            if (exception instanceof InvalidInputException) {
                // Incorrect number provided
            } else if (exception instanceof ServiceErrorException) {
                // Verification initiation aborted due to early reject feature,
                // client callback denial, or some other Sinch service error.
                // Fallback to other verification method here.

                if (mShouldFallback) {
                    mIsSmsVerification = !mIsSmsVerification;
                    if (mIsSmsVerification) {
                        Log.i(TAG, "Falling back to sms verification.");
                    } else {
                        Log.i(TAG, "Falling back to flashcall verification.");
                    }
                    mShouldFallback = false;
                    // Initiate verification with the alternative method.
                    requestPermissions();
                }
            } else {
                // Other system error, such as UnknownHostException in case of network error
            }
        }

        @Override
        public void onVerified() {
            Log.d(TAG, "Verified!" + mPhoneNumber);
            hideProgressAndShowMessage(R.string.verified);
            showCompleted();
            if (option.equals("signin")) {
                DatabaseReference retreiveDetails = DBREF_USER_MOBS.child(mPhoneNumber).getRef();
                System.out.println("dbref phnum otpact" + retreiveDetails);
                retreiveDetails.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        MobileKey mobileKey = MobileKey.parse(dataSnapshot);
                        UserSession session = new UserSession(OtpAct.this);
                        session.create_oldusersession(mobileKey.getUserkey());
                        startService(new Intent(OtpAct.this, LocServ.class));
                        Intent in = new Intent(OtpAct.this, MainAct.class);
                        in.putExtra("mykey", mobileKey.getUserkey());
                        System.out.println(mobileKey.getUserkey() + " starting loc service from otp act " + session.getUserKey());
                        myxinstance = MyXMPP2.getInstance(OtpAct.this, getString(R.string.server), mobileKey.getUserkey());

                        startActivity(in);
                        finish();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else {
                try {
                    System.out.println("otp act successful signin verification " + mPhoneNumber);
                    Intent intent = new Intent(OtpAct.this, ChooseAct.class);
                    intent.putExtra(MobileRegisterAct.INTENT_PHONENUMBER, mPhoneNumber);
                    startActivity(intent);
                    finish();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }

        @Override
        public void onVerificationFailed(Exception exception) {
            exception.printStackTrace();

            hideProgressAndShowMessage(R.string.failed);

            enableInputField(true);
            btnresendotp.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        listener = null;
        finish();
    }

}
