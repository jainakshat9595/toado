package com.app.toado.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;

import com.app.toado.SplashAct;
import com.app.toado.activity.main.MainAct;
import com.app.toado.activity.settings.DistancePreferencesActivity;
import com.app.toado.settings.UserSession;
import com.facebook.login.LoginManager;

/**
 * Created by ghanendra on 16/06/2017.
 */

public class ToadoAlerts {
    Context contx;
    final AlertDialog.Builder builder;

    public ToadoAlerts(Context contx) {

        this.contx = contx;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(contx, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(contx);
        }

    }

    public void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(contx);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        contx.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void buildNoNearUsersAlert() {
        builder.setTitle("Cant find any one nearby?")
                .setMessage("Increase your radius of search!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        contx.startActivity(new Intent(contx, DistancePreferencesActivity.class));
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        System.out.println("alert dismissed sending broadcast to change tab to index 1 on main act toadoalerts");
                        contx.sendBroadcast(new Intent().putExtra("tabindex", "1").setAction("MainActTabHandler"));
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    public static void showLogoutAlert(final Activity contx, final UserSession usess) {
        System.out.println("deleteing user session toadoalerts");
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(contx, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(contx);
        }
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to Logout?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        usess.deleteSession();
                         contx.startActivity(new Intent(contx, SplashAct.class));
                        contx.finish();
                        if(LoginManager.getInstance()!=null)
                            LoginManager.getInstance().logOut();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void showGpsTrackingAlert(final Activity contx, final UserSession usess) {
        System.out.println("showgpstrackingakert toadoalerts");
//        AlertDialog.Builder builder;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            builder = new AlertDialog.Builder(contx, android.R.style.Theme_Material_Dialog_Alert);
//        } else {
//            builder = new AlertDialog.Builder(contx);
//        }
//        builder.setTitle("Background Location Tracking")
//                .setMessage("Want to stay up to date with Toado users around you? Click Yes to close this message or no to change settings now. You can change this setting again from setting menu.")
//                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .setNegativeButton("no", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        contx.startActivity(new Intent(contx, DistancePreferencesActivity.class));
//                    }
//                })
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .show();
    }

}
