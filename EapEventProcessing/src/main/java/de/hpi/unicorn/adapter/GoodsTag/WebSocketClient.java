package de.hpi.unicorn.adapter.GoodsTag;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@ClientEndpoint
public class WebSocketClient {

    private Session session;
    private List<WebSocketMessageHandler> messageHandlers;
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
        this.messageHandlers = new ArrayList<>();
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

    public void addMessageHandler(WebSocketMessageHandler messageHandler) {
        this.messageHandlers.add(messageHandler);
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
        if (this.messageHandlers.size() <= 0) {
            System.out.println(String.format("web socket client received message ('%s'), but has no message handlers", message));
            return;
        }

        for (WebSocketMessageHandler messageHandler : this.messageHandlers) {
            messageHandler.onMessage(this, message);
        }
    }

    public boolean sendMessage(String message) {
        if (!this.isConnected) {
            return false;
        }

        this.session.getAsyncRemote().sendText(message);
        return true;
    }
}
