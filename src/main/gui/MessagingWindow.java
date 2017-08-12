package main.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
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
import java.io.File;
import java.io.IOException;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import main.data.ChatRoom;
import main.data.Message;
import main.data.User;
import main.networking.NetworkInterface;

public class MessagingWindow extends StatePanel {
    
    private static final long serialVersionUID = 1L;
    
    private Set<User> onlineUsers = new HashSet<>();
    private Set<ChatRoom> onlineChats = new HashSet<>();
    private List<Message> messages = new ArrayList<Message>();
    
    private List<JButton> userButtons = new ArrayList<>();
    private final JPanel userListPanel = new JPanel();
    
    private List<JButton> chatButtons = new ArrayList<>();
    private final JPanel chatListPanel = new JPanel();
    private JPanel myUserPanel = new JPanel();
    private final JScrollPane messagingPane;
    private final JPanel messagingWindow;
    private Optional<User> selectedUser = Optional.empty();
    private Optional<ChatRoom> selectedChat = Optional.empty();
    
    private final PlaceHolderTextField sendMessages;
    private final JButton sendMessageButton;
    
    private final JButton addGroupButton;
    
    Font font = null;

    private final JTabbedPane tabPanel;
    
    private final List<Message> messageQueue;
    private BufferedImage userIcon;
    
    String nickname = null;
    
    private final Clip messageRecieved = getClip("src/resources/sound/messagerecieved.wav");
    private final Clip userJoined = getClip("src/resources/sound/userjoined.wav");
    private final Clip userLeft = getClip("src/resources/sound/userleft.wav");
    
