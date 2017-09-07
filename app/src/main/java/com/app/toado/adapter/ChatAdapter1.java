package com.app.toado.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.toado.BuildConfig;
import com.app.toado.R;
import com.app.toado.activity.chat.MediaPreviewActivity;
import com.app.toado.activity.chat.ForwardChatActivity;
import com.app.toado.helper.ChatHelper;
import com.app.toado.helper.OpenFile;
import com.app.toado.model.ChatMessageForward;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderAdapter;
import io.realm.Realm;
import jp.wasabeef.glide.transformations.BlurTransformation;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_FAILED;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_PROGRESS;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_STARTING;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_SUCCESS;
import static com.app.toado.services.UploadFileService.MEDIA_PROGRESSING;
import static com.app.toado.services.UploadFileService.MEDIA_STARTING;
import static com.app.toado.services.UploadFileService.MEDIA_SUCCESS;

public class ChatAdapter1 extends RecyclerView.Adapter<ChatAdapter1.MyViewHolder> implements StickyHeaderAdapter<ChatHeaderViewHolder> {
    ArrayList<ChatMessageRealm> mList = new ArrayList<>();
    private Activity context;
    private UserSession session;
    public static final int SENDER = 0;
    public static final int RECIPIENT = 1;
    String TAG = "ChatAdapter1";
    UploadFileService ulservice;
    String otheruserkey;
    String otherusername;
    String todaytimestamp;
    private boolean multiSelect = false;
    private ArrayList<ChatMessageRealm> selectedItems = new ArrayList<>();
    private ArrayList<MyViewHolder> selectedHolders = new ArrayList<>();
    String stringcopy = "";
    ActionMode mMode;

