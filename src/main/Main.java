package main;

import java.awt.Color;
import java.net.ConnectException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import main.data.Message;
import main.data.User;
import main.gui.Window;
import main.networking.MockServer;
import main.networking.NetworkInterface;

public class Main {
    
    public static void main(String[] args) {
        Window window = new Window();
        window.setStatePanel(window.loginWindow);
        window.setVisible(true);
        window.toFront();
        window.setBackground(new Color(60, 60, 60));
        NetworkInterface network = new MockServer();
        
        if(network instanceof MockServer) {
            MockServer mockNetwork = (MockServer)network;
            
            mockNetwork.loginResult = NetworkInterface.SUCCESS;
            mockNetwork.loginTime = 2500;
            
            mockNetwork.getNicknameResult = NetworkInterface.NOT_LOGGED_IN;
            mockNetwork.getNicknameTime = 0;
            
            mockNetwork.getUserUpdatesResult = NetworkInterface.NOT_LOGGED_IN;
            mockNetwork.getUserUpdatesTime = 0;
            
            mockNetwork.getMessagesResult = NetworkInterface.NOT_LOGGED_IN;
            mockNetwork.getMessagesTime = 0;
            
            mockNetwork.sendMessageResult = NetworkInterface.NOT_LOGGED_IN;
            mockNetwork.sendMessageTime = 0;
            
            mockNetwork.logoutResult = NetworkInterface.NOT_LOGGED_IN;
            mockNetwork.logoutTime = 0;
        }
        
        
        Optional<User> loginInfo = null;
        while(!(loginInfo = window.loginWindow.getLoginInfo()).isPresent()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            if(!window.isShowing()) {
                System.exit(1);
            }
        }
        
        window.loginWindow.setActionText("Logging in...");
        network.login(loginInfo.get());
        
        if(network instanceof MockServer) {
            MockServer mockNetwork = (MockServer)network;
            
            mockNetwork.loginResult = NetworkInterface.ALREADY_LOGGED_IN;
            mockNetwork.loginTime = 0;
            
            mockNetwork.getNicknameResult = NetworkInterface.SUCCESS;
            mockNetwork.getNicknameTime = 75;
            mockNetwork.userMap.put("Stealth", "Stealth - 2706🍁");
            mockNetwork.userMap.put("biscuitseed", "builderman's son");
            mockNetwork.userMap.put("C#", "Java 1.11");
            mockNetwork.userMap.put("Java", "C+=2");
            mockNetwork.userMap.put("cheeseburger", "Nathan");
            mockNetwork.userMap.put("el Presidentai", "George");
            mockNetwork.userMap.put("broom&scoop", "Matt");
            mockNetwork.userMap.put("Kip", "Dario");
            mockNetwork.userMap.put("Sweet Necture", "Ty");
            mockNetwork.userMap.put("outof", "ideas");
            mockNetwork.userMap.put("pi", "3.1415926535897932384664338327950288419716939937510582097492307");
            mockNetwork.userMap.put("cheeselover1052", "Charles");
            mockNetwork.userMap.put("rnande24", "Rick");
            mockNetwork.userMap.put("nick_565", "Nick");
            mockNetwork.userMap.put("windyman52", "Lori");
            mockNetwork.userMap.put("rylon01", "Andy");
            mockNetwork.userMap.put("baconburger202", "Glenn");
            ;
            mockNetwork.getUserUpdatesResult = NetworkInterface.SUCCESS;
            mockNetwork.getUserUpdatesTime = 250;
            
            mockNetwork.getMessagesResult = NetworkInterface.SUCCESS;
            mockNetwork.getMessagesTime = 750;
            mockNetwork.messages.add(new Message(new User("Stealth", mockNetwork.userMap.get("Stealth")),
                                                 "I am angery!!!1!11", LocalDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).minusHours(1)));
            mockNetwork.messages.add(new Message(new User("biscuitseed",  mockNetwork.userMap.get("biscuitseed")),
                                                 "Ya dingus", LocalDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).minusHours(4)));
            
            mockNetwork.sendMessageResult = NetworkInterface.SUCCESS;
            mockNetwork.sendMessageTime = 500;
            
            mockNetwork.logoutResult = NetworkInterface.SUCCESS;
            mockNetwork.logoutTime = 750;
        }
        
        window.loginWindow.setActionText("Getting online users...");
        
        try {
            window.messagingWindow.initializeUserSet(network.getAllUsers().orElseThrow(network::makeException));
        } catch (ConnectException e) {
            e.printStackTrace();
        }
        
        window.setStatePanel(window.messagingWindow);
        
        while(window.isShowing()) {
            try {
                Map<User, List<Integer>> userUpdates = network.getUserUpdates().orElseThrow(network::makeException);
                for(User user : userUpdates.keySet()) {
                    for(int state : userUpdates.get(user)) {
                        window.messagingWindow.updateUser(user, state, network);
                    }
                }
                List<Message> messages = network.getIncomingMessages().orElseThrow(network::makeException);
                for(Message message : messages) {
                    window.messagingWindow.addMessage(message);
                }
                
                Thread.sleep(250);
            } catch (InterruptedException | ConnectException e) {
                e.printStackTrace();
            }
        }
        
        network.logout();
        
        if(network instanceof MockServer) {
            MockServer mockNetwork = (MockServer)network;
            
            mockNetwork.loginResult = NetworkInterface.SUCCESS;
            mockNetwork.loginTime = 2500;
            
            mockNetwork.getNicknameResult = NetworkInterface.NOT_LOGGED_IN;
            mockNetwork.getNicknameTime = 0;
            
            mockNetwork.getUserUpdatesResult = NetworkInterface.NOT_LOGGED_IN;
            mockNetwork.getUserUpdatesTime = 0;
            
            mockNetwork.getMessagesResult = NetworkInterface.NOT_LOGGED_IN;
            mockNetwork.getMessagesTime = 0;
            
            mockNetwork.sendMessageResult = NetworkInterface.NOT_LOGGED_IN;
            mockNetwork.sendMessageTime = 0;
            
            mockNetwork.logoutResult = NetworkInterface.NOT_LOGGED_IN;
            mockNetwork.logoutTime = 0;
        }
        
        System.exit(0);
    }
    
}
