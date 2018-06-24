package de.hpi.unicorn.adapter.GoodsTag;

import de.hpi.unicorn.adapter.GoodsTag.STOMP.MessageDispatcher;
import de.hpi.unicorn.adapter.GoodsTag.STOMP.MessageReceiver;
import de.hpi.unicorn.adapter.GoodsTag.STOMP.MessageSender;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@ClientEndpoint
public class WebSocketClient implements SendHandler, MessageSender<String>, MessageDispatcher<String> {

    private Session session;
    private List<MessageReceiver<String>> receivers = new ArrayList<>();
    private CloseReason closeReason;
    private boolean isConnected;

    public boolean isConnected() {
        return this.isConnected;
    }

    public CloseReason getCloseReason() {
        return this.closeReason;
    }

    public WebSocketClient(URI serverURI) {
        this.session = null;
        this.closeReason = null;
        this.isConnected = false;

        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, serverURI);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        if (!this.isConnected) {
            return;
        }

        try {
            this.session.close();
        } catch (IOException e) {
            // empty, since we don't need to handle this exception
        }

        this.isConnected = false;
        this.session = null;
    }

    @OnOpen
    public void onOpen(Session session) {
        this.isConnected = true;
        this.session = session;
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.isConnected = false;
        this.closeReason = reason;
        this.session = null;
    }

    @OnMessage
    public void onMessage(String message) {
        if (this.receivers.size() <= 0) {
            System.out.println(String.format("web socket client received message ('%s'), but has no message handlers", message));
            return;
        }

        for (MessageReceiver<String> receiver : this.receivers) {
            receiver.messageReceived(this, message);
        }
    }

    @Override
    public boolean canSend() {
        return this.isConnected;
    }

    @Override
    public boolean sendMessage(String message) {
        if (!this.isConnected || this.session == null) {
            return false;
        }

        this.session.getAsyncRemote().sendText(message, this);
        return true;
    }

    @Override
    public void onResult(SendResult sendResult) {
        if (!sendResult.isOK()) {
            System.out.println(String.format("cannot send WebSocket message: %s", sendResult.getException().getMessage()));
        }
    }

    @Override
    public boolean canReceive() {
        return this.isConnected;
    }

    @Override
    public void addMessageReceiver(MessageReceiver<String> receiver) {
        this.receivers.add(receiver);
    }

    @Override
    public void removeMessageReceiver(MessageReceiver<String> receiver) {
        this.receivers.remove(receiver);
    }
}
