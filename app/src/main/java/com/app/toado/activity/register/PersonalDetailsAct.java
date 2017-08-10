package com.app.toado.activity.register;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.app.toado.R;
import com.app.toado.activity.ToadoBaseActivity;
import com.app.toado.activity.main.MainAct;
import com.app.toado.helper.CircleTransform;
import com.app.toado.helper.CloseKeyboard;
import com.app.toado.helper.MarshmallowPermissions;
import com.app.toado.helper.MobileNumProcess;
import com.app.toado.helper.RegisterProfileFirebase;
import com.app.toado.helper.ToadoAlerts;
import com.app.toado.model.MobileKey;
import com.app.toado.model.User;
import com.app.toado.model.Usersession;
import com.app.toado.settings.UserSession;
import com.app.toado.settings.UserSettingsSharedPref;
import com.bumptech.glide.Glide;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.calendardatepicker.MonthAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.i18n.phonenumbers.Phonenumber;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.app.toado.helper.ToadoConfig.DBREF_USER_MOBS;
import static com.app.toado.helper.ToadoConfig.DBREF_USER_PROFILES;
import static com.app.toado.helper.ToadoConfig.DBREF_USER_SESSIONS;
import static com.app.toado.helper.ToadoConfig.STORAGE_REFERENCE;

/**
 * Created by ghanendra on 01/07/2017.
 */

