package com.app.toado.fragments.sharedviewpager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


import com.app.toado.fragments.sharedItems.SharedDocuments;
import com.app.toado.fragments.sharedItems.SharedLinks;
import com.app.toado.fragments.sharedItems.SharedMedia;

import java.util.ArrayList;

public class SharedPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<SharedPagerItems> mItems;
    Context mContext;

    private String mOtherUserKey;

    public SharedPagerAdapter(FragmentManager fm1, Context context, String otherUserKey) {
        super(fm1);
        mContext=context;
        mOtherUserKey = otherUserKey;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        Bundle args = new Bundle();
        args.putString("OtherUserKey", mOtherUserKey);
        switch (mItems.get(position).getType()) {
            case Media:
                fragment = SharedMedia.newInstance(args);
                break;
            case Documents:
                fragment = SharedDocuments.newInstance(args);
                break;
            case Links:
                fragment = SharedLinks.newInstance(args);
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    public void setItems(ArrayList<SharedPagerItems> mItems) {
        this.mItems = mItems;
    }

    @Override
    public int getItemPosition(Object object) {
        return mItems.size();
    }

}