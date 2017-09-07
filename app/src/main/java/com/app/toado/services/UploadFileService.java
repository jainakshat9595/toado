package com.app.toado.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.app.toado.R;
import com.app.toado.helper.ChatHelper;
import com.app.toado.helper.GetTimeStamp;
import com.app.toado.helper.MyXMPP2;
import com.app.toado.helper.OpenFile;
import com.app.toado.helper.ThumbnailHelper;
import com.app.toado.model.ChatMessage;
import com.app.toado.model.realm.ChatMessageRealm;
import com.app.toado.model.realm.UploadTable;
import com.app.toado.settings.UserMediaPrefs;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.tape2.ObjectQueue;

import java.io.File;
import java.io.IOException;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;

import static com.app.toado.helper.ToadoConfig.STORAGE_REFERENCE;

public class UploadFileService extends Service {
    UserMediaPrefs umpref;
    private static String LOG_TAG = "UploadFileService";
    private IBinder mBinder = new MyBinder();
    private String TAG = "UPLOADFILESERVICE";
    public static final String MEDIA_STARTING = "upload starting";
    public static final String MEDIA_SUCCESS = "upload success";
    public static final String MEDIA_FAILED = "upload failed";
    public static final String MEDIA_QUEUED = "upload queued";
    public static final String MEDIA_PROGRESSING = "upload progressing";
    public static final String MEDIA_DOWNLOAD_STARTING = "DOWNLOAD STARTING";
    public static final String MEDIA_DOWNLOAD_SUCCESS = "DOWNLOAD success";
    public static final String MEDIA_DOWNLOAD_FAILED = "DOWNLOAD failed";
    public static final String MEDIA_DOWNLOAD_PROGRESS = "DOWNLOAD progressing";
    ObjectQueue<String> queue;
    Boolean bolupload = false;

