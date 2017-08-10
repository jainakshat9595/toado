package com.app.toado.helper;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by ghanendra on 02/07/2017.
 */

public class CloseKeyboard {

    public boolean closeKeyb2(final EditText et, final Context contx, View v, int keyCode, KeyEvent event) {
        System.out.println("keycode value android closekeyboard" + keyCode);
        if (keyCode == EditorInfo.IME_ACTION_SEARCH ||
                keyCode == EditorInfo.IME_ACTION_DONE ||
                keyCode == EditorInfo.IME_ACTION_GO ||
                keyCode == EditorInfo.IME_ACTION_NEXT ||
                keyCode == EditorInfo.IME_ACTION_UNSPECIFIED) {
            System.out.println(" key down and key enter mob register act " + et.getText());

            InputMethodManager imm = (InputMethodManager) contx.getSystemService(Context.INPUT_METHOD_SERVICE);
            et.clearFocus();
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    public void closeKeybChatAct(final EditText et, final Context contx,View v) {
        System.out.println("keycode cht activity closekeyboard");

        InputMethodManager imm = (InputMethodManager) contx.getSystemService(Context.INPUT_METHOD_SERVICE);
        et.clearFocus();
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}




