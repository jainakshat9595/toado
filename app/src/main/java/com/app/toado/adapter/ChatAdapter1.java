package com.app.toado.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.toado.BuildConfig;
import com.app.toado.R;
import com.app.toado.helper.OpenFile;
import com.app.toado.helper.UriHelper;
import com.app.toado.model.realm.ChatMessageRealm;
import com.app.toado.services.UploadFileService;
import com.app.toado.settings.UserSession;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static android.R.attr.path;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_FAILED;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_PROGRESS;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_STARTING;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_SUCCESS;
import static com.app.toado.services.UploadFileService.MEDIA_PROGRESSING;
import static com.app.toado.services.UploadFileService.MEDIA_STARTING;
import static com.app.toado.services.UploadFileService.MEDIA_SUCCESS;

public class ChatAdapter1 extends RecyclerView.Adapter<ChatAdapter1.MyViewHolder> {
    ArrayList<ChatMessageRealm> mList = new ArrayList<>();
    private Activity context;
    private UserSession session;
    public static final int SENDER = 0;
    public static final int RECIPIENT = 1;
    String TAG = "ChatAdapter1";
    UploadFileService ulservice;
    String otheruserkey;
    String todaytimestamp;

    public ChatAdapter1(ArrayList<ChatMessageRealm> list, Activity context, String otheruserkey, String todaytimestamp) {
        this.mList = list;
        this.context = context;
        session = new UserSession(context);
        this.otheruserkey = otheruserkey;
        this.todaytimestamp = todaytimestamp;
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.get(position).getSenderjid().matches(session.getUserKey())) {
            return SENDER;
        } else {
            return RECIPIENT;
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        switch (viewType) {
            case SENDER:
                View viewSender = inflater.inflate(R.layout.row_chats_sender, viewGroup, false);
                viewHolder = new MyViewHolder(viewSender);
                break;
            case RECIPIENT:
                View viewRecipient = inflater.inflate(R.layout.row_chats_receiver, viewGroup, false);
                viewHolder = new MyViewHolder(viewRecipient);
                break;
        }
        return (MyViewHolder) viewHolder;
    }

