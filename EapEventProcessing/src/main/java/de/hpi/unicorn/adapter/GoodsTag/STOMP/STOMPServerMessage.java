package de.hpi.unicorn.adapter.GoodsTag.STOMP;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static de.hpi.unicorn.adapter.GoodsTag.STOMP.STOMPConstants.C_ENTRY_SEPARATOR;
import static de.hpi.unicorn.adapter.GoodsTag.STOMP.STOMPConstants.C_HEADER_SEPARATOR;
import static de.hpi.unicorn.adapter.GoodsTag.STOMP.STOMPConstants.C_MESSAGE_END;

public class STOMPServerMessage {

    private STOMPServerCommand command;
    private String body;
    private Map<String, String> headers;

    public static STOMPServerMessage parse(String message) {
        if (message == null
                || message.length() <= 0
                || message.charAt(message.length() - 1) != C_MESSAGE_END) {
            System.out.println("Cannot parse STOMP message: message was null, empty or did not end with ^0!");
            return invalidMessage();
        }

        Stack<String> messageLines = new Stack<>();
        messageLines.addAll(Lists.reverse(Arrays.asList(message.split("\r?\n"))));

        if (messageLines.empty()) {
            System.out.println("Cannot parse STOMP message: message has no lines!");
            return invalidMessage();
        }

        STOMPServerCommand command = STOMPServerCommand.INVALID_COMMAND;
        Map<String, String> headers = new HashMap<>();
        String body = "";

        // parse command
        String commandText = messageLines.pop();
        for (STOMPServerCommand c : STOMPServerCommand.values()) {
            if (!commandText.equalsIgnoreCase(c.toString())) {
                continue;
            }

            command = c;
            break;
        }

        if (command == STOMPServerCommand.INVALID_COMMAND) {
            System.out.println(String.format("Cannot parse STOMP message: unrecognized command '%s'", commandText));
            return invalidMessage();
        }

        // parse headers
        while (!messageLines.empty()) {
            String line = messageLines.pop();

            if (line.equals("")) {
                // found empty line ==> separator for headers and body
                break;
            }

            String[] header = line.split(C_HEADER_SEPARATOR);

            if (header.length != 2) {
                // invalid header
                System.out.println("Cannot parse STOMP message: invalid header format!");
                return invalidMessage();
            }

            setHeader(headers, header[0], header[1]);
        }

        // parse body
        StringBuilder sb = new StringBuilder();
        while (!messageLines.empty()) {
            sb.append(messageLines.pop());
            sb.append(C_ENTRY_SEPARATOR);
        }
        body = sb.toString();

        return new STOMPServerMessage(command, body, headers);
    }

    private static STOMPServerMessage invalidMessage() {
        return new STOMPServerMessage(STOMPServerCommand.INVALID_COMMAND, "", new HashMap<String, String>());
    }

    private static void setHeader(Map<String, String> headers, String header, String value) {
        if (headers.containsKey(header.toLowerCase())) {
            return;
        }

        headers.put(header.toLowerCase(), value.toLowerCase());
    }

    private STOMPServerMessage(STOMPServerCommand command, String body, Map<String, String> headers) {
        this.command = command;
        this.body = body;
        this.headers = headers;
    }

    public boolean isValid() {
        return this.command != STOMPServerCommand.INVALID_COMMAND;
    }

    public STOMPServerCommand getServerCommand() {
        return this.command;
    }

    public String getBody() {
        return this.body;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public boolean containsHeader(String header) {
        return this.headers.containsKey(header.toLowerCase());
    }

    public String getHeader(String header) throws RuntimeException {
        if (!this.containsHeader(header)) {
            throw new RuntimeException(String.format("STOMP message does not contain header '%s'", header));
        }

        return this.headers.get(header.toLowerCase());
    }

    public String getHeader(String header, String defaultValue) {
        if (!this.containsHeader(header)) {
            return defaultValue;
        }

        return this.getHeader(header);
    }
}
