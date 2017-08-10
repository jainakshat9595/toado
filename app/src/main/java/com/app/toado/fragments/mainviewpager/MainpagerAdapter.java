package com.app.toado.fragments.mainviewpager;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.app.toado.fragments.call.CallFragment;
import com.app.toado.fragments.chat.ChatFragment;
import com.app.toado.fragments.groupchat.GroupchatFragment;
import com.app.toado.fragments.home.HomeFragment;

import java.util.ArrayList;

public class MainpagerAdapter extends FragmentStatePagerAdapter {
    private ArrayList<MainpagerItems> mItems;
    Context mContext;

    public MainpagerAdapter(FragmentManager fm1, Context context) {
        super(fm1);
        mContext=context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        switch (mItems.get(position).getType()){
            case Home:
                fragment = HomeFragment.newInstance();
                break;
            case Chat:
                fragment = ChatFragment.newInstance();
                break;
            case GroupChats:
                fragment = GroupchatFragment.newInstance();
                break;
            case Calls:
                fragment = CallFragment.newInstance();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    public void setItems(ArrayList<MainpagerItems> mItems) {
        this.mItems = mItems;
    }

    @Override
    public int getItemPosition(Object object) {
        // POSITION_NONE makes it possible to reload the PagerAdapter
//        return POSITION_NONE;
        return mItems.size();
    }

}