package de.hpi.unicorn.adapter.GoodsTag;

import de.hpi.unicorn.adapter.EventAdapter;
import de.hpi.unicorn.adapter.GoodsTag.STOMP.*;
import de.hpi.unicorn.configuration.EapConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;

import java.net.URISyntaxException;
import java.util.*;

/*
Example: https://stackoverflow.com/questions/26452903/javax-websocket-client-simple-example
 */

public class GoodsTagAdapter extends EventAdapter implements MessageReceiver<STOMPServerMessage> {

    private static String C_HTTPS_PREFIX = "https://";
    private static String C_WSS_PREFIX = "wss://";
    private static String C_URI_ACCESS_TOKEN_PARAMETER = "/ws?access_token=";
    private static String C_URI_NOTIFICATIONS_PATH = "/notifications";
    private static String C_URI_SUBSCRIPTION_PATH = "/subscribe";
    private static String C_TOKEN_ROUTE = "/oauth/token";
    private static String C_DEVICE_ID_SEPARATOR = "|";

    private String goodsTagUri = "";
    private String goodsTagUsername = "";
    private String goodsTagPassword = "";
    private String[] goodsTagDeviceIds;
    private String goodsTagAccessToken = "";
    private Date refreshAccessToken = Calendar.getInstance().getTime();
    private WebSocketClient webSocketClient = null;
    private STOMPClient stompClient = null;
    private STOMPSubscription notificationsSubscription = null;


    public GoodsTagAdapter(String name) {
        super(name);

        this.readGoodsTagUri();
        this.readGoodsTagUsername();
        this.readGoodsTagPassword();
        this.readGoodsTagDeviceIds();
    }

    private void readGoodsTagUri() {
        this.goodsTagUri = EapConfiguration.goodsTagUri;

        if (this.goodsTagUri.length() <= 0) {
            throw new RuntimeException("GoodsTag uri is not configured. please check your unicorn.properties");
        }
    }

    private void readGoodsTagUsername() {
        this.goodsTagUsername = EapConfiguration.goodstagUsername;

        if (this.goodsTagUsername.length() <= 0) {
            throw new RuntimeException("GoodsTag username is not configured. please check your unicorn.properties");
        }
    }

    private void readGoodsTagPassword() {
        this.goodsTagPassword = EapConfiguration.goodsTagPassword;

        if (this.goodsTagPassword.length() <= 0) {
            throw new RuntimeException("GoodsTag password is not configured. please check your unicorn.properties");
        }
    }

    private void readGoodsTagDeviceIds() {
        String ids = EapConfiguration.goodsTagDeviceIds;

        if (ids.length() <= 0) {
            throw new RuntimeException("GoodsTag device ids are not configured. please check your unicorn.properties");
        }

        this.goodsTagDeviceIds = ids.split(C_DEVICE_ID_SEPARATOR);
    }

    private void disconnect() {
        if (this.webSocketClient == null || !this.webSocketClient.isConnected()) {
            return;
        }

        this.webSocketClient.close();
    }

    private String authenticate() {
        Date now = Calendar.getInstance().getTime();

        // check whether we already have a access token and if it is still valid
        if (this.goodsTagAccessToken.length() > 0 && now.before(this.refreshAccessToken)) {
            return this.goodsTagAccessToken;
        }

        String url = C_HTTPS_PREFIX + this.goodsTagUri + C_TOKEN_ROUTE;

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", "password"));
        params.add(new BasicNameValuePair("username", this.goodsTagUsername));
        params.add(new BasicNameValuePair("password", this.goodsTagPassword));
        params.add(new BasicNameValuePair("client_id", "gt-user"));

        final HttpClient httpClient = new DefaultHttpClient();
        String responseBody = "";
        JSONObject response = new JSONObject();

        try {
            final HttpPost request = new HttpPost(url);
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");
            request.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = httpClient.execute(request);

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                System.out.println(String.format("cannot authenticate at '%s'.", url));
                return "";
            }

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            responseBody = responseHandler.handleResponse(httpResponse);
            response = new JSONObject(responseBody);
        }
        catch (IOException e) {
            System.out.println("cannot send authentication request to GoodsTag!");
            throw new RuntimeException(e);
        }
        catch (JSONException e) {
            System.out.println("cannot parse GoodsTag authentication response!");
            throw new RuntimeException(e);
        }

        String access_token = "";
        int validityDuration = 0;

        try {
            access_token = response.getString("access_token");
            validityDuration = response.getInt("expires_in");
        }
        catch (JSONException e) {
            System.out.println("cannot read 'access_token' from GoodsTag authentication response");
        }

        this.goodsTagAccessToken = access_token;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, validityDuration);

        this.refreshAccessToken = calendar.getTime();


        return access_token;
    }

    private void connect() {
        this.goodsTagAccessToken = this.authenticate();

        if (this.goodsTagAccessToken.length() <= 0) {
            throw new RuntimeException("cannot connect to GoodsTag: access_token not granted!");
        }

        String uriString = C_WSS_PREFIX + this.goodsTagUri + C_URI_ACCESS_TOKEN_PARAMETER + this.goodsTagAccessToken;

        try {
            URI uri = new URI(uriString);

            // close existing connection
            this.disconnect();

            this.webSocketClient = new WebSocketClient(uri);
        }
        catch (URISyntaxException e) {
            System.out.println(String.format("cannot connect to GoodsTag: '%s' is not a valid URI!", uriString));
            throw new RuntimeException(e);
        }
        catch (RuntimeException e) {
            System.out.println("cannot connect to GoodsTag: web socket webSocketClient connection was refused");
            throw e;
        }

        this.stompClient = new STOMPClient(this.webSocketClient, this.webSocketClient);
        this.notificationsSubscription = this.stompClient.subscribe(C_URI_NOTIFICATIONS_PATH);
        this.notificationsSubscription.addMessageReceiver(this);

        for (String deviceId : this.goodsTagDeviceIds) {
            this.subscribeToDevice(deviceId);
        }
    }

    private void subscribeToDevice(String deviceId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");

        JSONObject message = new JSONObject();

        try {
            message.put("type", "eventSubscribe");
            message.put("eventType", "device.tag.detect");
            message.put("source", String.format("urn:device:%s", deviceId));

            this.stompClient.send(C_URI_SUBSCRIPTION_PATH, message.toString(), headers);
        }
        catch (JSONException ex) {
            System.out.println("cannot create subscription message for GoodsTag web socket!");
        }
    }

    @Override
    public void trigger() {
        // check, whether our webSocketClient connection was closed.
        if (this.webSocketClient != null && this.webSocketClient.isConnected()) {
            return;
        }

        // if so, initialize a new connection
        this.connect();
    }

    @Override
    public boolean stop() {
        this.disconnect();
        return super.stop();
    }

    @Override
    public void messageReceived(MessageDispatcher<STOMPServerMessage> dispatcher, STOMPServerMessage message) {
        if (dispatcher != this.notificationsSubscription) {
            System.out.println("GoodsTag adapter received message from unknown subscription!");
            return;
        }

        System.out.println(String.format("GoodsTag event received: '%s'", message.getBody()));
        // TODO: create event from received message
    }
}
