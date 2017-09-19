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


public class SharedMediaAdapter extends RecyclerView.Adapter<SharedMediaAdapter.MyViewHolder> {

    private final Context context;
    private final ArrayList<ChatMessageRealm> list;

    public SharedMediaAdapter(ArrayList<ChatMessageRealm> list, Context context) {
        this.context = context;
        this.list = list;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile;

        public MyViewHolder(View itemView) {
            super(itemView);
            imgProfile = (ImageView) itemView.findViewById(R.id.shared_media_row_image);
        }

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_shared_media, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        System.out.println();
        Glide.with(context).load(list.get(position).getMsgweburl()).dontAnimate()
                .transform(new CircleTransform(context))
                .into(holder.imgProfile);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