    public ChatAdapter1(ArrayList<ChatMessageRealm> list, Activity context, String otheruserkey, String todaytimestamp, String otherusername) {
        this.mList = list;
        this.context = context;
        session = new UserSession(context);
        this.otheruserkey = otheruserkey;
        this.todaytimestamp = todaytimestamp;
        this.otherusername = otherusername;
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
    public void onBindViewHolder(final ChatAdapter1.MyViewHolder holder, final int position) {
        final ChatMessageRealm comment = mList.get(position);

        Log.d(TAG, "chatadapter " + comment.getMsgstring());

        if (comment.getMsgstring() != null) {
            if (!comment.getMsgstring().matches("")) {
                holder.layonimage.setVisibility(View.GONE);
                holder.msglay.setVisibility(View.VISIBLE);
                holder.sender_Timestamp.setText(comment.getSendertime());
                if (comment.getStar() != null && comment.getStar())
                    holder.iconstar1.setVisibility(View.VISIBLE);
                else
                    holder.iconstar1.setVisibility(View.GONE);
            } else {
                holder.layonimage.setVisibility(View.VISIBLE);
                holder.msglay.setVisibility(View.GONE);
                holder.timestampnotext.setText(comment.getSendertime());
                if (comment.getStar() != null && comment.getStar())
                    holder.iconstar2.setVisibility(View.VISIBLE);
                else
                    holder.iconstar2.setVisibility(View.GONE);
            }
        }

        // LONG PRESS VIEW HOLDER SETUP

        holder.msgbackground.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!multiSelect) {
                    ((AppCompatActivity) v.getContext()).startSupportActionMode(actionModeCallbacks);
                    selectItem(holder, comment);
                }
                return true;
            }
        });

        holder.imgchat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((AppCompatActivity) v.getContext()).startSupportActionMode(actionModeCallbacks);
                selectItem(holder, comment);
                return true;
            }
        });


        holder.msgbackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(holder, comment);
            }
        });

        holder.progressbar.setVisibility(View.GONE);
        holder.progressbar.setIndeterminate(true);
        setStatus(mList.get(position).getMsgstatus(), holder);

        switch (comment.getMsgtype()) {
            case "text":
                String decryptedmsg = comment.getMsgstring();
                holder.commentString.setText(decryptedmsg);
                holder.imgchatrel.setVisibility(View.GONE);
                holder.commentString.post(new Runnable() {
                    @Override
                    public void run() {
                        int lineCount = holder.commentString.getLineCount();
                        Log.d(TAG, holder.msglay.getOrientation() + " comment strings number of lines " + lineCount);
                        if (lineCount > 1) {
                            holder.msglay.setOrientation(LinearLayout.VERTICAL);
                        } else
                            holder.msglay.setOrientation(LinearLayout.HORIZONTAL);
                    }
                });
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
                if (comment.getMsgweburl() != null) {
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
                            exception.printStackTrace();
                        }
                    });
                }
            }
        });

        holder.imgchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (multiSelect) {
                    selectItem(holder, comment);
                } else {
                    String path = mList.get(holder.getAdapterPosition()).getMsglocalurl();
                    Log.d(TAG, "path imagchat holder" + path);

                    if (path == null || path.matches("")) {
                        holder.btndown.performClick();
                    } else {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);

                        File file = new File(path);

                        String ext = OpenFile.getFileExtension(file);
                        String type = OpenFile.getFileMimeType(ext);
                        Log.d(TAG, file.getAbsolutePath() + "mime " + type + " ext " + ext + "uri file " + FileProvider.getUriForFile(context,
                                BuildConfig.APPLICATION_ID + ".provider",
                                file).toString());

                        Intent in = new Intent(context, MediaPreviewActivity.class);
                        if (getItemViewType(position) == SENDER)
                            in.putExtra("sender", "You");
                        else
                            in.putExtra("sender", otherusername);

                        in.putExtra("timestamp", mList.get(position).getSenderdate() + ", " + mList.get(position).getSendertime());
                        in.putExtra("caption", mList.get(position).getMsgstring());
                        in.putExtra("imagepath", mList.get(position).getMsglocalurl());
                        in.putExtra("mediatype", type);
                        context.startActivity(in);

                    }
                }
            }
        });
    }

    private void setStatus(String msgstatus, MyViewHolder holder) {
        switch (msgstatus) {
            case "0":
                holder.imgstatus.setImageDrawable(context.getDrawable(R.mipmap.ic_pending));
                holder.imgstatus2.setImageDrawable(context.getDrawable(R.mipmap.ic_pending));
                break;
            case "1":
                holder.imgstatus.setImageDrawable(context.getDrawable(R.mipmap.ic_sent));
                holder.imgstatus2.setImageDrawable(context.getDrawable(R.mipmap.ic_sent));
                break;
            case "2":
                holder.imgstatus.setImageDrawable(context.getDrawable(R.mipmap.ic_delivered));
                holder.imgstatus2.setImageDrawable(context.getDrawable(R.mipmap.ic_delivered));
                break;
            case "3":
                holder.imgstatus.setImageDrawable(context.getDrawable(R.mipmap.ic_read));
                holder.imgstatus2.setImageDrawable(context.getDrawable(R.mipmap.ic_read));
                break;
        }
    }

    @Override
    public long getHeaderId(int position) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(mList.get(position).getMsgid()));
        return Long.parseLong(calendar.get(Calendar.YEAR) + "" + calendar.get(Calendar.MONTH) + "" + calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public ChatHeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return new ChatHeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.view_holder_post_header, parent, false));
    }

    @Override
    public void onBindHeaderViewHolder(ChatHeaderViewHolder viewholder, int position) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(mList.get(position).getMsgid()));
        viewholder.mPostDate.setText(
                String.format("%s %s, %s",
                        calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()),
                        calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR)
                )
        );
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView sender_Timestamp, commentString, timestampnotext, viewselected;
        public ImageView imgchat;
        public ImageView btndown;
        public ImageView imgstatus;
        public ImageView imgstatus2;
        public ImageView iconstar1;
        public ImageView iconstar2;
        public ProgressBar progressbar;
        public CoordinatorLayout imgchatrel;
        public LinearLayout msglay;
        public RelativeLayout msgbackground;
        public LinearLayout layonimage;

        public MyViewHolder(View itemView) {
            super(itemView);
            sender_Timestamp = (TextView) itemView.findViewById(R.id.meSender_TimeStamp);
            timestampnotext = (TextView) itemView.findViewById(R.id.timestampnotext);
            commentString = (TextView) itemView.findViewById(R.id.commentString);
            viewselected = (TextView) itemView.findViewById(R.id.viewselected);
            imgchat = (ImageView) itemView.findViewById(R.id.imgchat);
            iconstar1 = (ImageView) itemView.findViewById(R.id.iconstar);
            iconstar2 = (ImageView) itemView.findViewById(R.id.iconstar2);
            imgchat = (ImageView) itemView.findViewById(R.id.imgchat);
            imgstatus = (ImageView) itemView.findViewById(R.id.status);
            imgstatus2 = (ImageView) itemView.findViewById(R.id.status2);
            btndown = (ImageView) itemView.findViewById(R.id.btndown);
            progressbar = (ProgressBar) itemView.findViewById(R.id.progressbar);
            imgchatrel = (CoordinatorLayout) itemView.findViewById(R.id.imgchatrel);
            msglay = (LinearLayout) itemView.findViewById(R.id.msglay);
            msgbackground = (RelativeLayout) itemView.findViewById(R.id.msgbackground);
            layonimage = (LinearLayout) itemView.findViewById(R.id.layonimage);

        }
    }

    public void setUploadProgress(MyViewHolder holder, String progress, String progresstext) {
        Log.d(TAG, progresstext + " upload check " + (holder.progressbar.getVisibility() == View.VISIBLE));
        holder.progressbar.setVisibility(View.VISIBLE);
        holder.progressbar.setIndeterminate(true);
        ChatMessageRealm comment = mList.get(holder.getAdapterPosition());
        if (comment.getMsgstring() != null) {
            if (!comment.getMsgstring().matches("")) {
                holder.layonimage.setVisibility(View.GONE);
                holder.msglay.setVisibility(View.VISIBLE);
                holder.sender_Timestamp.setText(comment.getSendertime());
                if (comment.getStar() != null && comment.getStar())
                    holder.iconstar1.setVisibility(View.VISIBLE);
                else
                    holder.iconstar1.setVisibility(View.GONE);
            } else {
                holder.layonimage.setVisibility(View.VISIBLE);
                holder.msglay.setVisibility(View.GONE);
                holder.timestampnotext.setText(comment.getSendertime());
                if (comment.getStar() != null && comment.getStar())
                    holder.iconstar2.setVisibility(View.VISIBLE);
                else
                    holder.iconstar2.setVisibility(View.GONE);
            }
        }
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

    public void setDeliveryStatus(MyViewHolder holder, String status) {
        Log.d(TAG, mList.get(holder.getAdapterPosition()).getMsgstatus() + " delivery status " + mList.get(holder.getAdapterPosition()).getMsgweburl());
        setStatus(status, holder);
    }

    public void setUploadServiceRef(UploadFileService ulservice) {
        Log.d(TAG, "setting upload file reference chatadapt");
        this.ulservice = ulservice;
    }

    private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_actionmode, menu);
            selectedItems = new ArrayList<>();
            selectedHolders = new ArrayList<>();
            multiSelect = true;
            mMode = mode;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            doTask(item);
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            multiSelect = false;
            stringcopy = "";
            removeHighlights();
