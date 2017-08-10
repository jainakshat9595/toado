package com.app.toado.services;


import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.app.toado.helper.OpenFile;
import com.app.toado.settings.UserMediaPrefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static com.app.toado.helper.ToadoConfig.DBREF;
import static com.app.toado.helper.ToadoConfig.DBREF_CHATS;

public class DownloadMangerService extends Service {
    Context context;
    private long enqueue;
    LongOperation lo;
    private DownloadManager downloadManager;
    UserMediaPrefs umprefs;
    String dbtablekey;
    OpenFile openF;

    public DownloadMangerService() {
    }

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Random number generator
    private final Random mGenerator = new Random();
    //    private SharedPreferences prefs;

    public class LocalBinder extends Binder {
        public DownloadMangerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return DownloadMangerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void onCreate() {

        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        System.out.println("inside on create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("inside on start command");
        openF = new OpenFile();
        context = getApplicationContext();
        umprefs = new UserMediaPrefs(context);
        return super.onStartCommand(intent, flags, startId);
    }

    public void start_download(String type, String url, String id, String dbTablekey1) {
        dbtablekey = dbTablekey1;
        System.out.println("starting download for id" + id);
        lo = new LongOperation();
        String[] ar = {url, id, type};
        try {
            String a = lo.execute(ar).get();
            System.out.println(" a from downloadmanagerserv " + a);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }


    public void cancel_download(String id) {
//        downloadManager.
        System.out.println("stop called for id:" + id);
//        Long stopid ;
//        downloadManager.remove(stopid);
//        System.out.println("stopped dL for " + stopid);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("on destroy called");

    }

    public int getRandomNumber() {
        return mGenerator.nextInt(100);
    }

    private class LongOperation extends AsyncTask<String, Integer, String> {
        String id1 = "";

        @Override
        protected String doInBackground(String... params) {
            if (context != null) {
                System.out.println("from async task downloadmanagerservice");
                final String url = params[0];
                final String id = params[1];
                id1 = id;
                final String type = params[2];

                DBREF_CHATS.child(dbtablekey).child("ChatMessages").child(id1).child("filext").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                        String ex = dataSnapshot.getValue().toString();
                        System.out.println("file extension downloadservice");
                        String name = type + "-" + id + "." + ex;

                        System.out.println(ex + "from async task downloadmanagerservice" + url + " and type " + type);

                        //Save file to destination folder
//                        request.setDestinationInExternalPublicDir(context.getPackageName()+ File.separator + "Media", name);
                        request.setDestinationInExternalFilesDir(context, context.getPackageName(),File.separator + "Media" + File.separator + name);
                        enqueue = downloadManager.enqueue(request);



                        Thread t = new Thread() {
                            @Override
                            public void run(){
                                System.out.println("new thread to get downloadmanager service data");
                                boolean downloading = true;
                                DownloadManager.Query q = new DownloadManager.Query();
                                q.setFilterById(enqueue);
                                while (downloading) {
                                    Cursor cursor = downloadManager.query(q);
                                    cursor.moveToFirst();
                                    int bytes_downloaded = cursor.getInt(cursor
                                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                                    final double dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

                                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                                        downloading = false;
                                        System.out.println(dl_progress+"file downloaded fully "+id1);
                                        String s = String.valueOf(downloadManager.getUriForDownloadedFile(enqueue));
                                        DBREF_CHATS.child(dbtablekey).child("ChatMessages").child(id1).child("downloaduri").setValue(s);
                                        System.out.println("double progress of download downloadmanager service " + s);
                                    }

                                    saveDLToFirebase(id1,dl_progress);

                                    System.out.println(id1+" download progress dLmanagerservice "+dl_progress);
                                    cursor.close();

                                }
                            }
                        };
                        t.start();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                return "dL success";
            } else
                return "context null";
        }

        @Override
        protected void onPostExecute(String result) {
//            umprefs.setUri(id1, result);
            System.out.println(id1 + " file downloaded successfully downloadmanagerservice downloaded uri " + result);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            System.out.println(dbtablekey + "async progress downloadmanagerservice" + values.length + " wew " + values[0]);
        }
    }

    private void saveDLToFirebase(String id1,double dl_progress) {
        DBREF_CHATS.child(dbtablekey).child("ChatMessages").child(id1).child("downloadprogress").setValue(dl_progress);
    }

}
