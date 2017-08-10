package com.app.toado.helper;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by ghanendra on 11/06/2017.
 */

public class ToadoConfig {
     public static final String SINCH_APPLICATION_KEY = "30805608-153d-43e1-8e5b-240b1cc185a5";
     public static final String SINCH_SECRET_KEY = "sA3Z003lpEKzMegsQi5sFQ==";
     public static final DatabaseReference DBREF = FirebaseDatabase.getInstance().getReference();
     public static final DatabaseReference DBREF_CHATS = FirebaseDatabase.getInstance().getReference().child("Chats");
     public static final DatabaseReference DBREF_CALLS = FirebaseDatabase.getInstance().getReference().child("VoiceCalls");
     public static final DatabaseReference DBREF_USERS_CHATS = FirebaseDatabase.getInstance().getReference().child("Users").child("Userchats");
     public static final DatabaseReference DBREF_FCM = FirebaseDatabase.getInstance().getReference().child("Fcmtokens");
     public static final DatabaseReference DBREF_USER_MOBS = FirebaseDatabase.getInstance().getReference().child("Users").child("Usermobs");
     public static final DatabaseReference DBREF_USER_PICS = FirebaseDatabase.getInstance().getReference().child("Users").child("Userpics");
     public static final DatabaseReference DBREF_USER_SESSIONS = FirebaseDatabase.getInstance().getReference().child("Users").child("Usersession");
     public static final DatabaseReference DBREF_USER_PROFILES = FirebaseDatabase.getInstance().getReference().child("Users").child("Userprofiles");
     public static final DatabaseReference DBREF_USER_LOC = FirebaseDatabase.getInstance().getReference().child("Users").child("Userloc");
     public static final StorageReference STORAGE_REFERENCE = FirebaseStorage.getInstance().getReference();
     public static String STARTFOREGROUND_ACTION = "com.app.toado.services.SinchCallService.action.startforeground";
     public static String STOPFOREGROUND_ACTION = "com.app.toado.services.SinchCallService.action.stopforeground";

}
