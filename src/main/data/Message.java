package main.data;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import main.gui.ColourConstants;

/**
 * 
 * Stores data for each specific message
 *
 */
public class Message extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * The sender or receiver of the message
     */
    public final User user;
    
    /**
     * The addressed chat. This may be empty if the message was
     * addressed to was from a user.
     */
    public final Optional<ChatRoom> chatRoom;
    
    /**
     * The contents of the message
     */
    public String message;
    
    /**
     * The time the message was sent in UTC
     */
    public final ZonedDateTime dateTime;
    
    /**
     * Whether the message is being sent directly to the 
     */
    public final boolean loopback;
    
    private JTextArea messageLabel;
    private JLabel dateTimeLabel;
    private JLabel userLabel;
    
    /**
     * Constructs a message with a chatroom
     * 
     * @param chatRoom The chatroom the message is from or is going to
     * @param user The user sending or receiving the message
     * @param message The contents of the message
     * @param dateTime UTC date time the message was sent
     */
    public Message(ChatRoom chatRoom, User user, String message, ZonedDateTime dateTime) {
        this(chatRoom, user, message, dateTime, false);
    }
    
    
    /**
     * Constructs a message with a chatroom looped back
     * 
     * @param chatRoom The chatroom the message is from or is going to
     * @param user The user sending or receiving the message
     * @param message The contents of the message
     * @param dateTime UTC date time the message was sent
     * @param loopback Whether this message should be put into the window directly without receiving it from the server
     */
    public Message(ChatRoom chatRoom, User user, String message, ZonedDateTime dateTime, boolean loopback) {
        this(Optional.of(chatRoom), user, message, dateTime, loopback);
    }
    
    /**
     * Constructs a message with a user
     * 
     * @param user The user sending or receiving the message
     * @param message The contents of the message
     * @param dateTime UTC date time the message was sent
     */
    public Message(User user, String message, ZonedDateTime dateTime) {
        this(user, message, dateTime, false);
    }
    
    /**
     * Constructs a message with a user looped back
     * 
     * @param user The user sending or receiving the message
     * @param message The contents of the message
     * @param dateTime UTC date time the message was sent
     * @param loopback Whether this message should be put into the window directly without receiving it from the server
     */
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
        if(loopback)
            userLabel.setText("You, at");
        else
            userLabel.setText(user.nickname + " at");

        messageLabel = new JTextArea(message);
        
        
        
        messageLabel.setLineWrap(true);
        messageLabel.setWrapStyleWord(true);
        
        messageLabel.setMinimumSize(new Dimension(messageLabel.getMinimumSize().width, 20));
        
        dateTimeLabel = new JLabel(dateTime.withZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("hh:mma")) + " said: ");
        
        // Coloring of text
        this.setBackground(ColourConstants.MESSAGING_PANE_BACKGROUND_COLOR);
        messageLabel.setForeground(ColourConstants.MESSAGE_COLOR);
        userLabel.setForeground(ColourConstants.MESSAGE_INFO_COLOR);
        dateTimeLabel.setForeground(ColourConstants.MESSAGE_INFO_COLOR);
        
        // Tells the BoxLayout not to fill the page with one message
        this.setMinimumSize(new Dimension(this.getMinimumSize().width, 20));
        
        messageLabel.setOpaque(false);
        messageLabel.setEditable(false);
        
        // Adds the text to the JPanel
        this.add(userLabel);
        this.add(dateTimeLabel);
        this.add(messageLabel);
    }
    
    /**
     * Re linebreaks the message when the window is resized
     * 
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
    }
}
