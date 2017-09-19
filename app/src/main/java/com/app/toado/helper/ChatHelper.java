package com.app.toado.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.app.toado.R;
import com.app.toado.activity.chat.ChatActivity;
import com.app.toado.model.ChatMessage;
import com.app.toado.model.User;
import com.app.toado.model.realm.ActiveChatsRealm;
import com.app.toado.model.realm.ChatMessageRealm;
import com.app.toado.model.realm.UploadTable;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static com.app.toado.helper.ToadoConfig.DBREF_USER_PROFILES;
import static com.app.toado.services.UploadFileService.MEDIA_QUEUED;

/**
 * Created by ghanendra on 09/07/2017.
 */

public class ChatHelper {
    private static final String TAG = "CHatHELPER";
    private static String otherusername = "";
    private static String otheruserprofpic = "";

    private static ChatMessageRealm maingson;
    private  static String mainothr;

    public static void goToChatActivity(Context contx, String otheruserkey, String otherusername, String profpic) {
        Intent in = new Intent(contx, ChatActivity.class);
        in.putExtra("otheruserkey", otheruserkey);
        in.putExtra("otherusername", otherusername);
        in.putExtra("profpic", profpic);
        Log.d(TAG, "oth " + otheruserkey + " othna " + otherusername + " profpic " + profpic);
        contx.startActivity(in);
    }

    public static org.jivesoftware.smack.chat.Chat createChat(AbstractXMPPConnection connection, Activity context, String receiver, ChatMessageListener mMessageListener) {
        org.jivesoftware.smack.chat.Chat mychat = ChatManager.getInstanceFor(connection).createChat(
                receiver + "@"
                        + context.getString(R.string.server_host),
                mMessageListener);
        return mychat;
    }


