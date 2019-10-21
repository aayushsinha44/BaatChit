package com.aayush.baatchit.server;

import android.util.Log;

import com.aayush.baatchit.Constants;

import fi.iki.elonen.NanoHTTPD;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;

public class WebServer extends NanoHTTPD {
    private WebServerMessageInterface webServerMessageInterface;

    public WebServer(WebServerMessageInterface webServerMessageInterface) {
        super(Constants.JAVA_SERVER_PORT);
        this.webServerMessageInterface = webServerMessageInterface;
    }
    public WebServer(int port) {
        super(port);
    }

    public WebServer(String hostname, int port) {
        super(hostname, port);
    }

    public String convert(InputStream inputStream, Charset charset) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }

        return stringBuilder.toString();
    }

    private JSONObject getResponse(String status, int code, String message) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("status", status);
        obj.put("code", code);
        obj.put("message", message);
        return obj;
    }

    @Override
    public Response serve(IHTTPSession session) {

        String from = session.getHeaders().get("from");
        String message = session.getHeaders().get("message");
        if(from == null) {
            try {
                return newFixedLengthResponse(getResponse("failure", 400, "no from").toString());
            } catch (JSONException e) {
                return newFixedLengthResponse(e.toString());
            }
        }
        if(message == null) {
            try {
                return newFixedLengthResponse(getResponse("failure", 400, "no message").toString());
            } catch (JSONException e) {
                return newFixedLengthResponse(e.toString());
            }
        }

        webServerMessageInterface.messageReceived(from, message);

        try {
            return newFixedLengthResponse(getResponse("success", 200, "success").toString());
        } catch (JSONException e) {
            return newFixedLengthResponse(e.toString());
        }
    }
}
