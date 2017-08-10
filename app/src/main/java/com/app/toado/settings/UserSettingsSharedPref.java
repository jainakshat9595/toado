package com.app.toado.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by RajK on 16-06-2017.
 */

public class UserSettingsSharedPref {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int mode = 0;
    String prefname = "GlobalSettings";

    private String DEFAULT_UNIT = "miles";
    private float DEFAULT_CONVERSIONFACTOR = 1.6F;


    public UserSettingsSharedPref(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(prefname, mode);
        editor = pref.edit();
    }

    public Boolean getGpsBackSetting() {
        return pref.getBoolean("gpsbackgroundtracking", true );
    }

    public void setGpsBackSetting(Boolean b) {
        editor.putBoolean("gpsbackgroundtracking", b);
        editor.commit();
    }

    public void setDistancePref(String unit, Integer value, float convFact) {
        editor.putString("unit", unit);
        editor.putInt("value", value);
        editor.putFloat("conversionFactor", convFact);
        editor.putBoolean("isDistancePrefSet", true);
        editor.apply();
    }

    public String getUnit() {
        return pref.getString("unit", DEFAULT_UNIT);
    }

    public Integer getValue() {
        return pref.getInt("value", 10);
    }

    public float getConversionFactor() {
        return pref.getFloat("conversionFactor", DEFAULT_CONVERSIONFACTOR);
    }

    public void clearOldSetting() {
        editor.clear();
        editor.apply();
    }

    public boolean getisDistancePrefSet() {
        return pref.getBoolean("isDistancePrefSet", false);
    }
}
