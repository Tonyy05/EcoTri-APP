package iut.dam.ecotri;

public class ChatMessage {
    private final String text;
    private final boolean isUser;
    private final long timestamp;

    public ChatMessage(String text, boolean isUser, long timestamp) {
        this.text = text;
        this.isUser = isUser;
        this.timestamp = timestamp;
    }

    public String getText() { return text; }
    public boolean isUser() { return isUser; }
    public long getTimestamp() { return timestamp; }
}
