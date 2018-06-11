package de.hpi.unicorn.adapter.GoodsTag;

public interface WebSocketMessageHandler {

    void onMessage(WebSocketClient sender, String message);
}
