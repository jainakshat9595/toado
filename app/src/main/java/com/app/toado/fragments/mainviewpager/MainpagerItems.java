package com.app.toado.fragments.mainviewpager;

/**
 * Created by ghanendra on 14/06/2017.
 */

public class MainpagerItems {
//    private String title;
    private PAGE_TYPE type;
    private String tabicon;

    public enum PAGE_TYPE {
        Home,Chat,GroupChats,Calls
    }

    public String getTabicon() {
        return tabicon;
    }

    public MainpagerItems( PAGE_TYPE type) {
//        this.title = title;
        this.type = type;
    }

    public void setTabicon(String tabicon) {
        this.tabicon = tabicon;
    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }

    public PAGE_TYPE getType() {
        return type;
    }

    public void setType(PAGE_TYPE type) {
        this.type = type;
    }
}
