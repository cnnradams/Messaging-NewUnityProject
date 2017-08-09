package main.gui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;

import main.data.Message;
import main.data.User;
import main.networking.NetworkInterface;

public class MessagingWindow extends StatePanel {
    
    private static final long serialVersionUID = 1L;
    
    private Set<User> onlineUsers = new HashSet<>();
    private List<Message> messages = new ArrayList<Message>();
    
    private List<JButton> userButtons = new ArrayList<>();
    
    public MessagingWindow() {
        this.setSize(848, 477);
        JLabel j = new JLabel("Test");
        j.setVisible(true);
        this.add(j);
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
                this.add(userButton);
                userButtons.add(userButton);
            break;
            case NetworkInterface.DISCONNECTED:
                onlineUsers.remove(user);
                this.remove(getUserButtonByUser(user).orElse(null));
                userButtons.remove(getUserButtonByUser(user).orElse(null));
            break;
            case NetworkInterface.CHANGED_NICKNAME:
                onlineUsers.remove(user);
                this.remove(getUserButtonByUser(user).orElse(null));
                userButtons.remove(getUserButtonByUser(user).orElse(null));
                onlineUsers.add(new User(user.username, network));
                userButton = createButtonForUser(user);
                this.add(userButton);
                userButtons.add(userButton);
            break;
            default:
                throw new IllegalArgumentException("Unknown user update state");
        }
    }
    
    public void initializeUserSet(Set<User> users) {
        onlineUsers = users;
        
        for(User user : onlineUsers) {
            JButton userButton = createButtonForUser(user);
            this.add(userButton);
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
}
