package main.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import main.data.ChatRoom;
import main.data.Message;
import main.data.User;
import main.networking.NetworkInterface;
/**
 *
 *	This code is for the messaging window that appears when you click on a group
 *	or a user.
 *
 *	@author eandr127, biscuitseed, Stealth
 *
 */
public class MessagingWindow extends StatePanel {

	private static final long serialVersionUID = 1L;
	
	// List of online users, chats, and incoming messages.
	private Set<User> onlineUsers = new HashSet<>();
	private Set<ChatRoom> onlineChats = new HashSet<>();
	private List<Message> messages = new ArrayList<Message>();

	// List of user buttons to put in the user scroll list.
	private List<JButton> userButtons = new ArrayList<>();
	private final JPanel userListPanel = new JPanel();

	// List of group buttons to put in the group scroll list.
	private List<JButton> chatButtons = new ArrayList<>();
	private final JPanel chatListPanel = new JPanel();
	// For the user panel that appears in the top left.
	private JPanel myUserPanel = new JPanel();
	private final JScrollPane messagingPane;
	private final JPanel messagingWindow;
	// For highlighting the selected user/group
	private Optional<User> selectedUser = Optional.empty();
	private Optional<ChatRoom> selectedChat = Optional.empty();

	// For creating groups
	private String groupName;

	// Text box for typing messages
	private final PlaceHolderTextField sendMessages;
	// Button for sending the message, which is also clicked when return is pressed
	private final JButton sendMessageButton;
	// this button brings up a dialog box to add groups
	private final JButton addGroupButton;

	private Font font = null;

	// For choosing between users and groups
	private final JTabbedPane tabPanel;

	// List of incoming messages
	private final List<Message> messageQueue;
	private BufferedImage userIcon;

	private String nickname = null;

	// Audio files for users sending you messages, users joining, and users leaving
	private final Clip messageRecieved = getClip("/resources/sound/messagerecieved.wav");
	private final Clip mentionRecieved = getClip("/resources/sound/mentionrecieved.wav");
	private final Clip userJoined = getClip("/resources/sound/userjoined.wav");
	private final Clip userLeft = getClip("/resources/sound/userleft.wav");

