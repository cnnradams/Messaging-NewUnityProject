package main.data;

import java.time.LocalDateTime;

public class Message {

    public final User user;
    public final String message;
    public final LocalDateTime dateTime;
    
    public Message(User user, String message, LocalDateTime dateTime) {
        this.user = user;
        this.message = message;
        this.dateTime = dateTime;
    }
}
