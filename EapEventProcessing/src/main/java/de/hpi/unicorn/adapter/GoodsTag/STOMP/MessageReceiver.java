package de.hpi.unicorn.adapter.GoodsTag.STOMP;

public interface MessageReceiver<MessageType> {

    void messageReceived(MessageDispatcher<MessageType> dispatcher, MessageType message);
}
