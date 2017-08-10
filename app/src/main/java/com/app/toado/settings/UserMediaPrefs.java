package com.app.toado.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.toado.model.LocalMediaUri;

/**
 * Created by RajK on 16-06-2017.
 */

public class UserMediaPrefs {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int mode=0;
    String prefname="LocalURi";

    private String NO_ID_FOUND = "nouri";


    public UserMediaPrefs(Context context)
    {
        this._context=context;
        pref = _context.getSharedPreferences(prefname,mode);
        editor = pref.edit();
    }

    public void setUri(String id,String uri)
    {
        editor.putString(id,uri);
        editor.apply();
    }

    public String getURI(String id)
    {
        return pref.getString(id,NO_ID_FOUND);
    }

    public void clearOldSetting()
    {
        editor.clear();
        editor.apply();
    }

}
