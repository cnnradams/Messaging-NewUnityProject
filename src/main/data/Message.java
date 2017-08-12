package main.data;

import java.awt.Dimension;
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
    public final boolean loopback;
    
    public Message(ChatRoom chatRoom, User user, String message, ZonedDateTime dateTime) {
        this(chatRoom, user, message, dateTime, false);
    }
    
    public Message(ChatRoom chatRoom, User user, String message, ZonedDateTime dateTime, boolean loopback) {
        this(Optional.of(chatRoom), user, message, dateTime, loopback);
    }
    
    public Message(User user, String message, ZonedDateTime dateTime) {
        this(user, message, dateTime, false);
    }
    
    public Message(User user, String message, ZonedDateTime dateTime, boolean loopback) {
        this(Optional.empty(), user, message, dateTime, loopback);
    }
    
    private Message(Optional<ChatRoom> chatRoom, User user, String message, ZonedDateTime dateTime, boolean loopback) {
        this.user = user;
        this.chatRoom = chatRoom;
        this.message = message;
        this.dateTime = dateTime;
        this.loopback = loopback;
        
        JLabel userLabel = new JLabel();
        
        if(loopback) {
            userLabel.setText("You");
        }
        else {
            userLabel.setText(user.username + " (" + user.nickname + ")");
        }
        JLabel messageLabel = new JLabel(message);
        
       
        JLabel dateTimeLabel = new JLabel(dateTime.withZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME));
        this.setMaximumSize(new Dimension(2000,20));
        this.setMinimumSize(new Dimension(2000,20));
        userLabel.setVisible(true);
        messageLabel.setVisible(true);
        dateTimeLabel.setVisible(true);
        this.add(userLabel);
        this.add(messageLabel);
        this.add(dateTimeLabel);
    }
}
