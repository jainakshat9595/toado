package com.app.toado.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.toado.R;
import com.app.toado.activity.chat.ForwardChatActivity;
import com.app.toado.helper.ChatHelper;
import com.app.toado.helper.CircleTransform;
import com.app.toado.helper.EncryptUtils;
import com.app.toado.model.ChatListModel;
import com.app.toado.model.ChatMessage;
import com.app.toado.model.realm.ActiveChatsRealm;
import com.app.toado.model.realm.ChatMessageRealm;
import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.app.toado.helper.ToadoConfig.DBREF;


public class RecentChatsAdapter extends RecyclerView.Adapter<RecentChatsAdapter.MyViewHolder> {
    ArrayList<ActiveChatsRealm> list = new ArrayList<>();
    private Context context;
    EncryptUtils encryptUtils = new EncryptUtils();
    String TAG = "CHATLISTADAPTER";
    String mykey;
    private boolean multiSelect = false;
    private ArrayList<ActiveChatsRealm> selectedItems=new ArrayList<>();

    public RecentChatsAdapter(ArrayList<ActiveChatsRealm> list, Context context, String mykey) {
        this.context = context;
        this.list = list;
        this.mykey = mykey;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView author, status, viewselected;
        ImageView imgselected;
        ImageView imgProfile;
        RelativeLayout messageContainer;

        public MyViewHolder(View itemView) {
            super(itemView);
            author = (TextView) itemView.findViewById(R.id.author);
            status = (TextView) itemView.findViewById(R.id.status);
            viewselected = (TextView) itemView.findViewById(R.id.viewselected);
            imgselected = (ImageView) itemView.findViewById(R.id.selectedmsg);
            imgProfile = (ImageView) itemView.findViewById(R.id.icon_profile);
            messageContainer = (RelativeLayout) itemView.findViewById(R.id.message_container);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_recent_chatlist, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final ActiveChatsRealm topic = list.get(position);
        holder.author.setText(topic.getName());
        Log.d(TAG, "mykey chatlistadapter" + topic.getName());

        holder.messageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otherkey = topic.getChatref().replace(mykey, "");
                Log.d(TAG, mykey + "chatadaptersending othreuserkey" + topic.getChatref() + " " + otherkey);
                ChatHelper.goToChatActivity(context, otherkey, topic.getName(), topic.getProfpic());
            }
        });
        applyProfilePicture(holder, topic);

        holder.messageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(holder, topic);
            }
        });
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

    void selectItem(MyViewHolder holder, ActiveChatsRealm item) {
        if (selectedItems.contains(item)) {
            holder.viewselected.setVisibility(View.GONE);
            holder.imgselected.setVisibility(View.GONE);
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
            int hei = holder.messageContainer.getHeight();
            Log.d(TAG, "height selected item " + hei);
            holder.imgselected.setVisibility(View.VISIBLE);
            holder.viewselected.setVisibility(View.VISIBLE);
            holder.viewselected.getLayoutParams().height = hei;
            holder.viewselected.requestLayout();
        }


        ArrayList<String> arr = new ArrayList<>();
        if (selectedItems.size() > 0) {
            for (ActiveChatsRealm ac : selectedItems)
                arr.add(ac.getName());
            ((ForwardChatActivity) context).showSelectedNames(arr, 1,selectedItems);
        } else {
            ((ForwardChatActivity) context).showSelectedNames(arr, 0,selectedItems);
        }
    }
}
