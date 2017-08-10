//package com.app.toado.helper;
//
//import android.app.Activity;
//import android.content.Context;
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
//import org.jivesoftware.smack.SmackException;
//import org.jivesoftware.smack.XMPPConnection;
//import org.jivesoftware.smack.XMPPException;
//import org.jivesoftware.smack.chat.Chat;
//import org.jivesoftware.smack.chat.ChatManager;
//import org.jivesoftware.smack.chat.ChatManagerListener;
//import org.jivesoftware.smack.chat.ChatMessageListener;
//import org.jivesoftware.smack.packet.Message;
//import org.jivesoftware.smack.packet.Presence;
//import org.jivesoftware.smack.tcp.XMPPTCPConnection;
//import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//import io.realm.Realm;
//
//import static org.jivesoftware.smack.packet.Presence.Type.available;
//
//public class MyXMPP {
//    public static ArrayList<HashMap<String, String>> usersList = new ArrayList<HashMap<String, String>>();
//
//    public static boolean connected = false;
//    public boolean loggedin = false;
//    public static boolean isconnecting = false;
//    public static boolean isToasted = true;
//    private boolean chat_created = false;
//    private String serverAddress;
//    public static AbstractXMPPConnection connection;
//    Gson gson;
//    private final String TAG = "MyXMPP";
//
//
//    Context context;
//    public static MyXMPP instance = null;
//    public static boolean instanceCreated = false;
//    ChatManagerListenerImpl mChatManagerListener;
//    MMessageListener mMessageListener;
//
//    public MyXMPP(Context context) {
//        this.context = context;
//        init();
//
//    }
//
//    public static MyXMPP getInstance(Context context) {
//
//        if (instance == null) {
//            instance = new MyXMPP(context);
//            instanceCreated = true;
//        }
//        return instance;
//
//    }
//
//    public org.jivesoftware.smack.chat.Chat Mychat;
//
//    String text = "";
//    String mMessage = "", mReceiver = "";
//
//    static {
//        try {
//            Class.forName("org.jivesoftware.smack.ReconnectionManager");
//        } catch (ClassNotFoundException ex) {
//            // problem loading reconnection manager
//        }
//    }
//
//    public void init() {
//        gson = new Gson();
//        if (!connected) {
//            mMessageListener = new MMessageListener();
//            mChatManagerListener = new ChatManagerListenerImpl();
//            initialiseConnection();
//        }
//    }
//
//    public AbstractXMPPConnection initialiseConnection() {
//        String serveradd = context.getString(R.string.server);
//        Log.d("MyXmpp", "initialise connection myxmpp" + serveradd);
//        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
//        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
//        config.setServiceName(serveradd);
//        config.setPort(5222);
//        config.setDebuggerEnabled(true);
//        config.setConnectTimeout(100000);
//        XMPPTCPConnection.setUseStreamManagementResumptiodDefault(true);
//        XMPPTCPConnection.setUseStreamManagementDefault(true);
//        connection = new XMPPTCPConnection(config.build());
//        XMPPConnectionListener connectionListener = new XMPPConnectionListener();
//        connection.addConnectionListener(connectionListener);
//
//        ChatManager chatmanager = ChatManager.getInstanceFor(connection);
//        chatmanager.addChatListener(mChatManagerListener);
//
//        return connection;
//    }
//
//    public static void createChat(Realm realm, String otherusername, String otheruserkey, String profilepic) {
//        ChatHelper.addNewChatList(realm, otherusername, otheruserkey, profilepic);
//    }
//
//    public void sendMessage(Realm realm, String otheruserkey, String mykey, Activity context, ChatMessageRealm chatmessg) {
//
//        init();
//        if (!chat_created) {
//            Mychat = ChatManager.getInstanceFor(connection).createChat(
//                    otheruserkey + "@"
//                            + context.getString(R.string.server_host),
//                    mMessageListener);
//            chat_created = true;
//        }
//        System.out.println(Mychat.getParticipant() + " chat created sendMessage MyXmpp " + chat_created);
//        ChatHelper.sendMessage(realm, mykey, context, Mychat, chatmessg);
//
//    }
//
//
//    private class ChatManagerListenerImpl implements ChatManagerListener {
//        @Override
//        public void chatCreated(final org.jivesoftware.smack.chat.Chat chat,
//                                final boolean createdLocally) {
//
//            Log.d(TAG, createdLocally + "new chat created" + mMessageListener);
//            chat.addMessageListener(mMessageListener);
//        }
//    }
//
//    public void connect(final String caller) {
//
//        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
//            @Override
//            protected synchronized Boolean doInBackground(Void... arg0) {
//                if (connection.isConnected())
//                    return false;
//                isconnecting = true;
//                if (isToasted)
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            Toast.makeText(context, caller + "=>connecting....", Toast.LENGTH_LONG).show();
//                        }
//                    });
//                Log.d("Connect() Function", caller + "=>connecting....");
//
//                try {
//                    connection.connect();
//
//                    connected = true;
//                    Log.d("Connect() Function", caller + "=>connected...");
//
//                } catch (IOException e) {
//                    if (isToasted)
//                        new Handler(Looper.getMainLooper())
//                                .post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(context, "(" + caller + ")" + "IOException: ", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//
//                    Log.e("(" + caller + ")", "IOException: " + e.getMessage());
//                } catch (SmackException e) {
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(context, "(" + caller + ")" + "SMACKException: ", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    Log.e("(" + caller + ")", "SMACKException: " + e.getMessage());
//                } catch (XMPPException e) {
//                    if (isToasted)
//                        new Handler(Looper.getMainLooper())
//                                .post(new Runnable() {
//
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(context, "(" + caller + ")" + "XMPPException: ", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                    Log.e("connect(" + caller + ")", "XMPPException: " + e.getMessage());
//                }
//                return isconnecting = false;
//            }
//        };
//        connectionThread.execute();
//    }
//
//    public void logout() {
//        try {
//            connection.disconnect();
//        } catch (Exception e) {
//        }
//
//    }
//
//    public void login(String mykey) {
//
////        SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
////        SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
//        if (!loggedin) {
//            try {
//                connection.login(mykey + "@myserver", mykey);
//                Log.i("LOGIN", "Yey! We're connected to the Xmpp server!");
//            } catch (XMPPException | SmackException | IOException e) {
//                e.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//        requestQueue.add(stringRequest);
//
//    }
//
//    public void manageDndPresence() {
//        Presence p = new Presence(available, "I am busy", 42, Presence.Mode.away);
//        try {
//            connection.sendStanza(p);
//        } catch (SmackException.NotConnectedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void manageAvailablePresence() {
//        Presence p = new Presence(available, "I available", 42, Presence.Mode.available);
//        try {
//            connection.sendStanza(p);
//        } catch (SmackException.NotConnectedException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public class XMPPConnectionListener implements ConnectionListener {
//        @Override
//        public void connected(final XMPPConnection connection) {
//            connected = true;
//            UserSession usr = new UserSession(context);
//            Log.d("xmpp", "Connected to client now!" + usr.getUserKey());
//            if (!connection.isAuthenticated()) {
//                login(usr.getUserKey());
//            }
//        }
//
//        @Override
//        public void connectionClosed() {
//            if (isToasted)
//
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        Toast.makeText(context, "ConnectionCLosed!",
//                                Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//            Log.d("xmpp", "ConnectionCLosed!");
//            connected = false;
//            chat_created = false;
//            loggedin = false;
//
//        }
//
//        @Override
//        public void connectionClosedOnError(Exception arg0) {
//            if (isToasted)
//
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        Toast.makeText(context, "ConnectionClosedOn Error!!",
//                                Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//            Log.d("xmpp", "ConnectionClosedOn Error!");
//            connected = false;
//
//            chat_created = false;
//            loggedin = false;
//            init();
//        }
//
//        @Override
//        public void reconnectingIn(int arg0) {
//
//            Log.d("xmpp", "Reconnecting " + arg0);
//
//            loggedin = false;
//        }
//
//        @Override
//        public void reconnectionFailed(Exception arg0) {
//            if (isToasted)
//
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//                    @Override
//                    public void run() {
//
//                        Toast.makeText(context, "ReconnectionFailed!",
//                                Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//            Log.d("xmpp", "ReconnectionFailed!");
//            connected = false;
//
//            chat_created = false;
//            loggedin = false;
//        }
//
//        @Override
//        public void reconnectionSuccessful() {
//            if (isToasted)
//
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        Toast.makeText(context, "REConnected!",
//                                Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//            Log.d("xmpp", "ReconnectionSuccessful");
//            connected = true;
//
//            chat_created = false;
//            loggedin = false;
//        }
//
//        @Override
//        public void authenticated(XMPPConnection arg0, boolean arg1) {
//            Log.d("xmpp", "Authenticated!");
//            loggedin = true;
//
//            new Thread(new Runnable() {
//
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//
//                }
//            }).start();
//            if (isToasted)
//
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        // TODO Auto-generated method stub
//
//                        Toast.makeText(context, "Connected!",
//                                Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//        }
//
//
//    }
//
//
//    private class MMessageListener implements ChatMessageListener {
//
//
//        @Override
//        public void processMessage(final org.jivesoftware.smack.chat.Chat chat,
//                                   final Message message) {
//            Log.i("MyXMPP_MESSAGE_LISTENER", chat.getParticipant() + " Xmpp message received: '"
//                    + message);
//
//            System.out.println("Body-----" + message.getBody());
//
//            if (message.getType() == Message.Type.chat
//                    && message.getBody() != null) {
//                final ChatMessageXmpp chatMessage = new ChatMessageXmpp();
//                chatMessage.setBody(message.getBody());
//
//                processMessage(chatMessage);
//            }
//        }
//
//        private void processMessage(final ChatMessageXmpp chatMessage) {
//            //add to realm here then send broadcast about new msg to chat activity
//
//
////            chatMessage.isMine = false;
////            Chats.chatlist.add(chatMessage);
////            new Handler(Looper.getMainLooper()).post(new Runnable() {
////
////                @Override
////                public void run() {
////                    Chats.chatAdapter.notifyDataSetChanged();
////
////                }
////            });
//        }
//
//    }
//
//    public AbstractXMPPConnection getConn() {
//        return connection;
//    }
//
//
//}