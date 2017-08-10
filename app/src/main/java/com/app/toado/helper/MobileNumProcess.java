package com.app.toado.helper;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

/**
 * Created by ghanendra on 05/07/2017.
 */

public class MobileNumProcess {
    public static Phonenumber.PhoneNumber processMobNum(String mPhone1){
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber numberProto = new Phonenumber.PhoneNumber();
        try {
            numberProto = phoneUtil.parse(mPhone1, "");
        } catch (NumberParseException e) {
            e.printStackTrace();
            numberProto=null;
        }
        return numberProto;
    }
}
