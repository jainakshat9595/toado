package com.app.toado.adapter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.toado.R;
import com.app.toado.activity.chat.ChatActivity;
import com.app.toado.activity.userprofile.UserProfileAct;
import com.app.toado.helper.MobileNumProcess;
import com.app.toado.model.MobileKey;
import com.app.toado.model.PhoneContacts;
import com.app.toado.settings.UserSession;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.List;
import java.util.Locale;

import static com.app.toado.helper.ToadoConfig.DBREF;
import static com.app.toado.helper.ToadoConfig.DBREF_CHATS;
import static com.app.toado.helper.ToadoConfig.DBREF_USERS_CHATS;
import static com.app.toado.helper.ToadoConfig.DBREF_USER_MOBS;

/**
 * Created by ghanendra on 02/07/2017.
 */

public class NewChatAdapter extends RecyclerView.Adapter<NewChatAdapter.MyViewHolder> {

    private List<PhoneContacts> phnList;
    private Context contx;
    UserSession us;

    public NewChatAdapter(List<PhoneContacts> phnList, Context contx) {
        this.phnList = phnList;
        this.contx = contx;
        us = new UserSession(contx);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView number;
        public ImageView imgv;
        public RelativeLayout relcont;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.tvcontactname);
            number = (TextView) view.findViewById(R.id.tvcontactnum);
            imgv = (ImageView) view.findViewById(R.id.imgcontact);
            relcont = (RelativeLayout) view.findViewById(R.id.relcontact);

        }
    }


    @Override
    public NewChatAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_newchat_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final NewChatAdapter.MyViewHolder holder, int position) {
        PhoneContacts phn = phnList.get(position);
        holder.title.setText(phn.getContactname());
        holder.number.setText(phn.getPhnnum().replace(" ", "").trim());
        Glide.with(contx).load(phn.getContactphoto()).error(R.drawable.whatsapplogo).into(holder.imgv);
       /* holder.relcont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phnselect = phnList.get(holder.getLayoutPosition()).getPhnnum();
                String a = phnselect.replace("*", "").replace(" ", "").replace("#", "").replace("-", "").trim();
                String num = "";
                Phonenumber.PhoneNumber ph = MobileNumProcess.processMobNum(a);
                if (ph != null)
                    num = String.valueOf(MobileNumProcess.processMobNum(a).getNationalNumber());
                else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        num = PhoneNumberUtils.formatNumber(a, Locale.getDefault().getISO3Country());
                    }
                }
                System.out.println(num + "item clicked newchatadapter" + DBREF_USER_MOBS.child(num));
                DBREF_USER_MOBS.child(num).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            MobileKey mk = MobileKey.parse(dataSnapshot);
                            System.out.println(mk.getUserkey() + " datasnap newchtadapter " + mk.getUsermob());
                            checkChatref(us.getUserKey(), mk.getUserkey());
                        } else {
                            Toast.makeText(contx, "Invite your friend to chat with him on toado now!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return phnList.size();
    }


    private void checkChatref(final String mykey, final String otheruserkey) {
        DatabaseReference dbChat = DBREF_CHATS.child(mykey + otheruserkey).getRef();
        dbChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("query1" + mykey + otheruserkey);
                System.out.println("datasnap 1" + dataSnapshot.toString());
                if (dataSnapshot.exists()) {
                    System.out.println("datasnap exists1" + dataSnapshot.toString());
                    String dbTablekey = mykey + otheruserkey;
                    goToChatActivity(dbTablekey, otheruserkey);

                } else {
                    checkChatref2(mykey, otheruserkey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void checkChatref2(final String mykey, final String otheruserkey) {
        final DatabaseReference dbChat = DBREF_CHATS.child(otheruserkey + mykey).getRef();
        final String dbTablekey = otheruserkey + mykey;
        dbChat.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    System.out.println("query1" + otheruserkey + mykey);
                    goToChatActivity(dbTablekey, otheruserkey);


                } else {
                    dbChat.child("Blocked").child(mykey).setValue("no");
                    dbChat.child("Blocked").child(otheruserkey).setValue("no");
                    DBREF_USERS_CHATS.child(mykey).child(otheruserkey).setValue(dbTablekey);
                    DBREF_USERS_CHATS.child(otheruserkey).child(mykey).setValue(dbTablekey);
                    goToChatActivity(dbTablekey, otheruserkey);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void goToChatActivity(String dbTablekey, String usrkey) {
        Intent in = new Intent(contx, ChatActivity.class);
        in.putExtra("dbTableKey", dbTablekey);
        in.putExtra("otheruserkey", usrkey);
        contx.startActivity(in);
        ((Activity) contx).finish();
    }

    public void updateList(List<PhoneContacts> mList) {
        phnList = mList;
        notifyDataSetChanged();
    }

}