    @Override
    public void onBindViewHolder(final ChatAdapter1.MyViewHolder holder, int position) {
        final ChatMessageRealm comment = mList.get(position);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        if (comment.getMsgstring().matches("")) {
            holder.msglay.setVisibility(View.GONE);
            holder.timestampnotext.setVisibility(View.VISIBLE);
            holder.timestampnotext.setText(comment.getSendertime());
        } else {
            holder.msglay.setVisibility(View.VISIBLE);
            holder.timestampnotext.setVisibility(View.GONE);
            holder.sender_Timestamp.setText(comment.getSendertime());
        }

        holder.progressbar.setVisibility(View.GONE);
        holder.progressbar.setIndeterminate(true);
        switch (comment.getMsgtype()) {
            case "text":
                String decryptedmsg = comment.getMsgstring();
                holder.commentString.setText(decryptedmsg);
                break;
            case "photo":
                holder.imgchatrel.setVisibility(View.VISIBLE);
                Glide.clear(holder.imgchat);
                holder.commentString.setText(comment.getMsgstring());
                holder.imgchat.setVisibility(View.VISIBLE);
                if (getItemViewType(position) == SENDER) {
                    holder.btndown.setVisibility(View.GONE);
                    Glide.with(context).load(comment.getMsglocalurl()).into(holder.imgchat);
                    if (comment.getMsgweburl().matches("")) {
                        holder.progressbar.setVisibility(View.VISIBLE);
                    }
                } else if (comment.getMsglocalurl().matches("")) {
                    holder.btndown.setVisibility(View.VISIBLE);
                    Glide.with(context).load(comment.getMediathumbnail()).bitmapTransform(new BlurTransformation(context)).dontAnimate().into(holder.imgchat);
                } else if (!comment.getMsglocalurl().matches("")) {
                    holder.btndown.setVisibility(View.GONE);
                    Glide.with(context).load(comment.getMsglocalurl()).into(holder.imgchat);
                }
                break;
            case "video":
                Log.d(TAG, "video thumbnail " + comment.getMediathumbnail());
                holder.imgchatrel.setVisibility(View.VISIBLE);
                Glide.clear(holder.imgchat);
                holder.commentString.setText(comment.getMsgstring());
                holder.imgchat.setVisibility(View.VISIBLE);
                if (getItemViewType(position) == SENDER) {
                    holder.btndown.setVisibility(View.GONE);
                    if (comment.getMsgweburl().matches("")) {
                        holder.progressbar.setVisibility(View.VISIBLE);
                    }
                    Glide.with(context).load(comment.getMediathumbnail()).dontAnimate().into(holder.imgchat);
                } else if (comment.getMsglocalurl().matches("")) {
                    holder.btndown.setVisibility(View.VISIBLE);
                    Glide.with(context).load(comment.getMediathumbnail()).bitmapTransform(new BlurTransformation(context)).dontAnimate().into(holder.imgchat);
                } else if (!comment.getMsglocalurl().matches("")) {
                    holder.btndown.setVisibility(View.GONE);
                    Glide.with(context).load(comment.getMediathumbnail()).dontAnimate().into(holder.imgchat);
                }
                break;
            case "doc":
                holder.imgchatrel.setVisibility(View.VISIBLE);
                Glide.clear(holder.imgchat);
                holder.commentString.setText(comment.getMsgstring());
                holder.imgchat.setVisibility(View.VISIBLE);
                Glide.with(context).load(R.drawable.docsimage).into(holder.imgchat);

                if (getItemViewType(position) == SENDER) {
                    holder.btndown.setVisibility(View.GONE);
                    if (comment.getMsgweburl().matches("")) {
                        holder.progressbar.setVisibility(View.VISIBLE);
                    }
                    Glide.with(context).load(R.drawable.docsimage).into(holder.imgchat);
                } else if (comment.getMsglocalurl().matches("")) {
                    holder.btndown.setVisibility(View.VISIBLE);
                } else if (!comment.getMsglocalurl().matches("")) {
                    holder.btndown.setVisibility(View.GONE);
                }
                break;
            case "location":
                holder.imgchatrel.setVisibility(View.VISIBLE);
                Glide.clear(holder.imgchat);
                holder.commentString.setText(comment.getMsgstring());
                holder.imgchat.setVisibility(View.VISIBLE);
                if (getItemViewType(position) == SENDER) {
                    if (comment.getMsgweburl().matches("")) {
                        holder.progressbar.setVisibility(View.VISIBLE);
                    }
                    holder.btndown.setVisibility(View.GONE);
                    Glide.with(context).load(comment.getMsglocalurl()).into(holder.imgchat);
                } else if (comment.getMsglocalurl().matches("")) {
                    holder.btndown.setVisibility(View.VISIBLE);
                    Glide.with(context).load(comment.getMediathumbnail()).bitmapTransform(new BlurTransformation(context)).dontAnimate().into(holder.imgchat);

                } else if (!comment.getMsglocalurl().matches("")) {
                    holder.btndown.setVisibility(View.GONE);
                    Glide.with(context).load(comment.getMsglocalurl()).into(holder.imgchat);
                }
                break;
        }

        holder.btndown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, holder.getAdapterPosition() + " down clicked for" + comment.getMsgid() + comment.getMsglocalurl() + comment.getMsgweburl());
                FirebaseStorage storage = FirebaseStorage.getInstance();
                final StorageReference httpsReference = storage.getReferenceFromUrl(comment.getMsgweburl());
                httpsReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        Log.d(TAG, "Storage meta data" + storageMetadata.getContentType() + " " + storageMetadata.getName());
                        String mdata = storageMetadata.getContentType();
                        String filetype = "incoming" + File.separator + comment.getMsgtype();
                        String filextension = mdata.substring(mdata.indexOf("/") + 1, mdata.length());
                        String filename = comment.getSenderdate() + " " + comment.getSendertime() + "." + filextension;
                        File file = OpenFile.createFile(context, filename, filetype);
                        Log.d(TAG, file.getAbsolutePath() + "Storage meta data" + filetype + " " + filename);
                        ulservice.downloadFile(session.getUserKey(), otheruserkey, comment, httpsReference, file, context);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        // Uh-oh, an error occurred!
                        exception.printStackTrace();
                    }
                });
            }
        });

        holder.imgchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = mList.get(holder.getAdapterPosition()).getMsglocalurl();
                if (path == null || path.matches("")) {
                    Toast.makeText(context, "Please download media first.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);

                    File file = new File(path);

                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                    String type = mime.getMimeTypeFromExtension(ext);
                    Log.d(TAG, file.getAbsolutePath() + "mime " + type + " ext " + ext + "uri file " + FileProvider.getUriForFile(context,
                            BuildConfig.APPLICATION_ID + ".provider",
                            file).toString());
//                    Uri ur = UriHelper.getNougatUri(file);
//                    Log.d(TAG, "uri2 " + ur);
//                    intent.setDataAndType(FileProvider.getUriForFile(context,
//                            BuildConfig.APPLICATION_ID + ".provider",
//                            file), type);
                    try {
                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                        m.invoke(null);
                        intent.setDataAndType(Uri.fromFile(file), type);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView sender_Timestamp, commentString, timestampnotext;
        public ImageView imgchat;
        public ImageView btndown;
        public ProgressBar progressbar;
        public CoordinatorLayout imgchatrel;
        public LinearLayout msglay;

        public MyViewHolder(View itemView) {
            super(itemView);
            sender_Timestamp = (TextView) itemView.findViewById(R.id.meSender_TimeStamp);
            timestampnotext = (TextView) itemView.findViewById(R.id.timestampnotext);
            commentString = (TextView) itemView.findViewById(R.id.commentString);
//            progress = (TextView) itemView.findViewById(R.id.mediaprogress);
            imgchat = (ImageView) itemView.findViewById(R.id.imgchat);
            btndown = (ImageView) itemView.findViewById(R.id.btndown);
            progressbar = (ProgressBar) itemView.findViewById(R.id.progressbar);
            imgchatrel = (CoordinatorLayout) itemView.findViewById(R.id.imgchatrel);
            msglay = (LinearLayout) itemView.findViewById(R.id.msglay);
        }
    }

    public void setUploadProgress(MyViewHolder holder, String progress, String progresstext) {
        Log.d(TAG, progresstext + " upload check " + (holder.progressbar.getVisibility() == View.VISIBLE));
//        holder.progress.setVisibility(View.VISIBLE);
        holder.progressbar.setVisibility(View.VISIBLE);
        holder.progressbar.setIndeterminate(true);
//        holder.progress.setText("u/L status: " + progress);
        if (progresstext.matches(MEDIA_STARTING) || progresstext.matches(MEDIA_PROGRESSING)) {
            holder.progressbar.setVisibility(View.VISIBLE);
        } else if (progresstext.matches(MEDIA_SUCCESS)) {
            holder.progressbar.setVisibility(View.GONE);
        }
    }

    public void setDownloadProgress(MyViewHolder holder, String progress, String progressint, String localurl) {
        Log.d(TAG, progress + " download check " + "  " + progressint + "  " + localurl);
//        holder.progress.setVisibility(View.VISIBLE);
        holder.progressbar.setIndeterminate(true);
//        holder.progress.setText("D/L status: " + progressint);
        if (progress.matches(MEDIA_DOWNLOAD_STARTING) || progress.matches(MEDIA_DOWNLOAD_PROGRESS)) {
            holder.progressbar.setVisibility(View.VISIBLE);
        } else if (progress.matches(MEDIA_DOWNLOAD_SUCCESS)) {
            Log.d(TAG, " chatadapter1 " + mList.get(holder.getAdapterPosition()).getMsgtype());
//            holder.progress.setVisibility(View.GONE);
            holder.progressbar.setVisibility(View.GONE);
            holder.btndown.setVisibility(View.GONE);
            if (mList.get(holder.getAdapterPosition()).getMsgtype().matches("photo")) {
                try {
                    Log.d(TAG, " loading photo now " + localurl);
                    Glide.with(context).load(localurl).dontAnimate().into(holder.imgchat);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (mList.get(holder.getAdapterPosition()).getMsgtype().matches("video")) {
                Log.d(TAG, " loading video success photo now " + localurl);

            } else if (mList.get(holder.getAdapterPosition()).getMsgtype().matches("doc")) {
                Log.d(TAG, " loading doc success photo now " + localurl);
                Glide.with(context).load(R.drawable.docsimage).dontAnimate().into(holder.imgchat);
            } else if (mList.get(holder.getAdapterPosition()).getMsgtype().matches("location")) {
                Log.d(TAG, " loading location success photo now " + localurl);
                Glide.with(context).load(mList.get(holder.getAdapterPosition()).getMsglocalurl()).dontAnimate().into(holder.imgchat);
            }
        } else if (progress.matches(MEDIA_DOWNLOAD_FAILED)) {
//            holder.progress.setVisibility(View.GONE);
            holder.progressbar.setVisibility(View.GONE);
        }
    }

    public void setUploadServiceRef(UploadFileService ulservice) {
        Log.d(TAG, "setting upload file reference chatadapt");
        this.ulservice = ulservice;
    }
}