public class PersonalDetailsAct extends ToadoBaseActivity implements CalendarDatePickerDialogFragment.OnDateSetListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private ImageButton imgb;
    private TextView tvDob;
    private EditText etName;
    RadioButton rbmale, rbfemale;
    MarshmallowPermissions marshper;
    String imguri = "nil";
    String fbimguri = "nil";
    private static final String[] STORAGE_PERMISSION = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS
    };
    Phonenumber.PhoneNumber numberProto;
    private String mPhone;
    ProgressDialog pd;
    String mykey;
    DatabaseReference dbnewusref;
    private String downloadUrl;
    private UserSession sharedpref;
    private UserSettingsSharedPref userSettingsSharedPref;

    private boolean agecheck=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personaldetailsact);

        System.out.println("personal details act1");

        pd = new ProgressDialog(this);

        marshper = new MarshmallowPermissions(this);
        marshper.reguestNewPermissions(this, STORAGE_PERMISSION);
        imgb = (ImageButton) findViewById(R.id.profilepicchooser);
        etName = (EditText) findViewById(R.id.name);
        tvDob = (TextView) findViewById(R.id.dobpers);
        rbmale = (RadioButton) findViewById(R.id.radiomale);
        rbfemale = (RadioButton) findViewById(R.id.radiofemale);

        mPhone = getIntent().getStringExtra(MobileRegisterAct.INTENT_PHONENUMBER);

        dbnewusref = DBREF_USER_PROFILES.push();
        mykey = dbnewusref.getKey();

        sharedpref = new UserSession(this);
        userSettingsSharedPref = new UserSettingsSharedPref(this);

        final LocationManager manager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ToadoAlerts alr = new ToadoAlerts(this);
            alr.buildAlertMessageNoGps();
        }

        final CloseKeyboard cb = new CloseKeyboard();
        etName.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                System.out.println("on editor action mob register act");
                return cb.closeKeyb2(etName, PersonalDetailsAct.this, v, actionId, event);
            }

        });

    }

    public void openDatePicker(View view) {
        System.out.println("clicked editdob");
        Date now = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(now);
        MonthAdapter.CalendarDay minDate = new MonthAdapter.CalendarDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                .setOnDateSetListener(PersonalDetailsAct.this)
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setDateRange(null, minDate)
                .setDoneText("Ok")
                .setCancelText("Cancel")
                .setThemeDark();
        cdp.show(getSupportFragmentManager(), "Select Day, Month and Year.");
    }


    public void chooseMale(View view) {
        rbmale.setChecked(true);
        rbfemale.setChecked(false);
    }

    public void chooseFemale(View view) {
        rbmale.setChecked(false);
        rbfemale.setChecked(true);
    }


    public void btnToMain(View view) {
        handleProgressDialog("show");
        String name = etName.getText().toString().trim();
        String dob = tvDob.getText().toString();
        String gender = "nil";
        if (rbmale.isChecked()) {
            gender = "male";
        } else if (rbfemale.isChecked()) {
            gender = "female";
        }
        if(name.matches("")){
            handleProgressDialog("hide");
            Toast.makeText(this, R.string.enternameerror, Toast.LENGTH_SHORT).show();
        }else if(dob.trim().matches("Click to Enter Date of Birth")){
            handleProgressDialog("hide");
            Toast.makeText(this, R.string.enterageerror, Toast.LENGTH_SHORT).show();
        }else if(!agecheck){
            handleProgressDialog("hide");
            Toast.makeText(this, R.string.enterage13error, Toast.LENGTH_SHORT).show();
        }else if(gender.matches("nil")){
            handleProgressDialog("hide");
            Toast.makeText(this, R.string.entergendererror, Toast.LENGTH_SHORT).show();
        } else {
            switch (imguri) {
                case "nil":
                    System.out.println("image uri is nil");
                    RegisterProfileFirebase.saveDataToFirebase(name, mPhone, gender, dob, "nil",mykey,sharedpref,userSettingsSharedPref,PersonalDetailsAct.this);
                    break;
                default:
                    uploadImage(name,mPhone,gender,dob,Uri.parse(imguri));
                    break;
            }
        }
     }

    public void imagePicker(View view) {

        imgMeth();

    }

    private void imgMeth() {
        if (marshper.reguestNewPermissions(this, STORAGE_PERMISSION).matches("pernotreq")) {
            System.out.println("permission not required imagepicker personaldetact");
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMultiTouchEnabled(true)
                    .start(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        System.out.println(" on request permission result persdetact");
//        imgMeth();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CropImage.ActivityResult result = CropImage.getActivityResult(data);
        if (resultCode == RESULT_OK) {
            imguri = result.getUri().toString();
            System.out.println("image cropped uri personaldetailsact" + imguri);
            Glide.with(this).load(new File(result.getUri().getPath())).transform(new CircleTransform(this)).into(imgb);

        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            System.out.println("image pick failed");
            imguri = "nil";
        }
    }

    private void uploadImage(final String name, final String mPhone, final String gender, final String dob, Uri uri) {
        StorageReference profilepicref = STORAGE_REFERENCE.child(mykey).child(uri.getLastPathSegment());
        profilepicref.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        downloadUrl = taskSnapshot.getDownloadUrl().toString();
                        System.out.println("file upload successfulregister user pdetailsact" + downloadUrl);
                        RegisterProfileFirebase.saveDataToFirebase(name, mPhone, gender, dob, downloadUrl,mykey,sharedpref,userSettingsSharedPref,PersonalDetailsAct.this);
//                            goToHome(downloadUrl.toString());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                        downloadUrl = "nil";
                        System.out.println("failed to upload image to fireabse" + downloadUrl);
                        RegisterProfileFirebase.saveDataToFirebase(name, mPhone, gender, dob, downloadUrl,mykey,sharedpref,userSettingsSharedPref,PersonalDetailsAct.this);
                    }
                });

    }

    private void handleProgressDialog(String s) {
        pd.setMessage("Please wait.");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setIndeterminate(true);
        switch (s) {
            case "show":
                pd.show();
                break;
            case "hide":
                pd.cancel();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
        String df = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("datetimefrom" + dayOfMonth + monthOfYear + 1 + "/" + "/" + year + "  " + df.format(Calendar.getInstance().getTime().toString()));
        tvDob.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
        LocalDate enteredage = new LocalDate(year,monthOfYear+1,dayOfMonth);
        int a = calculateAge(enteredage);
        if(a<13){
            Toast.makeText(this, R.string.enterage13error , Toast.LENGTH_LONG).show();
            agecheck=false;
        }else{
            agecheck=true;
        }
    }

    public int calculateAge(LocalDate enteredage) {
        LocalDate now = new LocalDate();
        Years age = Years.yearsBetween(enteredage, now);
        return age.getYears();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pd.cancel();
    }
}
