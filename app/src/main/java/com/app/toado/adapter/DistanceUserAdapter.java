package com.app.toado.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.toado.R;
import com.app.toado.settings.UserSettingsSharedPref;
import com.app.toado.activity.userprofile.UserProfileAct;
import com.app.toado.model.DistanceUser;

import java.util.ArrayList;


public class DistanceUserAdapter extends RecyclerView.Adapter<DistanceUserAdapter.MyViewHolder> {
    ArrayList<DistanceUser> list = new ArrayList<>();
    private Context context;
    private UserSettingsSharedPref userSettingsSharedPref;

    public DistanceUserAdapter(ArrayList<DistanceUser> list, Context context) {
        this.list = list;
        userSettingsSharedPref = new UserSettingsSharedPref(context);
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, distance;
        LinearLayout lay;
        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            distance = (TextView) itemView.findViewById(R.id.distance);
            lay=(LinearLayout) itemView.findViewById(R.id.distlay);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_distance_user_list, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DistanceUserAdapter.MyViewHolder holder, final int position) {
        DistanceUser distanceUser = list.get(position);
        holder.name.setText(distanceUser.getName());
        holder.distance.setText(distanceUser.getDist() + " " + userSettingsSharedPref.getisDistancePrefSet() + " away from you");
//        System.out.println(distanceUser.getName() + "holder vals" + distanceUser.getKey());
        holder.distance.setText(distanceUser.getDist() + " " + userSettingsSharedPref.getUnit() + " away from you");
        holder.lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getLayoutPosition();
                System.out.println(list.size() + " posclicked " + pos + "distance user adapter clicked item");
                Intent in = new Intent(context, UserProfileAct.class);
                in.putExtra("profiletype", "otherprofile");
                in.putExtra("keyval", list.get(pos).getKey());
                System.out.println("distance");
                in.putExtra("distance", String.valueOf(list.get(pos).getDist()));
                context.startActivity(in);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


}
