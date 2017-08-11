package main.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
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

    private final JScrollPane messagingPane;
    private final JPanel messagingWindow;
    private Optional<User> selectedUser = Optional.empty();
    private Optional<ChatRoom> selectedChat = Optional.empty();
    
    private final PlaceHolderTextField sendMessages;
    private final JButton sendMessageButton;
    
    private final List<Message> messageQueue;
    
    public MessagingWindow() {
    	this.setLayout(null);
    	this.setSize(800, 439);
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
        messagingPane.setPreferredSize(new Dimension(300, 300));
        messagingPane.setVisible(false);
        messagingPane.setBounds(200, 0, getWidth() - 210, getHeight() - 10);
        
        messageQueue = new ArrayList<>();
        
        sendMessages = new PlaceHolderTextField("Type a message here");
        sendMessages.setVisible(false);
        sendMessageButton = new JButton("Send");
        sendMessageButton.setVisible(false);
        sendMessageButton.addActionListener(e -> {
            if (sendMessages.isVisible() && !sendMessages.isPlaceHolder()) {
                if(selectedChat.isPresent()) {
                    messageQueue.add(new Message(selectedChat.get(), null, sendMessages.getText(), ZonedDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC))));
                }
                else if(selectedUser.isPresent()) {
                    messageQueue.add(new Message(selectedUser.get(), sendMessages.getText(), ZonedDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC))));
                }
                sendMessages.reset();
                this.grabFocus();
            }
        });
        JPanel tab1 = new JPanel();
        tab1.setLayout(null);
        chatScrollPane.setBounds(0, 0, 200, getHeight());
        userScrollPane.setBounds(0, 0, 200, getHeight());
        	tab1.add(chatScrollPane);
        	JTabbedPane tabPanel = new JTabbedPane();
        	tabPanel.addTab("Groups", chatScrollPane);
    tabPanel.addTab("Users", userScrollPane);
    tabPanel.setBounds(0, 25, 200, getHeight() - 25);
        this.add(tabPanel);
       // this.add(userScrollPane);
        //this.add(chatScrollPane);
        this.add(messagingPane);
        this.add(sendMessages);
        this.add(sendMessageButton);
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
				tabPanel.setBounds(0, 25, 200, getHeight() - 25);
				messagingPane.setBounds(200, 0, getWidth() - 210, getHeight() - 10);
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
    public Image getImage() {
        return null;
    }

    @Override
    public JButton getSubmitButton() {
        return sendMessageButton;
    }
    
    public void updateMessages() {
        if(selectedChat.isPresent()) {
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
                    
                    if(!added)
                        messagingWindow.add(m);
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
            
            List<Message> allUserMessages = new ArrayList<>();
            
            for(Message m : messages) {
                if(!m.chatRoom.isPresent() && m.user.equals(selectedUser.get())) {
                    allUserMessages.add(m);
                    
                    boolean added = false;
                    for(Component comp : messagingWindow.getComponents()) {
                        if(comp == m) {
                            added = true;
                        }
                    }
                    
                    if(!added)
                        messagingWindow.add(m);
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
            }
        }
        else {
            if(messagingPane.isVisible()) {
                messagingPane.setVisible(false);
            }
            
            SwingUtilities.updateComponentTreeUI(this.getParent());
            return;
        }
    }
    
    public void updateUser(User user, int state, NetworkInterface network) {
        switch(state) {
            case NetworkInterface.CONNECTED:
                onlineUsers.add(user);
                JButton userButton = createButtonForUser(user);
                userListPanel.add(userButton);
                userButtons.add(userButton);
            break;
            case NetworkInterface.DISCONNECTED:
                onlineUsers.remove(user);
                userListPanel.remove(getUserButtonByUser(user).orElse(null));
                userButtons.remove(getUserButtonByUser(user).orElse(null));
            break;
            case NetworkInterface.CHANGED_NICKNAME:
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
            case NetworkInterface.CONNECTED:
                onlineChats.add(chat);
                JButton chatButton = createButtonForChat(chat);
                chatListPanel.add(chatButton);
                chatButtons.add(chatButton);
            break;
            case NetworkInterface.DISCONNECTED:
                onlineChats.remove(chat);
                chatListPanel.remove(getChatButtonByChat(chat).orElse(null));
                chatButtons.remove(getChatButtonByChat(chat).orElse(null));
            break;
            case NetworkInterface.CHANGED_NICKNAME:
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
        onlineUsers = users;
        
        for(User user : onlineUsers) {
            JButton userButton = createButtonForUser(user);
            userListPanel.add(userButton);
            userButtons.add(userButton);
        }
    }
    
    public void addMessage(Message message) {
        messages.add(message);
    }
    
    public JButton createButtonForUser(User user) {
        JButton userButton = new JButton(user.username + " (" + user.nickname + ")");
        userButton.addActionListener(new UserButtonPress(user));
        return userButton;
    }
    
    public void showMessages(User user) {
        System.out.println("Showing messages for " + user.username + " (" + user.nickname + ")");
        selectedChat = Optional.empty();
        selectedUser = Optional.of(user);
        sendMessages.setVisible(true);
        sendMessageButton.setVisible(true);
    }
    
    private Optional<JButton> getUserButtonByUser(User user) {
        for(JButton userButton : userButtons) {
            for(ActionListener listener : userButton.getActionListeners()) {
                if(listener instanceof UserButtonPress && ((UserButtonPress)listener).user == user) { 
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
                if(listener instanceof ChatButtonPress && ((ChatButtonPress)listener).chat == chat) { 
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
            showMessages(chat);
        }
    }
    
    public List<Message> getQueuedMessages() {
        return new ArrayList<>(messageQueue);
    }
    
    public void emptyQueuedMessages() {
        while(messageQueue.size() != 0) messageQueue.remove(0);
    }
}
