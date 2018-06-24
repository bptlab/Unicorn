package de.hpi.unicorn.adapter.GoodsTag.STOMP;

public class STOMPConstants {

    public static String C_ENTRY_SEPARATOR = "\r\n";
    public static String C_HEADER_SEPARATOR = ":";
    public static char C_MESSAGE_END = '\0';

    public static String C_ACCEPT_VERSION_HEADER = "accept-version";
    public static String C_HOST_HEADER = "host";
    public static String C_HEARTBEAT_HEADER = "heart-beat";
    public static String C_DESTINATION_HEADER = "destination";
    public static String C_ID_HEADER = "id";
    public static String C_ACKNOWLEDGE_HEADER = "ack";
    public static String C_TRANSACTION_HEADER = "transaction";
    public static String C_RECEIPT_HEADER = "receipt";

    public static String C_VERSION_HEADER = "version";
    public static String C_SUBSCRIPTION_HEADER = "subscription";

    private STOMPConstants() {
        // this should never be called
    }
}
