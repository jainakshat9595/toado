package com.app.toado.helper;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by ghanendra on 27/06/2017.
 */

public class OpenFile {

    public static File createFile(Context context, String fname, String filetype) {
        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File(root + File.separator + context.getPackageName() + File.separator + "Media" + File.separator + filetype, fname);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                MediaScannerConnection.scanFile(context, new String[]{file.getPath()}, new String[]{getFileMimeType(getFileExtension(file))}, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static String getFileExtension(File file) {
        return file.getName().substring(file.getName().indexOf(".") + 1);
    }

    public static String getFileMimeType(String ext) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String type = mime.getMimeTypeFromExtension(ext);
        return type;
    }

}
