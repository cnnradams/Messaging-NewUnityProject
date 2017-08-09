package main;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
        
        NetworkInterface network = new MockServer();
        
        if(network instanceof MockServer) {
            MockServer mockNetwork = (MockServer)network;
            
            mockNetwork.loginResult = NetworkInterface.SUCCESS;
            mockNetwork.loginTime = 2500;
            
            mockNetwork.getNicknameResult = NetworkInterface.SUCCESS;
            mockNetwork.getNicknameTime = 1000;
            mockNetwork.nicknameMap.put("Stealth", "Stealth - 2706");
            mockNetwork.nicknameMap.put("biscuitseed", "builderman's son");
            
            mockNetwork.getMessagesResult = NetworkInterface.SUCCESS;
            mockNetwork.getMessagesTime = 3500;
            mockNetwork.messages.add(new Message(new User("stealth", network.getNickname("Stealth").get()),
                                                 "I am angery!!!1!11", LocalDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).minusHours(1)));
            mockNetwork.messages.add(new Message(new User("biscuitseed", network.getNickname("biscuitseed").get()),
                                                 "Ya dingus", LocalDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).minusHours(4)));
            
            mockNetwork.sendMessageResult = NetworkInterface.COULD_NOT_CONNECT;
            mockNetwork.sendMessageTime = 500;
            
            mockNetwork.logoutResult = NetworkInterface.SUCCESS;
            mockNetwork.logoutTime = 750;
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
        
        network.login(loginInfo.get());
        
        window.setStatePanel(window.messagingWindow);
        
        while(window.isShowing()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        network.logout();
        
        System.exit(0);
    }
    
}
