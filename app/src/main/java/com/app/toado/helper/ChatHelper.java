package com.app.toado.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.app.toado.R;
import com.app.toado.activity.chat.ChatActivity;
import com.app.toado.model.User;
import com.app.toado.model.realm.ActiveChatsRealm;
import com.app.toado.model.realm.ChatMessageRealm;
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

import io.realm.Realm;
import io.realm.RealmResults;

import static com.app.toado.helper.ToadoConfig.DBREF_USER_PROFILES;

/**
 * Created by ghanendra on 09/07/2017.
 */

public class ChatHelper {
    private static final String TAG = "CHatHELPER";
    private static String otherusername = "", otheruserimage = "";

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


    public static Message getMessageBody(ChatMessageRealm chatMessage, String mykey, Context context) {
        final Message message = new Message();
        Gson gson = new Gson();
        String jsonString = gson.toJson(chatMessage);
        String msgbody;
        try {
            JSONObject request = new JSONObject(jsonString);
            Log.d(TAG, chatMessage.getMsgid() + "json string sendMessage ChatHelper" + request.toString());
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

    public static void addChatMsgRealm(final Message message, final String mykey, final Context context) {
        final Realm re = Realm.getDefaultInstance();
        re.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                try {
                    ChatMessageRealm chat = bgRealm.createObject(ChatMessageRealm.class, message.getStanzaId());
                    String js = HtmlManipulator.replaceHtmlEntities(message.getBody());
                    System.out.println("message body replace html entities" + js);
                    Gson gson = new Gson();
                    ChatMessageRealm gsonchat = gson.fromJson(js, ChatMessageRealm.class);
                    String othr = "";
                    if (!mykey.matches(gsonchat.getSenderjid())) {
                        Log.d(TAG, mykey + "  " + gsonchat.getOtherjid() + " gson chat chatherlper1 " + gsonchat.getSenderjid());
                        chat.setChatref(mykey + gsonchat.getSenderjid());
                        othr = gsonchat.getSenderjid ();
                    } else {
                        Log.d(TAG, mykey + "  " + gsonchat.getOtherjid() + " gson chat chatherlper2 " + gsonchat.getSenderjid());
                        chat.setChatref(mykey + gsonchat.getOtherjid());
                        othr = gsonchat.getOtherjid();
                    }

                    Log.d(TAG, mykey + "othr chathelper " + othr);

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
                System.out.println("new message stored realm success chathelper");
                context.sendBroadcast(new Intent().putExtra("reloadchat", "yes").setAction("reloadchataction"));
                re.close();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                // Transaction failed and was automatically canceled.
                error.printStackTrace();
                System.out.println("new message stored realm failed chathelper");
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

    public static void checkActiveChats(final String mykey, final String otheruserkey, final String msgid) {
        Log.d(TAG, mykey + otheruserkey + " check activechats shows size1  ");
        if ((mykey + otheruserkey).matches(mykey + mykey))
            return;

        DBREF_USER_PROFILES.child(otheruserkey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User usr = User.parse(dataSnapshot);
                Log.d(TAG, "data snap " + dataSnapshot.toString());
                otherusername = usr.getName();
                otheruserimage = usr.getProfpicurl();

                final ActiveChatsRealm ac = new ActiveChatsRealm(otheruserimage, msgid, otherusername, "nil", "nil", "nil", false, otheruserkey, mykey + otheruserkey, false);
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

}
