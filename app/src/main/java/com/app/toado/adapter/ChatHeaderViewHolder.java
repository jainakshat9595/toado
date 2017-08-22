package com.app.toado.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.app.toado.R;

public class ChatHeaderViewHolder extends RecyclerView.ViewHolder {
    public final TextView mPostDate;

    public ChatHeaderViewHolder(View itemView) {
        super(itemView);
        mPostDate = (TextView) itemView.findViewById(R.id.post_date);
    }
}
