package main;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.SwingUtilities;

import main.data.ChatRoom;
import main.data.Message;
import main.data.User;
import main.gui.Window;
import main.networking.MockServer;
import main.networking.NetworkInterface;
import main.networking.ZeroMQServer;

public class Main {
    
    public static void main(String[] args) {
        Window window = new Window();
        window.setStatePanel(window.loginWindow);
        window.setVisible(true);
        window.toFront();
        window.setBackground(new Color(60, 60, 60));
        
        InetAddress address = null;
        
        File ipOverride = new File("ip-conf.txt");
        if(!ipOverride.exists()) {
            try {
                address = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                    e.printStackTrace();
            }
        }
        else {
            try(BufferedReader br = new BufferedReader(new FileReader(ipOverride))) {
                address = InetAddress.getByName(br.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        
        final NetworkInterface network = new ZeroMQServer(address, 8743);
        
        if(network instanceof MockServer) {
            MockServer mockNetwork = (MockServer)network;
            
            mockNetwork.loginResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.loginTime = 2500;
            
            mockNetwork.getNicknameResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
            mockNetwork.getNicknameTime = 0;
            
            mockNetwork.getUserUpdatesResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
            mockNetwork.getUserUpdatesTime = 0;
            
            mockNetwork.getMessagesResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
            mockNetwork.getMessagesTime = 0;
            
            mockNetwork.sendMessageResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
            mockNetwork.sendMessageTime = 0;
            
            mockNetwork.logoutResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
            mockNetwork.logoutTime = 0;
        }
        
        boolean loggedIn = false;
        while(!loggedIn) {
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
            loggedIn = network.login(loginInfo.get());
            
            if(!loggedIn) {
                window.loginWindow.setActionText(NetworkInterface.ERROR_MEANINGS.get(network.getLastResultCode()));
                window.loginWindow.resetLoginInfo();
            }
        }
        
        if(network instanceof MockServer) {
            MockServer mockNetwork = (MockServer)network;
            
            mockNetwork.loginResult = NetworkInterface.RESULT_ALREADY_LOGGED_IN;
            mockNetwork.loginTime = 0;
            
            mockNetwork.getChatNameResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.getChatNameTime = 75;
            mockNetwork.chatMap.put(0, "General");
            mockNetwork.chatMap.put(1, "Off topic");
            
            mockNetwork.getChatUpdatesResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.getChatUpdatesTime = 250;
            
            mockNetwork.getNicknameResult = NetworkInterface.RESULT_SUCCESS;
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
            
            mockNetwork.getUserUpdatesResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.getUserUpdatesTime = 250;
            
            mockNetwork.getMessagesResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.getMessagesTime = 750;
            mockNetwork.messages.add(new Message(new User("Stealth", mockNetwork.userMap.get("Stealth")),
                                                 "I am angery!!!1!11", ZonedDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).minusHours(1)));
            mockNetwork.messages.add(new Message(new User("biscuitseed",  mockNetwork.userMap.get("biscuitseed")),
                                                 "Ya dingus", ZonedDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).minusHours(4)));
            
            mockNetwork.sendMessageResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.sendMessageTime = 500;
            
            mockNetwork.logoutResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.logoutTime = 750;
        }
        
        window.loginWindow.setActionText("Getting online chats...");
        
        try {
            window.messagingWindow.initializeChatRoomSet(network.getAllChats().orElseThrow(network::makeException));
        } catch (ConnectException e) {
            e.printStackTrace();
        }
        
        window.loginWindow.setActionText("Getting online users...");
        
        try {
            window.messagingWindow.initializeUserSet(network.getAllUsers().orElseThrow(network::makeException));
        } catch (ConnectException e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> window.setStatePanel(window.messagingWindow));
        
        while(window.isShowing()) {
        	window.setResizable(true);
            try {
                for(Message m : window.messagingWindow.getQueuedMessages()) {
                    network.sendMessage(m);
                    
                }
                window.messagingWindow.emptyQueuedMessages();
                
                Map<ChatRoom, List<Integer>> chatUpdates = network.getChatUpdates().orElseThrow(network::makeException);
                for(ChatRoom chat : chatUpdates.keySet()) {
                    for(int state : chatUpdates.get(chat)) {
                        SwingUtilities.invokeLater(() -> window.messagingWindow.updateChat(chat, state, network));
                    }
                }
                Map<User, List<Integer>> userUpdates = network.getUserUpdates().orElseThrow(network::makeException);
                for(User user : userUpdates.keySet()) {
                    for(int state : userUpdates.get(user)) {
                        SwingUtilities.invokeLater(() -> window.messagingWindow.updateUser(user, state, network));
                    }
                }
                List<Message> messages = network.getIncomingMessages().orElseThrow(network::makeException);
                for(Message message : messages) {
                    SwingUtilities.invokeLater(() -> window.messagingWindow.addMessage(message));
                }
                
                SwingUtilities.invokeLater(window.messagingWindow::updateMessages);
                
                SwingUtilities.invokeLater(window.messagingWindow::updateComponents);
                
                Thread.sleep(250);
            } catch (ConnectException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        network.logout();
        
        if(network instanceof MockServer) {
            MockServer mockNetwork = (MockServer)network;
            
            mockNetwork.loginResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.loginTime = 2500;
            
            mockNetwork.getNicknameResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
            mockNetwork.getNicknameTime = 0;
            
            mockNetwork.getUserUpdatesResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
            mockNetwork.getUserUpdatesTime = 0;
            
            mockNetwork.getMessagesResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
            mockNetwork.getMessagesTime = 0;
            
            mockNetwork.sendMessageResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
            mockNetwork.sendMessageTime = 0;
            
            mockNetwork.logoutResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
            mockNetwork.logoutTime = 0;
        }
        
        System.exit(0);
    }
    
}
