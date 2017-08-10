package com.app.toado.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager.AutoReceiptMode;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.app.toado.R;
import com.app.toado.model.realm.ChatMessageRealm;
import com.app.toado.settings.UserSession;
import com.google.gson.Gson;

import io.realm.Realm;

import static org.jivesoftware.smack.packet.Presence.Type.available;

public class MyXMPP2 {
    public static ArrayList<HashMap<String, String>> usersList = new ArrayList<HashMap<String, String>>();

    public static boolean connected = false;
    public boolean loggedin = false;
    public static boolean isconnecting = false;
    public static boolean isToasted = true;
    private boolean chat_created = false;
    private String serverAddress;
    public static XMPPTCPConnection connection;
    public static String userkey;
    public static String passwordUser;
    Gson gson;
    Activity context;
    public static MyXMPP2 instance = null;
    public static boolean instanceCreated = false;
    public org.jivesoftware.smack.chat.Chat Mychat;
    Message message;
    String TAG = "MyXMPP2";
    ChatManagerListenerImpl mChatManagerListener;
    MMessageListener mMessageListener;
    Realm realm;
    UserSession usr;

    public MyXMPP2(Activity context, String serverAdress, String key) {
        this.serverAddress = serverAdress;
        this.userkey = key;
        this.context = context;
        realm = Realm.getDefaultInstance();
        init();
        login(userkey);
    }

    public static MyXMPP2 getInstance(Activity context, String server,
                                      String key) {

        if (instance == null) {
            instance = new MyXMPP2(context, server, key);
            instanceCreated = true;
        }
        return instance;

    }

