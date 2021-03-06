package com.app.toado.activity.chat;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.toado.R;
import com.app.toado.activity.ToadoAppCompatActivity;
import com.app.toado.activity.ToadoBaseActivity;
import com.app.toado.activity.main.MainAct;
import com.app.toado.activity.settings.SettingsActivity;
import com.app.toado.activity.userdetail.UserDetail;
import com.app.toado.activity.userprofile.UserProfileAct;
import com.app.toado.adapter.ChatAdapter1;
import com.app.toado.helper.CallHelper;
import com.app.toado.helper.ChatHelper;
import com.app.toado.helper.CircleTransform;
import com.app.toado.helper.EncryptUtils;
import com.app.toado.helper.GetTimeStamp;
import com.app.toado.helper.ImageComment;
import com.app.toado.helper.MarshmallowPermissions;
import com.app.toado.helper.MyXMPP2;
import com.app.toado.helper.OpenFile;
import com.app.toado.helper.ThumbnailHelper;
import com.app.toado.helper.ToadoAlerts;
import com.app.toado.helper.UriHelper;
import com.app.toado.model.User;
import com.app.toado.model.realm.ActiveChatsRealm;
import com.app.toado.model.realm.ActiveChatsWallpaperRealm;
import com.app.toado.model.realm.UploadTable;
import com.app.toado.services.SinchCallService;
import com.app.toado.settings.UserMediaPrefs;
import com.app.toado.settings.UserSession;
import com.app.toado.model.realm.ChatMessageRealm;
import com.app.toado.services.UploadFileService;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.necistudio.libarary.FilePickerActivity;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderDecoration;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.app.toado.helper.ToadoConfig.DBREF_USER_PROFILES;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_FAILED;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_PROGRESS;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_STARTING;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_SUCCESS;
import static com.app.toado.services.UploadFileService.MEDIA_PROGRESSING;
import static com.app.toado.services.UploadFileService.MEDIA_STARTING;
import static com.app.toado.services.UploadFileService.MEDIA_SUCCESS;

public class ChatActivity extends ToadoAppCompatActivity {
    private EditText typeComment;
    private ImageButton attachment, takephoto, imgdocs1, imgdocs2;
    ImageView imgback;
    FloatingActionButton sendButton;
    Intent intent;
    private RecyclerView recyclerView;
    private String otheruserkey;
    LinearLayoutManager linearLayoutManager;
    private MarshmallowPermissions marshmallowPermissions;
    private ActionMode actionMode;
    UploadFileService uploadFileService;
    boolean mServiceBound = false;
    private ChatAdapter1 mAdapter;
    private ArrayList<ChatMessageRealm> chatList = new ArrayList<>();
    private ArrayList<String> chatListIds = new ArrayList<>();
    String username, mykey;
    private UserSession session;
    String receiverToken = "nil";
    boolean clicked;
    LinearLayout layoutToAdd, layoutToAdd2;
    LinearLayout commentView;
    ImageButton galleryattach, galleryattach2;
    private LinearLayout spamView;
    TextView tvTitle;
    ImageView imgprof;
    private ProgressBar progressBar;
    UserMediaPrefs umprefs;
    private String TAG = "ChatActivity";
    Realm mRealm;
    Boolean chatexists;
    private String otherusername;
    private String profpic;
    private MyXMPP2 myxinstance = null;
    String imageEncoded;
    List<String> imagesEncodedList;
    ArrayList<String> multipleImagesPathList;
    private final int PICK_IMAGE_MULTIPLE = 199;
    private int MULTIPLE_IMAGE_SELECT = 111;
    private static final int VIDEO_ATTACH = 22;
    private int PICK_LOC = 33;
    private int PICK_DOCS = 44;
    private int PICK_WALLPAPER = 152;
    LinearLayout editcontainer;
    RelativeLayout chatlay;
    Boolean bolkeypad = false;
    private ImageButton takephoto2;
    Runnable runn;
    Handler han;
    StickyHeaderDecoration stickydecor;
    String[] mimetypes = {"application/pdf", "application/docx", "application/xlsx", "application/pptx", "application/pptx", "application/txt"};
    private RealmResults<UploadTable> result;

    private ImageButton mVideoCallButton, mCallButton, mEmailButton;
    private String imgurl;
    private SinchCallService callserv;

