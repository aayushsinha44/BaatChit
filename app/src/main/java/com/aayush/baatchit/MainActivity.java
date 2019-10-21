package com.aayush.baatchit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aayush.baatchit.server.WebServer;
import com.aayush.baatchit.server.WebServerMessageInterface;
import com.aayush.baatchit.tor.SendMessage;
import com.aayush.baatchit.tor.SendMessageInterface;
import com.aayush.baatchit.tor.StartTorService;
import com.aayush.baatchit.tor.StartTorServiceInterface;
import com.aayush.baatchit.tor.Tor;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements StartTorServiceInterface, SendMessageInterface, WebServerMessageInterface {

    private TextView onionUrlTextView;
    private EditText toEditText, messageEditText;
    private Button sendMessageButton;
    private Snackbar snackbar;
    private Handler mHandler;
    private MainActivity obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Constants.FILE_PATH_DIR = getFilesDir().toString();

        toEditText = (EditText) findViewById(R.id.to_onion_url);
        messageEditText = (EditText) findViewById(R.id.message_et);
        sendMessageButton = (Button) findViewById(R.id.send_message);

        obj = this;


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar = showSnackbar("Sending message ...");
                snackbar.show();
                String to = toEditText.getText().toString();
                String message = messageEditText.getText().toString();
                new Thread(new SendMessage(obj, to, message)).start();
            }
        });


        mHandler = new Handler();
        onionUrlTextView = (TextView) findViewById(R.id.onion_url);
        snackbar = showSnackbar("Starting tor service ...");
        snackbar.show();

        new Thread(new StartTorService(this)).start();

        startServer();

    }

    private void startServer() {
        WebServer webServer = new WebServer(this);
        try {
            webServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Snackbar showSnackbar(String message) {
        Snackbar snackBar = Snackbar.make(findViewById(R.id.main_activity_layout), message, Snackbar.LENGTH_INDEFINITE);
        return snackBar;
    }

    @Override
    public void onionUrl(final String onion_url) {
        snackbar.dismiss();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onionUrlTextView.setText(onion_url);
                toEditText.setText(Tor.getInstance(Constants.FILE_PATH_DIR).getOnionVal());
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    public void messageSentStatus(final String message) {

        if(message.equalsIgnoreCase(Constants.TOR_MESSAGE_SEND_SUCCESS)) {
            // success
        } else if(message.equalsIgnoreCase(Constants.TOR_MESSAGE_SEND_FAILURE)) {
            // failure
        } else if(message.equalsIgnoreCase(Constants.TOR_NOT_RUNNING)) {
            // tor not running
        } else {
            // some other error
        }

        snackbar.dismiss();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onionUrlTextView.setText(message);
            }
        });
    }

    @Override
    public void messageReceived(final String from, final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(obj, "From="+from + " message="+message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
