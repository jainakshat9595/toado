package com.app.toado.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.toado.BuildConfig;
import com.app.toado.R;
import com.app.toado.activity.chat.ForwardChatActivity;
import com.app.toado.activity.chat.MediaPreviewActivity;
import com.app.toado.helper.ChatHelper;
import com.app.toado.helper.CircleTransform;
import com.app.toado.helper.OpenFile;
import com.app.toado.model.ChatMessage;
import com.app.toado.model.ChatMessageForward;
import com.app.toado.model.User;
import com.app.toado.model.realm.ChatMessageRealm;
import com.app.toado.services.UploadFileService;
import com.app.toado.settings.UserSession;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderAdapter;
import jp.wasabeef.glide.transformations.BlurTransformation;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static com.app.toado.helper.ToadoConfig.DBREF;
import static com.app.toado.helper.ToadoConfig.DBREF_USER_PROFILES;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_FAILED;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_PROGRESS;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_STARTING;
import static com.app.toado.services.UploadFileService.MEDIA_DOWNLOAD_SUCCESS;
import static com.app.toado.services.UploadFileService.MEDIA_PROGRESSING;
import static com.app.toado.services.UploadFileService.MEDIA_STARTING;
import static com.app.toado.services.UploadFileService.MEDIA_SUCCESS;

public class StarredMessageAdapter extends RecyclerView.Adapter<StarredMessageAdapter.MyViewHolder> implements StickyHeaderAdapter<ChatHeaderViewHolder> {
    ArrayList<ChatMessageRealm> mList = new ArrayList<>();
    private Activity context;
    private UserSession session;
    public static final int SENDER = 0;
    public static final int RECIPIENT = 1;
    String TAG = "starmessageadapter";
    String mykey;
    String todaytimestamp;
    String searchtext;

    public StarredMessageAdapter(ArrayList<ChatMessageRealm> list, Activity context, String mykey, String todaytimestamp, String searchtext) {
        this.mList = list;
        this.context = context;
        session = new UserSession(context);
        this.mykey = mykey;
        this.todaytimestamp = todaytimestamp;
        this.searchtext = searchtext;
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
        View viewSender = inflater.inflate(R.layout.row_starred_messages, viewGroup, false);
        viewHolder = new MyViewHolder(viewSender);
        return (MyViewHolder) viewHolder;
    }