    private LinearLayout mUserTitleLay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat1);
        session = new UserSession(this);
        mykey = session.getUserKey();

        mRealm = Realm.getDefaultInstance();
        result = mRealm.where(UploadTable.class).findAllAsync();
        result.addChangeListener(callback);

//      connection = MyXMPP2.getInstance(this,).getConn();

        mRealm = Realm.getDefaultInstance();
        checkChatRef(otheruserkey);

        clicked = false;
        layoutToAdd = (LinearLayout) findViewById(R.id.attachmentpopup);
        layoutToAdd2 = (LinearLayout) findViewById(R.id.attachmentpopup2);
        marshmallowPermissions = new MarshmallowPermissions(this);

        chatlay = (RelativeLayout) findViewById(R.id.chatlay);
        spamView = (LinearLayout) findViewById(R.id.spamView);

        umprefs = new UserMediaPrefs(this);

        //get these 2 things from notifications also
        intent = getIntent();

        otheruserkey = intent.getStringExtra("otheruserkey");
        otherusername = intent.getStringExtra("otherusername");
        profpic = intent.getStringExtra("profpic");

        applyWallpaper();

        Log.d(TAG, profpic + "recevier token chat act oncreate" + otheruserkey);

        imgprof = (ImageView) findViewById(R.id.icon_profile);
        Glide.with(this).load(profpic).transform(new CircleTransform(this)).into(imgprof);

        editcontainer = (LinearLayout) findViewById(R.id.editcontainer);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(otherusername);
        commentView = (LinearLayout) findViewById(R.id.commentView);

        progressBar = (ProgressBar) findViewById(R.id.progress);

        typeComment = (EditText) findViewById(R.id.typeComment);
        sendButton = (FloatingActionButton) findViewById(R.id.sendButton);
        attachment = (ImageButton) findViewById(R.id.attachment);

        imgback = (ImageView) findViewById(R.id.imgback);
        takephoto = (ImageButton) findViewById(R.id.takephoto);
        takephoto2 = (ImageButton) findViewById(R.id.takephoto2);
        galleryattach = (ImageButton) findViewById(R.id.galleryattach);
        galleryattach2 = (ImageButton) findViewById(R.id.galleryattach2);
        imgdocs1 = (ImageButton) findViewById(R.id.buttondocs);
        imgdocs2 = (ImageButton) findViewById(R.id.buttondocs2);

        imgdocs1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDocs();
            }
        });
        imgdocs2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDocs();
            }
        });

        takephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        takephoto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        galleryattach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Multiple images called " + MULTIPLE_IMAGE_SELECT);
                Intent intent = new Intent();
                intent.setType("image/* video/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Images"), MULTIPLE_IMAGE_SELECT);
                layoutToAdd.setVisibility(View.GONE);
                layoutToAdd2.setVisibility(View.GONE);
            }
        });

        galleryattach2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Multiple images called 2 " + MULTIPLE_IMAGE_SELECT);
                Intent intent = new Intent();
                intent.setType("image/* video/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Images"), MULTIPLE_IMAGE_SELECT);
                layoutToAdd.setVisibility(View.GONE);
                layoutToAdd2.setVisibility(View.GONE);
            }
        });

        mAdapter = new ChatAdapter1(chatList, this, otheruserkey, GetTimeStamp.timeStampDate(), otherusername);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        stickydecor = new StickyHeaderDecoration(mAdapter);
        recyclerView.addItemDecoration(stickydecor);
        recyclerView.setAdapter(mAdapter);

        final View parent = (View) imgback.getParent();  // imgback: the view you want to enlarge hit area
        parent.post(new Runnable() {
            public void run() {
                final Rect rect = new Rect();
                imgback.getHitRect(rect);
                rect.top -= 100;    // increase top hit area
                rect.left -= 100;   // increase left hit area
                rect.bottom += 100; // increase bottom hit area
                rect.right += 100;  // increase right hit area
                parent.setTouchDelegate(new TouchDelegate(rect, imgback));
            }
        });

        chatlay.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                chatlay.getWindowVisibleDisplayFrame(r);
                int screenHeight = chatlay.getRootView().getHeight();

                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    bolkeypad = true;
                } else {
                    bolkeypad = false;
                }
            }
        });


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(mykey + " chat created " + otheruserkey);
                ChatMessageRealm cm = null;

                if (!typeComment.getText().toString().matches("")) {
                    cm = new ChatMessageRealm(mykey + otheruserkey, otheruserkey, typeComment.getText().toString().trim(), mykey, GetTimeStamp.timeStampTime(), GetTimeStamp.timeStampDate(), "text", String.valueOf(GetTimeStamp.Id()), "1");
                }
                if (cm != null)
                    myxinstance.sendMessage(cm);

                loadData(true);
                typeComment.setText("");

            }
        });

        attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutToAdd.getVisibility() == View.VISIBLE || layoutToAdd2.getVisibility() == View.VISIBLE) {
                    layoutToAdd.setVisibility(View.GONE);
                    layoutToAdd2.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "bol keypad" + bolkeypad);

                    if (bolkeypad) {
                        layoutToAdd.setVisibility(View.GONE);
                        layoutToAdd2.setVisibility(View.VISIBLE);
                    } else {
                        layoutToAdd.setVisibility(View.VISIBLE);
                        layoutToAdd2.setVisibility(View.GONE);
                    }
                }
            }
        });

