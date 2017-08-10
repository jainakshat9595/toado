package com.app.toado.model;

/**
 * Created by ghanendra on 02/07/2017.
 */

public class PhoneContacts {
    String phnnum,contactname,contactphoto,toadouser;

    public PhoneContacts(String phnnum, String contactname, String contactphoto, String toadouser) {
        this.phnnum = phnnum;
        this.contactname = contactname;
        this.contactphoto = contactphoto;
        this.toadouser = toadouser;
    }

    public String getPhnnum() {
        return phnnum;
    }

    public void setPhnnum(String phnnum) {
        this.phnnum = phnnum;
    }

    public String getContactname() {
        return contactname;
    }

    public void setContactname(String contactname) {
        this.contactname = contactname;
    }

    public String getContactphoto() {
        return contactphoto;
    }

    public void setContactphoto(String contactphoto) {
        this.contactphoto = contactphoto;
    }

    public String getToadouser() {
        return toadouser;
    }

    public void setToadouser(String toadouser) {
        this.toadouser = toadouser;
    }
}
