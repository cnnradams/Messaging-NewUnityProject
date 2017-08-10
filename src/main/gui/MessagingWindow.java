package main.gui;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
    
    public MessagingWindow() {
        this.setSize(848, 477);
        
        JScrollPane userScrollPane = new JScrollPane();
        userScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        userScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));
        userScrollPane.setViewportView(userListPanel);
        userScrollPane.setPreferredSize(new Dimension(300, 300));
        this.add(userScrollPane);
        
        JScrollPane chatScrollPane = new JScrollPane();
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatListPanel.setLayout(new BoxLayout(chatListPanel, BoxLayout.Y_AXIS));
        chatScrollPane.setViewportView(chatListPanel);
        chatScrollPane.setPreferredSize(new Dimension(300, 300));
        this.add(chatScrollPane);
    }
    
    @Override
    public String getTitle() {
        return "NewUnityProject - Messaging";
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public JButton getSubmitButton() {
        return null;
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
}