/*
        videoattach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutToAdd.setVisibility(View.GONE);
                clicked = false;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("video*/
/*");
                startActivityForResult(intent, VIDEO_ATTACH);
            }
        });
*/

/*
        locattach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutToAdd.setVisibility(View.GONE);
                Log.d(TAG, " send location called ");
                Intent inten = new Intent(ChatActivity.this, MapsActivity.class);
                inten.putExtra("comment_type", "loc");
                inten.putExtra("otheruserkey", otheruserkey);
                inten.putExtra("mykey", mykey);
                inten.putExtra("username", username);
                startActivityForResult(inten, PICK_LOC);
            }
        });
*/

        mVideoCallButton = (ImageButton) findViewById(R.id.imgvideo);
        mCallButton = (ImageButton) findViewById(R.id.imgcall);

        mVideoCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallHelper.vidcallbtnClicked(getSinchServiceInterface(), marshmallowPermissions, mykey, mykey, otherusername, imgurl, ChatActivity.this);
            }
        });

        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallHelper.callbtnClicked(getSinchServiceInterface(), marshmallowPermissions, mykey, mykey, otherusername, imgurl, ChatActivity.this);
            }
        });

        mUserTitleLay = (LinearLayout) findViewById(R.id.user_title_lay);
        mUserTitleLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), UserDetail.class);
                intent.putExtra("UserName", otherusername);
                intent.putExtra("UserKey", otheruserkey);
                intent.putExtra("UserPicture", profpic);
                startActivity(intent);
            }
        });

        mEmailButton = (ImageButton) findViewById(R.id.imgsettings);
        mEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu popup = new PopupMenu(getApplicationContext(), findViewById(R.id.imgsettings));
                popup.getMenuInflater()
                        .inflate(R.menu.menu_chat, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case (R.id.menu_contact):
                                Intent intent = new Intent(getApplicationContext(), UserDetail.class);
                                intent.putExtra("UserName", otherusername);
                                intent.putExtra("UserKey", otheruserkey);
                                intent.putExtra("UserPicture", profpic);
                                startActivity(intent);
                                break;
                            case (R.id.menu_media):
                                break;
                            case (R.id.menu_email):
                                selectAll();
                                break;
                            case (R.id.menu_wallpaper):
                                changeWallpaper();
                                break;
                        }
                        return true;
                    }
                });
                popup.show(); //showing popup menu
            }
        });

    }

    private void pickDocs() {

        if (marshmallowPermissions.checkPermissionForReadStorage()) {
            Intent intent = new Intent(getApplicationContext(), FilePickerActivity.class);
            startActivityForResult(intent, PICK_DOCS);
            try {

            } catch (android.content.ActivityNotFoundException ex) {
                // Potentially direct the user to the Market with a Dialog

            }
        } else {
            marshmallowPermissions.requestPermissionForReadExternalStorage();
        }
        layoutToAdd.setVisibility(View.GONE);
        clicked = false;

    }

    @Override
    protected void onResume() {
        super.onResume();
        mykey = session.getUserKey();
        han = new Handler();
        if (myxinstance == null)
            myxinstance = MyXMPP2.getInstance(ChatActivity.this, getString(R.string.server), mykey);

        han.postDelayed(new Runnable() {
            @Override
            public void run() {
                runn = this;
//                Log.d(TAG,"handler chatact onresume"+myxinstance.isConnected());

                if (!myxinstance.isConnected()) {
                    myxinstance.init();
                    Log.d(TAG, "handler chatact onresume2 " + myxinstance.isConnected());
                }
                han.postDelayed(runn, 5000);
            }
        }, 5000);

        username = session.getUsername();
        loadData(true);
        Intent intent = new Intent(this, UploadFileService.class);
        startService(intent);
        if (!mServiceBound) {
            bindService(intent, muploadserconn, Context.BIND_AUTO_CREATE);
        }

        System.out.println("user key from userprof act" + mykey);
        if (!mykey.matches("nil"))
            getOtherUserFirebaseData(mykey);
        else {
            System.out.println("no key matches error userprofile act");
        }

    }

    private void getOtherUserFirebaseData(final String k) {
        DBREF_USER_PROFILES.child(k).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    System.out.println("user profiles datasnapshot" + dataSnapshot.toString());
                    User u = User.parse(dataSnapshot);
                    imgurl = u.getProfpicurl();
                } else
                    System.out.println("no snapshot exists userprof act");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadData(Boolean t) {
        Log.d(TAG, "load data called");
        Sort sort[] = {Sort.ASCENDING};
        String[] fieldNames = {"msgid"};

        RealmResults<ChatMessageRealm> shows = mRealm.where(ChatMessageRealm.class).equalTo("chatref", mykey + otheruserkey).findAllSorted(fieldNames, sort);
        Log.d(TAG, "mykey otheruserkey" + mykey + otheruserkey);

        if (shows.size() > 0) {
            Log.d(TAG, shows.size() + "LOAD DATA CALLED chatactivity " + shows.get(shows.size() - 1).getMsgstring());
            recyclerView.setVisibility(View.VISIBLE);
            for (ChatMessageRealm cm : shows) {
                if (!chatList.contains(cm)) {
                    chatList.add(cm);
                    if (!cm.getMsgstatus().matches("3") && !cm.getSenderjid().matches(mykey)) {
                        ChatMessageRealm cmn = new ChatMessageRealm(cm.getChatref(), otheruserkey, cm.getMsgstring(), cm.getSenderjid(), cm.getSendertime(), cm.getSenderdate(), "status", cm.getMsgid(), "3");
                        myxinstance.sendMessage(cmn);
                    }
                }
                if (!chatListIds.contains(cm.getMsgid())) {
                    chatListIds.add(cm.getMsgid());
                }
                mAdapter.notifyDataSetChanged();
            }
            mAdapter.notifyDataSetChanged();

            if (t)
                scrollRV();

        } else {
            Log.d(TAG, "load data called else");
        }
    }

    private void scrollRV() {
        recyclerView.scrollToPosition(chatList.size() - 1);
    }

    private void checkChatRef(String otheruserkey) {
        RealmQuery<ActiveChatsRealm> query = mRealm.where(ActiveChatsRealm.class);
        query.equalTo("otherkey", otheruserkey);
        RealmResults<ActiveChatsRealm> result1 = query.findAll();
        if (result1.size() == 0) {
            chatexists = false;
        } else {
            chatexists = true;
        }
        System.out.println(result1.size() + "chat exists chatactivity" + chatexists);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(this.reloadData, new IntentFilter("reloadchataction"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (reloadData != null)
            unregisterReceiver(reloadData);
    }

    @Override
    protected void onPause() {
        super.onPause();
        han.removeCallbacks(runn);
        if (mServiceBound) {
            unbindService(muploadserconn);
            mServiceBound = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, data + " request code chatactivity " + requestCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null) {
                if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    Log.d(TAG, "crop activity");
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {
                        final String imguri = result.getUri().toString();
                        try {
                            final File file = createImageFile();
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    final int chunkSize = 1024;  // We'll read in one kB at a time
                                    byte[] imageData = new byte[chunkSize];

                                    InputStream in = null;
                                    OutputStream out = null;
                                    try {
                                        in = getContentResolver().openInputStream(Uri.parse(imguri));
                                        out = new FileOutputStream(file);
                                        int bytesRead;
                                        while ((bytesRead = in.read(imageData)) > 0) {
                                            out.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)));
                                        }
                                        String s = file.getAbsolutePath();
                                        Log.d(TAG, "image cropped uri chatact22" + file.getAbsolutePath());
                                        Intent intent = new Intent(ChatActivity.this, ImageComment.class);
                                        intent.putExtra("URI", s);
                                        intent.putExtra("comment_type", "photo");
                                        startImageComment(intent);

                                    } catch (Exception ex) {
                                        Log.e("Something went wrong.", ex.toString());
                                    } finally {
                                        try {
                                            in.close();
                                            out.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    return null;
                                }
                            }.execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (requestCode == MULTIPLE_IMAGE_SELECT) {
                    Log.d(TAG, "multiple image select data" + data.getData());

                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    imagesEncodedList = new ArrayList<>();
                    multipleImagesPathList = new ArrayList<>();
                    if (data.getClipData() == null) {

                        Uri mImageUri = data.getData();
                        Log.d(TAG, "mimageuri " + mImageUri);
                        // Get the cursor
                        Intent intent = new Intent(this, ShowPhotoActivity.class);
                        intent.putExtra("path1", UriHelper.getPath(ChatActivity.this, mImageUri));
                        intent.putExtra("username", otherusername);
                        intent.putExtra("otheruserkey", otheruserkey);
                        intent.putExtra("mykey", mykey);
                        startActivity(intent);
                        Log.d(TAG, imageEncoded + " imageencoded");
                    } else {
                        if (data.getClipData() != null) {
                            ClipData mClipData = data.getClipData();
                            ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                            for (int i = 0; i < mClipData.getItemCount(); i++) {
                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                multipleImagesPathList.add(UriHelper.getPath(ChatActivity.this, uri));
                            }
                            Intent intent = new Intent(this, ShowPhotoActivity.class);
                            intent.putStringArrayListExtra("pathmultiple", multipleImagesPathList);
                            intent.putExtra("username", otherusername);
                            intent.putExtra("otheruserkey", otheruserkey);
                            intent.putExtra("mykey", mykey);
                            startActivity(intent);
                        }
                    }
                } else if (requestCode == VIDEO_ATTACH) {
                    Uri videoUri = data.getData();
                    Log.d(TAG, "video attach on act result" + videoUri);
                    Intent videointent = new Intent(this, ImageComment.class);
                    videointent.putExtra("URI", videoUri.toString());
                    videointent.putExtra("otheruserkey", otheruserkey);
                    videointent.putExtra("mykey", mykey);
                    videointent.putExtra("comment_type", "video");
                    startImageComment(videointent);
                } else if (requestCode == PICK_LOC) {
                    String filepath = data.getStringExtra("locresult");
                    Intent intent = new Intent(ChatActivity.this, ImageComment.class);
                    intent.putExtra("otheruserkey", otheruserkey);
                    intent.putExtra("mykey", mykey);
                    intent.putExtra("URI", filepath);
                    intent.putExtra("comment_type", "location");
                    startImageComment(intent);
                } else if (requestCode == PICK_DOCS) {
                    String filePath = data.getStringExtra("path");
                    Log.d(TAG, "file path of picked docs" + filePath);
                    Intent intent = new Intent(ChatActivity.this, ImageComment.class);
                    intent.putExtra("otheruserkey", otheruserkey);
                    intent.putExtra("mykey", mykey);
                    intent.putExtra("URI", filePath);
                    intent.putExtra("comment_type", "doc");
//                    startImageComment(intent);
                } else if (requestCode == PICK_WALLPAPER) {
                    updateWallpaper(data);
                }
            }

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = GetTimeStamp.timeStamp() + ".jpeg";
        String filesent = "sent";
        File image = OpenFile.createFile(this, imageFileName, filesent);
        // Save a file: path for use with ACTION_VIEW intents
        Log.d(TAG, "file createimagefile: " + image.getAbsolutePath());
        return image;
    }

    private void startImageComment(Intent intent) {
        Log.d(TAG, "image comment sending");
        intent.putExtra("username", username);
        intent.putExtra("otheruserkey", otheruserkey);
        intent.putExtra("mykey", mykey);
        startActivity(intent);
    }

    BroadcastReceiver reloadData = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("reloadchat") != null) {
                Log.d(TAG, " reloading data broadcast receiver" + intent.getStringExtra("reloadchat"));
                loadData(false);
                mAdapter.notifyDataSetChanged();
                scrollRV();
            } else if (intent.getStringExtra("reloadchatmediastatus") != null) {
                String stringext = intent.getStringExtra("reloadchatmediastatus");
                Log.d(TAG, " reloading data status " + intent.getStringExtra("reloadchatmediastatus"));
                Log.d(TAG, " reloading data media id " + intent.getStringExtra("reloadchatmediaid"));
                if (stringext.matches(MEDIA_STARTING)) {
                    loadData(false);
                } else if (stringext.matches(MEDIA_PROGRESSING) || stringext.matches(MEDIA_SUCCESS)) {
                    final String msgid = intent.getStringExtra("reloadchatmediaid");
                    String fileprogress = intent.getStringExtra("reloadchatmediaprogresstatus");
                    final int ind1 = chatListIds.indexOf(msgid);
                    Log.d(TAG, ind1 + "chat list broadcast progress " + fileprogress);
                    try {
                        View ve = linearLayoutManager.findViewByPosition(ind1);
                        ChatAdapter1.MyViewHolder holder = (ChatAdapter1.MyViewHolder) recyclerView.getChildViewHolder(ve);
                        mAdapter.setUploadProgress(holder, fileprogress, stringext);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (stringext.matches(MEDIA_DOWNLOAD_STARTING)) {
                    final String msgid = intent.getStringExtra("reloadchatmediaid");
                    final int ind1 = chatListIds.indexOf(msgid);
                    Log.d(TAG, ind1 + "chat list broadcast  download starting");
                    try {
                        View ve = linearLayoutManager.findViewByPosition(ind1);
                        ChatAdapter1.MyViewHolder holder = (ChatAdapter1.MyViewHolder) recyclerView.getChildViewHolder(ve);
                        mAdapter.setDownloadProgress(holder, stringext, "0", "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (stringext.matches(MEDIA_DOWNLOAD_PROGRESS)) {
                    final String msgid = intent.getStringExtra("reloadchatmediaid");
                    final int ind1 = chatListIds.indexOf(msgid);
                    String fileprogress = intent.getStringExtra("reloadchatmediaprogresstatus");
                    Log.d(TAG, ind1 + "chat list broadcast  download progressing" + fileprogress);
                    try {
                        View ve = linearLayoutManager.findViewByPosition(ind1);
                        ChatAdapter1.MyViewHolder holder = (ChatAdapter1.MyViewHolder) recyclerView.getChildViewHolder(ve);
                        mAdapter.setDownloadProgress(holder, stringext, fileprogress, "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (stringext.matches(MEDIA_DOWNLOAD_FAILED)) {
                    final String msgid = intent.getStringExtra("reloadchatmediaid");
                    final int ind1 = chatListIds.indexOf(msgid);
                    String fileprogress = intent.getStringExtra("reloadchatmediaprogresstatus");
                    Log.d(TAG, ind1 + "chat list broadcast  download failed");
                    try {
                        View ve = linearLayoutManager.findViewByPosition(ind1);
                        ChatAdapter1.MyViewHolder holder = (ChatAdapter1.MyViewHolder) recyclerView.getChildViewHolder(ve);
                        mAdapter.setDownloadProgress(holder, stringext, fileprogress, "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (stringext.matches(MEDIA_DOWNLOAD_SUCCESS)) {
                    final String msgid = intent.getStringExtra("reloadchatmediaid");
                    final String localurl = intent.getStringExtra("reloadchatmedialocalurl");
                    final int ind1 = chatListIds.indexOf(msgid);

                    String fileprogress = intent.getStringExtra("reloadchatmediaprogresstatus");
                    Log.d(TAG, ind1 + "chat list broadcast  download success" + localurl);
                    try {
                        View ve = linearLayoutManager.findViewByPosition(ind1);
                        ChatAdapter1.MyViewHolder holder = (ChatAdapter1.MyViewHolder) recyclerView.getChildViewHolder(ve);
                        mAdapter.setDownloadProgress(holder, stringext, fileprogress, localurl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (intent.getStringExtra("messagedeliverystatus") != null) {
                try {
                    final String a = intent.getStringExtra("messagedeliverystatus");
                    final int ind1 = chatListIds.indexOf(a);
                    final ChatMessageRealm cm = mRealm.copyFromRealm(chatList.get(ind1));
                    Log.d(TAG, a + " delivery status chat activity " + cm.getMsgstatus());
                    final ChatMessageRealm cmn = new ChatMessageRealm(cm.getChatref(), cm.getOtherjid(), cm.getMsgstring(), cm.getSenderjid(), cm.getSendertime(), cm.getSenderdate(), cm.getMsgtype(), cm.getMsgid(), "2", cm.getMsgweburl(), cm.getMsglocalurl(), cm.getMediathumbnail());
                    mRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm bgRealm) {
                            try {
                                bgRealm.copyToRealmOrUpdate(cmn);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "new status stored realm success chatact");
                            Log.d(TAG, a + "delivery status chat activity" + cm.getMsgid() + "  " + cm.getMsgstatus());
                            try {
                                if (linearLayoutManager.findViewByPosition(ind1) != null) {
                                    View ve = linearLayoutManager.findViewByPosition(ind1);
                                    if (recyclerView.getChildViewHolder(ve) != null) {
                                        ChatAdapter1.MyViewHolder holder = (ChatAdapter1.MyViewHolder) recyclerView.getChildViewHolder(ve);
                                        mAdapter.setDeliveryStatus(holder, "2");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Realm.Transaction.OnError() {
                        @Override
                        public void onError(Throwable error) {
                            // Transaction failed and was automatically canceled.
                            error.printStackTrace();
                            Log.d(TAG, "new status stored realm failed chatact");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (intent.getStringExtra("readstatus") != null) {
                try {
                    final String a = intent.getStringExtra("readstatus");
                    final int ind1 = chatListIds.indexOf(a);
                    final ChatMessageRealm cm = mRealm.copyFromRealm(chatList.get(ind1));
                    Log.d(TAG, a + " read status chat activity " + cm.getMsgstring());
                    View ve = linearLayoutManager.findViewByPosition(ind1);
                    ChatAdapter1.MyViewHolder holder = (ChatAdapter1.MyViewHolder) recyclerView.getChildViewHolder(ve);
                    mAdapter.setDeliveryStatus(holder, "3");
                } catch (Exception e) {
                    e.printStackTrace();
                    loadData(false);
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (layoutToAdd.getVisibility() == View.VISIBLE || layoutToAdd2.getVisibility() == View.VISIBLE) {
            layoutToAdd.setVisibility(View.GONE);
            layoutToAdd2.setVisibility(View.GONE);
        } else if (mAdapter.isActionEnabled()) {
            mAdapter.removeItems();
        } else
            finish();
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
            mAdapter.setUploadServiceRef(uploadFileService);
        }
    };

    public void takePhoto() {
        Log.d(TAG, "take photo case2");
        layoutToAdd.setVisibility(View.GONE);
        layoutToAdd2.setVisibility(View.GONE);
        Intent in = new Intent(this, CamActivity.class);
        startImageComment(in);
        layoutToAdd.setVisibility(View.GONE);
        layoutToAdd2.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        result.removeChangeListener(callback);
        mRealm.close();
    }

    public void onBack(View view) {
        finish();
    }

    private OrderedRealmCollectionChangeListener<RealmResults<UploadTable>> callback = new OrderedRealmCollectionChangeListener<RealmResults<UploadTable>>() {
        @Override
        public void onChange(RealmResults<UploadTable> chatMessageRealms, OrderedCollectionChangeSet changeSet) {
            if (changeSet == null) {

                // The first time async returns with an null changeSet.
            } else {
                // Called on every update.

                for (UploadTable cmr : chatMessageRealms) {
                    RealmResults<UploadTable> result2 = mRealm.where(UploadTable.class)
                            .equalTo("uploadstatus", MEDIA_PROGRESSING)
                            .findAll();
                    Log.d(TAG,"result 2 "+result2.size());
                    if(result2.size()<=0){
                        final String timestampdate = GetTimeStamp.timeStampDate();
                        final String timestamptime = GetTimeStamp.timeStampTime();
                        final long id = GetTimeStamp.Id();
                        String filename = cmr.getFilepath().substring(cmr.getFilepath().lastIndexOf("/") + 1, cmr.getFilepath().length());
                        Log.d(TAG, "file name ul service" + filename);
                        final String thumbpath = ThumbnailHelper.createThumbnail(cmr.getFilepath(), ChatActivity.this, filename, cmr.getFiletype());


                        ChatMessageRealm chatm = new ChatMessageRealm(mykey + otheruserkey, otheruserkey, cmr.getMsg(), mykey, timestamptime, timestampdate, cmr.getFiletype(), String.valueOf(id), "0", "", cmr.getFilepath(), thumbpath);
                        ChatHelper.addChatMesgRealmMedia1(chatm, ChatActivity.this, mykey, otheruserkey);
                        if (cmr.getFiletype().matches("photo"))
                            sendBroadcast(new Intent().putExtra("reloadchatmediastatus", MEDIA_STARTING).putExtra("reloadchatmediaid", String.valueOf(id)).putExtra("reloadchatmedialocalurl", cmr.getFilepath()).setAction("reloadchataction"));
                        else
                            sendBroadcast(new Intent().putExtra("reloadchatmediastatus", MEDIA_STARTING).putExtra("reloadchatmediaid", String.valueOf(id)).putExtra("reloadchatmedialocalurl", thumbpath).setAction("reloadchataction"));

                        Log.d(TAG, " file path extension upload file " + cmr.getFilepath());


                        uploadFileService.firebasestorageMeth(id,timestampdate,timestamptime,thumbpath,cmr.getMsg(),cmr.getFilepath(),cmr.getFiletype(),cmr.getMykey(),cmr.getOtheruserkey(),cmr.getOtherusername(),ChatActivity.this);
                    }
                }



            }
        }
    };

    @Override
    public void onServiceConnected() {

        try {
            callserv = getSinchServiceInterface().getService();
            getSinchServiceInterface().startClient(mykey);
            mServiceBound = true;
        } catch (NullPointerException e) {
            //getSinchServiceInterface() in doStuff below throw null pointer error.
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceBound = false;
    }

    private void selectAll() {

        String stringcopy = null;

        for (ChatMessageRealm cm : chatList) {
            String msgtime = cm.getSenderdate().replace("-2017", "").replace("-", "/") + ", " + cm.getSendertime().replace(" ", "");
            stringcopy = stringcopy + "\n[" + msgtime + "] " + otherusername + ": " + cm.getMsgstring();
            Log.d(TAG, "string copy" + stringcopy);
        }

        Log.d(TAG, "string copyfinal" + stringcopy);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Whatsapp Chat");
        emailIntent.putExtra(Intent.EXTRA_TEXT, stringcopy);
        startActivity(Intent.createChooser(emailIntent, "Send email..."));

    }

    private void applyWallpaper() {
        RealmResults<ActiveChatsWallpaperRealm> shows = mRealm.where(ActiveChatsWallpaperRealm.class).equalTo("chatref", mykey + otheruserkey).findAll();

        if (shows.size() > 0) {
            Log.d(TAG, shows.size() + "LOAD DATA CALLED chatactivity " + shows.get(shows.size() - 1).getMsgstring());
            recyclerView.setVisibility(View.VISIBLE);
            for (ChatMessageRealm cm : shows) {
                if (!chatList.contains(cm)) {
                    chatList.add(cm);
                    if (!cm.getMsgstatus().matches("3") && !cm.getSenderjid().matches(mykey)) {
                        ChatMessageRealm cmn = new ChatMessageRealm(cm.getChatref(), otheruserkey, cm.getMsgstring(), cm.getSenderjid(), cm.getSendertime(), cm.getSenderdate(), "status", cm.getMsgid(), "3");
                        myxinstance.sendMessage(cmn);
                    }
                }
                if (!chatListIds.contains(cm.getMsgid())) {
                    chatListIds.add(cm.getMsgid());
                }
                mAdapter.notifyDataSetChanged();
            }
            mAdapter.notifyDataSetChanged();

            if (t)
                scrollRV();

        } else {
            Log.d(TAG, "load data called else");
        }
    }

    private void changeWallpaper() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_WALLPAPER);
    }

    private void updateWallpaper(Intent data) {
        Uri mImageUri = data.getData();
        System.out.println("mImageUri: "+mImageUri);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference wallpaperRef = storageRef.child(mykey).child("wallpaper/"+mImageUri.getLastPathSegment());

        UploadTask uploadTask = wallpaperRef.putFile(mImageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                System.out.println("Image upload task failed");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

                final ActiveChatsWallpaperRealm activeChatsWallpaperRealm = new ActiveChatsWallpaperRealm(downloadUrl.toString(), mykey+otheruserkey);
                Realm realm1 = Realm.getDefaultInstance();

                realm1.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {
                            realm.copyToRealmOrUpdate(activeChatsWallpaperRealm);
                        } catch (Exception e) {
                            ActiveChatsWallpaperRealm acr = realm.createObject(ActiveChatsWallpaperRealm.class, activeChatsWallpaperRealm.getChatref());
                            realm.copyToRealmOrUpdate(acr);
                            e.printStackTrace();
                        } finally {
                            realm.close();
                        }
                    }
                });
            }
        });
    }


}