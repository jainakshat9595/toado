//package com.app.toado.helper;
//
///**
// * Created by ghanendra on 31/07/2017.
// */
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.os.AsyncTask;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.android.volley.AuthFailureError;
//import com.android.volley.NetworkResponse;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.VolleyLog;
//import com.android.volley.toolbox.HttpHeaderParser;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
//import com.app.toado.R;
//import com.app.toado.activity.chat.ChatActivity;
//import com.app.toado.model.ChatMessage;
//import com.app.toado.model.ChatMessageXmpp;
//import com.app.toado.model.realm.ChatMessageRealm;
//import com.app.toado.settings.UserSession;
//import com.google.gson.Gson;
//
//import org.jivesoftware.smack.AbstractXMPPConnection;
//import org.jivesoftware.smack.ConnectionConfiguration;
//import org.jivesoftware.smack.ConnectionListener;
//import org.jivesoftware.smack.MessageListener;
//import org.jivesoftware.smack.SASLAuthentication;
//import org.jivesoftware.smack.SmackConfiguration;
//import org.jivesoftware.smack.SmackException;
//import org.jivesoftware.smack.StanzaListener;
//import org.jivesoftware.smack.XMPPConnection;
//import org.jivesoftware.smack.XMPPException;
//import org.jivesoftware.smack.chat.Chat;
//import org.jivesoftware.smack.chat.ChatManager;
//import org.jivesoftware.smack.chat.ChatManagerListener;
//import org.jivesoftware.smack.chat.ChatMessageListener;
//import org.jivesoftware.smack.packet.Message;
//import org.jivesoftware.smack.packet.Presence;
//import org.jivesoftware.smack.packet.Stanza;
//import org.jivesoftware.smack.tcp.XMPPTCPConnection;
//import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
//import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
//import org.jivesoftware.smackx.iqregister.AccountManager;
//import org.jivesoftware.smackx.ping.PingManager;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.jxmpp.jid.DomainBareJid;
//import org.jxmpp.jid.EntityFullJid;
//import org.jxmpp.jid.EntityJid;
//import org.jxmpp.jid.impl.JidCreate;
//import org.jxmpp.stringprep.XmppStringprepException;
//
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//import io.realm.Realm;
//
//
//public class XMPP {
//
//    public static final int PORT = 5222;
//    private static XMPP instance;
//    private XMPPTCPConnection connection;
//    private static String TAG = "XMPP-EXAMPLE";
//    public static final String ACTION_LOGGED_IN = "liveapp.loggedin";
//    private String HOST = "192.168.5.168";
//
//    private XMPPTCPConnectionConfiguration buildConfiguration() throws XmppStringprepException {
//        XMPPTCPConnectionConfiguration.Builder builder =
//                XMPPTCPConnectionConfiguration.builder();
//
//        builder.setHost(HOST);
//        builder.setPort(PORT);
//        builder.setCompressionEnabled(false);
//        builder.setDebuggerEnabled(true);
//        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
//        builder.setSendPresence(true);
//
////        if (Build.VERSION.SDK_INT >= 14) {
////            builder.setKeystoreType("AndroidCAStore");
////            // config.setTruststorePassword(null);
////            builder.setKeystorePath(null);
////        } else {
////            builder.setKeystoreType("BKS");
////            String str = System.getProperty("javax.net.ssl.trustStore");
////            if (str == null) {
////                str = System.getProperty("java.home") + File.separator + "etc" + File.separator + "security"
////                        + File.separator + "cacerts.bks";
////            }
////            builder.setKeystorePath(str);
////        }
//
//        DomainBareJid serviceName = JidCreate.domainBareFrom(HOST);
//        builder.setServiceName(serviceName);
//        return builder.build();
//    }
//
//    private XMPPTCPConnection getConnection() throws XMPPException, SmackException, IOException, InterruptedException {
//        Log.d(TAG, "Getting XMPP Connect");
//        if (isConnected()) {
//            Log.d(TAG, "Returning already existing connection");
//            return this.connection;
//        }
//
//        long l = System.currentTimeMillis();
//        try {
//            if(this.connection != null){
//                Log.d(TAG, "Connection found, trying to connect");
//                this.connection.connect();
//            }else{
//                Log.d(TAG, "No Connection found, trying to create a new connection");
//                XMPPTCPConnectionConfiguration config = buildConfiguration();
//                SmackConfiguration.DEBUG = true;
//                this.connection = new XMPPTCPConnection(config);
//                this.connection.connect();
//            }
//        } catch (Exception e) {
//            Log.e(TAG,"some issue with getting connection :" + e.getMessage());
//
//        }
//
//        Log.d(TAG, "Connection Properties: " + connection.getHost() + " " + connection.getServiceName());
//        Log.d(TAG, "Time taken in first time connect: " + (System.currentTimeMillis() - l));
//        return this.connection;
//    }
//
//    public static XMPP getInstance() {
//        if (instance == null) {
//            synchronized (XMPP.class) {
//                if (instance == null) {
//                    instance = new XMPP();
//                }
//            }
//        }
//        return instance;
//    }
//
//    public void close() {
//        Log.d(TAG, "Inside XMPP close method");
//        if (this.connection != null) {
//            this.connection.disconnect();
//        }
//    }
//
//    private XMPPTCPConnection connectAndLogin(Context context,String mykey) {
//        Log.d(TAG, "Inside connect and Login");
//        if (!isConnected()) {
//            Log.d(TAG, "Connection not connected, trying to login and connect");
//            try {
//                // Save username and password then use here
//                this.connection = getConnection();
//                Log.d(TAG, "XMPP username :" + mykey);
//                this.connection.login(mykey, mykey);
//                Log.d(TAG, "Connect and Login method, Login successful");
//                context.sendBroadcast(new Intent(ACTION_LOGGED_IN));
//            } catch (XMPPException localXMPPException) {
//                Log.e(TAG, "Error in Connect and Login Method");
//                localXMPPException.printStackTrace();
//            } catch (SmackException e) {
//                Log.e(TAG, "Error in Connect and Login Method");
//                e.printStackTrace();
//            } catch (IOException e) {
//                Log.e(TAG, "Error in Connect and Login Method");
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                Log.e(TAG, "Error in Connect and Login Method");
//                e.printStackTrace();
//            } catch (IllegalArgumentException e) {
//                Log.e(TAG, "Error in Connect and Login Method");
//                e.printStackTrace();
//            } catch (Exception e) {
//                Log.e(TAG, "Error in Connect and Login Method");
//                e.printStackTrace();
//            }
//        }
//        Log.d(TAG, "Inside getConnection - Returning connection");
//        return this.connection;
//    }
//
//    public boolean isConnected() {
//        return (this.connection != null) && (this.connection.isConnected());
//    }
//
//    public EntityFullJid getUser() {
//        if (isConnected()) {
//            return connection.getUser();
//        } else {
//            return null;
//        }
//    }
//
//    public void login(String user, String pass, String username)
//            throws XMPPException, SmackException, IOException, InterruptedException{
//        Log.d(TAG, "inside XMPP getlogin Method");
//        long l = System.currentTimeMillis();
//        XMPPTCPConnection connect = getConnection();
//        if (connect.isAuthenticated()) {
//            Log.d(TAG, "User already logged in");
//            return;
//        }
//
//        Log.d(TAG, "Time taken to connect: " + (System.currentTimeMillis() - l));
//
//        l = System.currentTimeMillis();
//        try{
//            connect.login(user, pass);
//        }catch (Exception e){
//            Log.e(TAG, "Issue in login, check the stacktrace");
//            e.printStackTrace();
//        }
//
//        Log.d(TAG, "Time taken to login: " + (System.currentTimeMillis() - l));
//
//        Log.d(TAG, "login step passed");
//
//        PingManager pingManager = PingManager.getInstanceFor(connect);
//        pingManager.setPingInterval(5000);
//
//    }
//
//    public void register(String mykey) throws XMPPException, SmackException.NoResponseException, SmackException.NotConnectedException {
//        Log.d(TAG, "inside XMPP register method, " + mykey + " : " + mykey);
//        long l = System.currentTimeMillis();
//        try {
//            AccountManager accountManager = AccountManager.getInstance(getConnection());
//            accountManager.sensitiveOperationOverInsecureConnection(true);
//            accountManager.createAccount(mykey, mykey);
//        } catch (SmackException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        Log.d(TAG, "Time taken to register: " + (System.currentTimeMillis() - l));
//    }
//
//
//    public void addStanzaListener(Context context, StanzaListener stanzaListener){
//         connection.addAsyncStanzaListener(stanzaListener, null);
//    }
//
//    public void removeStanzaListener(Context context, StanzaListener stanzaListener){
//         connection.removeAsyncStanzaListener(stanzaListener);
//    }
//
//    public void addChatListener(Context context, ChatManagerListener chatManagerListener){
//        ChatManager.getInstanceFor(connection)
//                .addChatListener(chatManagerListener);
//    }
//
//    public void removeChatListener(Context context, ChatManagerListener chatManagerListener){
//        ChatManager.getInstanceFor(connection).removeChatListener(chatManagerListener);
//    }
//
//    public void getSrvDeliveryManager(Context context){
//        ServiceDiscoveryManager sdm = ServiceDiscoveryManager
//                .getInstanceFor(connection);
//        //sdm.addFeature("http://jabber.org/protocol/disco#info");
//        //sdm.addFeature("jabber:iq:privacy");
//        sdm.addFeature("jabber.org/protocol/si");
//        sdm.addFeature("http://jabber.org/protocol/si");
//        sdm.addFeature("http://jabber.org/protocol/disco#info");
//        sdm.addFeature("jabber:iq:privacy");
//
//    }
//
//    public String getUserLocalPart(Context context){
//        return  connection.getUser().getLocalpart().toString();
//    }
//
//    public EntityFullJid getUser(Context context){
//        return  connection.getUser();
//    }
//
//    public Chat getThreadChat(Context context, String party1, String party2){
//        Chat chat = ChatManager.getInstanceFor(connection)
//                .getThreadChat(party1 + "-" + party2);
//        return chat;
//    }
//
//    public Chat createChat(Context context, EntityJid jid, String party1, String party2, ChatMessageListener messageListener){
//        Chat chat = ChatManager.getInstanceFor(connection)
//                .createChat(jid, party1 + "-" + party2,
//                        messageListener);
//        return chat;
//    }
//
//    public void sendPacket(Context context, Stanza packet){
//        try {
//            connection.sendStanza(packet);
//        } catch (SmackException.NotConnectedException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