    public MessagingWindow() {
    	
    	
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResource("/resources/font/RobotoMono-Medium.ttf").openStream());
		} catch (FontFormatException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}   

		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		genv.registerFont(font);
		font = font.deriveFont(15f);
		
    	this.setLayout(null);
    	this.setSize(800, 439);
    	this.setBackground(new Color(60, 60, 60));
        JScrollPane userScrollPane = new JScrollPane();
        userScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        userScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));
        userScrollPane.setViewportView(userListPanel);
        userScrollPane.setPreferredSize(new Dimension(300, 300));
        
        
        JScrollPane chatScrollPane = new JScrollPane();
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatListPanel.setLayout(new BoxLayout(chatListPanel, BoxLayout.Y_AXIS));
        chatScrollPane.setViewportView(chatListPanel);
        chatScrollPane.setPreferredSize(new Dimension(300, 300));
      
        
        messagingWindow = new JPanel();
        
        messagingPane = new JScrollPane();
        messagingPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messagingPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        messagingWindow.setLayout(new BoxLayout(messagingWindow, BoxLayout.Y_AXIS));
        messagingPane.setViewportView(messagingWindow);
        messagingPane.setAlignmentY(BOTTOM_ALIGNMENT);
        messagingPane.setPreferredSize(new Dimension(300, 300));
        messagingPane.setVisible(false);
        messagingPane.setBounds(200, 0, getWidth() - 210, getHeight() - 10 - 100);
        
        messageQueue = new ArrayList<>();
        
        sendMessages = new PlaceHolderTextField("Type a message here");
        sendMessages.setVisible(true);
        sendMessages.setLayout(null);
        sendMessageButton = new JButton("Send");
        sendMessageButton.setVisible(true);
        sendMessageButton.addActionListener(e -> {
            if (sendMessages.isVisible() && !sendMessages.isPlaceHolder()) {
                if(selectedChat.isPresent()) {
                    Message message = new Message(selectedChat.get(), null, sendMessages.getText(), ZonedDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC)), true);
                    messageQueue.add(message);
                    addMessage(message);
                }
                else if(selectedUser.isPresent()) {
                    Message message = new Message(selectedUser.get(), sendMessages.getText(), ZonedDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC)), true);
                    messageQueue.add(message);
                    addMessage(message);
                }
                sendMessages.reset();
                this.requestFocus();
                sendMessages.requestFocus();
            }
        });
        sendMessageButton.setLayout(null);
        addGroupButton = new JButton("Add Group");
        addGroupButton.setVisible(true);
        addGroupButton.addActionListener(e -> {
        	
        });
        addGroupButton.setLayout(null);
        JPanel tab1 = new JPanel();
        tab1.setLayout(null);
        chatScrollPane.setBounds(0, 0, 200, getHeight());
        userScrollPane.setBounds(0, 0, 200, getHeight());
        	tab1.add(chatScrollPane);
        	tabPanel = new JTabbedPane();
        	tabPanel.addTab("Groups", chatScrollPane);
        	tabPanel.addTab("Users", userScrollPane);
        this.add(tabPanel);
        sendMessages.setVisible(false);
        sendMessageButton.setVisible(false);
        this.add(sendMessages);
        this.add(sendMessageButton);
       // this.add(userScrollPane);
        //this.add(chatScrollPane);
        this.add(messagingPane);
        this.add(addGroupButton);
        
        //set all the ui colours
		userListPanel.setBackground(new Color(60,60,60));
		messagingPane.setBackground(new Color(40, 40, 40));
		messagingWindow.setBackground(new Color (40, 40, 40));
		sendMessages.setBackground(new Color(78, 78, 78));
		sendMessageButton.setBackground(new Color(78, 78, 78));
		tabPanel.setBackground(new Color(60, 60, 60));
		tabPanel.setForeground(Color.WHITE);
		sendMessages.setBorder(BorderFactory.createMatteBorder(0,0,0,0, new Color(105,105,105)));
		sendMessageButton.setBorderPainted(false);
		sendMessageButton.setForeground(new Color(160,160,160));
		addGroupButton.setBackground(new Color(78,78,78));
		addGroupButton.setOpaque(false);
		addGroupButton.setFont(font);
		addGroupButton.setBorderPainted(false);
		addGroupButton.setForeground(new Color(160,160,160));
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
				// TODO Auto-generated method stub
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
        return "jmessage - Messaging | Property of NewUnityProject Team!";
    }

    @Override
    public JButton getSubmitButton() {
        return sendMessageButton;
    }
    
    public void updateMessages() {
        if(selectedChat.isPresent()) {
        	 messagingWindow.removeAll();
            List<Message> allChatMessages = new ArrayList<>();
            for(Message m : messages) {
                if(m.chatRoom.isPresent() && m.chatRoom.get().equals(selectedChat.get())) {
                    allChatMessages.add(m);
                    
                    boolean added = false;
                    for(Component comp : messagingWindow.getComponents()) {
                        if(comp.equals(m)) {
                            added = true;
                        }
                    }
                    
                    if(!added) {
                    	m.setAlignmentX(LEFT_ALIGNMENT);
                    	messagingWindow.add(m,-1);
                    	messagingWindow.setAlignmentX(LEFT_ALIGNMENT);
                    }
                        
                }
            }
            
            for(Component comp : messagingWindow.getComponents()) {
                if(comp instanceof Message) {
                    boolean remove = true;
                    for(Message m : allChatMessages) {
                        if(comp.equals(m)) {
                            remove = false;
                            break;
                        }
                    }
                    if(remove) {
                        this.remove(comp);
                        for(Component sub : ((Container)comp).getComponents()) {
                            this.remove(sub);
                        }
                    }
                }
            }
            
            if(!messagingPane.isVisible()) {
                messagingPane.setVisible(true);
                SwingUtilities.updateComponentTreeUI(this);
            };
        }
        else if(selectedUser.isPresent()) {
        	messagingWindow.removeAll();
            List<Message> allUserMessages = new ArrayList<>();
            
            for(Message m : messages) {
            	m.reBreak(messagingPane.getWidth());
                if(!m.chatRoom.isPresent() && m.user.equals(selectedUser.get())) {
                    allUserMessages.add(m);
                    
                    boolean added = false;
                    for(Component comp : messagingWindow.getComponents()) {
                        if(comp == m) {
                            added = true;
                        }
                    }
                    
                    if(!added) {
                        m.setAlignmentX(LEFT_ALIGNMENT);
                        messagingWindow.add(m,-1);
                        messagingWindow.setAlignmentX(LEFT_ALIGNMENT);
                    }
                }
            }
            
            for(Component comp : messagingWindow.getComponents()) {
                if(comp instanceof Message) {
                    boolean remove = true;
                    for(Message m : allUserMessages) {
                        if(comp.equals(m)) {
                            remove = false;
                            break;
                        }
                    }
                    if(remove) {
                        this.remove(comp);
                        for(Component sub : ((Container)comp).getComponents()) {
                            this.remove(sub);
                        }
                    }
                }
            }
            
            if(!messagingPane.isVisible()) {
                messagingPane.setVisible(true);
                SwingUtilities.updateComponentTreeUI(this);
            }
        }
        else {
            if(messagingPane.isVisible()) {
                messagingPane.setVisible(false);
                sendMessages.setVisible(false);
                sendMessageButton.setVisible(false);
                SwingUtilities.updateComponentTreeUI(this);
            }
            
            return;
        }
    }
    
    public void updateUser(User user, int state, NetworkInterface network) {
        switch(state) {
            case NetworkInterface.CHANGE_CONNECTED:
            	if(user.username.equals(User.getMe().username))
            		break;
                onlineUsers.add(user);
                JButton userButton = createButtonForUser(user);
                userListPanel.add(userButton);
                userButtons.add(userButton);
                playNewUser();
            break;
            case NetworkInterface.CHANGE_DISCONNECTED:
            	if(user.username.equals(User.getMe().username))
            		break;
                onlineUsers.remove(user);
                userListPanel.remove(getUserButtonByUser(user).orElse(null));
                userButtons.remove(getUserButtonByUser(user).orElse(null));
                if(selectedUser.isPresent() && selectedUser.get().equals(user)) {
                    selectedUser = Optional.empty();
                }
                playLessUser();
                
            break;
            case NetworkInterface.CHANGE_CHANGED_NICKNAME:
            	if(user.username.equals(User.getMe().username))
            		break;
                onlineUsers.remove(user);
                userListPanel.remove(getUserButtonByUser(user).orElse(null));
                userButtons.remove(getUserButtonByUser(user).orElse(null));
                onlineUsers.add(new User(user.username, network));
                userButton = createButtonForUser(user);
                userListPanel.add(userButton);
                userButtons.add(userButton);
            break;
            case NetworkInterface.CHANGE_CHANGED_PICTURE:
                if(user.username.equals(User.getMe().username))
                    break;
                onlineUsers.remove(user);
                userListPanel.remove(getUserButtonByUser(user).orElse(null));
                userButtons.remove(getUserButtonByUser(user).orElse(null));
                onlineUsers.add(new User(user.username, network));
                userButton = createButtonForUser(user);
                userListPanel.add(userButton);
                userButtons.add(userButton);
            break;
            default:
                throw new IllegalArgumentException("Unknown user update state");
        }
    }
    
    public void updateChat(ChatRoom chat, int state, NetworkInterface network) {
        switch(state) {
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
                
                if(selectedChat.isPresent() && selectedChat.get().equals(chat)) {
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
    
    public void initializeChatRoomSet(Set<ChatRoom> chats) {
        onlineChats = chats;
        
        for(ChatRoom chat : onlineChats) {
            JButton userButton = createButtonForChat(chat);
            chatListPanel.add(userButton);
            chatButtons.add(userButton);
        }
    }
    
    public void initializeUserSet(Set<User> users) {
    	   myUserPanel = createUserProfile();
    	   myUserPanel.setLayout(null);
           
       	myUserPanel.setBounds(0, 0, 200, 50);
           this.add(myUserPanel);
        onlineUsers = users;
        
        for(User user : onlineUsers) {
        	if(user.username.equals(User.getMe().username))
        		continue;
            JButton userButton = createButtonForUser(user);
            userListPanel.add(userButton);
            userButtons.add(userButton);
        }
    }
    public JPanel createUserProfile() {
    	 User user = User.getMe();
    	 JPanel userButton = new JPanel();
         userButton.setLayout(null);
         userButton.setMaximumSize(new Dimension(2000, 50));
         userButton.setOpaque(false);
         BufferedImage bufferedImage = null;
         try {
         	bufferedImage = ImageIO.read(new File("src/resources/server-icon.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
         Image newimg = bufferedImage.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
         JButton button = new JButton();
         button.setIcon(new ImageIcon(newimg));
         button.setBounds(10, 10, 30, 30);
         button.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 BufferedImage bufferedImage = getImageFromFileSystem();
                 if(bufferedImage != null) {
                     Image newimg = bufferedImage.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
                     button.setIcon(new ImageIcon(newimg));
                 }
                 userIcon = bufferedImage;
             }
         });
         JTextField nickname = new JTextField(user.nickname);
         nickname.setBackground(new Color(60,60,60));
         nickname.setBorder(BorderFactory.createMatteBorder(0,0,0,0, new Color(105,105,105)));
         nickname.setFont(font);
         nickname.setForeground(new Color(160,160,160));
         nickname.setBounds(50,15,140,20);
         nickname.addActionListener((e) -> {
             if(e.getActionCommand().isEmpty()) {
                 nickname.setText(user.nickname);
                 this.requestFocus();
             }
             else {
                 this.nickname = e.getActionCommand();
                 this.requestFocus();
             }
         });
         userButton.add(button);
         userButton.add(nickname);
         
         return userButton;
    }
    @Override
	public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(new Color(78,78,78));
			((Graphics2D) g).setStroke(new BasicStroke(2));
			g.drawRoundRect(3, 3, 197, 47, 10, 10);//paint border
			g.fillRoundRect(3, 53, 197, 20, 10, 10);
	}
    public void addMessage(Message message) {
    	message.reBreak(messagingPane.getWidth());
        messages.add(message);
    }
    
    public JButton createButtonForUser(User user) {
        JButton userButton = new JButton();
        userButton.setLayout(null);
        userButton.addActionListener(new UserButtonPress(user));
        userButton.setMaximumSize(new Dimension(2000, 50));
        userButton.setBackground(new Color(60,60,60));
        BufferedImage bufferedImage = null;
        try {
            if(user.image != null) {
                bufferedImage = user.image;
            }
            else {
                bufferedImage = ImageIO.read(new File("src/resources/server-icon.png"));
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
        ImageIcon imageIcon = new ImageIcon(bufferedImage);
        Image image = imageIcon.getImage(); // transform it 
        Image newimg = image.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
        imageIcon = new ImageIcon(newimg);  // transform it back
        
        JLabel profilePic = new JLabel(imageIcon);
        profilePic.setBounds(10, 10, 30, 30);
        JLabel nickname = new JLabel(user.nickname);
        nickname.setFont(font);
        nickname.setForeground(new Color(160,160,160));
        nickname.setBounds(50,15,140,20);
        nickname.setAlignmentY(CENTER_ALIGNMENT);
        
        userButton.add(profilePic);
        userButton.add(nickname);
        return userButton;
    }
    
    public void showMessages(User user) {
        selectedChat = Optional.empty();
        selectedUser = Optional.of(user);
        sendMessages.setVisible(true);
        sendMessageButton.setVisible(true);
    }
    
    private Optional<JButton> getUserButtonByUser(User user) {
        for(JButton userButton : userButtons) {
            for(ActionListener listener : userButton.getActionListeners()) {
                if(listener instanceof UserButtonPress && ((UserButtonPress)listener).user.equals(user)) { 
                    return Optional.of(userButton);
                }
            }
        }
        return Optional.empty();
    }
    
    private class UserButtonPress implements ActionListener {

        private final User user;
        
        public UserButtonPress(User user) {
            this.user = user;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            sendMessages.reset();
            showMessages(user);
        }
    }
    
    
    public JButton createButtonForChat(ChatRoom chat) {
        JButton userButton = new JButton(chat.name + " (#" + chat.id + ")");
        userButton.addActionListener(new ChatButtonPress(chat));
        return userButton;
    }
    
    public void showMessages(ChatRoom chat) {
        System.out.println("Showing messages for " + chat.name + " (#" + chat.id + ")");
        selectedChat = Optional.of(chat);
        selectedUser = Optional.empty();
        sendMessages.setVisible(true);
        sendMessageButton.setVisible(true);
    }
    
    private Optional<JButton> getChatButtonByChat(ChatRoom chat) {
        for(JButton chatButton : chatButtons) {
            for(ActionListener listener : chatButton.getActionListeners()) {
                if(listener instanceof ChatButtonPress && ((ChatButtonPress)listener).chat.equals(chat)) { 
                    return Optional.of(chatButton);
                }
            }
        }
        return Optional.empty();
    }
    
    private class ChatButtonPress implements ActionListener {

        private final ChatRoom chat;
        
        public ChatButtonPress(ChatRoom chat) {
            this.chat = chat;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            sendMessages.reset();
            showMessages(chat);
        }
    }
    
    public List<Message> getQueuedMessages() {
        return new ArrayList<>(messageQueue);
    }
    
    public void emptyQueuedMessages() {
        while(messageQueue.size() != 0) messageQueue.remove(0);
    }
    
    public void setSendMessagesOnTop() {
        boolean wasFocused = sendMessages.hasFocus();
        boolean textWasVisible = sendMessages.isVisible();
        boolean buttonWasVisible = sendMessageButton.isVisible();
        
        
        this.remove(sendMessages);
        this.add(sendMessages);
        this.remove(sendMessageButton);
        this.add(sendMessageButton);
        
        if(wasFocused) {
            sendMessages.requestFocus();
        }
        sendMessages.setVisible(textWasVisible);
        sendMessageButton.setVisible(buttonWasVisible);
    }
    
    public void updateComponents() {
        SwingUtilities.updateComponentTreeUI(tabPanel);
    }
    
    public BufferedImage getImageFromFileSystem() {
        JFrame fileFrame = new JFrame();
        fileFrame.setSize(0, 0);
        FileDialog fileDialog = new FileDialog(fileFrame, "Choose a profile picture", FileDialog.LOAD);
        String fileTypes = "";
        for(String reader : ImageIO.getReaderFileSuffixes()) {
            fileTypes += "*." + reader + ";";
        }
        fileTypes = fileTypes.substring(0, fileTypes.length() - 1);
        
        fileDialog.setFile(fileTypes);
        fileDialog.setVisible(true);
        
        
        String imageFilename = fileDialog.getFile();
        if(imageFilename != null) {
            try {
                return ImageIO.read(new File(fileDialog.getDirectory(), imageFilename));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        return null;
    }
    
    public BufferedImage getNewIcon() {
        BufferedImage returnImage = userIcon;
        userIcon = null;
        return returnImage;
    }
    
    public String getNewNickname() {
        String returnString = nickname;
        nickname = null;
        return returnString;
    }
    
    private static Clip getClip(String location) {
        try {
            Clip clip = AudioSystem.getClip();
            AudioInputStream in = AudioSystem.getAudioInputStream(new File(location));
            clip.open(in);
            return clip;
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public void playNewMessage() {
        messageRecieved.start();
    }
    
    public void playNewUser() {
        userJoined.start();
    }
    
    public void playLessUser() {
        userLeft.start();
    }
}