	/**
	 * Loads resources and adds all the {@code JComponent}s for this panel
	 */
	public MessagingWindow() {

		// Set the font to Roboto-Mono, which is used for header-type stuff
		try {
			font = Font.createFont(Font.TRUETYPE_FONT,
					getClass().getResource("/resources/font/RobotoMono-Medium.ttf").openStream());
		} catch (FontFormatException | IOException e1) {
			e1.printStackTrace();
		}

		// Registers font
		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		genv.registerFont(font);
		font = font.deriveFont(15f);
		
    	this.setLayout(null);
    	this.setSize(800, 439);
    	this.setBackground(ColourConstants.BACKGROUND_COLOR);

    	// User list
        JScrollPane userScrollPane = new JScrollPane();
        userScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        userScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));
        userScrollPane.setViewportView(userListPanel);
        userScrollPane.setPreferredSize(new Dimension(300, 300));
        
        // Groups list
        JScrollPane chatScrollPane = new JScrollPane();
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatListPanel.setLayout(new BoxLayout(chatListPanel, BoxLayout.Y_AXIS));
        chatScrollPane.setViewportView(chatListPanel);
        chatScrollPane.setPreferredSize(new Dimension(300, 300));
      
        // Messaging window
        messagingWindow = new JPanel();
        messagingWindow.setLayout(new BoxLayout(messagingWindow, BoxLayout.Y_AXIS));
        
        // The scroll bar inside of the messaging area
        messagingPane = new JScrollPane();
        messagingPane.getVerticalScrollBar().setUnitIncrement(16);
        messagingPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messagingPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messagingPane.setViewportView(messagingWindow);
        messagingPane.setAlignmentY(BOTTOM_ALIGNMENT);
        messagingPane.setPreferredSize(new Dimension(300, 300));
        messagingPane.setVisible(false);
        messagingPane.setBounds(200, 0, getWidth() - 210, getHeight() - 10 - 100);
        
        messageQueue = new ArrayList<>();
        
        // Send messages placeholder
        sendMessages = new PlaceHolderTextField("Type a message here");
        sendMessages.setVisible(true);
        sendMessages.setLayout(null);
        
        // Send Message buttpm
        sendMessageButton = new JButton("Send");
        sendMessageButton.setVisible(true);
        sendMessageButton.setLayout(null);
        sendMessageButton.addActionListener(e -> {
            if (sendMessages.isVisible() && !sendMessages.isPlaceHolder()) {
                if(selectedChat.isPresent()) {
                    Message message = new Message(selectedChat.get(), null, sendMessages.getText(), ZonedDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC)), true);
                    if (message.message.length() > 1000) {
						JOptionPane.showMessageDialog(null, "Messages have a character limit of 1000! Shorten your message.", "Chill out my dude", JOptionPane.ERROR_MESSAGE);
                    } else {
                    messageQueue.add(message);
                    addMessage(message);
                    }
                }
                else if(selectedUser.isPresent()) {
                    Message message = new Message(selectedUser.get(), sendMessages.getText(), ZonedDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC)), true);
                    if (message.message.length() > 1000) {
                    	Component frame = null;
						JOptionPane.showMessageDialog(frame, "Messages have a character limit of 1000! Shorten your message.", "Chill out my dude", JOptionPane.ERROR_MESSAGE);
                    } else {
                    messageQueue.add(message);
                    addMessage(message);
                    }
                }
                sendMessages.reset();
                this.requestFocus();
                sendMessages.requestFocus();
            }
        });
      
        // Add group button  
        addGroupButton = new JButton("Add Group");
        addGroupButton.setVisible(true);
        addGroupButton.setLayout(null);

        // Fill each of the tabs with the chat list and user list
        chatScrollPane.setBounds(0, 0, 200, getHeight());
        userScrollPane.setBounds(0, 0, 200, getHeight());

        // Groups and users tab
        tabPanel = new JTabbedPane();
        tabPanel.addTab("Groups", chatScrollPane);
        tabPanel.addTab("Users", userScrollPane);
        
        // Don't see these until you start talking to someone
        sendMessages.setVisible(false);
        sendMessageButton.setVisible(false);

        // Add everything to the view
        this.add(tabPanel);
        this.add(sendMessages);
        this.add(sendMessageButton);
        this.add(messagingPane);
        this.add(addGroupButton);
        
        // Set all the ui colours
		userListPanel.setBackground(ColourConstants.BACKGROUND_COLOR);
		chatListPanel.setBackground(ColourConstants.BACKGROUND_COLOR);
		
		messagingPane.setBackground(ColourConstants.MESSAGING_PANE_BACKGROUND_COLOR);
		messagingWindow.setBackground(ColourConstants.MESSAGING_PANE_BACKGROUND_COLOR);
		
		sendMessages.setBackground(ColourConstants.LOGIN_RECTANGLE_COLOR);
		sendMessageButton.setBackground(ColourConstants.LOGIN_RECTANGLE_COLOR);
		
		tabPanel.setBackground(ColourConstants.BACKGROUND_COLOR);
		tabPanel.setForeground(Color.WHITE);
		
		sendMessages.setBorder(BorderFactory.createMatteBorder(0,0,0,0, ColourConstants.BACKGROUND_COLOR));
		sendMessageButton.setBorderPainted(false);
		sendMessageButton.setForeground(ColourConstants.FOCUSED_COLOR);
		
		addGroupButton.setBackground(ColourConstants.LOGIN_RECTANGLE_COLOR);
		addGroupButton.setOpaque(false);
		addGroupButton.setFont(font);
		addGroupButton.setBorderPainted(false);
		addGroupButton.setForeground(ColourConstants.FOCUSED_COLOR);
		
		addGroupButton.addActionListener(e -> {
			UIManager.put("OptionPane.background", ColourConstants.BACKGROUND_COLOR);
			UIManager.put("Panel.background", ColourConstants.BACKGROUND_COLOR);

			UIManager.put("OptionPane.messageForeground", Color.WHITE);
			groupName = JOptionPane.showInputDialog("What should this group be called?");
		});
		
		// Whenever the view is resized let the messaging area move out to the max size
		this.addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentResized(ComponentEvent e) {
				//When the window is resized make sure the elements are correctly positioned and scaled
				tabPanel.setBounds(0, 75, 200, getHeight() - 75);
				messagingPane.setBounds(200, 0, getWidth() - 200, getHeight() - 20);
				sendMessageButton.setBounds(getWidth() - 80, getHeight() - 20, 80, 20);
				addGroupButton.setBounds(3, 53, 197, 20);
				sendMessages.setBounds(200, getHeight() - 20, getWidth() - 280, 20);

			}

			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	public String getTitle() {
		// Set the title of the window
		return "jmessage - Messaging | Property of NewUnityProject Team!";
	}

	@Override
	public JButton getSubmitButton() {
		return sendMessageButton;
	}

	/**
	 * Makes change colour if selected or unread, makes sure only the messages for right selected user/chat are showing
	 */
	public void updateMessages() {
		// This is used to check for unread messages, and if so then highlight the user that sent them
		for (JButton userButton : userButtons) {
			if (selectedUser.isPresent()
					&& ((UserButtonPress) userButton.getActionListeners()[0]).user.equals(selectedUser.get())) {
				userButton.setBackground(ColourConstants.NEW_MESSAGES_BACKGROUND_COLOR);
			} else if (((UserButtonPress) userButton.getActionListeners()[0]).unread) {
				userButton.setBackground(Color.YELLOW);
			} else {
				userButton.setBackground(ColourConstants.BACKGROUND_COLOR);
			}
		}

		for (JButton chatButton : chatButtons) {
			if (selectedChat.isPresent()
					&& ((ChatButtonPress) chatButton.getActionListeners()[0]).chat.equals(selectedChat.get())) {
				chatButton.setBackground(ColourConstants.NEW_MESSAGES_BACKGROUND_COLOR);
			} else if (((ChatButtonPress) chatButton.getActionListeners()[0]).unread) {
				chatButton.setBackground(Color.YELLOW);
			} else {
				chatButton.setBackground(ColourConstants.BACKGROUND_COLOR);
			}
		}

		if (selectedChat.isPresent()) {
			ChatButtonPress button = (ChatButtonPress) getChatButtonByChat(selectedChat.get()).get().getActionListeners()[0];
			button.unread = false;

			// Resets the messages for recalculation purposes
			messagingWindow.removeAll();
			List<Message> allChatMessages = new ArrayList<>();
			for (Message m : messages) {
				if (m.chatRoom.isPresent() && m.chatRoom.get().equals(selectedChat.get())) {
					allChatMessages.add(m);

					boolean added = false;
					for (Component comp : messagingWindow.getComponents()) {
						if (comp.equals(m)) {
							added = true;
						}
					}

					if (!added) {
						m.setAlignmentX(LEFT_ALIGNMENT);
						messagingWindow.add(m, -1);
						messagingWindow.setAlignmentX(LEFT_ALIGNMENT);
					    // If you're in the chat that the message was added to, then drag the scrollbar to the bottom.
						JScrollBar vertical = messagingPane.getVerticalScrollBar();
						vertical.setValue(vertical.getMaximum());
					}

				}
			}

			for (Component comp : messagingWindow.getComponents()) {
				if (comp instanceof Message) {
					boolean remove = true;
					for (Message m : allChatMessages) {
						if (comp.equals(m)) {
							remove = false;
							break;
						}
					}
					if (remove) {
						this.remove(comp);
						for (Component sub : ((Container) comp).getComponents()) {
							this.remove(sub);
						}
					}
				}
			}

			if (!messagingPane.isVisible()) {
				messagingPane.setVisible(true);
				SwingUtilities.updateComponentTreeUI(this);
			}
		} else if (selectedUser.isPresent()) {
			
			UserButtonPress button = (UserButtonPress) getUserButtonByUser(selectedUser.get()).get().getActionListeners()[0];
			button.unread = false;

			// Reset the messages
			messagingWindow.removeAll();
			List<Message> allUserMessages = new ArrayList<>();

			for (Message m : messages) {
				m.reBreak(messagingPane.getWidth(), messagingPane);
				if (!m.chatRoom.isPresent() && m.user.equals(selectedUser.get())) {
					allUserMessages.add(m);

					boolean added = false;
					for (Component comp : messagingWindow.getComponents()) {
						if (comp == m) {
							added = true;
						}
					}

					if (!added) {
						m.setAlignmentX(LEFT_ALIGNMENT);
						messagingWindow.add(m, -1);
						messagingWindow.setAlignmentX(LEFT_ALIGNMENT);
                        // If you're in the chat that the message was added to, then drag the scrollbar to the bottom.
                        JScrollBar vertical = messagingPane.getVerticalScrollBar();
                        vertical.setValue(vertical.getMaximum());
					}
				}
			}

			for (Component comp : messagingWindow.getComponents()) {
				if (comp instanceof Message) {
					boolean remove = true;
					for (Message m : allUserMessages) {
						if (comp.equals(m)) {
							remove = false;
							break;
						}
					}
					if (remove) {
						this.remove(comp);
						for (Component sub : ((Container) comp).getComponents()) {
							this.remove(sub);
						}
					}
				}
			}

			if (!messagingPane.isVisible()) {
				messagingPane.setVisible(true);
				SwingUtilities.updateComponentTreeUI(this);
			}
		} else {
			if (messagingPane.isVisible()) {
				messagingPane.setVisible(false);
				sendMessages.setVisible(false);
				sendMessageButton.setVisible(false);
				SwingUtilities.updateComponentTreeUI(this);
			}
		}
		
		// If its not windows you have to resize to refresh for some stupid reason
        if(!System.getProperty("os.name").toLowerCase().contains("win")) {
            int width = this.getWidth();
            messagingPane.setSize(width + 1, this.getHeight());
            SwingUtilities.updateComponentTreeUI(messagingPane);
            messagingPane.setSize(width, this.getHeight());
            SwingUtilities.updateComponentTreeUI(messagingPane);
        }
	}

	/**
	 * Change information displayed for a user
	 * 
	 * @param user The user to update
	 * @param state The update
	 * @param network The network to get new user information for
	 */
	public void updateUser(User user, int state, NetworkInterface network) {
		switch (state) {
		case NetworkInterface.CHANGE_CONNECTED:
			// On user connecting
			// if the user trying to connect has the same name as me then tell them 'no, you can't do that'
			if (user.username.equals(User.getMe().username))
				break;
			onlineUsers.add(user);
			// otherwise, add them to the list of online users
			JButton userButton = createButtonForUser(user);
			// ...and create a button for them
			userListPanel.add(userButton);
			userButtons.add(userButton);
			// Finally, play the User joined sound
			playNewUser();
			break;
		case NetworkInterface.CHANGE_DISCONNECTED:
			if (user.username.equals(User.getMe().username))
				break;
			onlineUsers.remove(user);
			// Remove the user from the list of online users
			userListPanel.remove(getUserButtonByUser(user).orElse(null));
			userButtons.remove(getUserButtonByUser(user).orElse(null));
			if (selectedUser.isPresent() && selectedUser.get().equals(user)) {
				selectedUser = Optional.empty();
			}
			// Play the user left sound
			playLessUser();

			break;
		case NetworkInterface.CHANGE_CHANGED_NICKNAME:
			if (user.username.equals(User.getMe().username))
				break;
			/* Remove the user button and add a new one with the new
			 * nickname; the chats will stay as the username is the same.
			 */
			onlineUsers.remove(user);
			userListPanel.remove(getUserButtonByUser(user).orElse(null));
			userButtons.remove(getUserButtonByUser(user).orElse(null));
			onlineUsers.add(new User(user.username, network));
			userButton = createButtonForUser(user);
			userListPanel.add(userButton);
			userButtons.add(userButton);
			break;
		case NetworkInterface.CHANGE_CHANGED_PICTURE:
			if (user.username.equals(User.getMe().username))
				break;
			/* Remove the user button and add a new one with the new
			 * picture; the chats will stay as the username doesn't change.
			 */
			onlineUsers.remove(user);
			userListPanel.remove(getUserButtonByUser(user).orElse(null));
			userButtons.remove(getUserButtonByUser(user).orElse(null));
			onlineUsers.add(new User(user.username, network));
			userButton = createButtonForUser(user);
			userListPanel.add(userButton);
			userButtons.add(userButton);
			break;
		default:
			// If this happens then something is seriously wrong
			throw new IllegalArgumentException("Unknown user update state");
		}
	}

	/**
     * Change information displayed for a chat
     * 
     * @param chat The chat to update
     * @param state The update
     * @param network The network to get new user information for
     */
	public void updateChat(ChatRoom chat, int state, NetworkInterface network) {
		switch (state) {
		case NetworkInterface.CHANGE_CONNECTED:
			onlineChats.add(chat);
			JButton chatButton = createButtonForChat(chat);
			chatListPanel.add(chatButton);
			chatButtons.add(chatButton);
			break;
		case NetworkInterface.CHANGE_DISCONNECTED:
			onlineChats.remove(chat);
			chatListPanel.remove(getChatButtonByChat(chat).orElse(null));
			chatButtons.remove(getChatButtonByChat(chat).orElse(null));

			if (selectedChat.isPresent() && selectedChat.get().equals(chat)) {
				selectedChat = Optional.empty();
			}
			break;
		case NetworkInterface.CHANGE_CHANGED_NICKNAME:
			onlineChats.remove(chat);
			chatListPanel.remove(getChatButtonByChat(chat).orElse(null));
			chatButtons.remove(getChatButtonByChat(chat).orElse(null));
			onlineChats.add(chat);
			chatButton = createButtonForChat(chat);
			chatListPanel.add(chatButton);
			chatButtons.add(chatButton);
			break;
		default:
			throw new IllegalArgumentException("Unknown user update state");
		}
	}

	/**
	 * Makes buttons for all of the chats and adds them to the window
	 * 
	 * @param chats The chats to display
	 */
	public void initializeChatRoomSet(Set<ChatRoom> chats) {
		onlineChats = chats;
		for (ChatRoom chat : onlineChats) {
			JButton userButton = createButtonForChat(chat);
			chatListPanel.add(userButton);
			chatButtons.add(userButton);
		}
	}

	// Initialize the user buttons
	/**
     * Makes buttons for all of the users and adds them to the window
     * 
     * @param users The users to display
     */
	public void initializeUserSet(Set<User> users) {
		myUserPanel = createUserProfile();
		myUserPanel.setLayout(null);
		myUserPanel.setBounds(0, 0, 200, 50);
		this.add(myUserPanel);
		onlineUsers = users;

		for (User user : onlineUsers) {
			if (user.username.equals(User.getMe().username))
				continue;
			JButton userButton = createButtonForUser(user);
			userListPanel.add(userButton);
			userButtons.add(userButton);
		}
	}

	/**
	 * Set up the panel for the self-user in the top left
	 * 
	 * @return The panel with user information
	 */
	public JPanel createUserProfile() {
		User user = User.getMe();
		
		// Unclickable panel
		JPanel userPanel = new JPanel();
		userPanel.setLayout(null);
		userPanel.setMaximumSize(new Dimension(2000, 50));
		userPanel.setOpaque(false);
		
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = ImageIO.read(this.getClass().getResourceAsStream("/resources/server-icon.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedImage newimg = User.scaleImage(bufferedImage);
		JButton button = new JButton();
		button.setIcon(new ImageIcon(newimg));
		button.setBounds(10, 10, 30, 30);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BufferedImage bufferedImage = getImageFromFileSystem();
				if (bufferedImage != null) {
					bufferedImage = User.scaleImage(bufferedImage);
					button.setIcon(new ImageIcon(bufferedImage));
				}
				userIcon = bufferedImage;
			}
		});
		
		// Your nickname
		JTextField nickname = new JTextField(user.nickname);
		nickname.setBackground(ColourConstants.BACKGROUND_COLOR);
		nickname.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, ColourConstants.BACKGROUND_COLOR));
		nickname.setFont(font);
		nickname.setForeground(ColourConstants.FOCUSED_COLOR);
		nickname.setBounds(50, 15, 140, 20);
		nickname.addActionListener((e) -> {
			if (e.getActionCommand().isEmpty()) {
				nickname.setText(user.nickname);
				this.requestFocus();
			} else if(e.getActionCommand().length() < 40) {
				this.nickname = e.getActionCommand();
				this.requestFocus();
			}
			else {
			    JOptionPane.showMessageDialog(null, "Nicknames have a character limit of 40! Shorten your message.", "Chill out my dude", JOptionPane.ERROR_MESSAGE);
	            nickname.setText(user.nickname);
	            this.requestFocus();
			}
		});
		
		userPanel.add(button);
		userPanel.add(nickname);

		return userPanel;
	}

	@Override
	// This is for drawing the rounded rectangles around the Add Group button and the self-user profile
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(ColourConstants.LOGIN_RECTANGLE_COLOR);
		((Graphics2D) g).setStroke(new BasicStroke(2));
		g.drawRoundRect(3, 3, 197, 47, 10, 10);// paint border
		g.fillRoundRect(3, 53, 197, 20, 10, 10);
	}

	
	
	/**
	 * Adds message to the messaging window
	 * 
	 * @param message The message to add
	 */
	public void addMessage(Message message) {
		message.reBreak(messagingPane.getWidth(), messagingPane);
		messages.add(message);
		// Otherwise, if you're not in the chat, make the unread notification go off for the user that sent the message.
		if (message.chatRoom.isPresent()
				&& (!selectedChat.isPresent() || !message.chatRoom.get().equals(selectedChat.get()))) {
			Optional<JButton> button = getChatButtonByChat(message.chatRoom.get());
			if (button.isPresent()) {
				((ChatButtonPress) button.get().getActionListeners()[0]).unread = true;
			}
		} else if (!message.chatRoom.isPresent() && (!selectedUser.isPresent() || !message.user.equals(selectedUser.get()))) {
			Optional<JButton> button = getUserButtonByUser(message.user);
			if (button.isPresent()) {
				((UserButtonPress) button.get().getActionListeners()[0]).unread = true;
			}
		}
	}
	
	// When a user joins, create a button for them.
	private JButton createButtonForUser(User user) {
		
		JButton userButton = new JButton();
		userButton.setLayout(null);
		userButton.setMaximumSize(new Dimension(2000, 50));
		userButton.setBackground(ColourConstants.BACKGROUND_COLOR);
		userButton.addActionListener(new UserButtonPress(user));
		// Set the user profile picture to the default
		BufferedImage bufferedImage = null;
		try {
			if (user.image != null) {
				bufferedImage = user.image;
			} else {
				bufferedImage = ImageIO.read(this.getClass().getResourceAsStream("/resources/server-icon.png"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		ImageIcon imageIcon = new ImageIcon(bufferedImage);
		Image image = imageIcon.getImage();
		Image newimg = image.getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH);
		imageIcon = new ImageIcon(newimg);

		JLabel profilePic = new JLabel(imageIcon);
		profilePic.setBounds(10, 10, 30, 30);
		
		JLabel nickname = new JLabel(user.nickname);
		nickname.setFont(font);
		nickname.setForeground(ColourConstants.FOCUSED_COLOR);
		nickname.setBounds(50, 15, 140, 20);
		nickname.setAlignmentY(CENTER_ALIGNMENT);

		userButton.add(profilePic);
		userButton.add(nickname);
		return userButton;
	}

	/**
	 * Show the messages of the user
	 * 
	 * @param user The user to show the messages for
	 */
	public void showMessages(User user) {
		selectedChat = Optional.empty();
		selectedUser = Optional.of(user);
		sendMessages.setVisible(true);
		sendMessageButton.setVisible(true);
	}
	
	private Optional<JButton> getUserButtonByUser(User user) {
	    // Finds a JButton for a given user by looking at an ActionListener that has the user in it
		for (JButton userButton : userButtons) {
			for (ActionListener listener : userButton.getActionListeners()) {
				if (listener instanceof UserButtonPress && ((UserButtonPress) listener).user.equals(user)) {
					return Optional.of(userButton);
				}
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Shows messages for a user when pressed, also identifies the user that the button is for
	 */
	private class UserButtonPress implements ActionListener {

		private final User user;
		
		/**
		 * Whether the user has unread messages
		 */
		public boolean unread = false;

		/**
		 * Sets the user to show messages for
		 * 
		 * @param user The user
		 */
		public UserButtonPress(User user) {
			this.user = user;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			sendMessages.reset();
			showMessages(user);
		}
	}

	// When a new chat is added create the button for it
	private JButton createButtonForChat(ChatRoom chat) {
		JButton userButton = new JButton(chat.name + " (#" + chat.id + ")");
		userButton.setLayout(null);
		userButton.setHorizontalAlignment(SwingConstants.LEFT);
		userButton.setMaximumSize(new Dimension(2000, 50));
		userButton.setBackground(ColourConstants.BACKGROUND_COLOR);
		userButton.addActionListener(new ChatButtonPress(chat));
		userButton.setFont(font);
		userButton.setForeground(ColourConstants.FOCUSED_COLOR);
		return userButton;
	}

	/**
     * Show the messages of the chat
     * 
     * @param chat The chat to show the messages for
     */
	public void showMessages(ChatRoom chat) {
		selectedChat = Optional.of(chat);
		selectedUser = Optional.empty();
		sendMessages.setVisible(true);
		sendMessageButton.setVisible(true);
	}
	
	private Optional<JButton> getChatButtonByChat(ChatRoom chat) {
	    // Finds a JButton for a given chat by looking at an ActionListener that has the chat in it
		for (JButton chatButton : chatButtons) {
			for (ActionListener listener : chatButton.getActionListeners()) {
				if (listener instanceof ChatButtonPress && ((ChatButtonPress) listener).chat.equals(chat)) {
					return Optional.of(chatButton);
				}
			}
		}
		return Optional.empty();
	}

	/**
     * Shows messages for a user when pressed, also identifies the user that the button is for
     */
	private class ChatButtonPress implements ActionListener {

		private final ChatRoom chat;
		
         /**
          * Whether the user has unread messages
          */
		public boolean unread = false;

	    /**
         * Sets the user to show messages for
         * 
         * @param user The user
         */
		public ChatButtonPress(ChatRoom chat) {
			this.chat = chat;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			sendMessages.reset();
			showMessages(chat);
		}
	}

	/**
	 * Gets a copy of the incoming messages queue
	 * 
	 * @return The copy of {@code messageQueue}
	 */
	public List<Message> getQueuedMessages() {
		return new ArrayList<>(messageQueue);
	}

	/**
	 * Remove the all incoming messages
	 */
	public void emptyQueuedMessages() {
		while (messageQueue.size() != 0)
			messageQueue.remove(0);
	}

	/**
	 * Update the tab panel for groups and users
	 */
	public void updateComponents() {
		SwingUtilities.updateComponentTreeUI(tabPanel);
	}

	// For when a user clicks on their profile picture to choose a new one
	private BufferedImage getImageFromFileSystem() {

		JFrame fileFrame = new JFrame();
		fileFrame.setSize(500, 500);
		fileFrame.setIconImages(Window.ICONS);

		JFileChooser fileChooser = new JFileChooser();

		fileChooser.setDialogTitle("Choose a profile picture");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes()));
		fileChooser.showOpenDialog(fileFrame);
		File imageFilename = fileChooser.getSelectedFile();
		if (imageFilename != null) {
			try {
				return ImageIO.read(imageFilename);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}

	/**
	 * Returns a newly set user profile picture, otherwise null
	 * 
	 * @return The new profile picture
	 */
	public BufferedImage getNewIcon() {
		BufferedImage returnImage = userIcon;
		userIcon = null;
		return returnImage;
	}

	/**
     * Returns a newly set user nickname, otherwise null
     * 
     * @return The new nickname
     */
	public String getNewNickname() {
		String returnString = nickname;
		nickname = null;
		return returnString;
	}

	// Import audio clips
	private static Clip getClip(String location) {
		try {
			Clip clip = AudioSystem.getClip(null);

			InputStream in = MessagingWindow.class.getResourceAsStream(location);
			InputStream bufferedIn = new BufferedInputStream(in);
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);

			clip.open(audioStream);
			return clip;
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Play audio for getting a new message from a user
	 */
	public void playNewMessage() {
		messageRecieved.setFramePosition(0);
		messageRecieved.start();
	}
	/**
	 * Play audio for getting a new mention from a user
	 */
	public void playNewMention() {
		mentionRecieved.setFramePosition(0);
		mentionRecieved.start();
	}

	/**
	 * Play audio for a new user joining jmessage
	 */
	public void playNewUser() {
		messageRecieved.setFramePosition(0);
		userJoined.start();
	}

	/**
	 * Play audio for a user leaving jmessage
	 */
	public void playLessUser() {
		messageRecieved.setFramePosition(0);
		userLeft.start();
	}

	/**
     * Returns a newly create chat, otherwise null
     * 
     * @return The new chat
     */
	public String getNewGroup() {
		String returnGroup = groupName;
		groupName = null;
		return returnGroup;
	}
}
