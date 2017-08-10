package main.data;

import java.time.LocalDateTime;
import java.util.Optional;

public class Message {

    public final User user;
    public final Optional<ChatRoom> chatRoom;
    public final String message;
    public final LocalDateTime dateTime;
    
    public Message(ChatRoom chatRoom, User user, String message, LocalDateTime dateTime) {
        this(Optional.of(chatRoom), user, message, dateTime);
    }
    
    public Message(User user, String message, LocalDateTime dateTime) {
        this(Optional.empty(), user, message, dateTime);
    }
    
    private Message(Optional<ChatRoom> chatRoom, User user, String message, LocalDateTime dateTime) {
        this.user = user;
        this.chatRoom = chatRoom;
        this.message = message;
        this.dateTime = dateTime;
    }
}
