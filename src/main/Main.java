package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import main.data.ChatRoom;
import main.data.Message;
import main.data.User;
import main.gui.MessagingWindow;
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
        //Set parameters for windows
        
        
        List<BufferedImage> icons = new ArrayList<>(4);
        //All the icon sizes, 16 is used for small taskbar / titlebar, 32 for large taskbar etc.
        try {
            icons.add(ImageIO.read(new File("src/resources/icon16.png")));
            icons.add(ImageIO.read(new File("src/resources/icon32.png")));
            icons.add(ImageIO.read(new File("src/resources/icon64.png")));
            icons.add(ImageIO.read(new File("src/resources/icon128.png")));
        }
        catch(IOException e) {
            e.printStackTrace();
            //If this happens something is seriously wrong
        }
        
        //Set the window's titlebar icon
        window.setIconImages(icons);
        
        //what is the address to the server?
        InetAddress address = null;
        
        File ipOverride = new File("ip-conf.txt");
        if(!ipOverride.exists()) {
            try {
                address = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {				//if the file that lists the IP to the server
                    e.printStackTrace();					//doesn't exist then try localhost
            }
        }
        else {												//if the file does exist then find the IP in the file
            try(BufferedReader br = new BufferedReader(new FileReader(ipOverride))) {
                address = InetAddress.getByName(br.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        
        final NetworkInterface network = new ZeroMQServer(address, 8743);
        
        if(network instanceof MockServer) {					//Mock server settings for testing
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
        

        login(window, network);
        
        if(network instanceof MockServer) {
            MockServer mockNetwork = (MockServer)network;
            
            mockNetwork.loginResult = NetworkInterface.RESULT_ALREADY_LOGGED_IN;
            mockNetwork.loginTime = 0;
            
            //create groups for mock server
            mockNetwork.getChatNameResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.getChatNameTime = 75;
            mockNetwork.chatMap.put(0, "General");
            mockNetwork.chatMap.put(1, "Off topic");
            
            //how often should the chat update in milliseconds
            mockNetwork.getChatUpdatesResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.getChatUpdatesTime = 250;
            
            //create users for mock server
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
            //mock messages
            mockNetwork.messages.add(new Message(new User("Stealth", mockNetwork.userMap.get("Stealth"), null),
                                                 "I am angery!!!1!11", ZonedDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).minusHours(1)));
            mockNetwork.messages.add(new Message(new User("biscuitseed",  mockNetwork.userMap.get("biscuitseed"), null),
                                                 "Ya dingus", ZonedDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).minusHours(4)));
            
            mockNetwork.sendMessageResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.sendMessageTime = 500;
            
            mockNetwork.logoutResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.logoutTime = 750;
        }
        
        while(window.isShowing()) {
            if(network.keepAlive() == NetworkInterface.RESULT_NOT_LOGGED_IN) {
                window.messagingWindow = new MessagingWindow();
                window.loginWindow.resetLoginInfo();							//log out the user and tell them they be gone
                window.loginWindow.setActionText("Lost connection to server");
                
                login(window, network);
            }
            
        	window.setResizable(true);						//now that we are out of the login screen we can resize window
            try {
                for(Message m : window.messagingWindow.getQueuedMessages()) {
                    network.sendMessage(m);
                    
                }
                window.messagingWindow.emptyQueuedMessages();
                
                Map<ChatRoom, List<Integer>> chatUpdates = network.getChatUpdates().orElseThrow(network::makeException);
                for(ChatRoom chat : chatUpdates.keySet()) {
                    for(int state : chatUpdates.get(chat)) {
                        window.messagingWindow.updateChat(chat, state, network);	//Handling for group chats
                    }
                }
                Map<User, List<Integer>> userUpdates = network.getUserUpdates().orElseThrow(network::makeException);
                for(User user : userUpdates.keySet()) {
                    for(int state : userUpdates.get(user)) {
                        window.messagingWindow.updateUser(user, state, network);	//Handling for user chats
                    }
                }
                List<Message> messages = network.getIncomingMessages().orElseThrow(network::makeException);
                for(Message message : messages) {
                    window.messagingWindow.playNewMessage();						//play message sound if recieved message
                    SwingUtilities.invokeLater(() -> window.messagingWindow.addMessage(message));
                }
                
                BufferedImage icon = window.messagingWindow.getNewIcon();			//profile pictures
                if(icon != null) {
                    network.setProfilePicture(icon);
                }
                
                String nickname = window.messagingWindow.getNewNickname();			//nicknames
                if(nickname != null) {
                    network.setNickname(nickname);
                }
                
                String groupName = window.messagingWindow.getNewGroup();			//group names
                if(groupName != null) {
                    network.createChat(groupName);
                }
                
                SwingUtilities.invokeLater(window.messagingWindow::updateMessages);	//Update messages etc every 250ms
                
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

    private static void login(Window window, NetworkInterface network) {
        window.setStatePanel(window.loginWindow);
        
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
                    System.exit(0);
                }
            }
        
            
            window.loginWindow.setActionText("Logging in...");
            loggedIn = network.login(loginInfo.get());
            if(!loggedIn) {
                window.loginWindow.setActionText(NetworkInterface.ERROR_MEANINGS.get(network.getLastResultCode()));
                window.loginWindow.resetLoginInfo();
            }
            else {
                User.setMe(loginInfo.get());
            }
        }
        
        
        window.loginWindow.setActionText("Getting online chats...");
        
        try {
            window.messagingWindow.initializeChatRoomSet(network.getAllChats().orElseThrow(network::makeException));
        } catch (ConnectException e) {									//List all available groups
            e.printStackTrace();
        }
        
        window.loginWindow.setActionText("Getting online users...");
        
        try {
            window.messagingWindow.initializeUserSet(network.getAllUsers().orElseThrow(network::makeException));
        } catch (ConnectException e) {									//List all available users
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> window.setStatePanel(window.messagingWindow));
        
    }
}
