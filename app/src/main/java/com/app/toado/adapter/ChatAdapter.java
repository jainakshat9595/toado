//package com.app.toado.adapter;
//
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Handler;
//import android.os.IBinder;
//import android.support.v7.widget.RecyclerView;
//import android.util.SparseBooleanArray;
//import android.view.HapticFeedbackConstants;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.app.toado.R;
//import com.app.toado.helper.EncryptUtils;
//import com.app.toado.helper.OpenFile;
//import com.app.toado.helper.PathFromUri;
//import com.app.toado.helper.UriHelper;
//import com.app.toado.settings.UserSession;
//import com.app.toado.model.ChatMessage;
//import com.app.toado.services.DownloadMangerService;
//import com.app.toado.settings.UserMediaPrefs;
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.bumptech.glide.request.target.BitmapImageViewTarget;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import static com.app.toado.helper.ToadoConfig.DBREF;
//import static com.app.toado.helper.ToadoConfig.DBREF_CHATS;
//
//public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyChatsViewHolder> {
//    ArrayList<ChatMessage> list = new ArrayList<>();
//    private Context context;
//    private UserSession session;
//    String dbTablekey;
//    EncryptUtils encryptUtils = new EncryptUtils();
//    private SparseBooleanArray selectedItems;
//    private static int currentSelectedIndex = -1;
//    private SparseBooleanArray animationItemsIndex;
//    private boolean reverseAllAnimations = false;
//    private ChatAdapterListener listener;
//    UserMediaPrefs umprefs;
//
//    boolean mdownbound = false;
//    DownloadMangerService dmservice;
//    PathFromUri pat;
//    final Handler handler = new Handler();
//    Runnable ru;
//
//    public ChatAdapter(ArrayList<ChatMessage> list, Context context, ChatAdapterListener listener) {
//        this.list = list;
//        this.context = context;
//        session = new UserSession(context);
//         selectedItems = new SparseBooleanArray();
//        animationItemsIndex = new SparseBooleanArray();
//        this.listener = listener;
//        pat = new PathFromUri();
//    }
//
//    @Override
//    public MyChatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chats, parent, false);
//
//        MyChatsViewHolder m = new MyChatsViewHolder(view);
//        System.out.println(list.size() + "m get layout pos" + view.getTag());
//        return new MyChatsViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(final ChatAdapter.MyChatsViewHolder holder, int position) {
//        final ChatMessage comment = list.get(position);
//
//        umprefs = new UserMediaPrefs(context);
//        if (comment.getSenderUId().equals(session.getUserKey())) {
//            //status for messages sent by me
//            holder.otherSender.setVisibility(View.GONE);
//            holder.meSender.setVisibility(View.VISIBLE);
//            holder.status.setVisibility(View.VISIBLE);
//            holder.meSender_sender.setText(comment.getSender());
//            holder.meSender_Timestamp.setText(comment.getSendertimestamp());
//            applyStatus(comment, holder);
//        } else {
//            holder.meSender.setVisibility(View.GONE);
//            holder.otherSender.setVisibility(View.VISIBLE);
//            holder.otherSender_sender.setText(comment.getSender());
//            holder.otherSender_Timestamp.setText(comment.getSendertimestamp());
//            holder.status.setVisibility(View.GONE);
//        }
//        applyClickEvents(holder, position);
//        String type = comment.getType();
//
//        if (comment.getPercentUploaded() == 100 || comment.getType().matches("text"))
//            setView(type, holder, comment);
//    }
//
//    private void setView(String type, final MyChatsViewHolder holder, final ChatMessage comment) {
//
//        //setting photo to null in set view
//
////        if (comment.getPercentUploaded() < 100) {
////            applyUploadProgressBar(holder, comment);
////        }
//
//        holder.photo.setImageDrawable(null);
//        if (!comment.getStatus().matches("0")) {
//            if (comment.getCommentString().equals("")) {
//                holder.commentString.setVisibility(View.GONE);
//            } else {
//                holder.commentString.setVisibility(View.VISIBLE);
//                holder.commentString.setText(comment.getCommentString());
//            }
//            switch (type) {
//                case "text":
////                    holder.mapv.setVisibility(View.GONE);
//                    holder.btndown.setVisibility(View.GONE);
//                    String decryptedmsg = encryptUtils.decrypt(comment.getCommentString(), comment.getSendertimestamp());
//                    holder.commentString.setText(decryptedmsg);
//                    holder.photo.setVisibility(View.GONE);
//                    break;
//
//                case "photo":
////                    holder.mapv.setVisibility(View.GONE);
//                    holder.btndown.setVisibility(View.GONE);
//                    holder.photo.setVisibility(View.VISIBLE);
//                    String um = umprefs.getURI(comment.getId());
//                    System.out.println("case photo chatadapter" + um);
//                    if (!um.matches("nouri")) {
//                        Glide.clear(holder.photo);
//                        Glide.with(context).load(um).error(R.drawable.whatsapplogo).centerCrop().into(holder.photo);
//                    } else {
//                        getUriGlide(comment, holder);
//                    }
//                    break;
//
//                case "video":
////                    holder.mapv.setVisibility(View.GONE);
//                    holder.btndown.setVisibility(View.VISIBLE);
//                    holder.photo.setVisibility(View.VISIBLE);
////                    startBtnDownListeners("vid", comment, holder);
//                    String umv = umprefs.getURI(comment.getId());
//                    if (comment.getSenderUId().equals(session.getUserKey()) && !umv.matches("nouri")) {
//                        holder.btndown.setText("open");
//                    } else if (!umv.matches("nouri")) {
//                        holder.btndown.setText("open");
//                    } else {
//                        holder.btndown.setText("download");
//                    }
//
//                    break;
//
//                case "doc":
////                    holder.mapv.setVisibility(View.GONE);
//                    holder.btndown.setVisibility(View.VISIBLE);
//                    holder.photo.setVisibility(View.VISIBLE);
//                    String umv1 = umprefs.getURI(comment.getId());
//                    System.out.println(comment.getImgurl() + "case doc chatadapter" + umv1);
//
////                    startBtnDownListeners("doc", comment, holder);
//
//                    Glide.clear(holder.photo);
//                    Glide.with(context).load(R.drawable.docsimage).into(holder.photo);
//                    if (comment.getSenderUId().equals(session.getUserKey()) && !umv1.matches("nouri")) {
//                        holder.btndown.setText("open");
//                    } else if (!umv1.matches("nouri")) {
//                        holder.btndown.setText("open");
//                    } else {
//                        holder.btndown.setText("download");
//                    }
//                    break;
//            }
//            //special case for location
//            if (type.contains("location")) {
//                String[] a = type.split(",");
//                System.out.println("chat adapter location snapshot " + a[0] + a[1] + a[2]);
//                holder.btndown.setVisibility(View.GONE);
//                holder.photo.setVisibility(View.VISIBLE);
//
//                String um = umprefs.getURI(comment.getId());
//                System.out.println("case location chatadapter" + um);
//                if (!um.matches("nouri")) {
//                    Glide.clear(holder.photo);
//                    Glide.with(context).load(um).error(R.drawable.whatsapplogo).into(holder.photo);
//                } else {
//                    getUriGlide(comment, holder);
//                }
//
//            }
//        }
//    }
//
////    private void startBtnDownListeners(String doc, ChatMessageRealm comment, final MyChatsViewHolder holder) {
////        final DatabaseReference dbr = DBREF_CHATS.child(dbTablekey).child("ChatMessages");
////        switch (doc) {
////            case "vid":
////                String umv = umprefs.getURI(comment.getId());
////                System.out.println(comment.getImgurl() + "case video chatadapter" + umv);
////                Glide.clear(holder.photo);
////                Glide.with(context).load(R.drawable.iconvideo).into(holder.photo);
////                if (comment.getSenderUId().equals(session.getUserKey()) && !umv.matches("nouri")) {
////                    holder.btndown.setText("open");
////                } else if (!umv.matches("nouri")) {
////                    holder.btndown.setText("open");
////                } else {
////                    dbr.child(comment.getId()).child("imgurl").addValueEventListener(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(DataSnapshot dataSnapshot) {
////                            if (!dataSnapshot.getValue().toString().matches("nouri")) {
////                                holder.btndown.setText("download");
////                                holder.btndown.setEnabled(true);
////                                dbr.removeEventListener(this);
////                            } else {
////                                holder.btndown.setText("pls wait");
////                                holder.btndown.setEnabled(false);
////                            }
////                        }
////
////                        @Override
////                        public void onCancelled(DatabaseError databaseError) {
////
////                        }
////                    });
////                }
////                break;
////            case "doc":
////                String umv1 = umprefs.getURI(comment.getId());
////                System.out.println(comment.getImgurl() + "case doc chatadapter" + umv1);
////
////                Glide.clear(holder.photo);
////                Glide.with(context).load(R.drawable.docsimage).into(holder.photo);
////                if (comment.getSenderUId().equals(session.getUserKey()) && !umv1.matches("nouri")) {
////                    holder.btndown.setText("open");
////                } else if (!umv1.matches("nouri")) {
////                    holder.btndown.setText("open");
////                } else {
////                    dbr.child(comment.getId()).child("imgurl").addValueEventListener(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(DataSnapshot dataSnapshot) {
////                            if (!dataSnapshot.getValue().toString().matches("nouri")) {
////                                holder.btndown.setText("download");
////                                holder.btndown.setEnabled(true);
////                                dbr.removeEventListener(this);
////                            } else {
////                                holder.btndown.setText("pls wait");
////                                holder.btndown.setEnabled(false);
////                            }
////                        }
////
////                        @Override
////                        public void onCancelled(DatabaseError databaseError) {
////
////                        }
////                    });
////                }
////                break;
////        }
////    }
//
//    private void getUriGlide(final ChatMessage comment, final MyChatsViewHolder holder) {
//        DatabaseReference dbr = DBREF.child("Chats").child(dbTablekey).child("ChatMessages").child(comment.getId()).child("imgurl");
//        dbr.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    String val = dataSnapshot.getValue().toString();
//                    if (!val.matches("nourl")&&context!=null) {
//                        Glide.with(context)
//                                .load(val)
//                                .asBitmap()
//                                .into(new BitmapImageViewTarget(holder.photo) {
//                                    @Override
//                                    protected void setResource(Bitmap resource) {
//                                        imageAsynct(resource, comment.getId());
//                                        super.setResource(resource);
//
//                                    }
//                                });
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//    public void applyStatus2(String status, MyChatsViewHolder holder, ChatMessage comment) {
//        System.out.println("context applystatus" + context);
//        switch (status) {
//            case "0":
//                Glide.with(context).load(R.mipmap.ic_pending).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.status);
//                break;
//            case "1":
//                Glide.with(context).load(R.mipmap.ic_sent).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.status);
//                break;
//
//            case "2":
//                Glide.with(context).load(R.mipmap.ic_delivered).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.status);
//                break;
//
//            case "3":
//                Glide.with(context).load(R.mipmap.ic_read).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.status);
//                break;
//
//        }
//    }
//
//
//    public void applyStatus(final ChatMessage comment, final MyChatsViewHolder holder) {
//        DatabaseReference dbCommentStatus = FirebaseDatabase.getInstance().getReference().child("Chats").child(dbTablekey).child("ChatMessages").child(comment.getId()).child("status").getRef();
//        dbCommentStatus.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    String status = dataSnapshot.getValue(String.class);
//                    System.out.println(status + " status string chatadapter applystatus" + comment.getCommentString());
//                    switch (status) {
//                        case "0":
//                            Glide.with(context).load(R.mipmap.ic_pending).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.status);
//                            break;
//                        case "1":
//                            Glide.with(context).load(R.mipmap.ic_sent).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.status);
//                            break;
//
//                        case "2":
//                            Glide.with(context).load(R.mipmap.ic_delivered).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.status);
//                            break;
//
//                        case "3":
//                            Glide.with(context).load(R.mipmap.ic_read).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.status);
//                            break;
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//
//    public void applyUploadProgressBar(final MyChatsViewHolder holder, final ChatMessage chatMessage) {
//        int percent = chatMessage.getPercentUploaded();
//        holder.btndown.setVisibility(View.VISIBLE);
//        holder.photo.setVisibility(View.VISIBLE);
//        switch (chatMessage.getType()) {
//            case "photo":
//                Uri uri = Uri.parse(umprefs.getURI(chatMessage.getId()));
//                Glide.clear(holder.photo);
//                if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.M) {
//                    Uri uri2 = UriHelper.getNougatUri(new File(uri.getPath()));
//                    System.out.println("photo uri 2 from chatadapter " + uri2);
//                    Glide.with(context).load(uri2).error(R.drawable.whatsapplogo).into(holder.photo);
//                } else {
//                    System.out.println("photo uri from chatadapter " + uri);
//                    Glide.with(context).load(uri).error(R.drawable.whatsapplogo).into(holder.photo);
//                }
//                break;
//            case "video":
//                Glide.with(context).load(R.drawable.iconvideo).into(holder.photo);
//                break;
//            case "doc":
//                Glide.with(context).load(R.drawable.docsimage).into(holder.photo);
//                break;
//
//        }
//        if(chatMessage.getType().contains("location")){
//            Uri uriloc = Uri.parse(umprefs.getURI(chatMessage.getId()));
//            Glide.clear(holder.photo);
//            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.M) {
//                Uri uri2 = UriHelper.getNougatUri(new File(uriloc.getPath()));
//                System.out.println("photo uri 2 from chatadapter " + uri2);
//                Glide.with(context).load(uri2).error(R.drawable.whatsapplogo).into(holder.photo);
//            } else {
//                System.out.println("photo uri from chatadapter " + uriloc);
//                Glide.with(context).load(uriloc).error(R.drawable.whatsapplogo).into(holder.photo);
//            }
//        }
//
//        if (percent != 100) {
//            System.out.println(chatMessage.getSendertimestamp() + "progress chatadapter " + percent);
//            holder.btndown.setEnabled(false);
//            holder.btndown.setText("Uploading: " + percent);
//        } else {
//            holder.btndown.setVisibility(View.GONE);
//        }
//    }
//
//    public void applyDownloadProgressBar(final MyChatsViewHolder holder, final ChatMessage chatMessage) {
//        int percent = chatMessage.getDownloadprogress();
//        System.out.println("download percent applydownloadprogressbar "+percent);
//        holder.btndown.setVisibility(View.VISIBLE);
//        holder.photo.setVisibility(View.VISIBLE);
//        switch (chatMessage.getType()) {
//            case "photo":
//                Uri uri = Uri.parse(umprefs.getURI(chatMessage.getId()));
//                Glide.clear(holder.photo);
//                if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.M) {
//                    Uri uri2 = UriHelper.getNougatUri(new File(uri.getPath()));
//                    System.out.println("photo uri 2 from chatadapter " + uri2);
//                    Glide.with(context).load(uri2).error(R.drawable.whatsapplogo).into(holder.photo);
//                } else {
//                    System.out.println("photo uri from chatadapter " + uri);
//                    Glide.with(context).load(uri).error(R.drawable.whatsapplogo).into(holder.photo);
//                }
//                break;
//            case "video":
//                Glide.with(context).load(R.drawable.iconvideo).into(holder.photo);
//                break;
//            case "doc":
//                Glide.with(context).load(R.drawable.docsimage).into(holder.photo);
//                break;
//        }
//
//        if(chatMessage.getType().contains("location")){
//            Uri uriloc = Uri.parse(umprefs.getURI(chatMessage.getId()));
//            Glide.clear(holder.photo);
//            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.M) {
//                Uri uri2 = UriHelper.getNougatUri(new File(uriloc.getPath()));
//                System.out.println("photo uri 2 from chatadapter " + uri2);
//                Glide.with(context).load(uri2).error(R.drawable.whatsapplogo).into(holder.photo);
//            } else {
//                System.out.println("photo uri from chatadapter " + uriloc);
//                Glide.with(context).load(uriloc).error(R.drawable.whatsapplogo).into(holder.photo);
//            }
//        }
//
//        if (percent > 0 && percent < 100) {
//            System.out.println(chatMessage.getSendertimestamp() + "download progress chatadapter " + percent);
//            holder.btndown.setEnabled(false);
//            holder.btndown.setText("Downloading: " + percent);
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return list.size();
//    }
//
//    public class MyChatsViewHolder extends RecyclerView.ViewHolder {
//        public TextView otherSender_sender, otherSender_Timestamp, meSender_sender, meSender_Timestamp, commentString;
//        public LinearLayout otherSender, meSender, messageContainer;
//        public ImageView status, photo;
//
//        public Button btndown;
//
//
//        public MyChatsViewHolder(View itemView) {
//            super(itemView);
//
//            messageContainer = (LinearLayout) itemView.findViewById(R.id.message_container);
//            otherSender_sender = (TextView) itemView.findViewById(R.id.otherSender_Sender);
//            otherSender_Timestamp = (TextView) itemView.findViewById(R.id.otherSender_TimeStamp);
//            otherSender = (LinearLayout) itemView.findViewById(R.id.otherSender);
//            status = (ImageView) itemView.findViewById(R.id.status);
//            btndown = (Button) itemView.findViewById(R.id.btndown);
//
//            meSender_sender = (TextView) itemView.findViewById(R.id.meSender_Sender);
//            meSender_Timestamp = (TextView) itemView.findViewById(R.id.meSender_TimeStamp);
//            meSender = (LinearLayout) itemView.findViewById(R.id.meSender);
//
//            commentString = (TextView) itemView.findViewById(R.id.commentString);
//
//            photo = (ImageView) itemView.findViewById(R.id.photo);
//
//        }
//    }
//
//    public void resetAnimationIndex() {
//        reverseAllAnimations = false;
//        animationItemsIndex.clear();
//    }
//
//    public void toggleSelection(int pos) {
//        currentSelectedIndex = pos;
//        if (selectedItems.get(pos, false)) {
//            selectedItems.delete(pos);
//            animationItemsIndex.delete(pos);
//        } else {
//            selectedItems.put(pos, true);
//            animationItemsIndex.put(pos, true);
//        }
//        notifyItemChanged(pos);
//    }
//
//    public void clearSelections() {
//        reverseAllAnimations = true;
//        selectedItems.clear();
//        notifyDataSetChanged();
//    }
//
//    public int getSelectedItemCount() {
//        return selectedItems.size();
//    }
//
//    public List<Integer> getSelectedItems() {
//        List<Integer> items =
//                new ArrayList<>(selectedItems.size());
//        for (int i = 0; i < selectedItems.size(); i++) {
//            items.add(selectedItems.keyAt(i));
//        }
//        return items;
//    }
//
//    public void removeData(int position) {
//        list.remove(position);
//        resetCurrentIndex();
//    }
//
//    private void resetCurrentIndex() {
//        currentSelectedIndex = -1;
//    }
//
//    public interface ChatAdapterListener {
//
//        void onMessageRowClicked(int position);
//
//        void onRowLongClicked(int position);
//
//    }
//
//    private void applyRowAnimation(MyChatsViewHolder holder, int position) {
//        if ((reverseAllAnimations && animationItemsIndex.get(position, false)) || currentSelectedIndex == position) {
//            //FlipAnimator.flipView(mContext, holder.iconBack, holder.iconFront, false);
////TODO
//            resetCurrentIndex();
//        }
//
//    }
//
//    private void applyClickEvents(final MyChatsViewHolder holder, final int position) {
//
//        holder.messageContainer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                listener.onMessageRowClicked(position);
//            }
//        });
//
//        holder.messageContainer.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                listener.onRowLongClicked(position);
//                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
//                return true;
//            }
//        });
//
//        holder.btndown.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                System.out.println(holder.getLayoutPosition() + " int pos " + position + " download button clicked for" + holder.btndown.getText().toString().toLowerCase());
//                if (holder.btndown.getText().toString().toLowerCase().matches("download")) {
////                    applyUploadProgressBar(holder, mList.get(position));
//                    DBREF_CHATS.child(dbTablekey).child("ChatMessages").child(list.get(position).getId()).child("imgurl").addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            saveOtherFiles(list.get(position).getType(), dataSnapshot.getValue().toString(), list.get(position).getId(), holder);
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });
//
//                } else
//                    openFile(list.get(position).getId());
//            }
//        });
//    }
//
//    private void openFile(String id) {
//        String fileuri = umprefs.getURI(id);
//        System.out.println("open uri chatadapter" + fileuri);
//
//        try {
//            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.M)
//                OpenFile.openFile(context, new File(fileuri));
//            else
//                OpenFile.normalopenFile(context, Uri.parse(fileuri));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private void saveOtherFiles(final String filetype, final String url, final String id, final MyChatsViewHolder holder) {
//        if (!url.matches("nourl")) {
//            dmservice.start_download(filetype, url, id, dbTablekey);
//        } else
//            Toast.makeText(context, "No url to download chatadapter", Toast.LENGTH_SHORT).show();
//    }
//
//    private void saveImage(Bitmap finalBitmap, String id) {
//
//        String fname = "photo-" + id + ".jpg";
//        File file = OpenFile.createFile(context, fname);
//        try {
//            FileOutputStream out = new FileOutputStream(file);
//            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
//            System.out.println(file.getPath() + " finalbitmap saveimage " + file.getAbsolutePath());
//            umprefs.setUri(id, file.getAbsolutePath());
//            out.flush();
//            out.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void imageAsynct(final Bitmap finalBitmap, final String id) {
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                saveImage(finalBitmap, id);
//                return null;
//            }
//        }.execute();
//    }
//
//    private ServiceConnection mdownloadconnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className,
//                                       IBinder service) {
//            // We've bound to LocalService, cast the IBinder and get LocalService instance
//            DownloadMangerService.LocalBinder binder = (DownloadMangerService.LocalBinder) service;
//            dmservice = binder.getService();
//            mdownbound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            mdownbound = false;
//        }
//    };
//
//
//    public void unbinddownservice() {
//        context.unbindService(mdownloadconnection);
//        mdownbound = false;
//    }
//
//    public void bindtodownservice() {
//        Intent intent = new Intent(context, DownloadMangerService.class);
//        context.bindService(intent, mdownloadconnection, Context.BIND_AUTO_CREATE);
//    }
//
//
//    public void destroyHandler() {
//        handler.removeCallbacks(ru);
//    }
//}
//
//
