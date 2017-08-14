package main;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import main.data.ChatRoom;
import main.data.Message;
import main.data.User;
import main.gui.ColorConstants;
import main.gui.MessagingWindow;
import main.gui.Window;
import main.networking.MockServer;
import main.networking.NetworkInterface;
import main.networking.ZeroMQServer;

/**
 * Main class of the project, the shotcaller of the program
 */
public class Main {
    
    public static void main(String[] args) {
    	// Creates the JFrame
        Window window = new Window();
        window.setStatePanel(window.loginWindow);
        window.setVisible(true);
        window.toFront();
        window.setBackground(ColorConstants.BACKGROUND_COLOR);
        
        
        List<BufferedImage> icons = new ArrayList<>(4);
        
        // All the icon sizes, 16 is used for small taskbar / titlebar, 32 for large taskbar etc.
        try {
            icons.add(ImageIO.read(Main.class.getResourceAsStream("/resources/icon16.png")));
            icons.add(ImageIO.read(Main.class.getResourceAsStream("/resources/icon32.png")));
            icons.add(ImageIO.read(Main.class.getResourceAsStream("/resources/icon64.png")));
            icons.add(ImageIO.read(Main.class.getResourceAsStream("/resources/icon128.png")));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        
        // Set the window's titlebar icon
        window.setIconImages(icons);
        
        // What is the address to the server?
        InetAddress address = null;
        
        ClassLoader cl = Main.class.getClassLoader();
        String f = cl.getResource("").getFile();

        File cwd = new File(f);

        if (cwd.toString().endsWith("!"))
            cwd = cwd.getParentFile();
        
        File ipOverride = null;
        if(new File(cwd, "ip-conf.txt").exists()) {
            ipOverride = new File(cwd, "ip-conf.txt");
        }
        else {
            ipOverride = new File("ip-conf.txt");
        }
        
        if(!ipOverride.exists()) {
            try {
            	// If the file that lists the IP to the server doesn't exist then try localhost
                address = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                    e.printStackTrace();
            }
        }
        else {												
        	// If the file does exist then find the IP in the file
            try(BufferedReader br = new BufferedReader(new FileReader(ipOverride))) {
                address = InetAddress.getByName(br.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        
        final NetworkInterface network = new ZeroMQServer(address, 8743);
        
      // Mock server settings for testing
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
        
        // Logs into jmessage
        login(window, network);
        
        // In program mock settings
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
            mockNetwork.userMap.put("TestUser1", "DisplayName1");
            mockNetwork.userMap.put("TestUser2", "DisplayName2");
            mockNetwork.userMap.put("TestUser3", "DisplayName3");
            
            mockNetwork.getUserUpdatesResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.getUserUpdatesTime = 250;
            
            mockNetwork.getMessagesResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.getMessagesTime = 750;
            //mock messages
            
            mockNetwork.sendMessageResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.sendMessageTime = 500;
            
            mockNetwork.logoutResult = NetworkInterface.RESULT_SUCCESS;
            mockNetwork.logoutTime = 750;
        }
        
        while(window.isShowing()) {
            int keepAlive = network.keepAlive();
            if(keepAlive != NetworkInterface.RESULT_SUCCESS) {
                window.messagingWindow = new MessagingWindow();

                window.loginWindow.resetLoginInfo();							//log out the user and tell them they be gone
                window.loginWindow.setActionText(NetworkInterface.ERROR_MEANINGS.get(keepAlive));

                
                login(window, network);
            }

            // Now that we are out of the login screen we can resize window
        	window.setResizable(true);				
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
            		//If the message contains my username or nickname, play different mention sound
            		if (message.message.toLowerCase().contains(User.getMe().nickname.toLowerCase()) || message.message.toLowerCase().contains(User.getMe().username.toLowerCase())) {
            			window.messagingWindow.playNewMention();
            		} else {
            			window.messagingWindow.playNewMessage();						//play message sound if recieved message w/o mention
            		}
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
