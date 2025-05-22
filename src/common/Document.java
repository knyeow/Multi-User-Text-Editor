package common;

import java.io.Serializable;
import java.util.UUID;

public class Document implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String content;
    private long lastModified;

    public Document(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.content = "";
        this.lastModified = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.lastModified = System.currentTimeMillis();
    }

    public long getLastModified() {
        return lastModified;
    }

    public void updateContent(String newContent, int position, String text, boolean isInsert) {
        StringBuilder sb = new StringBuilder(content);
        if (isInsert) {
            sb.insert(position, text);
        } else {
            sb.delete(position, position + text.length());
        }
        this.content = sb.toString();
        this.lastModified = System.currentTimeMillis();
    }
} 