package com.app.toado.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.toado.R;
import com.app.toado.helper.CallHelper;
import com.app.toado.model.CallDetails;
import com.app.toado.model.ShowPhotoModel;
import com.app.toado.settings.UserSession;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by ghanendra on 02/07/2017.
 */

public class ShowPhotoAdapter extends RecyclerView.Adapter<ShowPhotoAdapter.MyViewHolder> {

    private List<ShowPhotoModel> phnList;
    private Context contx;
    UserSession us;

    public ShowPhotoAdapter(List<ShowPhotoModel> phnList, Context contx) {
        this.phnList = phnList;
        this.contx = contx;
        us = new UserSession(contx);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ImageView imgv;

        public MyViewHolder(View view) {
            super(view);

            title= (TextView) view.findViewById(R.id.typeComment);
            imgv= (ImageView) view.findViewById(R.id.imgclicked);
        }
    }


    @Override
    public ShowPhotoAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_imagepickmultiple, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ShowPhotoAdapter.MyViewHolder holder, int position) {
        ShowPhotoModel phn = phnList.get(position);

        Glide.with(contx).load(phn.getPath()).into(holder.imgv);

    }

    @Override
    public int getItemCount() {
        return phnList.size();
    }

    public void updateList(List<ShowPhotoModel> mList) {
        phnList = mList;
        notifyDataSetChanged();
    }

}
