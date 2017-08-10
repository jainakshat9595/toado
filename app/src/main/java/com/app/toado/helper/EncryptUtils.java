package com.app.toado.helper;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

/**
 * Created by ghanendra on 19/06/2017.
 */

public class EncryptUtils {

    public String encrypt(String txt, String key) {
        //encryption, first convert text to base 64 then xor it
//        byte[] bytesEncoded = Base64.encode(txt.getBytes(), 0);
//        System.out.println("bytes encoded: " + new String(bytesEncoded));
//        String enc = xorMessage(new String(bytesEncoded), key);
//        System.out.println(" XOR-ed to: " + enc);
        return txt;
    }

    public String decrypt(String enc, String key) {
        //encryption, first convert text to base 64 then xor it
//        String base64msg = xorMessage(enc, key);
//        System.out.println("decruption xor: " + base64msg);
//        byte[] bytesDecoded = Base64.decode(base64msg.getBytes(), 0);
//        String decoded = new String(bytesDecoded);
//        System.out.println(" bytes decoded: " + decoded);
        return enc;
    }


    public String xorMessage(String message, String key) {
        try {
            if (message == null || key == null) return null;

            char[] keys = key.toCharArray();
            char[] mesg = message.toCharArray();

            int ml = mesg.length;
            int kl = keys.length;
            char[] newmsg = new char[ml];

            for (int i = 0; i < ml; i++) {
                newmsg[i] = (char) (mesg[i] ^ keys[i % kl]);
            }//for i

            return new String(newmsg);
        } catch (Exception e) {
            return null;
        }
    }
}//class