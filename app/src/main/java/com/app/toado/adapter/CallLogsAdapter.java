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
import com.app.toado.settings.UserSession;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by ghanendra on 02/07/2017.
 */

public class CallLogsAdapter extends RecyclerView.Adapter<CallLogsAdapter.MyViewHolder> {

    private List<CallDetails> phnList;
    private Context contx;
    UserSession us;

    public CallLogsAdapter(List<CallDetails> phnList, Context contx) {
        this.phnList = phnList;
        this.contx = contx;
        us = new UserSession(contx);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView duration;
        public TextView timestamp;
        public TextView type;//outgoing incoming?
        public TextView status;
        public ImageView imgv;
        public RelativeLayout relcont;

        public MyViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.tvcontactname);
            timestamp = (TextView) view.findViewById(R.id.tvcalltimestamp);
            duration = (TextView) view.findViewById(R.id.tvcallduration);
            type = (TextView) view.findViewById(R.id.tvcalltype);
            status = (TextView) view.findViewById(R.id.tvcallstatus);
            imgv = (ImageView) view.findViewById(R.id.imgcontact);
            relcont = (RelativeLayout) view.findViewById(R.id.relcontact);
        }
    }


    @Override
    public CallLogsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_callog, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CallLogsAdapter.MyViewHolder holder, int position) {
        CallDetails phn = phnList.get(position);
        System.out.println(phn.getDuration() + " call logs adapter " + phn.getOtherusrname() + phn.getDuration());
        holder.title.setText(phn.getOtherusrname());
        if (us.getUserKey().matches(phn.getCalleruid())) {
            holder.type.setText("outgoing "+phn.getCalltype()+" call");
        } else {
            holder.type.setText("incoming "+phn.getCalltype()+" call"   );
        }

        holder.timestamp.setText(phn.getTimestamp());
        holder.duration.setText(CallHelper.formatTimeCallLogs(Long.parseLong(phn.getDuration())));
        switch (phn.getStatus()) {
            case "CANCELED":
                statusMeth("Outgoing Cancelled", "Missed Call", phn, holder, R.color.bpDarker_red);
                break;
            case "DENIED":
                statusMeth("Receiver Declined", "Missed Call", phn, holder, R.color.bpDarker_red);
                break;
            case "completed":
                statusMeth("Completed", "Completed", phn, holder, R.color.light_green);
                break;
            case "NO_ANSWER":
                statusMeth("Call Not Answered", "Missed Call", phn, holder, R.color.bpDarker_red);
                break;
        }
        Glide.with(contx).load(phn.getProfpicurl()).error(R.drawable.whatsapplogo).into(holder.imgv);

    }

    @Override
    public int getItemCount() {
        return phnList.size();
    }

    public void updateList(List<CallDetails> mList) {
        phnList = mList;
        notifyDataSetChanged();
    }

    public void statusMeth(String callermsg, String receivermsg, CallDetails phn, MyViewHolder holder, int colr) {
        System.out.println("status meth calllogs  adapter" + callermsg + receivermsg);
        holder.status.setTextColor(colr);
        if (us.getUserKey().matches(phn.getCalleruid())) {
            holder.status.setText(callermsg);
        } else {
            holder.status.setText(receivermsg);
        }
    }

}
