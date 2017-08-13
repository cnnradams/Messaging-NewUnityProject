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
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * 
 * Stores data for each specific message
 *
 */
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
        
        // If it's your own message don't print your username etc.
        if(loopback) {
            userLabel.setText("You, at");
        }
        else {
            userLabel.setText(user.username + " (" + user.nickname + ") at");
        }

        messageLabel = new JTextArea(message);
        dateTimeLabel = new JLabel(dateTime.withZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_TIME) + " said: ");
        
        // Coloring of text
        this.setBackground(new Color(40,40,40));
        userLabel.setForeground(new Color(120,120,120));
        messageLabel.setForeground(new Color(255,255,255));
        dateTimeLabel.setForeground(new Color(120,120,120));
        
        // Allows the message to linebreak onto a new line
        messageLabel.setLineWrap(true);
        messageLabel.setWrapStyleWord(true);
        messageLabel.setMinimumSize(new Dimension(messageLabel.getMinimumSize().width, 20));
        messageLabel.setPreferredSize(new Dimension(messageLabel.getPreferredSize().width, 20));
        this.setMinimumSize(new Dimension(this.getMinimumSize().width, 20));
        messageLabel.setOpaque(false);
        messageLabel.setEditable(false);
        
        // Makes sure you can actually see the message
        userLabel.setVisible(true);
        dateTimeLabel.setVisible(true);
        messageLabel.setVisible(true);
        
        // Adds the text to the JPanel
        this.add(userLabel);
        this.add(dateTimeLabel);
        this.add(messageLabel);
    }
    
    /**
     * Re linebreaks the message when the window is resized
     * @param width Width of the message area
     * @param pane Scrollbar details
     */
    public void reBreak(int width, JScrollPane pane) {
    	
    	// Shrinks the useable space by the length of the username, the date, and the scrollbar space taken up
    	width -= userLabel.getFontMetrics(userLabel.getFont()).stringWidth(userLabel.getText());
    	width -= dateTimeLabel.getFontMetrics(dateTimeLabel.getFont()).stringWidth(dateTimeLabel.getText());
    	width -= 50;
    	
    	// Allocates the amount of lines needed to fit the message
    	 messageLabel.setPreferredSize(new Dimension(width, 20 * (int)Math.ceil(messageLabel.getFontMetrics(messageLabel.getFont()).stringWidth(messageLabel.getText())/(double)width)));
    	 this.setMaximumSize(new Dimension(2000, 20 * (int)Math.ceil(messageLabel.getFontMetrics(messageLabel.getFont()).stringWidth(messageLabel.getText())/(double)width)));
    }
}
