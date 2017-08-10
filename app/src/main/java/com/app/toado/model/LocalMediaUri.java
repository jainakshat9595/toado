package com.app.toado.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ghanendra on 26/06/2017.
 */

public class LocalMediaUri   {
     private String id;
    private String uri;

    public LocalMediaUri() {
    }

    public LocalMediaUri(String id, String uri) {
        this.id = id;
        this.uri = uri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
