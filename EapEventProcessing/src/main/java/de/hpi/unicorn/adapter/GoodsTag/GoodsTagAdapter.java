package de.hpi.unicorn.adapter.GoodsTag;

import de.hpi.unicorn.adapter.EventAdapter;
import de.hpi.unicorn.adapter.GoodsTag.STOMP.*;
import de.hpi.unicorn.configuration.EapConfiguration;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.utils.DateUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import java.net.URISyntaxException;
import java.util.*;

/*
Example: https://stackoverflow.com/questions/26452903/javax-websocket-client-simple-example
 */

public class GoodsTagAdapter extends EventAdapter implements MessageReceiver<STOMPServerMessage>, STOMPClientConnectHandler {

    private static String C_HTTPS_PREFIX = "https://";
    private static String C_WSS_PREFIX = "wss://";
    private static String C_URI_ACCESS_TOKEN_PARAMETER = "/ws?access_token=";
    private static String C_URI_NOTIFICATIONS_PATH = "/notifications";
    private static String C_URI_SUBSCRIPTION_PATH = "/subscribe";
    private static String C_TOKEN_ROUTE = "/oauth/token";
    private static String C_DEVICE_ID_SEPARATOR = " ";

    private static String C_NFC_UNKNOWN_TAG_SCAN_NAME = "NFCUnknownTagScan";
    private static String C_NFC_UNMAPPED_TAG_SCAN_NAME = "NFCUnmappedTagScan";
    private static String C_NFC_USER_SCAN_NAME = "NFCUserScan";
    private static String C_NFC_BOOK_SCAN_NAME = "NFCBookScan";

    private static String C_GOODS_TAG_UNKNOWN_SCAN = "unknownTag";
    private static String C_GOODS_TAG_UNMAPPED_SCAN = "unmappedTag";
    private static String C_GOODS_TAG_CARD_SCAN = "cardscan";
    private static String C_GOODS_TAG_BOOK_SCAN = "bookscan";

    private static long C_GOODS_TAG_EVENT_ACCEPT_INTERVAL = 2000;

    private String goodsTagUri = "";
    private String goodsTagUsername = "";
    private String goodsTagPassword = "";
    private String[] goodsTagDeviceIds;
    private String goodsTagAccessToken = "";
    private Date refreshAccessToken = Calendar.getInstance().getTime();
    private WebSocketClient webSocketClient = null;
    private STOMPClient stompClient = null;
    private STOMPSubscription notificationsSubscription = null;
    private Map<String, Date> latestProcessedEvents = new HashMap<>();

    private EapEventType nfcUnknownTagScan = null;
    private EapEventType nfcUnmappedTagScan = null;
    private EapEventType nfcUserScan = null;
    private EapEventType nfcBookScan = null;


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

        this.stompClient = new STOMPClient(C_WSS_PREFIX + this.goodsTagUri, this.webSocketClient, this.webSocketClient);
        this.stompClient.connect(this);
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

    private void cacheEventTypes() {
        if (nfcUnknownTagScan == null) {
            nfcUnknownTagScan = EapEventType.findByTypeName(C_NFC_UNKNOWN_TAG_SCAN_NAME);
        }

        if (nfcUnmappedTagScan == null) {
            nfcUnmappedTagScan = EapEventType.findByTypeName(C_NFC_UNMAPPED_TAG_SCAN_NAME);
        }

        if (nfcUserScan == null) {
            nfcUserScan = EapEventType.findByTypeName(C_NFC_USER_SCAN_NAME);
        }

        if (nfcBookScan == null) {
            nfcBookScan = EapEventType.findByTypeName(C_NFC_BOOK_SCAN_NAME);
        }
    }

    private String getGoodsTagWorkaround(STOMPServerMessage goodsTagEvent) throws RuntimeException {
        // this is a workaround for a bug, which caused the JSON parser to fail
        // the bug consists of a JSON key being used twice within the GoodsTag event body

        // TODO: disable this function as soon as the bug in GoodsTag is fixed!

        // we are going to ignore the first few JSON attributes, until we find the "time" key
        // thus, we can avoid the JSON parser to fail and hence, continue the execution normally
        String[] keyValueSplit = goodsTagEvent.getBody().split("[:,]");
        int startIndex = 0;

        for (int i = 0; i < keyValueSplit.length; i += 2) {
            if (keyValueSplit[i].equalsIgnoreCase("\"time\"")) {
                break;
            }

            startIndex += keyValueSplit[i].length() + keyValueSplit[i + 1].length() + 2; // add 2, since "split" removed the separators
        }

        if (startIndex >= goodsTagEvent.getBody().length() || startIndex <= 0) {
            throw new RuntimeException("Cannot apply workaround for GoodsTag JSON bug!");
        }

        return "{" + goodsTagEvent.getBody().substring(startIndex); // add the "{" to start the JSON object correctly
    }