    public static Message getMessageBody(ChatMessageRealm chatMessage, String mykey, Activity context) {
        final Message message = new Message();
        Gson gson = new Gson();
        String jsonString = gson.toJson(chatMessage);
        String msgbody;
        try {
            JSONObject request = new JSONObject(jsonString);
            Log.d(TAG, chatMessage.getMsgid() + "   json string sendMessage ChatHelper    " + request.toString());
            msgbody = request.toString();
            message.setBody(msgbody);
            message.setType(Message.Type.normal);
            message.setStanzaId(chatMessage.getMsgid());
            if (!chatMessage.getMsgtype().matches("status") && chatMessage.getMsgtype().matches("text"))
                addChatMsgRealm(message, mykey, context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }

//    public static void addNewChatList(Realm realm, final String otherusername, final String otheruserkey, final String profilepic) {
//        realm.executeTransactionAsync(new Realm.Transaction() {
//            @Override
//            public void execute(Realm bgRealm) {
//                bgRealm.insertOrUpdate(ach);
//            }
//        }, new Realm.Transaction.OnSuccess() {
//            @Override
//            public void onSuccess() {
//                System.out.println("new active chat success chathelper");
//            }
//        }, new Realm.Transaction.OnError() {
//            @Override
//            public void onError(Throwable error) {
//                System.out.println("new active chat failed");
//                // Transaction failed and was automatically canceled.
//            }
//        });
//    }

    public static void addChatMsgRealm(final Message message, final String mykey, final Activity context) {
        Log.d(TAG, message.getStanzaId() + "message body chathlerpr " + message.getBody());

        final Realm re = Realm.getDefaultInstance();
        re.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                try {
                    ChatMessageRealm chat = bgRealm.createObject(ChatMessageRealm.class, message.getStanzaId());
                    String js = HtmlManipulator.replaceHtmlEntities(message.getBody());
                    System.out.println("message body replace html entities" + js);
                    Gson gson = new Gson();
                    ChatMessageRealm gsonchat = null;
                    try {
                        gsonchat = gson.fromJson(js, ChatMessageRealm.class);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String othr = "";
                    maingson = gsonchat;
                    if (!mykey.matches(gsonchat.getSenderjid())) {
                        Log.d(TAG, mykey + "  " + gsonchat.getOtherjid() + " gson chat chatherlper1 " + gsonchat.getSenderjid());
                        chat.setChatref(mykey + gsonchat.getSenderjid());
                        othr = gsonchat.getSenderjid();
                    } else {
                        Log.d(TAG, mykey + "  " + gsonchat.getOtherjid() + " gson chat chatherlper2 " + gsonchat.getSenderjid());
                        chat.setChatref(mykey + gsonchat.getOtherjid());
                        othr = gsonchat.getOtherjid();
                    }

                    mainothr = othr;
                    Log.d(TAG, gsonchat.getMsgtype() + "othr chathelper " + gsonchat.getMsgstring());

                    if (!gsonchat.getMsgtype().matches("status")) {
                        if (gsonchat.getMsgstring() != null)
                            chat.setMsgstring(gsonchat.getMsgstring());
                        if (gsonchat.getMsglocalurl() != null)
                            chat.setMsglocalurl(gsonchat.getMsglocalurl());
                        if (gsonchat.getSenderdate() != null)
                            chat.setSenderdate(gsonchat.getSenderdate());
                        if (gsonchat.getSendertime() != null)
                            chat.setSendertime(gsonchat.getSendertime());
                        if (gsonchat.getMsgstatus() != null)
                            chat.setMsgstatus(gsonchat.getMsgstatus());
                        if (gsonchat.getMsgtype() != null)
                            chat.setMsgtype(gsonchat.getMsgtype());
                        if (gsonchat.getSenderjid() != null)
                            chat.setSenderjid(gsonchat.getSenderjid());
                        if (gsonchat.getOtherjid() != null)
                            chat.setOtherjid(gsonchat.getOtherjid());
                        if (gsonchat.getMsgweburl() != null)
                            chat.setMsgweburl(gsonchat.getMsgweburl());
                        if (gsonchat.getMediathumbnail() != null)
                            chat.setMediathumbnail(gsonchat.getMediathumbnail());
                        bgRealm.insertOrUpdate(chat);
                        checkActiveChats(mykey, othr, gsonchat.getMsgid());

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                System.out.println(message.getBody().contains("\"msgstatus\":\"3\"") + "new message stored realm success chathelper" + message.getStanzaId() + " " + message.getBody());

                final ActiveChatsRealm ac = new ActiveChatsRealm(otheruserprofpic, maingson.getMsgid(), otherusername, maingson.getMsgstring(), maingson.getMsgstatus(), maingson.getSendertime(), false, mainothr, mykey + mainothr, false);

                System.out.println("Akshat");
                re.close();
                final Realm re1 = Realm.getDefaultInstance();
                /*ActiveChatsRealm aa = new ActiveChatsRealm();
                //ActiveChatsRealm aa = new Activrealm.createObject(ActiveChatsRealm.class, ac.getChatref());
                aa.setChatref(ac.getChatref());
                aa.setArchived(ac.getArchived());
                aa.setLastmsgbody(ac.getLastmsgbody());
                aa.setLastmsgtime(ac.getLastmsgtime());
                aa.setLastmsgtype(ac.getLastmsgtype());
                aa.setOtherkey(ac.getOtherkey());
                aa.setMsgid(ac.getMsgid());
                aa.setProfpic(ac.getProfpic());
                aa.setName(ac.getName());
                aa.setPinned(ac.getPinned());*/
                re1.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {

                            Log.d(TAG, " active chats add to realm " + ac.getChatref() + " " + ac.getName() + " " + ac.getOtherkey());
                            realm.copyToRealmOrUpdate(ac);
                            //realm.insert(aa);
                        } catch (Exception e) {
                            ActiveChatsRealm acr = realm.createObject(ActiveChatsRealm.class,ac.getChatref());
                            realm.copyToRealmOrUpdate(acr);
                            e.printStackTrace();
                            re1.close();
                        }
                    }
                });

                if (message.getBody().contains("\"msgstatus\":\"3\""))
                    addMessageRead(context, message.getStanzaId());
                else
                    context.sendBroadcast(new Intent().putExtra("reloadchat", "yes").setAction("reloadchataction"));

                re1.close();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                // Transaction failed and was automatically canceled.
                System.out.println(message.getBody().contains("\"msgstatus\":\"3\"") + "  new message stored realm failed chathelper    " + message.getStanzaId() + " " + message.getBody());

                if (message.getBody().contains("\"msgstatus\":\"3\""))
                    addMessageRead(context, message.getStanzaId());

                error.printStackTrace();
                re.close();
            }
        });
    }


    public static void addChatMesgRealmMedia1(final ChatMessageRealm chatm, final Context context, final String mykey, final String otheruserkey) {
        checkActiveChats(mykey, otheruserkey, chatm.getMsgid());
        final Realm re = Realm.getDefaultInstance();
        re.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                try {
                    Log.d(TAG, " addchatmmesgrealmedia " + chatm.getMsgid() + chatm.getMsglocalurl() + chatm.getMsgstring());
                    bgRealm.copyToRealmOrUpdate(chatm);
                } catch (Exception e) {
                    ChatMessageRealm chat = bgRealm.createObject(ChatMessageRealm.class, chatm.getMsgid());
                    bgRealm.copyToRealmOrUpdate(chat);
                    e.printStackTrace();
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "new media stored realm success chathelper");
                re.close();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                // Transaction failed and was automatically canceled.
                error.printStackTrace();
                Log.d(TAG, "new message stored realm failed chathelper");
                re.close();
            }
        });
    }

    public static void addMessageRead(final Context contx, final String msgid) {
        try {
            final Realm re = Realm.getDefaultInstance();
            final RealmResults<ChatMessageRealm> result2 = re.where(ChatMessageRealm.class).equalTo("msgid", msgid).findAll();
            Log.d(TAG, result2.get(0).getMsgstatus() + "sending broadcast for msg read " + result2.get(0).getMsgstring());

            if (!result2.get(0).getMsgstatus().matches("3")) {
                re.beginTransaction();
                ChatMessageRealm cm = new ChatMessageRealm(result2.get(0).getChatref(), result2.get(0).getOtherjid(), result2.get(0).getMsgstring(), result2.get(0).getSenderjid(), result2.get(0).getSendertime(), result2.get(0).getSenderdate(), result2.get(0).getMsgtype(), result2.get(0).getMsgid(), "3", result2.get(0).getMsgweburl(), result2.get(0).getMsglocalurl(), result2.get(0).getMediathumbnail());
                re.insertOrUpdate(cm);
                re.commitTransaction();
            }
            re.close();
            contx.sendBroadcast(new Intent().putExtra("readstatus", msgid).setAction("reloadchataction"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkActiveChats(final String mykey, final String otheruserkey, final String msgid) {
        Log.d(TAG, mykey + otheruserkey + " check activechats shows size1 ");

        if ((mykey + otheruserkey).matches(mykey + mykey))
            return;

        DBREF_USER_PROFILES.child(otheruserkey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User usr = User.parse(dataSnapshot);
                Log.d(TAG, "data snap " + dataSnapshot.toString());
                otherusername = usr.getName();
                otheruserprofpic = usr.getProfpicurl();

                final ActiveChatsRealm ac = new ActiveChatsRealm(otheruserprofpic, msgid, otherusername, "nil", "nil", "nil", false, otheruserkey, mykey + otheruserkey, false);
                final Realm re2 = Realm.getDefaultInstance();
                re2.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm bgRealm) {
                        try {
                            ActiveChatsRealm aa = bgRealm.createObject(ActiveChatsRealm.class, ac.getChatref());
                            aa.setArchived(ac.getArchived());
                            aa.setLastmsgbody(ac.getLastmsgbody());
                            aa.setLastmsgtime(ac.getLastmsgtime());
                            aa.setLastmsgtype(ac.getLastmsgtype());
                            aa.setOtherkey(ac.getOtherkey());
                            aa.setMsgid(ac.getMsgid());
                            aa.setProfpic(ac.getProfpic());
                            aa.setName(ac.getName());
                            aa.setPinned(ac.getPinned());
                            Log.d(TAG, " active chats add to realm " + aa.getChatref() + " " + aa.getName() + " " + ac.getOtherkey());
                            bgRealm.insert(aa);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "new active chat ref add success");
                        re2.close();
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        // Transaction failed and was automatically canceled.
                        error.printStackTrace();
                        Log.d(TAG, "new active chat ref error failed  ");
                        re2.close();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void deleteItems(ChatMessageRealm cm) {
        Realm realm = Realm.getDefaultInstance();

        final RealmResults<ChatMessageRealm> results = realm.where(ChatMessageRealm.class).equalTo("msgid", cm.getMsgid()).findAll();

        // All changes to data must happen in a transaction
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                results.deleteAllFromRealm();
            }
        });

        realm.close();
    }

    public static void queueUpload(final String msg, final String filePath, final String type, final String mykey, final String otheruserkey, final String sender,final String uploadstatus) {
        final Realm re = Realm.getDefaultInstance();
        re.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                UploadTable ut = new UploadTable(msg,String.valueOf(GetTimeStamp.Id()), type, filePath, mykey, otheruserkey, sender, uploadstatus);
                realm.insertOrUpdate(ut);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "realm succcess upload added");
                re.close();
            }
        });
    }

    public static void starMessage(final ChatMessageRealm cm) {
        Realm realm = Realm.getDefaultInstance();
        // All changes to data must happen in a transaction
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(cm);
            }
        });
        realm.close();
    }
}
