package main.data;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class Message extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    public final User user;
    public final Optional<ChatRoom> chatRoom;
    public final String message;
    public final ZonedDateTime dateTime;
    
    public Message(ChatRoom chatRoom, User user, String message, ZonedDateTime dateTime) {
        this(Optional.of(chatRoom), user, message, dateTime);
    }
    
    public Message(User user, String message, ZonedDateTime dateTime) {
        this(Optional.empty(), user, message, dateTime);
    }
    
    private Message(Optional<ChatRoom> chatRoom, User user, String message, ZonedDateTime dateTime) {
        this.user = user;
        this.chatRoom = chatRoom;
        this.message = message;
        this.dateTime = dateTime;
        
        JLabel userLabel = new JLabel(user.username + " (" + user.nickname + ")");
        JLabel messageLabel = new JLabel(message);
        JLabel dateTimeLabel = new JLabel(dateTime.withZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME));
        
        userLabel.setVisible(true);
        messageLabel.setVisible(true);
        dateTimeLabel.setVisible(true);
        
        this.add(userLabel);
        this.add(messageLabel);
        this.add(dateTimeLabel);
    }
}
