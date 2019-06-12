package de.hpi.unicorn.adapter.GoodsTag.STOMP;

public interface MessageDispatcher<MessageType> {

    boolean canReceive();

    void addMessageReceiver(MessageReceiver<MessageType> receiver);

    void removeMessageReceiver(MessageReceiver<MessageType> receiver);
}
