package com.aayush.baatchit.tor;

import com.aayush.baatchit.Constants;

public class StartTorService implements Runnable {

    private StartTorServiceInterface startTorServiceInterface;

    public StartTorService(StartTorServiceInterface startTorServiceInterface) {
        this.startTorServiceInterface = startTorServiceInterface;
    }
    @Override
    public void run() {

        Tor t = Tor.getInstance(Constants.FILE_PATH_DIR);
        if(!t.torStatus()) {
            String onionUrl = t.startTorAndGetOnionUrl();
            t.startServer();
            this.startTorServiceInterface.onionUrl(onionUrl);
        }
    }
}

