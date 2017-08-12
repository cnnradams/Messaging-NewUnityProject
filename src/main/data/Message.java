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
import javax.swing.JTextArea;

public class Message extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    public final User user;
    public final Optional<ChatRoom> chatRoom;
    public final String message;
    public final ZonedDateTime dateTime;
    public final boolean loopback;
    JTextArea messageLabel;
    JLabel dateTimeLabel;
    JLabel userLabel;
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
        
         userLabel = new JLabel();
        
        if(loopback) {
            userLabel.setText("You, at");
        }
        else {
            userLabel.setText(user.username + " (" + user.nickname + ") at");
        }

        messageLabel = new JTextArea(message);
        dateTimeLabel = new JLabel(dateTime.withZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_TIME) + " said: ");
        
        this.setBackground(new Color(40,40,40));
        userLabel.setForeground(new Color(120,120,120));
        messageLabel.setForeground(new Color(255,255,255));
        dateTimeLabel.setForeground(new Color(120,120,120));
        messageLabel.setLineWrap(true);
        messageLabel.setWrapStyleWord(true);
        messageLabel.setMinimumSize(new Dimension(messageLabel.getMinimumSize().width, 20));
        //messageLabel.setColumns(200);
        messageLabel.setPreferredSize(new Dimension(messageLabel.getPreferredSize().width, 20));
        this.setMinimumSize(new Dimension(this.getMinimumSize().width, 20));
        //this.setMaximumSize(new Dimension(2000, 40));
        messageLabel.setOpaque(false);
        messageLabel.setEditable(false);
        userLabel.setVisible(true);
        dateTimeLabel.setVisible(true);
        messageLabel.setVisible(true);
        this.add(userLabel);
        this.add(dateTimeLabel);
        this.add(messageLabel);
    }
    
    public void reBreak(int width) {
    	width -= userLabel.getFontMetrics(userLabel.getFont()).stringWidth(userLabel.getText());
    	width -= dateTimeLabel.getFontMetrics(dateTimeLabel.getFont()).stringWidth(dateTimeLabel.getText());
    	width -= 50;
    	 messageLabel.setPreferredSize(new Dimension(width, 20 * (int)Math.ceil(messageLabel.getFontMetrics(messageLabel.getFont()).stringWidth(messageLabel.getText())/(double)width)));
    	 this.setMaximumSize(new Dimension(2000, 20 * (int)Math.ceil(messageLabel.getFontMetrics(messageLabel.getFont()).stringWidth(messageLabel.getText())/(double)width)));
    	 // messageLabel.setMaximumSize(new Dimension(getWidth(), 2000000000));
        // this.setMaximumSize(new Dimension(getWidth(), 2000000000));
    	if(messageLabel != null) {
    		/*messageLabel.setText(messageLabel.getText().replaceAll("<br>", ""));
        	this.setMaximumSize(new Dimension(2000,20 * (int)(Math.ceil(messageLabel.getText().toCharArray().length / yCutoff))));
            this.setMinimumSize(new Dimension(2000,20 * (int)(Math.ceil(messageLabel.getText().toCharArray().length / yCutoff))));
            System.out.println("(.{" + yCutoff + "})");
            messageLabel.setText(messageLabel.getText().replaceAll("(.{" + (int)yCutoff + "})", "$1<br>"));*/
    	}
    }
}
