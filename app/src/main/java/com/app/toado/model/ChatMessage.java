package com.app.toado.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ChatMessage {
    private String commentString;
    private String sender;
    private String senderUId;
    private String receiverUId;
    private String sendertimestamp;
    private String type;
    private String id;
    private String status;
    private String imgurl;
//    private String videourl;
    private String receiverToken;
    private String chatref;
    private int percentUploaded = 0;
    private int downloadprogress = 0;

    public int getDownloadprogress() {
        return downloadprogress;
    }

    public void setDownloadprogress(int downloadprogress) {
        this.downloadprogress = downloadprogress;
    }

    public String getChatref() {
        return chatref;
    }

    public void setChatref(String chatref) {
        this.chatref = chatref;
    }


    public String getReceiverToken() {
        return receiverToken;
    }

    public void setReceiverToken(String receiverToken) {
        this.receiverToken = receiverToken;
    }

    public String getSenderUId() {
        return senderUId;
    }

    public void setSenderUId(String senderUId) {
        this.senderUId = senderUId;
    }

    public String getReceiverUId() {
        return receiverUId;
    }

    public void setReceiverUId(String receiverUId) {
        this.receiverUId = receiverUId;
    }

    public String getSendertimestamp() {
        return sendertimestamp;
    }

    public void setSendertimestamp(String sendertimestamp) {
        this.sendertimestamp = sendertimestamp;
    }


    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

//    public String getVideourl() {
//        return videourl;
//    }
//
//    public void setVideourl(String videourl) {
//        this.videourl = videourl;
//    }

    public String getCommentString() {
        return commentString;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCommentString(String commentString) {
        this.commentString = commentString;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }


    public ChatMessage() {

    }

    // for chat messages
    public ChatMessage(String commentString, String sender, String sendertimestamp, String type, String id, String status, String senderUId, String receiverUId, String receiverToken, String chatref) {
        this.commentString = commentString;
        this.sender = sender;
        this.sendertimestamp = sendertimestamp;
        this.type = type;
        this.id = id;
        this.status = status;
        this.senderUId = senderUId;
        this.receiverUId = receiverUId;
        this.receiverToken = receiverToken;
        this.chatref = chatref;
    }

    //for photos videos files
    public ChatMessage(String commentString, String sender, String sendertimestamp, String type, String id, String status, String imgurl, String senderUId, String receiverUId, String receiverToken, String chatref, int percentUploaded,int downloadprogress) {
        this.commentString = commentString;
        this.sender = sender;
        this.sendertimestamp = sendertimestamp;
        this.type = type;
        this.id = id;
        this.status = status;
        this.imgurl = imgurl;
        this.senderUId = senderUId;
        this.receiverUId = receiverUId;
        this.receiverToken = receiverToken;
        this.chatref = chatref;
        this.percentUploaded = percentUploaded;
        this.downloadprogress=downloadprogress;
    }

    public int getPercentUploaded() {
        return percentUploaded;
    }

    public void setPercentUploaded(int percentUploaded) {
        this.percentUploaded = percentUploaded;
    }

    public static ChatMessage parse(DataSnapshot dataSnapshot) throws NullPointerException {
        ChatMessage chatMessage = new ChatMessage();
        System.out.println("datasnapshot chatmessage.java "+dataSnapshot);
        chatMessage.setCommentString(dataSnapshot.child("commentString").getValue().toString());
        chatMessage.setChatref(dataSnapshot.child("chatref").getValue().toString());

        chatMessage.setReceiverToken(dataSnapshot.child("receiverToken").getValue().toString());
        chatMessage.setReceiverUId(dataSnapshot.child("receiverUId").getValue().toString());

        chatMessage.setStatus(dataSnapshot.child("status").getValue().toString());
        chatMessage.setSenderUId(dataSnapshot.child("senderUId").getValue().toString());
        chatMessage.setSender(dataSnapshot.child("sender").getValue().toString());
        chatMessage.setSendertimestamp(dataSnapshot.child("sendertimestamp").getValue().toString());
        chatMessage.setType(dataSnapshot.child("type").getValue().toString());
        chatMessage.setId(dataSnapshot.child("id").getValue().toString());

        if (!chatMessage.getType().equals("text")) {
            if(dataSnapshot.child("percentUploaded")!=null)
            chatMessage.setPercentUploaded(dataSnapshot.child("percentUploaded").getValue(int.class));
            if(dataSnapshot.child("imgurl")!=null)
            chatMessage.setImgurl(dataSnapshot.child("imgurl").getValue().toString());
            if(dataSnapshot.child("downloadprogress").getValue()!=null)
                chatMessage.setDownloadprogress(dataSnapshot.child("downloadprogress").getValue(int.class));
        }

        return chatMessage;
    }

}
