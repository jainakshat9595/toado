package com.app.toado.helper;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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

    public static void openFile(Context context, File url) throws IOException {
        // Create URI

        File file = url;
//        Uri uri = Uri.fromFile(file);
        System.out.println(file.getAbsoluteFile() + " nougat uri from OpenFile ");
        meth2(context, file, Uri.parse("content:///com.app.toado.provider/toado_external_files" + url));
    }

    public static void normalopenFile(Context context, Uri uri) throws IOException {
        // Create URI
        File file = new File(String.valueOf(uri));
        System.out.println(file.getAbsoluteFile() + " marshmallow uri from OpenFile ");
        meth2(context, file, uri);
    }


//
//    public static void openFileNormal(Context context, File url) {
//        File file = url;
//        Uri uri = Uri.fromFile(file);
//        System.out.println(url.toString().lastIndexOf(".") + " normal uri from OpenFile " + uri.getPath());
//
//        meth2(context, url, uri);
//
//    }
//

    public static void meth2(Context context, File url, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/zip");
        } else if (url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if (url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    public static String getFileType(String url) {
        String type = "nofiletype";

        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            type = "doc";
        } else if (url.toString().contains(".pdf")) {
            // PDF file
            type = "pdf";
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            type = "ppt";
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            type = "excel";
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            type = "audio";
        } else if (url.toString().contains(".rtf")) {
            // RTF file
            type = "rtf";
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            type = "audio";
        } else if (url.toString().contains(".gif")) {
            // GIF file
            type = "image";
        } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            type = "image";
        } else if (url.toString().contains(".txt")) {
            // Text file
            type = "doc";
        } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            type = "audio";
        }
        return type;
    }

    public static String getFileExtensionFromUri(Context context, Uri uri) {
        ContentResolver cR = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String type = mime.getExtensionFromMimeType(cR.getType(uri));
        return type;
    }

    public static String getFileExtFromPath(String filename) {

        String filenameArray[] = filename.split("\\.");
        String extension = filenameArray[filenameArray.length - 1];
        return extension;
    }

    public static File createFile(Context context, String fname,String filetype) {
        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File(root + File.separator + context.getPackageName() + File.separator + "Media"+ File.separator + filetype, fname);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

}
