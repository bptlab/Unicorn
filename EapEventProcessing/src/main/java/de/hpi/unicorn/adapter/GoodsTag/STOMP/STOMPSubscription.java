package de.hpi.unicorn.adapter.GoodsTag.STOMP;

import java.util.ArrayList;
import java.util.List;

public class STOMPSubscription implements MessageDispatcher<STOMPServerMessage> {

    private STOMPClient client;
    private String destination;
    private String id;
    private STOMPSubscriptionAcknowledgement acknowledgement;
    private List<MessageReceiver<STOMPServerMessage>> messageReceivers = new ArrayList<>();

    public STOMPSubscription(STOMPClient client,
                             String destination,
                             String id,
                             STOMPSubscriptionAcknowledgement acknowledgement) {
        this.client = client;
        this.destination = destination;
        this.id = id;
        this.acknowledgement = acknowledgement;
        this.send();
    }

    public STOMPSubscription(STOMPClient client,
                             String destination,
                             String id) {
        this(client, destination, id, STOMPSubscriptionAcknowledgement.AUTO);
    }

    private void send() {
        String message = STOMPClientMessageFactory.subscribe(this.destination, this.id, this.acknowledgement);
        this.client.getMessageSender().sendMessage(message);
    }

    @Override
    public boolean canReceive() {
        return this.client.getMessageDispatcher().canReceive();
    }

    public void consumeMessage(STOMPServerMessage message) {
        for (MessageReceiver<STOMPServerMessage> receiver : this.messageReceivers) {
            receiver.messageReceived(this, message);
        }
    }

    @Override
    public void addMessageReceiver(MessageReceiver<STOMPServerMessage> receiver) {
        this.messageReceivers.add(receiver);
    }

    @Override
    public void removeMessageReceiver(MessageReceiver<STOMPServerMessage> receiver) {
        this.messageReceivers.remove(receiver);
    }
}
