package de.hpi.unicorn.adapter.GoodsTag.STOMP;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static de.hpi.unicorn.adapter.GoodsTag.STOMP.STOMPConstants.C_SUBSCRIPTION_HEADER;

public class STOMPClient implements MessageReceiver<String> {

    private MessageSender<String> sender;
    private MessageDispatcher<String> dispatcher;
    private Map<String, STOMPSubscription> subscriptions = new HashMap<>();

    public STOMPClient(MessageSender<String> sender, MessageDispatcher<String> dispatcher) {
        this.sender = sender;
        this.dispatcher = dispatcher;

        this.dispatcher.addMessageReceiver(this);
    }

    public MessageSender<String> getMessageSender() {
        return this.sender;
    }

    public MessageDispatcher<String> getMessageDispatcher() {
        return this.dispatcher;
    }

    public STOMPSubscription subscribe(String destination) {
        String id = "";

        do {
            id = UUID.randomUUID().toString();
        } while (this.subscriptions.containsKey(id));

        STOMPSubscription subscription = new STOMPSubscription(this, destination, id);
        this.subscriptions.put(id, subscription);

        return subscription;
    }

    public boolean send(String destination, String body) {
        return this.send(destination, body, new HashMap<String, String>());
    }

    public boolean send(String destination, String body, Map<String, String> headers) {
        if (!this.sender.canSend()) {
            return false;
        }

        String message = STOMPClientMessageFactory.send(destination, body, headers);
        return this.sender.sendMessage(message);
    }

    @Override
    public void messageReceived(MessageDispatcher<String> dispatcher, String message) {
        if (dispatcher != this.dispatcher) {
            throw new RuntimeException("STOMP Client received message from unknown message dispatcher!");
        }

        STOMPServerMessage stompMessage = STOMPServerMessage.parse(message);

        if (!stompMessage.isValid()) {
            System.out.println(String.format("STOMP Client received invalid message: '%s'", message));
            return;
        }

        switch (stompMessage.getServerCommand()) {
            case MESSAGE: {
                this.processMessage(stompMessage);
                break;
            }
            case RECEIPT: {
                this.processReceipt(stompMessage);
                break;
            }
            case ERROR: {
                this.processError(stompMessage);
                break;
            }
            default: {
                System.out.println(String.format("STOMP message command unknown: '%s'", message));
                break;
            }
        }

    }

    private void processMessage(STOMPServerMessage message) {
        try {
            String subscriptionId = message.getHeader(C_SUBSCRIPTION_HEADER);

            if (!this.subscriptions.containsKey(subscriptionId)) {
                throw new RuntimeException("Cannot find subscription for received STOMP message");
            }

            STOMPSubscription subscription = this.subscriptions.get(subscriptionId);
            subscription.consumeMessage(message);

        } catch (RuntimeException ex) {
            System.out.println(String.format("Cannot process STOMP MESSAGE: '%s'", ex.getMessage()));
        }
    }

    private void processReceipt(STOMPServerMessage message) {
        try {

        } catch (RuntimeException ex) {
            System.out.println(String.format("Cannot process STOMP RECEIPT: '%s'", ex.getMessage()));
        }
    }

    private void processError(STOMPServerMessage message) {
        try {

        } catch (RuntimeException ex) {
            System.out.println(String.format("Cannot process STOMP ERROR: '%s'", ex.getMessage()));
        }
    }
}
