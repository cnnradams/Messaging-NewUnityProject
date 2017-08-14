package main.networking;

import java.awt.image.BufferedImage;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import main.data.ChatRoom;
import main.data.Message;
import main.data.User;

/**
 * Doesn't actually connect to a server over a network, instead has mock values to simulate a real server
 */
public class MockServer implements NetworkInterface {

    /**
     * The mock network is logged out
     */
    public static final int STATE_LOGGED_OUT = 0;
    
    /**
     * The mock network is logged in
     */
    public static final int STATE_LOGGED_IN = 1;
    
    private int resultCode;
    
    /**
     * The mock results for each operation
     */
    public int loginResult, getAllChatsResult, getChatNameResult, getChatUpdatesResult, getNicknameResult, getUsersResult, getUserUpdatesResult, getMessagesResult, sendMessageResult, logoutResult, keepAliveResult, setNicknameResult, getProfilePictureResult, setProfilePictureResult, createChatResult;
    
    /**
     * The time it takes for each operation to complete
     */
    public long loginTime, getAllChatsTime, getChatNameTime, getChatUpdatesTime, getNicknameTime, getUsersTime, getUserUpdatesTime, getMessagesTime, sendMessageTime, logoutTime, keepAliveTime, setNicknameTime, getProfilePictureTime, setProfilePictureTime, createChatTime;
    
    /**
     * All the users on the mock network
     */
    public Map<String, String> userMap = new HashMap<>();
    
    /**
     * All the messages on the mock network
     */
    public List<Message> messages = new ArrayList<>();
    
    /**
     * All the queued updates to users on the mock network
     */
    public Map<User, List<Integer>> userUpdateMap = new HashMap<>();
    
    /**
     * All the chats on the mock network
     */
    public Map<Integer, String> chatMap = new HashMap<>();
    
    /**
     * All the queued updates to chats on the mock network
     */
    public Map<ChatRoom, List<Integer>> chatUpdateMap = new HashMap<>();
    
    /**
     * The user that is logged in, {@code Optional.empty()} if no user is logged in
     */
    public Optional<User> loggedIn = Optional.empty();
    
    /**
     * Holds a map of all profile pictures
     */
    public Map<String, Optional<BufferedImage>> pictureMap = new HashMap<>();
    
