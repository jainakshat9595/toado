package com.app.toado.helper;

/**
 * Created by ghanendra on 11/06/2017.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MarshmallowPermissions {

    public static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 3;
    public static final int READSMS_REQUEST_CODE= 4;
    public static final int RECVSMS_REQUEST_CODE= 5;
    public static final int LOCSERV_REQUEST_CODE= 5;
    public static final int EXTERNAL_STORAGE_READ_PERMISSION_REQUEST_CODE  = 6;
    public static final int CALL_PERMISSION_REQUEST_CODE  = 7;
    private int CONTACT_PERMISSION_REQUEST_CODE=8;


    Activity activity;

    public MarshmallowPermissions(Activity activity) {
        this.activity = activity;
    }

    public boolean checkPermissionForExternalStorage(){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermissionForCamera(){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermissionForReadSms(){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermissionForRecvSms(){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECEIVE_SMS);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermissionForLocations(){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermissionForReadStorage(){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermissionForCalls(){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermissionForContacts(){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }


    public void requestPermissionForExternalStorage(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(activity, "External Storage permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    public void requestPermissionForReadsms(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_SMS)){
            Toast.makeText(activity, "Please allow to read sms to automatically detect otp.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.READ_SMS},READSMS_REQUEST_CODE);
        }
    }

    public void requestPermissionForRecievesms(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECEIVE_SMS)){
            Toast.makeText(activity, "Please allow to read sms to automatically detect otp.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.RECEIVE_SMS},RECVSMS_REQUEST_CODE);
        }
    }

    public void requestPermissionForLocations(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(activity, "Please allow us to use locations.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCSERV_REQUEST_CODE);
        }
    }

    public void requestPermissionForCamera(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)){
            Toast.makeText(activity, "Please allow to be able to take camera images.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    public void requestPermissionForReadExternalStorage(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)){
            Toast.makeText(activity, "Please allow to be able to read External Storage.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},EXTERNAL_STORAGE_READ_PERMISSION_REQUEST_CODE);
        }
    }

    public void requestPermissionForCalls(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)){
            Toast.makeText(activity, "Please allow to be able to make and receive calls.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.RECORD_AUDIO},CALL_PERMISSION_REQUEST_CODE);
        }
    }

    public void requestPermissionForContacts(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)){
            Toast.makeText(activity, "Please allow to be able to read contacts.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.READ_CONTACTS},CONTACT_PERMISSION_REQUEST_CODE);
        }
    }

    private List<String> getMissingPermissions(Context contx, String[] requiredPermissions) {
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(contx , permission)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        return missingPermissions;
    }

    public String reguestNewPermissions(Activity contx,String [] perm) {
        List<String> missingPermissions=getMissingPermissions(contx,perm);

        if (missingPermissions.isEmpty()) {
            return "pernotreq";
        } else {
            if (needPermissionsRationale(contx,missingPermissions)) {
                Toast.makeText(contx, "This application needs permissions to complete the action.", Toast.LENGTH_LONG)
                        .show();
            }
            ActivityCompat.requestPermissions(contx,
                    missingPermissions.toArray(new String[missingPermissions.size()]),
                    123);

            for(String miss : missingPermissions){
                System.out.println("missing permissions for marhsmallowpermissions"+miss);
            }
            return "perreq";
        }
    }

    private boolean needPermissionsRationale(Activity contx, List<String> permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(contx, permission)) {
                return true;
            }
        }
        return false;
    }
}