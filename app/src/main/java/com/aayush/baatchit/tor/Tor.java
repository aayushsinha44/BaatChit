package com.aayush.baatchit.tor;

import android.util.Log;

import com.aayush.baatchit.Constants;

import tormobile.MessageData;
import tormobile.Tormobile;


public class Tor {

    private String onionUrl;
    private String dataDirPath;
    private boolean isTorRunning;
    private static Tor t;
    public Tor(String dataDirPath) {
        this.dataDirPath = dataDirPath;
        isTorRunning = false;
    }

    public static Tor getInstance(String dataDirPath) {
        if(t == null) {
            t = new Tor(dataDirPath);
        }
        return t;
    }

    public String startTorAndGetOnionUrl() {
        // path --> getFilesDir().toString()
        String onionUrl = Tormobile.startTorAndGetOnionUrl(this.dataDirPath);
        this.onionUrl = onionUrl;
        isTorRunning = true;
        return onionUrl;
    }

    public boolean torStatus() {
        return this.isTorRunning;
    }

    public String startServer() {
        String status = Tormobile.startServer();
        if(status.equalsIgnoreCase("success")) {
            return "success";
        } else {
            return "failed";
        }
    }

    public String checkServerStatus(String onionUrl) {
        String status = Tormobile.testOnionUrl(onionUrl);
        if(status.equalsIgnoreCase("success")) {
            return "success";
        } else {
            return "failed";
        }
    }

    public String sendMessage(String to, String message) {
        MessageData msg = new MessageData();
        msg.setFrom(this.getOnionVal());
        msg.setMessage(message);
        Log.d("==========From========", msg.getFrom());
        Log.d("========message=======", msg.getMessage());
        String status = Tormobile.sendMessage(to, msg);
        return status;
//        if(status.equalsIgnoreCase("success")) {
//            return Constants.TOR_MESSAGE_SEND_SUCCESS;
//        } else {
//            return Constants.TOR_MESSAGE_SEND_FAILURE;
//        }
    }

    public String getOnionUrl() {
        return this.onionUrl;
    }

    public String getOnionVal() {
        String[] strings = this.onionUrl.split(".", 2)[1].split("/", 3);
        String s = strings[strings.length-1];
        String val = "";
        for(int i=0;i<s.length();i++) {
            if(s.charAt(i)!='.') {
                val = val + s.charAt(i);
            } else break;
        }
        return val;
    }

}