    private void pause(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private String makeResultString() {
        return "[Mock] Result: Code " + resultCode + " - " + NetworkInterface.ERROR_MEANINGS.get(resultCode);
    }
    
    private void printlnResult() {
        System.out.println(makeResultString());
    }
    
    @Override
    public ConnectException makeException() {
        return new ConnectException(makeResultString());
    }
    
    @Override
    public int getLastResultCode() {
        return resultCode;
    }
    
    @Override
    public boolean login(User user) {
        System.out.println("[Mock] Logging in as " + user.username + " (" + user.nickname + ")...\n"
                         + "[Mock] Waiting for " + loginTime + " milliseconds");
        pause(loginTime);
        
        loggedIn = Optional.of(user);
        
        System.out.println("[Mock] Login " + (loginResult == NetworkInterface.RESULT_SUCCESS ? "successful" : "failure"));
        
        if(loginResult == NetworkInterface.RESULT_SUCCESS)
            userMap.put(user.username, user.nickname);
        
        resultCode = loginResult;
        printlnResult();
        
        System.out.println();
        
        setState(STATE_LOGGED_IN);
        
        return loginResult == NetworkInterface.RESULT_SUCCESS;
    }

    @Override
    public Optional<Set<ChatRoom>> getAllChats() {
        System.out.println("[Mock] Getting all chats...\n"
                        + "[Mock] Waiting for " + getAllChatsTime + " milliseconds");
       pause(getAllChatsTime);
       
       if(getAllChatsResult != NetworkInterface.RESULT_SUCCESS) {
           System.out.println("[Mock] Failed to get chats");
           
           resultCode = getAllChatsResult;
           printlnResult();
           
           System.out.println();
           
           return Optional.empty();
       }
       else {
           System.out.println("[Mock] Found " + chatMap.size() + " chats");
           
           Set<ChatRoom> chats = new HashSet<>(chatMap.size());
           
           for(Integer chatId : chatMap.keySet()) {
               chats.add(new ChatRoom(chatId, this));
           }
           
           resultCode = getMessagesResult;
           printlnResult();
           
           System.out.println();
           
           return Optional.of(chats);
       }
    }
    
    @Override
    public Optional<String> getChatName(int id) {
      System.out.println("[Mock] Looking up chat name for id " + id + "...\n"
                       + "[Mock] Waiting for " + getChatNameTime + " milliseconds");
      pause(getChatNameTime);
      
      if(getChatNameResult != NetworkInterface.RESULT_SUCCESS && getChatNameResult != NetworkInterface.RESULT_UNKNOWN_USERNAME) {
          System.out.println("[Mock] Chat name for id " + id + " could not be checked");
          
          resultCode = getChatNameResult;
          printlnResult();
          
          System.out.println();
          
          return Optional.empty();
      }
      else {
          Optional<String> chatName = Optional.ofNullable(chatMap.get(id));
      
          System.out.println("[Mock] Chat name for id " + id + " is " + chatName.orElse("unknown"));
      
          resultCode = getChatNameResult;
          printlnResult();
      
          System.out.println();
          
          return chatName;
      }
    }
    
    @Override
    public Optional<Map<ChatRoom, List<Integer>>> getChatUpdates() {
        System.out.println("[Mock] Getting all chat updates...\n"
                        +  "[Mock] Waiting for " + getChatUpdatesTime + " milliseconds");
       pause(getChatUpdatesTime);
       
       if(getChatUpdatesResult != NetworkInterface.RESULT_SUCCESS) {
           System.out.println("[Mock] Failed to get chat updates");
           
           resultCode = getChatUpdatesResult;
           printlnResult();
           
           System.out.println();
           
           return Optional.empty();
       }
       else {
           System.out.println("[Mock] Found " + chatUpdateMap.size() + " chat updates");
           
           Map<ChatRoom, List<Integer>> copy = new HashMap<>();
           copy.putAll(chatUpdateMap);
           userUpdateMap = new HashMap<>();
           
           Optional<Map<ChatRoom, List<Integer>>> optional = Optional.of(copy);
           
           for(ChatRoom chat : optional.get().keySet()) {
               for(int state : optional.get().get(chat)) {
                   switch(state) {
                       case NetworkInterface.CHANGE_CONNECTED:
                           chatMap.put(chat.id, chat.name);
                       break;
                       case NetworkInterface.CHANGE_DISCONNECTED:
                           chatMap.remove(chat.id);
                       break;
                       case NetworkInterface.CHANGE_CHANGED_NICKNAME:
                           chatMap.remove(chat.id);
                           // May be null if user leaves server and changes nickname at same time
                           chatMap.put(chat.id, getChatName(chat.id).orElse(""));
                   }
               }
           }
           
           resultCode = getChatUpdatesResult;
           printlnResult();
           
           System.out.println();
           
           return optional;
       }
    }
    
    @Override
    public Optional<String> getNickname(String username) {
        System.out.println("[Mock] Looking up nickname for " + username + "...\n"
                         + "[Mock] Waiting for " + getNicknameTime + " milliseconds");
       pause(getNicknameTime);
       
       if(getNicknameResult != NetworkInterface.RESULT_SUCCESS && getNicknameResult != NetworkInterface.RESULT_UNKNOWN_USERNAME) {
           System.out.println("[Mock] Nickname for " + username + " could not be checked");
           
           resultCode = getNicknameResult;
           printlnResult();
           
           System.out.println();
           
           return Optional.empty();
       }
       else {
           Optional<String> nickname = Optional.ofNullable(userMap.get(username));
       
           System.out.println("[Mock] Nickname for " + username + " is " + nickname.orElse("unknown"));
       
           resultCode = getNicknameResult;
           printlnResult();
       
           System.out.println();
           
           return nickname;
       }
    }
    
    @Override
    public Optional<Set<User>> getAllUsers() {
        System.out.println("[Mock] Getting all users...\n"
                        + "[Mock] Waiting for " + getUsersTime + " milliseconds");
       pause(getUsersTime);
       
       if(getUsersResult != NetworkInterface.RESULT_SUCCESS) {
           System.out.println("[Mock] Failed to get users");
           
           resultCode = getMessagesResult;
           printlnResult();
           
           System.out.println();
           
           return Optional.empty();
       }
       else {
           System.out.println("[Mock] Found " + userMap.size() + " users online");
           
           Set<User> users = new TreeSet<User>();
           
           for(String username : userMap.keySet()) {
               users.add(new User(username, this));
           }
           
           resultCode = getMessagesResult;
           printlnResult();
           
           System.out.println();
           
           return Optional.of(users);
       }
    }
    
    @Override
    public Optional<Map<User, List<Integer>>> getUserUpdates() {
        System.out.println("[Mock] Getting all user updates...\n"
                        + "[Mock] Waiting for " + getUserUpdatesTime + " milliseconds");
       pause(getUserUpdatesTime);
       
       if(getUserUpdatesResult != NetworkInterface.RESULT_SUCCESS) {
           System.out.println("[Mock] Failed to get user updates");
           
           resultCode = getMessagesResult;
           printlnResult();
           
           System.out.println();
           
           return Optional.empty();
       }
       else {
           System.out.println("[Mock] Found " + userUpdateMap.size() + " user updates");
           
           Map<User, List<Integer>> copy = new HashMap<>();
           copy.putAll(userUpdateMap);
           userUpdateMap = new HashMap<>();
           
           Optional<Map<User, List<Integer>>> optional = Optional.of(copy);
           
           for(User user : optional.get().keySet()) {
               for(int state : optional.get().get(user)) {
                   switch(state) {
                       case NetworkInterface.CHANGE_CONNECTED:
                           userMap.put(user.username, user.nickname);
                       break;
                       case NetworkInterface.CHANGE_DISCONNECTED:
                           userMap.remove(user.username);
                       break;
                       case NetworkInterface.CHANGE_CHANGED_NICKNAME:
                           userMap.remove(user.username);
                           // May be null if user leaves server and changes nickname at same time
                           userMap.put(user.username, getNickname(user.username).orElse(""));
                   }
               }
           }
           
           resultCode = getUserUpdatesResult;
           printlnResult();
           
           System.out.println();
           
           return optional;
       }
    }
    
    @Override
    public Optional<List<Message>> getIncomingMessages() {
       System.out.println("[Mock] Getting messages...\n"
                        + "[Mock] Waiting for " + getMessagesTime + " milliseconds");
       pause(getMessagesTime);
       
       if(getMessagesResult != NetworkInterface.RESULT_SUCCESS) {
           System.out.println("[Mock] Failed to get messages");
           
           resultCode = getMessagesResult;
           printlnResult();
           
           System.out.println();
           
           return Optional.empty();
       }
       else {
           System.out.println("[Mock] Found " + messages.size() + " new messages");
           
           List<Message> copy = new ArrayList<>();
           copy.addAll(messages);
           messages = new ArrayList<>();
           
           Optional<List<Message>> optional = Optional.of(copy);
           
           resultCode = getMessagesResult;
           printlnResult();
           
           System.out.println();
           
           return optional;
       }
    }
    
    @Override
    public boolean sendMessage(Message message) {
        if(message.chatRoom.isPresent()) {
            System.out.println("[Mock] Sending message to " + message.chatRoom.get().name + " (#" +  message.chatRoom.get().id + ")...\n"
                             + "[Mock] Waiting for " + sendMessageTime + " milliseconds");
        }
        else {
            System.out.println("[Mock] Sending message to " + message.user.username + " (" + message.user.nickname + ")...\n"
                          + "[Mock] Waiting for " + sendMessageTime + " milliseconds");
        }
            
        pause(sendMessageTime);
        
        if(message.chatRoom.isPresent()) {
            messages.add(new Message(message.chatRoom.get(), loggedIn.get(), message.message, message.dateTime));
        }
        else {
            messages.add(new Message(loggedIn.get(), message.message, message.dateTime));
        }
        
        System.out.println("[Mock] Message sending " + (sendMessageResult == NetworkInterface.RESULT_SUCCESS ? "successful" : "failure"));
        
        resultCode = sendMessageResult;
        printlnResult();
        
        System.out.println();
        
        return sendMessageResult == NetworkInterface.RESULT_SUCCESS;
    }

    @Override
    public boolean logout() {
        System.out.println("[Mock] Logging out...\n"
                         + "[Mock] Waiting for " + sendMessageTime + " milliseconds");
        pause(logoutTime);
        
        System.out.println("[Mock] Logout " + (logoutResult == NetworkInterface.RESULT_SUCCESS ? "successful" : "failure"));
        
        resultCode = logoutResult;
        printlnResult();
        
        System.out.println();
        
        setState(STATE_LOGGED_OUT);
        
        return logoutResult == NetworkInterface.RESULT_SUCCESS;
    }
    
    @Override
    public int keepAlive() {
        System.out.println("[Mock] Keeping alive...\n"
                         + "[Mock] Waiting for " + keepAliveTime + " milliseconds");
        pause(keepAliveTime);
        
        resultCode = keepAliveResult;
        printlnResult();
        
        return keepAliveResult;
    }
    

    @Override
    public boolean setNickname(String nickname) {
        System.out.println("[Mock] Setting nickname to " + nickname + "...\n"
                         + "[Mock] Waiting for " + setNicknameTime + " milliseconds");
        pause(setNicknameTime);
       
        if(loggedIn.isPresent() && setNicknameResult == RESULT_SUCCESS) {
            userMap.put(userMap.get(loggedIn.get().username), nickname);
        }
        else if(!loggedIn.isPresent()) {
            resultCode = RESULT_NOT_LOGGED_IN;
            printlnResult();
            
            System.out.println("[Mock] Nickname change " + (resultCode == NetworkInterface.RESULT_SUCCESS ? "successful" : "failure"));
            
            return resultCode == RESULT_SUCCESS;
        }
        
        resultCode = setNicknameResult;
        printlnResult();
        
        System.out.println("[Mock] Nickname change " + (resultCode == NetworkInterface.RESULT_SUCCESS ? "successful" : "failure"));
       
        return resultCode == RESULT_SUCCESS;
    }

    @Override
    public Optional<BufferedImage> getProfilePicture(String username) {
        System.out.println("[Mock] Getting profile picture for " + username + "...\n"
                         + "[Mock] Waiting for " + getProfilePictureTime + " milliseconds");
        pause(getProfilePictureTime);
        
        resultCode = getProfilePictureResult;
        printlnResult();
        
        System.out.println("[Mock] Profile picture retrieval " + (resultCode == NetworkInterface.RESULT_SUCCESS ? "successful" : "failure"));
        
        if(resultCode == RESULT_SUCCESS) {
            return pictureMap.getOrDefault(username, Optional.empty());
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public boolean setProfilePicture(Optional<BufferedImage> image) {
        System.out.println("[Mock] Setting profile picture...\n"
                         + "[Mock] Waiting for " + setProfilePictureTime + " milliseconds");
        pause(setProfilePictureTime);
      
        if(loggedIn.isPresent() && setProfilePictureResult == RESULT_SUCCESS) {
            pictureMap.put(userMap.get(loggedIn.get().username), image);
        }
        else if(!loggedIn.isPresent()) {
            resultCode = RESULT_NOT_LOGGED_IN;
            printlnResult();
            
            System.out.println("[Mock] Setting profile picture " + (resultCode == NetworkInterface.RESULT_SUCCESS ? "successful" : "failure"));
            
            return resultCode == RESULT_SUCCESS;
        }
        
        resultCode = setProfilePictureResult;
        printlnResult();
        
        System.out.println("[Mock] Setting profile picture " + (resultCode == NetworkInterface.RESULT_SUCCESS ? "successful" : "failure"));
        
        return resultCode == RESULT_SUCCESS;
    }
    
    @Override
    public Optional<ChatRoom> createChat(String name) {
        System.out.println("[Mock] Creating chat " + name + "...\n"
                         + "[Mock] Waiting for " + createChatTime + " milliseconds");
        pause(createChatTime);
        
        resultCode = createChatResult;
        printlnResult();
        
        if(resultCode == RESULT_SUCCESS) {
            int id = -1;
            while(chatMap.containsKey(++id));
            
            chatMap.put(id, name);
            return Optional.of(new ChatRoom(id, name));
        }
        else {
            return Optional.empty();
        }
    }
    
    private void setState(int state) {
        switch(state) {
            case STATE_LOGGED_OUT: {
                loginResult = NetworkInterface.RESULT_SUCCESS;
                loginTime = 2500;
                
                getNicknameResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
                getNicknameTime = 0;
                
                getUserUpdatesResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
                getUserUpdatesTime = 0;
                
                getMessagesResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
                getMessagesTime = 0;
                
                sendMessageResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
                sendMessageTime = 0;
                
                logoutResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
                logoutTime = 0;
                
                keepAliveResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
                keepAliveTime = 0;
                
                setNicknameResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
                setNicknameTime = 0;
                
                getProfilePictureResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
                getProfilePictureResult = 0;
                
                setProfilePictureResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
                setProfilePictureTime = 0;
                
                createChatResult = NetworkInterface.RESULT_NOT_LOGGED_IN;
                createChatTime = 0;
            }
            break;
            case STATE_LOGGED_IN: {
                loginResult = NetworkInterface.RESULT_ALREADY_LOGGED_IN;
                loginTime = 0;
                
                //create groups for mock server
                getChatNameResult = NetworkInterface.RESULT_SUCCESS;
                getChatNameTime = 75;
                chatMap.put(0, "General");
                chatMap.put(1, "Off topic");
                
                //how often should the chat update in milliseconds
                getChatUpdatesResult = NetworkInterface.RESULT_SUCCESS;
                getChatUpdatesTime = 250;
                
                //create users for mock server
                getNicknameResult = NetworkInterface.RESULT_SUCCESS;
                getNicknameTime = 75;
                userMap.put("TestUser1", "DisplayName1");
                userMap.put("TestUser2", "DisplayName2");
                userMap.put("TestUser3", "DisplayName3");
                
                getUserUpdatesResult = NetworkInterface.RESULT_SUCCESS;
                getUserUpdatesTime = 250;
                
                getMessagesResult = NetworkInterface.RESULT_SUCCESS;
                getMessagesTime = 750;
                //mock messages
                
                sendMessageResult = NetworkInterface.RESULT_SUCCESS;
                sendMessageTime = 500;
                
                logoutResult = NetworkInterface.RESULT_SUCCESS;
                logoutTime = 750;
                
                keepAliveResult = NetworkInterface.RESULT_SUCCESS;
                keepAliveTime = 50;
                
                setNicknameResult = NetworkInterface.RESULT_SUCCESS;
                setNicknameResult = 250;
                
                getProfilePictureResult = NetworkInterface.RESULT_SUCCESS;
                getProfilePictureTime = 500;
                
                setProfilePictureResult = NetworkInterface.RESULT_SUCCESS;
                setProfilePictureTime = 500;
                
                createChatResult = NetworkInterface.RESULT_SUCCESS;
                createChatTime = 100;
            }
            break;
            default: {
                ;
            }
            break;
        }
    }
}
