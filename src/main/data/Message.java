package main.data;

import java.time.LocalDate;

public class Message {

    public final User user;
    public final String message;
    public final LocalDate dateTime;
    
    public Message(User user, String message, LocalDate dateTime) {
        this.user = user;
        this.message = message;
        this.dateTime = dateTime;
    }
}
