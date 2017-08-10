package com.app.toado.model;

import java.security.Key;
import java.util.jar.Attributes;

/**
 * Created by RajK on 19-05-2017.
 */

public class DistanceUser {
   private String Name,Key;
    private float Dist;

    public DistanceUser() {
    }

    public DistanceUser(String key,String name, float m) {
        Key = key;
        Name =name;
        Dist = m;

    }

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }

    public void setDist(float dist) {
        Dist = dist;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public float getDist() {
        return Dist;
    }

    }
