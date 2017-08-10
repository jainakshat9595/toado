package com.app.toado.activity.chat;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.app.toado.activity.chat.utils.BmpUtils;

import java.io.File;
import java.io.IOException;

public class CameraCropActivity extends AppCompatActivity {
    private String imagePath, saveUri;
    String TAG = "cam crop act";
    final int PIC_CROP = 1;

//    CropperView mImageView;
//    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_camera_crop);
//
//        Bundle extras = getIntent().getExtras();
//        if (extras != null) {
//            if (extras.containsKey("path")) {
//                imagePath = extras.getString("path");
//            }
//        }
//
//        mImageView = (CropperView) findViewById(R.id.cropper_view1);
//        mImageView.setGestureEnabled(true);
//
//        loadNewImage(imagePath);
//        mImageView.setDebug(true);
//
//        if (mImageView.getWidth() != 0) {
//            mImageView.setMaxZoom(mImageView.getWidth() * 2 / 1280f);
//        } else {
//
//            ViewTreeObserver vto = mImageView.getViewTreeObserver();
//            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//                @Override
//                public boolean onPreDraw() {
//                    mImageView.getViewTreeObserver().removeOnPreDrawListener(this);
//                    mImageView.setMaxZoom(mImageView.getWidth() * 2 / 1280f);
//                    return true;
//                }
//            });
//
//        }
//    }
//
//    private void performCrop(Uri picUri) {
//        try {
//            Intent intent = new Intent("com.android.camera.action.CROP");
//            intent.setClassName("com.android.camera", "com.android.camera.CropImage");
////            File file = new File(filePath);
//            intent.setData(picUri);
//            intent.putExtra("crop", "true");
//            intent.putExtra("aspectX", 1);
//            intent.putExtra("aspectY", 1);
//            intent.putExtra("outputX", 96);
//            intent.putExtra("outputY", 96);
//            intent.putExtra("noFaceDetection", true);
//            intent.putExtra("return-data", true);
//            startActivityForResult(intent, 1);
//        }
//        // respond to users whose devices do not support the crop action
//        catch (ActivityNotFoundException anfe) {
//            Log.d(TAG, " error ");
//            anfe.printStackTrace();
//            // display an error message
//            String errorMessage = "Whoops - your device doesn't support the crop action!";
//            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
//            toast.show();
//        }
//    }
//
//
//    private void loadNewImage(String imagePath) {
//
//        Log.i(TAG, "load image: " + imagePath);
//        mBitmap = BitmapFactory.decodeFile(imagePath);
//        Log.i(TAG, "bitmap: " + mBitmap.getWidth() + " " + mBitmap.getHeight());
//
//        int maxP = Math.max(mBitmap.getWidth(), mBitmap.getHeight());
//        float scale1280 = (float) maxP / 1280;
//
//        if (mImageView.getWidth() != 0) {
//            mImageView.setMaxZoom(mImageView.getWidth() * 2 / 1280f);
//        } else {
//
//            ViewTreeObserver vto = mImageView.getViewTreeObserver();
//            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//                @Override
//                public boolean onPreDraw() {
//                    mImageView.getViewTreeObserver().removeOnPreDrawListener(this);
//                    mImageView.setMaxZoom(mImageView.getWidth() * 2 / 1280f);
//                    return true;
//                }
//            });
//
//        }
//
//        mBitmap = Bitmap.createScaledBitmap(mBitmap, (int) (mBitmap.getWidth() / scale1280),
//                (int) (mBitmap.getHeight() / scale1280), true);
//
//        mImageView.setImageBitmap(mBitmap);
//
//    }
//
//
//    public void cancelCrop(View view) {
//        finish();
//    }
//
//
//    public void doneCrop(View view) {
//        performCrop(Uri.parse(imagePath));
////        cropImageAsync();
//    }
//
//    private void cropImageAsync() {
//        mImageView.getCroppedBitmapAsync(new CropperCallback() {
//            @Override
//            public void onCropped(Bitmap bitmap) {
//                if (bitmap != null) {
//
//                    try {
//                        BmpUtils.writeBitmapToFile(bitmap, new File(Environment.getExternalStorageDirectory() + "/crop_test.jpg"), 90);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            public void onOutOfMemoryError() {
//
//            }
//        });
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == PIC_CROP) {
//            if (data != null) {
//                // get the returned data
//                Bundle extras = data.getExtras();
//                // get the cropped bitmap
//                Bitmap selectedBitmap = extras.getParcelable("data");
//
//                mImageView.setImageBitmap(selectedBitmap);
//            }
//        }
//    }


    }
}
