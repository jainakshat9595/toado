package com.app.toado.activity.chat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.toado.R;
import com.app.toado.adapter.ShowPhotoAdapter;
import com.app.toado.adapter.utils.SnappyLinearLayoutManager;
import com.app.toado.adapter.utils.SnappyRecyclerView;
import com.app.toado.helper.ChatHelper;
import com.app.toado.helper.GetTimeStamp;
import com.app.toado.helper.ImageComment;
import com.app.toado.helper.UriHelper;
import com.app.toado.model.ShowPhotoModel;
import com.app.toado.model.realm.UploadTable;
import com.app.toado.services.UploadFileService;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import io.realm.Realm;

import static com.app.toado.services.UploadFileService.MEDIA_QUEUED;

public class ShowPhotoActivity extends Activity {

    private String imagePath;
    private ImageView ivImage;
    RecyclerView recyclerv;

    private boolean fromEdit = false;
    private String sender;
    private String mykey;
    private String otheruserkey;
    String commentString, comment_type;
    UploadFileService uploadFileService;
    boolean mServiceBound = false;
    final String TAG = "SHOWPHOTOACTIVITY";
    EditText typecomment;
    ArrayList<ShowPhotoModel> stringArrayList;
    Boolean multiple = false;
    RelativeLayout relsingle, relmultiple;
    ShowPhotoAdapter mAdapter;
    private String imagePath1;
    String[] arrstrings;
    TextView tvcomment2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.activity_show_photo);

        Bundle extras = getIntent().getExtras();

        sender = getIntent().getStringExtra("username");
        mykey = getIntent().getStringExtra("mykey");
        otheruserkey = getIntent().getStringExtra("otheruserkey");
        relsingle = (RelativeLayout) findViewById(R.id.relsingle);
        relmultiple = (RelativeLayout) findViewById(R.id.relmultiple);

        stringArrayList = new ArrayList<>();

        tvcomment2 = (TextView) findViewById(R.id.typeComment2);

        if (getIntent().getStringArrayListExtra("pathmultiple") != null) {
            ArrayList<String> arr = getIntent().getStringArrayListExtra("pathmultiple");
            arrstrings = new String[arr.size()];
            for (String u : arr) {
                Log.d(TAG, "string arr " + u);
                ShowPhotoModel sm = new ShowPhotoModel(u);
                stringArrayList.add(sm);
            }
            multiple = true;
        } else if (extras.containsKey("path")) {
            imagePath = extras.getString("path");
            multiple = false;
        } else if (getIntent().getStringExtra("path1") != null) {
            imagePath1 = extras.getString("path1");
            multiple = false;
        }

        Log.d(TAG, imagePath1 + " image paht " + imagePath);
        typecomment = (EditText) findViewById(R.id.typeComment);

        InitControls();
    }

    void InitControls() {
        Log.d(TAG, " multiple boolean " + multiple);
        if (!multiple) {
            relmultiple.setVisibility(View.GONE);
            relsingle.setVisibility(View.VISIBLE);
            ivImage = (ImageView) findViewById(R.id.ivImage);
            try {
                ivImage.setImageURI(Uri.parse(imagePath));
            } catch (Exception e) {
                e.printStackTrace();
                Glide.with(this).load(imagePath1).into(ivImage);
            }
        } else {
            relsingle.setVisibility(View.GONE);
            relmultiple.setVisibility(View.VISIBLE);
            recyclerv = (RecyclerView) findViewById(R.id.recyclermultiple);
            SnapHelper helper = new LinearSnapHelper();
            helper.attachToRecyclerView(recyclerv);
            final LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            recyclerv.setLayoutManager(lm);
            recyclerv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    int currentFirstVisible = lm.findFirstVisibleItemPosition();
                    Log.d(TAG, currentFirstVisible + " on scroll state changed" + newState);
                    if (!tvcomment2.getText().toString().matches(""))
                        arrstrings[currentFirstVisible] = tvcomment2.getText().toString();

                    Log.d(TAG, "arrstring " + arrstrings[currentFirstVisible]);

                    if (arrstrings[currentFirstVisible] != null)
                        tvcomment2.setText(arrstrings[currentFirstVisible]);
                    else
                        tvcomment2.setText("");
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
//                    int currentFirstVisible = lm.findFirstVisibleItemPosition();
//                    Log.d(TAG, "current visible item = "+currentFirstVisible);
                }
            });
            mAdapter = new ShowPhotoAdapter(stringArrayList, ShowPhotoActivity.this);
            recyclerv.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();

        }
    }

    public void sendPic(View view) {
        if (typecomment.getText().toString().matches(""))
            commentString = "";
        else
            commentString = typecomment.getText().toString();

        if (getIntent().getStringArrayListExtra("pathmultiple") != null) {
            Log.d(TAG, "string arr list" + stringArrayList.size());
            for (int i = 0; i < stringArrayList.size(); i++) {
                Log.d(TAG, stringArrayList.get(i) + " vals " + arrstrings[i]);
                uploadFile(arrstrings[i], stringArrayList.get(i).getPath(), "photo");
            }
        } else if (getIntent().getStringExtra("path") != null) {
            Log.d(TAG, "get path imagecomment1 " + imagePath);

            uploadFile(commentString, imagePath, "photo");

        } else if (getIntent().getStringExtra("path1") != null) {
            Log.d(TAG, "get path imagecomment2 " + imagePath1);
            uploadFile(commentString, imagePath1, "photo");
        }
    }

    public void cropImage(View view) {
//        Intent iSelect = new Intent(this, CameraCropActivity.class);
//        iSelect.putExtra("path", imagePath);
//        startActivity(iSelect);g
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceBound) {
            unbindService(muploadserconn);
            mServiceBound = false;
        }
    }


    private ServiceConnection muploadserconn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UploadFileService.MyBinder myBinder = (UploadFileService.MyBinder) service;
            uploadFileService = myBinder.getService();
            mServiceBound = true;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, UploadFileService.class);
        startService(intent);
        if (!mServiceBound) {
            bindService(intent, muploadserconn, Context.BIND_AUTO_CREATE);
        }
    }

    private void uploadFile(String msg, final String filePath, final String type) {
        try {
            Log.d(TAG, mykey + " filepath showphoto activity " + filePath + "   " + otheruserkey);
            if (mServiceBound) {
//                uploadFileService.uploadFile(msg, filePath, type, mykey, otheruserkey, sender, this);

                ChatHelper.queueUpload(msg,filePath,type,mykey,otheruserkey,sender,MEDIA_QUEUED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            finish();
        }
    }

    public void onBack(View view) {
        if (getIntent().getStringExtra("path") != null) {
            Intent intent = new Intent(this, CamActivity.class);
            intent.putExtra("username", sender);
            intent.putExtra("otheruserkey", otheruserkey);
            intent.putExtra("mykey", mykey);
            startActivity(intent);
        }

        finish();
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getStringExtra("path") != null) {
            Intent intent = new Intent(this, CamActivity.class);
            intent.putExtra("username", sender);
            intent.putExtra("otheruserkey", otheruserkey);
            intent.putExtra("mykey", mykey);
            startActivity(intent);
        }

        finish();
    }
}