    static {
        try {
            Class.forName("org.jivesoftware.smack.ReconnectionManager");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public void init() {
        if (!connected) {
            gson = new Gson();
            mMessageListener = new MMessageListener(context);
            mChatManagerListener = new ChatManagerListenerImpl();
            initialiseConnection();
        }
    }

    private void initialiseConnection() {
        ProviderManager.addExtensionProvider(ReadReceipt.ELEMENT, ReadReceipt.NAMESPACE, new ReadReceipt.Provider());

        final XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setServiceName(serverAddress);
        config.setPort(5222);
        config.setDebuggerEnabled(true);
        config.setConnectTimeout(100000);
        XMPPTCPConnection.setUseStreamManagementResumptiodDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        connection = new XMPPTCPConnection(config.build());
        XMPPConnectionListener connectionListener = new XMPPConnectionListener();
        connection.addConnectionListener(connectionListener);
        connect(" initialise conn ");

        connection.addAsyncStanzaListener(new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws NotConnectedException {

                Log.d(TAG, packet.getExtensions() + "stanza listener calling reload data chat act broadcast" + packet.getStanzaId());

                int a = packet.getExtensions().size();
                for (int i = 0; i < a; i++) {
                    Log.d(TAG, " packet extensions" + packet.getExtensions().get(i).getNamespace());
                    Log.d(TAG, " packet extensions" + packet.getExtensions().get(i).getElementName());
                    Log.d(TAG, " packet extensions" + packet.getStanzaId());
                }

                if (packet instanceof Message && ((Message) packet).getBody() != null) {
                    final Message m = (Message) packet;
                    Log.d(TAG, "message paacket stanza listener" + m.getBody() + m.getFrom());


                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (userkey != null)
                                ChatHelper.addChatMsgRealm(m, userkey, context);
                            else {
                                ChatHelper.addChatMsgRealm(m, getUserKey(), context);
                            }
                        }
                    });

                }

//                ReadReceipt dr = (ReadReceipt) packet.getExtension(ReadReceipt.ELEMENT, ReadReceipt.NAMESPACE);
//
//                if (dr != null) {
//                    Log.d(TAG, "read recicpt listneers" + dr.getElementName() + dr.getNamespace() + dr.getId());
//
//
//                }

            }
        }, null);

        DeliveryReceiptManager deliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(connection);
        deliveryReceiptManager.autoAddDeliveryReceiptRequests();
        deliveryReceiptManager.addReceiptReceivedListener(new ReceiptReceivedListener() {
            @Override
            public void onReceiptReceived(String fromJid, String toJid, String receiptId, Stanza receipt) {
                Log.i(TAG, "MSG DELIVERED From jid " + fromJid);
                Log.i(TAG, "MSG DELIVERED To jid " + toJid);
                Log.i(TAG, "MSG DELIVERED Receipt id  " + receiptId);
                Log.i(TAG, "MSG DELIVERED recipt " + receipt.getStanzaId());
                Log.i(TAG, "MSG DELIVERED body " + receipt.getFrom() + receipt.getTo());
            }
        });

    }


    private String getUserKey() {
        if (usr == null)
            usr = new UserSession(context);

        return usr.getUserKey();
    }

    private void connect(final String caller) {

        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected synchronized Boolean doInBackground(Void... arg0) {
                if (connection.isConnected())
                    return false;
                isconnecting = true;
                if (isToasted)
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(context, caller + "=>connecting....", Toast.LENGTH_LONG).show();
                        }
                    });
                Log.d("Connect() Function", caller + "=>connecting....");

                try {
                    connection.connect();
                    DeliveryReceiptManager dm = DeliveryReceiptManager
                            .getInstanceFor(connection);
                    dm.setAutoReceiptMode(AutoReceiptMode.always);
                    dm.autoAddDeliveryReceiptRequests();
                    dm.addReceiptReceivedListener(new ReceiptReceivedListener() {

                        @Override
                        public void onReceiptReceived(final String fromid, final String toid, final String msgid, final Stanza packet) {
                            System.out.println("msg recept recd" + fromid + toid + msgid + packet.getStanzaId() + msgid);
                        }

                    });
                    connected = true;
                    login(userkey);
                } catch (IOException e) {
                    if (isToasted)
                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "(" + caller + ")" + "IOException: ", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    Log.e("(" + caller + ")", "IOException: " + e.getMessage());
                } catch (final SmackException e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            e.printStackTrace();
                            Toast.makeText(context, "(" + caller + ")" + "SMACKException: ", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.e("(" + caller + ")", "SMACKException: " + e.getMessage());
                } catch (final XMPPException e) {
                    if (isToasted)
                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {

                                    @Override
                                    public void run() {
                                        e.printStackTrace();
                                        Toast.makeText(context, "(" + caller + ")" + "XMPPException: ", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    Log.e("connect(" + caller + ")", "XMPPException: " + e.getMessage());
                }
                return isconnecting = false;
            }
        };
        connectionThread.execute();

    }

    public void login(String key) {
        Log.d(TAG, "trying to login !");
        try {
            SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
            SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
            connection.login(key, key);
            Log.i("LOGIN", "Yey! We're connected to the Xmpp server!");
        } catch (XMPPException | SmackException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ChatManagerListenerImpl implements ChatManagerListener {
        @Override
        public void chatCreated(final org.jivesoftware.smack.chat.Chat chat,
                                final boolean createdLocally) {
            Log.d(TAG, createdLocally + "trying to login !" + chat.getParticipant());

            if (!createdLocally)
                chat.addMessageListener(mMessageListener);
        }
    }

    public void sendMessage(final ChatMessageRealm chatMessage) {
        Log.d(TAG, "sending message" + chatMessage.getMsgstring() + chatMessage.getOtherjid());

        if (!connected)
            init();

        if (!chat_created) {
            Mychat = ChatManager.getInstanceFor(connection).createChat(
                    chatMessage.getOtherjid() + "@" + context.getString(R.string.server_host) + "/Smack",
                    mMessageListener);
            chat_created = true;
        }

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (userkey != null)
                    message = ChatHelper.getMessageBody(chatMessage, userkey, context);
                else
                    message = ChatHelper.getMessageBody(chatMessage, getUserKey(), context);
            }
        });

        Log.d(TAG, userkey+getUserKey()+"message object sending" + message.getStanzaId() + message.getBody());

        try {
            if (connection.isAuthenticated()) {
                Mychat.sendMessage(message);
            } else {
                login(chatMessage.getSenderjid());
                Mychat.sendMessage(message);
            }
        } catch (NotConnectedException e) {
            Log.e(TAG, "msg Not sent!-Not Connected!");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.d(TAG, "Message id adding to read recipt request" + message.getStanzaId() + chatMessage.getMsgstring());
            ReadReceipt read = new ReadReceipt(message.getStanzaId());
            message.addExtension(read);
        }
    }


    public class XMPPConnectionListener implements ConnectionListener {
        @Override
        public void connected(final XMPPConnection connection) {
            Log.d(TAG, "Connected!");
            connected = true;
        }

        @Override
        public void connectionClosed() {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(context, "ConnectionCLosed!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d(TAG, "ConnectionCLosed!");
            connected = false;
            chat_created = false;
            loggedin = false;
        }

        @Override
        public void connectionClosedOnError(Exception arg0) {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(context, "ConnectionClosedOn Error!!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d(TAG, "ConnectionClosedOn Error!");
            connected = false;

            chat_created = false;
            loggedin = false;

            init();
        }

        @Override
        public void reconnectingIn(int arg0) {

            Log.d(TAG, "Reconnectingin " + arg0);

            loggedin = false;

        }

        @Override
        public void reconnectionFailed(Exception arg0) {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {

                        Toast.makeText(context, "ReconnectionFailed!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d(TAG, "ReconnectionFailed!");
            connected = false;

            chat_created = false;
            loggedin = false;
            init();
        }

        @Override
        public void reconnectionSuccessful() {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(context, "REConnected!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d(TAG, "ReconnectionSuccessful");
            connected = true;

            chat_created = false;
            loggedin = false;
            login(userkey);
        }

        @Override
        public void authenticated(XMPPConnection arg0, boolean arg1) {
            Log.d(TAG, "Authenticated!");
            loggedin = true;

            chat_created = false;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }).start();
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        Toast.makeText(context, "Connected!",
                                Toast.LENGTH_SHORT).show();

                    }
                });
        }
    }

    private class MMessageListener implements ChatMessageListener {

        public MMessageListener(Context contxt) {
        }

        @Override
        public void processMessage(final org.jivesoftware.smack.chat.Chat chat,
                                   final Message message) {
            Log.i("MyXMPP_MESSAGE_LISTENER", "Xmpp message received: '"
                    + message);
            System.out.println("Body-----" + message.getBody());
            if (message.getType() == Message.Type.chat
                    && message.getBody() != null) {
//                final ChatMessage chatMessage = new ChatMessage();
//                chatMessage.setBody(message.getBody());
//
//                processMessage(chatMessage);
            }
        }

//        private void processMessage(final ChatMessage chatMessage) {
//            chatMessage.isMine = false;
//            Chats.chatlist.add(chatMessage);
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//                @Override
//                public void run() {
//                    Chats.chatAdapter.notifyDataSetChanged();
//
//                }
//            });
//        }
    }


    public static XMPPTCPConnection getConn() {
        return connection;
    }

    public void manageDndPresence() {
        Presence p = new Presence(available, "I am busy", 42, Presence.Mode.away);
        try {
            connection.sendStanza(p);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public void manageAvailablePresence() {
        Presence p = new Presence(available, "I available", 42, Presence.Mode.available);
        try {
            connection.sendStanza(p);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }


    private static class ReadReceipt implements PacketExtension {

        public static final String NAMESPACE = "urn:xmpp:read";
        public static final String ELEMENT = "read";

        private String id; /// original ID of the delivered message

        public ReadReceipt() {

        }

        public ReadReceipt(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public String getElementName() {
            return ELEMENT;
        }

        @Override
        public String getNamespace() {
            return NAMESPACE;
        }

        @Override
        public String toXML() {
            return "<read xmlns='" + NAMESPACE + "' id='" + id + "'/>";
        }

        public static class Provider extends EmbeddedExtensionProvider {
            @Override
            protected ExtensionElement createReturnExtension(String currentElement, String currentNamespace, Map attributeMap, List content) {
                return new ReadReceipt(attributeMap.get("id").toString());
            }
        }


    }


}