//            removeItems();
            notifyDataSetChanged();
        }
    };

    private void doTask(MenuItem item) {
        switch (item.getTitle().toString().toLowerCase()) {
            case "delete":
                for (ChatMessageRealm cm : selectedItems) {
                    Log.d(TAG, "mlist size" + mList.size());
                    mList.remove(cm);
                    ChatHelper.deleteItems(cm);
                }
                notifyDataSetChanged();
                break;

            case "copy":
                Log.d(TAG, selectedItems.size() + " copy called");
                for (ChatMessageRealm cm : selectedItems) {
                    String msgtime = cm.getSenderdate().replace("-2017", "").replace("-", "/") + ", " + cm.getSendertime().replace(" ", "");
                    stringcopy = stringcopy + "\n[" + msgtime + "] " + otherusername + ": " + cm.getMsgstring();
                    Log.d(TAG, "string copy" + stringcopy);
                }
                Log.d(TAG, "string copyfinal" + stringcopy);

                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", stringcopy);
                clipboard.setPrimaryClip(clip);
                break;

            case "star":
                for (ChatMessageRealm msg : selectedItems) {
                    ChatMessageRealm starmsg = new ChatMessageRealm(msg.getChatref(), msg.getOtherjid(), msg.getMsgstring(), msg.getSenderjid(), msg.getSendertime(), msg.getSenderdate(), msg.getMsgtype(), msg.getMsgid(), msg.getMsgstatus(), msg.getMsgweburl(), msg.getMsglocalurl(), msg.getMediathumbnail(), true);
                    ChatHelper.starMessage(starmsg);
                }
                for (MyViewHolder hold : selectedHolders) {
                    if (hold.layonimage.getVisibility() == View.VISIBLE)
                        hold.iconstar2.setVisibility(View.VISIBLE);
                    else
                        hold.iconstar1.setVisibility(View.VISIBLE);
                }
                break;
            case "forward":
                Intent in = new Intent(context, ForwardChatActivity.class);
                Bundle forward = new Bundle();
                ArrayList<ChatMessageForward> arrlist = new ArrayList<>();
                for (ChatMessageRealm msg : selectedItems) {
                    ChatMessageForward cmf = new ChatMessageForward(msg.getMsgstring(), msg.getMsgtype(), msg.getMsglocalurl(), msg.getMsgweburl(), msg.getMediathumbnail());
                    arrlist.add(cmf);
                }
                forward.putSerializable("arrmsgids", arrlist);
                in.putExtras(forward);
                context.startActivity(in);
                break;
        }
    }

    void selectItem(ChatAdapter1.MyViewHolder holder, ChatMessageRealm item) {
        if (multiSelect) {
            if (selectedItems.contains(item)) {
                holder.viewselected.setVisibility(View.GONE);
                selectedItems.remove(item);
                removeItem(holder);
            } else {
                selectedItems.add(item);
                selectedHolders.add(holder);
                int hei = holder.msgbackground.getHeight();
                Log.d(TAG, "height selected item " + hei);
                holder.viewselected.setVisibility(View.VISIBLE);
                holder.viewselected.getLayoutParams().height = hei;
                holder.viewselected.requestLayout();
            }
        }

        if (selectedHolders.size() == 0 && mMode != null) {
            mMode.finish();
        }

        if (selectedItems.size() == 0 && mMode != null)
            mMode.finish();

        for (ChatMessageRealm cmr : selectedItems)
            Log.d(TAG, selectedItems.size() + "selected items" + cmr.getMsgstring() + " " + multiSelect);

    }

    public void removeItem(MyViewHolder holder) {
        holder.viewselected.setVisibility(View.GONE);
        selectedHolders.remove(holder);
    }

    public void removeItems() {
        if (mMode != null)
            mMode.finish();
    }

    private void removeSelectedItems(ChatMessageRealm chatMessageRealm) {
        selectedItems.remove(chatMessageRealm);
    }

    private void removeHighlights() {
        for (MyViewHolder hold : selectedHolders)
            hold.viewselected.setVisibility(View.GONE);
    }

    public boolean isActionEnabled() {
        return multiSelect;
    }
}