    private void throwIfContainsError(JSONObject executionResult) throws RuntimeException, JSONException {
        String errorMessage = executionResult.getString("errorMessage");

        if (!errorMessage.equalsIgnoreCase("null")) {
            System.out.println(String.format("GoodsTag event error message: '%s'", errorMessage));
            throw new RuntimeException(errorMessage);
        }
    }

    private JSONObject getEnglishTranslation(JSONObject executionResult) throws RuntimeException, JSONException {
        JSONArray translations = executionResult.getJSONArray("translations");

        if (translations.length() <= 0) {
            throw new RuntimeException("Cannot get english translation from GoodsTag event!");
        }

        for (int i = 0; i < translations.length(); i++) {
            JSONObject translation = translations.getJSONObject(i);

            if (!translation.getString("language").equalsIgnoreCase("en")) {
                continue;
            }

            return translation;
        }

        throw new RuntimeException("Cannot get english translation from GoodsTag event!");
    }

    private void parseGoodsTagEvent(JSONObject goodsTagEvent) throws RuntimeException, JSONException {
        String timestampString = goodsTagEvent.getString("time");
        Date timestamp = DateUtils.parseDate(timestampString);

        if (timestamp == null) {
            throw new RuntimeException(String.format("Cannot parse date from '%s'", timestampString));
        }

        JSONObject executionResult = goodsTagEvent.getJSONObject("executionResult");

        throwIfContainsError(executionResult);

        JSONObject result = executionResult.getJSONObject("result");
        String type = result.getString("type");

        // check, whether this event was processed already
        // this might happen, if the user holds the tag too close to the scanner for too long
        if (latestProcessedEvents.containsKey(type)) {
            Date lastExecution = latestProcessedEvents.get(type);

            if (lastExecution.getTime() + C_GOODS_TAG_EVENT_ACCEPT_INTERVAL > timestamp.getTime()) {
                // dont accept event, since it came too early
                return;
            }
        }

        latestProcessedEvents.put(type, timestamp);

        if (type.equalsIgnoreCase(C_GOODS_TAG_BOOK_SCAN)) {
            parseBookScanEvent(timestamp, result, goodsTagEvent);

        } else if (type.equalsIgnoreCase(C_GOODS_TAG_CARD_SCAN)) {
            parseCardScanEvent(timestamp, result, goodsTagEvent);

        } else if (type.equalsIgnoreCase(C_GOODS_TAG_UNKNOWN_SCAN)) {
            parseUnknownTagScanEvent(timestamp, result, goodsTagEvent);

        } else if (type.equalsIgnoreCase(C_GOODS_TAG_UNMAPPED_SCAN)) {
            parseUnmappedTagScanEvent(timestamp, result, goodsTagEvent);

        } else {
            throw new RuntimeException(String.format("Unknown GoodsTag event type: '%s'", type));
        }
    }

    private void parseBookScanEvent(Date timestamp, JSONObject executionResult, JSONObject goodsTagEvent) throws RuntimeException, JSONException {
        if (nfcBookScan == null) {
            throw new RuntimeException(String.format("Cannot send GoodsTag book scan event: Event Type '%s' is missing", C_NFC_BOOK_SCAN_NAME));
        }

        Map<String, Serializable> eventValues = new HashMap<>();

        String epc = goodsTagEvent.getJSONObject("data").getString("epc");
        JSONObject translation = getEnglishTranslation(executionResult);

        eventValues.put("NFCID", epc);
        eventValues.put("ISBN", executionResult.getString("isbn"));

        System.out.println("*** NEW GOODSTAG BOOK SCAN EVENT ***");
        Broker.getEventImporter().importEvent(new EapEvent(nfcBookScan, timestamp, eventValues));
    }

