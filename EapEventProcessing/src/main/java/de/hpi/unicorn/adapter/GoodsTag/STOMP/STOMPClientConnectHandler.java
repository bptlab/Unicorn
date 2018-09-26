package de.hpi.unicorn.adapter.GoodsTag.STOMP;

public interface STOMPClientConnectHandler {

    void connectFinished(STOMPClient sender, boolean connectionSucceeded);
}
