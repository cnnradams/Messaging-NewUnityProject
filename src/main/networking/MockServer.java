package main.networking;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import main.data.Message;
import main.data.User;

public class MockServer implements NetworkInterface {

    private int resultCode;
    
    public int loginResult, getNicknameResult, getUsersResult, getUserUpdatesResult, getMessagesResult, sendMessageResult, logoutResult;
    public long loginTime, getNicknameTime, getUsersTime, getUserUpdatesTime, getMessagesTime, sendMessageTime, logoutTime;
    
    public Map<String, String> userMap = new HashMap<>();
    public List<Message> messages = new ArrayList<>();
    public Map<User, List<Integer>> updateMap = new HashMap<>();
    
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
        
        System.out.println("[Mock] Login " + (loginResult == NetworkInterface.SUCCESS ? "successful" : "failure"));
        
        if(loginResult == NetworkInterface.SUCCESS)
            userMap.put(user.username, user.nickname);
        
        resultCode = loginResult;
        printlnResult();
        
        System.out.println();
        
        return loginResult == NetworkInterface.SUCCESS;
    }

    @Override
    public Optional<String> getNickname(String username) {
        System.out.println("[Mock] Looking up nickname for " + username + "...\n"
                         + "[Mock] Waiting for " + getNicknameTime + " milliseconds");
       pause(getNicknameTime);
       
       if(getNicknameResult != NetworkInterface.SUCCESS && getNicknameResult != NetworkInterface.UNKNOWN_USERNAME) {
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
       
       if(getUsersResult != NetworkInterface.SUCCESS) {
           System.out.println("[Mock] Failed to get users");
           
           resultCode = getMessagesResult;
           printlnResult();
           
           System.out.println();
           
           return Optional.empty();
       }
       else {
           System.out.println("[Mock] Found " + userMap.size() + " users online");
           
           Set<User> users = new HashSet<>(userMap.size());
           
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
       
       if(getUserUpdatesResult != NetworkInterface.SUCCESS) {
           System.out.println("[Mock] Failed to get user updates");
           
           resultCode = getMessagesResult;
           printlnResult();
           
           System.out.println();
           
           return Optional.empty();
       }
       else {
           System.out.println("[Mock] Found " + updateMap.size() + " user updates");
           
           Map<User, List<Integer>> copy = new HashMap<>();
           copy.putAll(updateMap);
           updateMap = new HashMap<>();
           
           Optional<Map<User, List<Integer>>> optional = Optional.of(copy);
           
           for(User user : optional.get().keySet()) {
               for(int state : optional.get().get(user)) {
                   switch(state) {
                       case NetworkInterface.CONNECTED:
                           userMap.put(user.username, user.nickname);
                       break;
                       case NetworkInterface.DISCONNECTED:
                           userMap.remove(user.username);
                       break;
                       case NetworkInterface.CHANGED_NICKNAME:
                           userMap.remove(user.username);
                           // May be null if user leaves server and changes nickname at same time
                           userMap.put(user.username, getNickname(user.username).orElse(""));
                   }
               }
           }
           
           resultCode = getMessagesResult;
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
       
       if(getMessagesResult != NetworkInterface.SUCCESS) {
           System.out.println("Failed to get messages");
           
           resultCode = getMessagesResult;
           printlnResult();
           
           System.out.println();
           
           return Optional.empty();
       }
       else {
           System.out.println("Found " + messages.size() + " new messages");
           
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
        System.out.println("[Mock] Sending message to " + message.user.username + " (" + message.user.nickname + ")...\n"
                         + "[Mock] Waiting for " + sendMessageTime + " milliseconds");
        pause(sendMessageTime);
        
        System.out.println("[Mock] Message sending " + (sendMessageResult == NetworkInterface.SUCCESS ? "successful" : "failure"));
        
        resultCode = sendMessageResult;
        printlnResult();
        
        System.out.println();
        
        return sendMessageResult == NetworkInterface.SUCCESS;
    }

    @Override
    public boolean logout() {
        System.out.println("[Mock] Logging out...\n"
                         + "[Mock] Waiting for " + sendMessageTime + " milliseconds");
        pause(logoutTime);
        
        System.out.println("[Mock] Logout " + (logoutResult == NetworkInterface.SUCCESS ? "successful" : "failure"));
        
        resultCode = logoutResult;
        printlnResult();
        
        System.out.println();
        
        return logoutResult == NetworkInterface.SUCCESS;
    }
}