    private void parseCardScanEvent(Date timestamp, JSONObject executionResult, JSONObject goodsTagEvent) throws RuntimeException, JSONException {
        if (nfcUserScan == null) {
            throw new RuntimeException(String.format("Cannot send GoodsTag user scan event: Event Type '%s' is missing", C_NFC_USER_SCAN_NAME));
        }

        Map<String, Serializable> eventValues = new HashMap<>();

        String epc = goodsTagEvent.getJSONObject("data").getString("epc");
        JSONObject translation = getEnglishTranslation(executionResult);

        eventValues.put("NFCID", epc);
        eventValues.put("UserId", executionResult.getString("user"));
        eventValues.put("Name", translation.getString("name"));
        eventValues.put("Mail", translation.getString("mail"));

        System.out.println("*** NEW GOODSTAG CARD SCAN EVENT ***");
        Broker.getEventImporter().importEvent(new EapEvent(nfcUserScan, timestamp, eventValues));
    }

    private void parseUnknownTagScanEvent(Date timestamp, JSONObject executionResult, JSONObject goodsTagEvent) throws RuntimeException, JSONException {
        if (nfcUnknownTagScan == null) {
            throw new RuntimeException(String.format("Cannot send GoodsTag unknown tag scan event: Event Type '%s' is missing", C_NFC_UNKNOWN_TAG_SCAN_NAME));
        }

        Map<String, Serializable> eventValues = new HashMap<>();

        String epc = goodsTagEvent.getJSONObject("data").getString("epc");

        eventValues.put("NFCID", epc);

        System.out.println("*** NEW GOODSTAG UNKNOWN TAG SCAN EVENT ***");
        Broker.getEventImporter().importEvent(new EapEvent(nfcUnknownTagScan, timestamp, eventValues));
    }

    private void parseUnmappedTagScanEvent(Date timestamp, JSONObject executionResult, JSONObject goodsTagEvent) throws RuntimeException, JSONException {
        if (nfcUnmappedTagScan == null) {
            throw new RuntimeException(String.format("Cannot send GoodsTag unknown tag scan event: Event Type '%s' is missing", C_NFC_UNMAPPED_TAG_SCAN_NAME));
        }

        Map<String, Serializable> eventValues = new HashMap<>();

        String epc = goodsTagEvent.getJSONObject("data").getString("epc");

        eventValues.put("NFCID", epc);

        System.out.println("*** NEW GOODSTAG UNMAPPED TAG SCAN EVENT ***");
        Broker.getEventImporter().importEvent(new EapEvent(nfcUnmappedTagScan, timestamp, eventValues));
    }

    @Override
    public void trigger() {
        cacheEventTypes();

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

        //System.out.println(String.format("GoodsTag event received: '%s'", message.getBody()));

        if (!message.containsHeader("content-type") || !message.getHeader("content-type").equalsIgnoreCase("application/json")) {
            System.out.println("GoodsTag event does not contain a JSON body. This is not supported (yet)!");
            return;
        }

        try {
            // TODO: remove this, as soon as the GoodsTag JSON bug is fixed:
            JSONObject jsonBody = new JSONObject(getGoodsTagWorkaround(message));

            // TODO: enable this again, as soon as the GoodsTag JSON bug is fixed:
            // JSONObject jsonBody = new JSONObject(message.getBody());
            parseGoodsTagEvent(jsonBody);

        } catch (JSONException ex) {
            System.out.println(String.format("Cannot parse GoodsTag event to JSON object: %s", ex.getMessage()));
        } catch (RuntimeException ex) {
            System.out.println(String.format("Cannot parse GoodsTag event: %s", ex.getMessage()));
        }
    }

    @Override
    public void connectFinished(STOMPClient sender, boolean connectionSucceeded) {
        if (sender != this.stompClient) {
            System.out.println("GoodsTag adapter received 'connection finished' from unknown STOMP client!");
            return;
        }

        if (!connectionSucceeded) {
            return;
        }

        this.notificationsSubscription = this.stompClient.subscribe(C_URI_NOTIFICATIONS_PATH);
        this.notificationsSubscription.addMessageReceiver(this);

        for (String deviceId : this.goodsTagDeviceIds) {
            this.subscribeToDevice(deviceId);
        }
    }
}
