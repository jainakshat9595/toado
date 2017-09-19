package com.app.toado.fragments.sharedviewpager;

/**
 * Created by ghanendra on 14/06/2017.
 */

public class SharedPagerItems {

    private String title;
    private String tabicon;

    private PAGE_TYPE type;


    public enum PAGE_TYPE {
        Media, Documents, Links
    }

    public SharedPagerItems(PAGE_TYPE type, String title) {
        this.title = title;
        this.type = type;
    }

    public String getTabicon() {
        return tabicon;
    }

    public void setTabicon(String tabicon) {
        this.tabicon = tabicon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PAGE_TYPE getType() {
        return type;
    }

    public void setType(PAGE_TYPE type) {
        this.type = type;
    }
}