    @Override
    public void onBindViewHolder(final StarredMessageAdapter.MyViewHolder holder, final int position) {
        final ChatMessageRealm comment = mList.get(position);

        Log.d(TAG, "star msg adapter" + comment.getMsgstring());

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

        setStatus(comment.getMsgstatus(), holder);

        DBREF_USER_PROFILES.child(comment.getChatref().replace(mykey, "")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = User.parse(dataSnapshot);
                Glide.with(context).load(u.getProfpicurl()).transform(new CircleTransform(context)).into(holder.profpic);
                if (holder.getItemViewType() == SENDER) {
                    holder.participants.setText("You > " + u.getName());
                } else {
                    holder.participants.setText(u.getName() + " > You");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        switch (comment.getMsgtype()) {
            case "text":
                String decryptedmsg = comment.getMsgstring();
                if (searchtext.matches(""))
                    holder.commentString.setText(decryptedmsg);
                else {
                    //color search text
                    if (searchtext.length() > 0) {
                        //color your text here
                        int startPos = decryptedmsg.toLowerCase(Locale.US).indexOf(searchtext.toLowerCase(Locale.US));
                        int endPos = startPos + searchtext.length();

                        if (startPos != -1) {
                            Spannable spannable = new SpannableString(decryptedmsg);
                            ColorStateList blueColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.BLUE});
                            TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1,blueColor, null);
                            spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            holder.commentString.setText(spannable);
                        }
                    }
                }
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
                    Glide.with(context).load(comment.getMsglocalurl()).into(holder.imgchat);
                    if (comment.getMsgweburl().matches("")) {
                    }
                } else if (comment.getMsglocalurl().matches("")) {
                    Glide.with(context).load(comment.getMediathumbnail()).bitmapTransform(new BlurTransformation(context)).dontAnimate().into(holder.imgchat);
                } else if (!comment.getMsglocalurl().matches("")) {
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
                    if (comment.getMsgweburl().matches("")) {
                    }
                    Glide.with(context).load(comment.getMediathumbnail()).dontAnimate().into(holder.imgchat);
                } else if (comment.getMsglocalurl().matches("")) {
                    Glide.with(context).load(comment.getMediathumbnail()).bitmapTransform(new BlurTransformation(context)).dontAnimate().into(holder.imgchat);
                } else if (!comment.getMsglocalurl().matches("")) {
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
                    if (comment.getMsgweburl().matches("")) {
                    }
                    Glide.with(context).load(R.drawable.docsimage).into(holder.imgchat);
                } else if (comment.getMsglocalurl().matches("")) {
                } else if (!comment.getMsglocalurl().matches("")) {
                }
                break;
            case "location":
                holder.imgchatrel.setVisibility(View.VISIBLE);
                Glide.clear(holder.imgchat);
                holder.commentString.setText(comment.getMsgstring());
                holder.imgchat.setVisibility(View.VISIBLE);
                if (getItemViewType(position) == SENDER) {
                    if (comment.getMsgweburl().matches("")) {
                    }
                    Glide.with(context).load(comment.getMsglocalurl()).into(holder.imgchat);
                } else if (comment.getMsglocalurl().matches("")) {
                    Glide.with(context).load(comment.getMediathumbnail()).bitmapTransform(new BlurTransformation(context)).dontAnimate().into(holder.imgchat);

                } else if (!comment.getMsglocalurl().matches("")) {
                    Glide.with(context).load(comment.getMsglocalurl()).into(holder.imgchat);
                }
                break;
        }


        holder.imgchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String path = mList.get(holder.getAdapterPosition()).getMsglocalurl();
                Log.d(TAG, "path imagchat holder" + path);

                if (path == null || path.matches("")) {

                } else {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
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
                        in.putExtra("sender", holder.participants.getText().toString().replace("You", "").replace(">", "").replace(" ", "").trim());

                    in.putExtra("timestamp", mList.get(position).getSenderdate() + ", " + mList.get(position).getSendertime());
                    in.putExtra("caption", mList.get(position).getMsgstring());
                    in.putExtra("imagepath", mList.get(position).getMsglocalurl());
                    in.putExtra("mediatype", type);
                    context.startActivity(in);

                }

            }
        });
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

    public void updateList(ArrayList<ChatMessageRealm> list, String searchtext) {
        this.mList = list;
        this.searchtext = searchtext;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView sender_Timestamp, commentString, timestampnotext, participants, dateofmsg;
        public ImageView imgchat;
        public ImageView imgstatus;
        public ImageView imgstatus2;
        public ImageView iconstar1;
        public ImageView iconstar2;
        public ImageView profpic;
        public CoordinatorLayout imgchatrel;
        public LinearLayout msglay;
        public RelativeLayout msgbackground;
        public LinearLayout layonimage;

        public MyViewHolder(View itemView) {
            super(itemView);

            participants = (TextView) itemView.findViewById(R.id.tvparticipants);
            dateofmsg = (TextView) itemView.findViewById(R.id.tvdate);
            sender_Timestamp = (TextView) itemView.findViewById(R.id.meSender_TimeStamp);
            timestampnotext = (TextView) itemView.findViewById(R.id.timestampnotext);
            commentString = (TextView) itemView.findViewById(R.id.commentString);
            profpic = (ImageView) itemView.findViewById(R.id.icon_profile);
            imgchat = (ImageView) itemView.findViewById(R.id.imgchat);
            iconstar1 = (ImageView) itemView.findViewById(R.id.iconstar);
            iconstar2 = (ImageView) itemView.findViewById(R.id.iconstar2);
            imgchat = (ImageView) itemView.findViewById(R.id.imgchat);
            imgstatus = (ImageView) itemView.findViewById(R.id.status);
            imgstatus2 = (ImageView) itemView.findViewById(R.id.status2);
            imgchatrel = (CoordinatorLayout) itemView.findViewById(R.id.imgchatrel);
            msglay = (LinearLayout) itemView.findViewById(R.id.msglay);
            msgbackground = (RelativeLayout) itemView.findViewById(R.id.msgbackground);
            layonimage = (LinearLayout) itemView.findViewById(R.id.layonimage);
        }
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


}
