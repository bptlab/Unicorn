package de.hpi.unicorn.adapter.GoodsTag.STOMP;

public interface MessageSender<MessageType> {

    boolean canSend();

    boolean sendMessage(MessageType message);
}
