package com.aayush.baatchit.tor;

import com.aayush.baatchit.Constants;

public class SendMessage implements Runnable{

    private SendMessageInterface sendMessageInterface;
    private String to, message;

    public SendMessage(SendMessageInterface sendMessageInterface, String to, String message) {
        this.sendMessageInterface = sendMessageInterface;
        this.to = "http://" + to + ".onion/accept";
        this.message = message;
    }


    @Override
    public void run() {
        Tor t = Tor.getInstance(Constants.FILE_PATH_DIR);
        if(t.torStatus()) {
            String status = t.sendMessage(to, message);
//            String status = t.checkServerStatus(t.getOnionUrl());
            this.sendMessageInterface.messageSentStatus(status);
        } else {
            this.sendMessageInterface.messageSentStatus(Constants.TOR_NOT_RUNNING);
        }
    }
}
