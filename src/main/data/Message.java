package main.data;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
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
    	this.setLayout((LayoutManager) new FlowLayout(FlowLayout.LEFT));
    	this.user = user;
        this.chatRoom = chatRoom;
        this.message = message;
        this.dateTime = dateTime;
        this.loopback = loopback;
        
        JLabel userLabel = new JLabel();
        
        if(loopback) {
            userLabel.setText("You, at");
        }
        else {
            userLabel.setText(user.username + " (" + user.nickname + ") at");
        }
        JLabel messageLabel = new JLabel(message);
        
       
        JLabel dateTimeLabel = new JLabel(dateTime.withZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_TIME) + " said: ");
        
        this.setBackground(new Color(40,40,40));
        userLabel.setForeground(new Color(120,120,120));
        messageLabel.setForeground(new Color(255,255,255));
        dateTimeLabel.setForeground(new Color(120,120,120));
        
        this.setMaximumSize(new Dimension(2000,20));
        this.setMinimumSize(new Dimension(2000,20));
        userLabel.setVisible(true);
        dateTimeLabel.setVisible(true);
        messageLabel.setVisible(true);
        this.add(userLabel);
        this.add(dateTimeLabel);
        this.add(messageLabel);
    }
}
