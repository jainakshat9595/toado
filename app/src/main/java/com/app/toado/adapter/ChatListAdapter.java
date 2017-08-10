package com.app.toado.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.toado.R;
import com.app.toado.activity.userprofile.UserProfileAct;
import com.app.toado.helper.ChatHelper;
import com.app.toado.helper.CircleTransform;
import com.app.toado.helper.EncryptUtils;
import com.app.toado.model.ChatListModel;
import com.app.toado.model.ChatMessage;
import com.app.toado.model.realm.ActiveChatsRealm;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.app.toado.helper.ToadoConfig.DBREF;


public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MyViewHolder> {
    ArrayList<ActiveChatsRealm> list = new ArrayList<>();
    private Context context;
    EncryptUtils encryptUtils = new EncryptUtils();
    String TAG = "CHATLISTADAPTER";
    String mykey;
    public ChatListAdapter(ArrayList<ActiveChatsRealm> list, Context context,String mykey) {
        this.context = context;
        this.list = list;
        this.mykey=mykey;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView author, message, timestamp, tvunread;
        ImageView imgProfile;
        RelativeLayout messageContainer;

        public MyViewHolder(View itemView) {
            super(itemView);
            author = (TextView) itemView.findViewById(R.id.author);
            message = (TextView) itemView.findViewById(R.id.message);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            tvunread = (TextView) itemView.findViewById(R.id.unreadmsgs);
            imgProfile = (ImageView) itemView.findViewById(R.id.icon_profile);
            messageContainer = (RelativeLayout) itemView.findViewById(R.id.message_container);
        }

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatlist1, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final ActiveChatsRealm topic = list.get(position);
        holder.author.setText(topic.getName());
        Log.d(TAG,"mykey chatlistadapter"+topic.getName());

        holder.messageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otherkey = topic.getChatref().replace(mykey,"");
                Log.d(TAG,mykey+"chatadaptersending othreuserkey"+topic.getChatref()+ " "+otherkey);
                ChatHelper.goToChatActivity(context,otherkey,topic.getName(),topic.getProfpic());
            }
        });
        applyProfilePicture(holder, topic);
//        applyLastMessage(holder, topic);
//        findunreadmsgs(holder, topic);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    private void applyProfilePicture(MyViewHolder holder, ActiveChatsRealm message) {
        Glide.with(context).load(message.getProfpic()).dontAnimate()
                .transform(new CircleTransform(context))
                .into(holder.imgProfile);
    }

    private void applyLastMessage(final MyViewHolder holder, ChatListModel topic) {
        DatabaseReference dbTopicLastComment = DBREF.child("Chats").child(topic.getDbTableKey()).child("ChatMessages").getRef();
        dbTopicLastComment.limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    if (chatMessage.getCommentString() != null) {
                        if (!chatMessage.getType().equals("text")) {
                            String encryptedmsg = encryptUtils.decrypt(chatMessage.getCommentString(), chatMessage.getSendertimestamp());
                            holder.message.setText(encryptedmsg);

                        } else
                            holder.message.setText(chatMessage.getCommentString());
                    }
                    holder.timestamp.setText(chatMessage.getSendertimestamp());
                }

            }


            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    if (chatMessage.getCommentString() != null) {
                        if (!chatMessage.getType().equals("text")) {
                            String encryptedmsg = encryptUtils.decrypt(chatMessage.getCommentString(), chatMessage.getSendertimestamp());
                            holder.message.setText(encryptedmsg);

                        } else
                            holder.message.setText(chatMessage.getCommentString());
                    }
                    holder.timestamp.setText(chatMessage.getSendertimestamp());
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void findunreadmsgs(final MyViewHolder holder, final ChatListModel topic) {
        String a = "nil";
        DatabaseReference dbTopicLastComment = DBREF.child("Chats").child(topic.getDbTableKey()).child("ChatMessages").getRef();
        System.out.println(topic.getDbTableKey() + " unreadmsgs called " + dbTopicLastComment);

        dbTopicLastComment.orderByChild("status").equalTo("2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    holder.tvunread.setVisibility(View.VISIBLE);

                    System.out.println(dataSnapshot.getChildrenCount() + " unreadmsgs " + dataSnapshot.getValue());
                    holder.tvunread.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                } else {
                    holder.tvunread.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
