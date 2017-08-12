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

public class MockServer implements NetworkInterface {

    private int resultCode;
    
    public int loginResult, getAllChatsResult, getChatNameResult, getChatUpdatesResult, getNicknameResult, getUsersResult, getUserUpdatesResult, getMessagesResult, sendMessageResult, logoutResult;
    public long loginTime, getAllChatsTime, getChatNameTime, getChatUpdatesTime, getNicknameTime, getUsersTime, getUserUpdatesTime, getMessagesTime, sendMessageTime, logoutTime;
    
    public Map<String, String> userMap = new HashMap<>();
    public List<Message> messages = new ArrayList<>();
    public Map<User, List<Integer>> userUpdateMap = new HashMap<>();
    public Map<Integer, String> chatMap = new HashMap<>();
    public Map<ChatRoom, List<Integer>> chatUpdateMap = new HashMap<>();
    
    public Optional<User> loggedIn = Optional.empty();
    
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
        
        return logoutResult == NetworkInterface.RESULT_SUCCESS;
    }
    
    @Override
    public void keepAlive() {
        System.out.println("[Mock] Not dead!");
    }
    

    @Override
    public boolean setNickname(String nickname) {
        System.out.println("[Mock] Nickname \"changed\"");
        return true;
    }

    @Override
    public BufferedImage getProfilePicture(User user) {
        System.out.println("[Mock] Getting profile picture for user");
        return null;
    }

    @Override
    public boolean setProfilePicture(BufferedImage image) {
        System.out.println("[Mock] \"Setting\" profile picture");
        return true;
    }
}
