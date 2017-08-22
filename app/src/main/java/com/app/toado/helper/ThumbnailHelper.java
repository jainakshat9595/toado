package com.app.toado.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ghanendra on 02/07/2017.
 */

public class ThumbnailHelper {
    final static String TAG = "bitmap helper";

    public static String createThumbnail(String path, Context context, String filename, String filetype) {
        File thumbnailFile=null;
        final int THUMBSIZE = 64;
        Bitmap ThumbImage = null;
        String fname = filename.substring(0,filename.lastIndexOf("."))+GetTimeStamp.timeStamp()+".jpeg";
        Log.d(TAG,"filename "+fname);
        try {
            thumbnailFile = OpenFile.createFile(context, "thumbnail" + fname, "sent");

            FileOutputStream fos = null;
            if (filetype.matches("photo")) {
                ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path),
                        THUMBSIZE, THUMBSIZE);
            } else {
                ThumbImage = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);
            }
            fos = new FileOutputStream(thumbnailFile);
            ThumbImage.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(ThumbImage!=null)
                ThumbImage.recycle();
            return thumbnailFile.getAbsolutePath();
        }
    }
}
