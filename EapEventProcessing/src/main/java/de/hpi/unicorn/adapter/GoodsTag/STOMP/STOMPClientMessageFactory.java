package de.hpi.unicorn.adapter.GoodsTag.STOMP;

import java.util.HashMap;
import java.util.Map;

import static de.hpi.unicorn.adapter.GoodsTag.STOMP.STOMPConstants.*;

public class STOMPClientMessageFactory {

    private STOMPClientMessageFactory() {
        // keep this private to avoid anyone from creating an instance of this class
    }

    public static void setHeader(Map<String, String> headers, String header, String value) {
        if (headers.containsKey(header)) {
            return;
        }

        headers.put(header, value);
    }


    public static String newMessage(STOMPClientCommand command, String body, Map<String, String> headers) {
        StringBuilder sb = new StringBuilder();

        sb.append(command.toString().toUpperCase());
        sb.append(C_ENTRY_SEPARATOR);

        if (headers.size() <= 0) {
            sb.append(C_ENTRY_SEPARATOR);
        }

        for (Map.Entry<String, String> header : headers.entrySet()) {
            sb.append(header.getKey());
            sb.append(C_HEADER_SEPARATOR);
            sb.append(header.getValue());
            sb.append(C_ENTRY_SEPARATOR);
        }

        sb.append(body);
        sb.append(C_MESSAGE_END);

        return sb.toString();
    }

    public static String newMessage(STOMPClientCommand command, String body) {
        return newMessage(command, body, new HashMap<String, String>());
    }

    //region send
    public static String send(String destination, String body, Map<String, String> headers) {
        setHeader(headers, C_DESTINATION_HEADER, destination);

        return newMessage(STOMPClientCommand.SEND, body, headers);
    }

    public String send(String destination, String body) {
        return send(destination, body, new HashMap<String, String>());
    }
    //endregion

    //region subscribe
    public static String subscribe(String destination, String subscriptionId, STOMPSubscriptionAcknowledgement clientAcknowledgement) {
        Map<String, String> headers = new HashMap<>();

        setHeader(headers, C_ID_HEADER, subscriptionId);
        setHeader(headers, C_DESTINATION_HEADER, destination);
        setHeader(headers, C_ACKNOWLEDGE_HEADER, clientAcknowledgement.toString().toLowerCase());

        return newMessage(STOMPClientCommand.SUBSCRIBE, "", headers);
    }

    public static String subscribe(String destination, String subscriptionId) {
        return subscribe(destination, subscriptionId, STOMPSubscriptionAcknowledgement.AUTO);
    }
    //endregion

    //region unsubscribe
    public static String unsubscribe(String subscriptionId) {
        Map<String, String> headers = new HashMap<>();

        setHeader(headers, C_ID_HEADER, subscriptionId);

        return newMessage(STOMPClientCommand.UNSUBSCRIBE, "", headers);
    }
    //endregion

    //region ack
    public static String ack(String messageId, String transactionName) {
        Map<String, String> headers = new HashMap<>();

        setHeader(headers, C_ID_HEADER, messageId);
        setHeader(headers, C_TRANSACTION_HEADER, transactionName);

        return newMessage(STOMPClientCommand.ACK, "", headers);
    }

    public static String ack(String messageId) {
        Map<String, String> headers = new HashMap<>();

        setHeader(headers, C_ID_HEADER, messageId);

        return newMessage(STOMPClientCommand.ACK, "", headers);
    }
    //endregion

    //region nack
    public static String nack(String messageId, String transactionName) {
        Map<String, String> headers = new HashMap<>();

        setHeader(headers, C_ID_HEADER, messageId);
        setHeader(headers, C_TRANSACTION_HEADER, transactionName);

        return newMessage(STOMPClientCommand.NACK, "", headers);
    }

    public static String nack(String messageId) {
        Map<String, String> headers = new HashMap<>();

        setHeader(headers, C_ID_HEADER, messageId);

        return newMessage(STOMPClientCommand.NACK, "", headers);
    }
    //endregion

    //region begin
    public static String begin(String transactionName) {
        Map<String, String> headers = new HashMap<>();

        setHeader(headers, C_TRANSACTION_HEADER, transactionName);

        return newMessage(STOMPClientCommand.BEGIN, "", headers);
    }
    //endregion

    //region commit
    public static String commit(String transactionName) {
        Map<String, String> headers = new HashMap<>();

        setHeader(headers, C_TRANSACTION_HEADER, transactionName);

        return newMessage(STOMPClientCommand.COMMIT, "", headers);
    }
    //endregion

    //region abort
    public static String abort(String transactionName) {
        Map<String, String> headers = new HashMap<>();

        setHeader(headers, C_TRANSACTION_HEADER, transactionName);

        return newMessage(STOMPClientCommand.ABORT, "", headers);
    }
    //endregion

    //region disconnect
    public static String disconnect(String receiptId) {
        Map<String, String> headers = new HashMap<>();

        setHeader(headers, C_RECEIPT_HEADER, receiptId);

        return newMessage(STOMPClientCommand.DISCONNECT, "", headers);
    }
    //endregion

}
