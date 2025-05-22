package common;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Type {
        LOGIN_REQUEST,
        LOGIN_RESPONSE,
        CREATE_DOCUMENT,
        DOCUMENT_CREATED,
        LIST_DOCUMENTS,
        DOCUMENT_LIST,
        OPEN_DOCUMENT,
        DOCUMENT_CONTENT,
        TEXT_UPDATE,
        TEXT_UPDATE_BROADCAST,
        USER_LIST,
        ERROR
    }

    private Type type;
    private String body;

    public Message(Type type, String body) {
        this.type = type;
        this.body = body;
    }

    public Type getType() {
        return type;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return type + "|" + body.length() + "\n" + body;
    }

    public static Message fromString(String messageStr) {
        String[] parts = messageStr.split("\n", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid message format");
        }

        String[] header = parts[0].split("\\|");
        if (header.length != 2) {
            throw new IllegalArgumentException("Invalid message header");
        }

        Type type = Type.valueOf(header[0]);
        String body = parts[1];

        return new Message(type, body);
    }
} 