    public UploadFileService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOG_TAG, "in onCreate");
        umpref = new UserMediaPrefs(this);
        queue = ObjectQueue.createInMemory();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "in onBind");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "in onDestroy");
    }


    public class MyBinder extends Binder {
        public UploadFileService getService() {
            return UploadFileService.this;
        }
    }

    public void uploadFile(String msg, String path, String filetype, final String mykey, final String otheruserkey, final String username, Activity act) {
        Log.d(TAG, path + "uri found upload service" + Uri.fromFile(new File(path)));
        if (Uri.fromFile(new File(path)) != null) {
            try {
                queue.add(path);
                for (String s : queue)
                    Log.d(TAG, queue.size() + "queue " + s);
                queue.remove();
                ;
//                firebasestorageMeth(msg, queue.peek(), filetype, mykey, otheruserkey, username, act);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkMeth() {
        while (queue.size() > 0) {
            try {
                Log.d(TAG, queue.size() + "queue " + queue.peek());
                queue.remove();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void firebasestorageMeth(final Long id, final String timestampdate, final String timestamptime, final String thumbpath, final String msg, final String path, final String filetype, final String mykey, final String otheruserkey, final String username, final Activity act) {
        final StorageReference riversRef = STORAGE_REFERENCE.child(mykey).child("files").child(String.valueOf(GetTimeStamp.Id()));
        Log.d(TAG, "riversref " + riversRef);

/*        final String timestampdate = GetTimeStamp.timeStampDate();
        final String timestamptime = GetTimeStamp.timeStampTime();
        final long id = GetTimeStamp.Id();
        String filename = path.substring(path.lastIndexOf("/") + 1, path.length());
        Log.d(TAG, "file name ul service" + filename);
        final String thumbpath = ThumbnailHelper.createThumbnail(path, act, filename, filetype);


        ChatMessageRealm cmr = new ChatMessageRealm(mykey + otheruserkey, otheruserkey, msg, mykey, timestamptime, timestampdate, filetype, String.valueOf(id), "0", "", path, thumbpath);
        ChatHelper.addChatMesgRealmMedia1(cmr, act, mykey, otheruserkey);
        if (filetype.matches("photo"))
            sendBroadcast(new Intent().putExtra("reloadchatmediastatus", MEDIA_STARTING).putExtra("reloadchatmediaid", String.valueOf(id)).putExtra("reloadchatmedialocalurl", path).setAction("reloadchataction"));
        else
            sendBroadcast(new Intent().putExtra("reloadchatmediastatus", MEDIA_STARTING).putExtra("reloadchatmediaid", String.valueOf(id)).putExtra("reloadchatmedialocalurl", thumbpath).setAction("reloadchataction"));

        Log.d(TAG, " file path extension upload file " + path);

 */
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Log.d(TAG, path + " thumbpath " + thumbpath);
                riversRef.putFile(Uri.fromFile(new File(thumbpath))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnap) {
                        bolupload = false;
                        final Uri thumbu = taskSnap.getDownloadUrl();
                        riversRef.putFile(Uri.fromFile(new File(path)))
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                        Log.d(TAG, "downloadurl video" + downloadUrl.getPath());
                                        ChatMessageRealm cmr = new ChatMessageRealm(mykey + otheruserkey, otheruserkey, msg, mykey, timestamptime, timestampdate, filetype, String.valueOf(id), "1", String.valueOf(downloadUrl), path, taskSnap.getDownloadUrl().toString());
                                        ChatHelper.addChatMesgRealmMedia1(cmr, getApplicationContext(), mykey, otheruserkey);
                                        ChatHelper.queueUpload(msg, path, filetype, mykey, otheruserkey, username, MEDIA_SUCCESS);
                                        if (filetype.matches("photo"))
                                            sendBroadcast(new Intent().putExtra("reloadchatmediastatus", MEDIA_SUCCESS).putExtra("reloadchatmediaid", String.valueOf(id)).putExtra("reloadchatmediaprogresstatus", "100").putExtra("reloadchatmediaurl", String.valueOf(downloadUrl)).putExtra("reloadchatmedialocalurl", path).setAction("reloadchataction"));
                                        else
                                            sendBroadcast(new Intent().putExtra("reloadchatmediastatus", MEDIA_SUCCESS).putExtra("reloadchatmediaid", String.valueOf(id)).putExtra("reloadchatmediaprogresstatus", "100").putExtra("reloadchatmediaurl", String.valueOf(downloadUrl)).putExtra("reloadchatmedialocalurl", thumbpath).setAction("reloadchataction"));
                                        ChatMessageRealm cmn = new ChatMessageRealm(cmr.getChatref(), cmr.getOtherjid(), cmr.getMsgstring(), cmr.getSenderjid(), cmr.getSendertime(), cmr.getSenderdate(), cmr.getMsgtype(), cmr.getMsgid(), cmr.getMsgstatus(), cmr.getMsgweburl(), "", taskSnap.getDownloadUrl().toString());
                                        cmn.setMsglocalurl("");
                                        Log.d(TAG, downloadUrl + "file weburl, file local url set to nil to send to receiver" + cmr.getMsglocalurl());
                                        MyXMPP2.getInstance(act, getString(R.string.server), mykey).sendMessage(cmn);
//                                        Toast.makeText(getApplicationContext(), "File Uploaded , msg sent", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        ChatHelper.queueUpload(msg, path, filetype, mykey, otheruserkey, username, MEDIA_FAILED);
                                        sendBroadcast(new Intent().putExtra("reloadchatmediastatus", MEDIA_FAILED).putExtra("reloadchatmediaid", String.valueOf(id)).putExtra("reloadchatmedialocalurl", path).setAction("reloadchataction"));
                                        exception.printStackTrace();

                                    }
                                })
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                        ChatHelper.queueUpload(msg, path, filetype, mykey, otheruserkey, username, MEDIA_PROGRESSING);

                                        int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                                        sendBroadcast(new Intent().putExtra("reloadchatmediastatus", MEDIA_PROGRESSING).putExtra("reloadchatmediaprogresstatus", progress + " ").putExtra("reloadchatmediaid", String.valueOf(id)).putExtra("reloadchatmedialocalurl", path).setAction("reloadchataction"));
                                    }
                                });

                    }
                });

                return null;
            }
        }.execute();
    }


    public void downloadFile(final String mykey, final String otheruserkey, final ChatMessageRealm chatmsg, StorageReference storageReference, final File file, final Activity act) {
        try {

            sendBroadcast(new Intent().putExtra("reloadchatmediastatus", MEDIA_DOWNLOAD_STARTING).putExtra("reloadchatmediaid", chatmsg.getMsgid()).putExtra("reloadchatmedialocalurl", "nil").setAction("reloadchataction"));

            storageReference.getFile(file).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Long prog = (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()) * 100;
                    Log.d(TAG, prog + "file progress" + taskSnapshot.getBytesTransferred() + "     " + taskSnapshot.getTotalByteCount());
                    sendBroadcast(new Intent().putExtra("reloadchatmediastatus", MEDIA_DOWNLOAD_PROGRESS).putExtra("reloadchatmediaprogresstatus", prog + " ").putExtra("reloadchatmediaid", chatmsg.getMsgid()).putExtra("reloadchatmedialocalurl", "nil").setAction("reloadchataction"));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    sendBroadcast(new Intent().putExtra("reloadchatmediastatus", MEDIA_DOWNLOAD_FAILED).putExtra("reloadchatmediaid", chatmsg.getMsgid()).putExtra("reloadchatmedialocalurl", "nil").setAction("reloadchataction"));
                    exception.printStackTrace();
                }
            }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    ChatMessageRealm cmn = new ChatMessageRealm(chatmsg.getChatref(), chatmsg.getOtherjid(), chatmsg.getMsgstring(), chatmsg.getSenderjid(), chatmsg.getSendertime(), chatmsg.getSenderdate(), chatmsg.getMsgtype(), chatmsg.getMsgid(), chatmsg.getMsgstatus(), chatmsg.getMsgweburl(), file.getAbsolutePath(), "");
                    ChatHelper.addChatMesgRealmMedia1(cmn, act, mykey, otheruserkey);
                    sendBroadcast(new Intent().putExtra("reloadchatmediastatus", MEDIA_DOWNLOAD_SUCCESS).putExtra("reloadchatmediaprogresstatus", "100").putExtra("reloadchatmediaid", chatmsg.getMsgid()).putExtra("reloadchatmedialocalurl", cmn.getMsglocalurl()).setAction("reloadchataction"));
                    Log.d(TAG, chatmsg.getMsglocalurl() + " file success download " + cmn.getMsglocalurl());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
