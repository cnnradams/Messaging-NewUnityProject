package main.networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import main.data.Message;
import main.data.User;

public class MockServer implements NetworkInterface {

    private int resultCode;
    
    public int loginResult, getMessagesResult, getNicknameResult, sendMessageResult, logoutResult;
    public long loginTime, getMessagesTime, getNicknameTime, sendMessageTime, logoutTime;
    
    public Map<String, String> nicknameMap = new HashMap<>();
    public List<Message> messages = new ArrayList<>();
    
    private void pause(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void printlnResult() {
        System.out.println("[Mock] Result: Code " + resultCode + " - " + NetworkInterface.ERROR_MEANINGS.get(resultCode));
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
            nicknameMap.put(user.username, user.nickname);
        
        resultCode = loginResult;
        printlnResult();
        
        System.out.println();
        
        return loginResult == 0;
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
           Optional<String> nickname = Optional.ofNullable(nicknameMap.get(username));
       
           System.out.println("[Mock] Nickname for " + username + " is " + nickname.orElse("unknown"));
       
           resultCode = getNicknameResult;
           printlnResult();
       
           System.out.println();
           
           return nickname;
       }
    }
    
    @Override
    public Optional<List<Message>> getIncomingMessages() {
       System.out.println("[Mock] Getting messages...\n"
                        + "[Mock] Waiting for " + getMessagesTime + " milliseconds");
       pause(getMessagesTime);
       
       if(getMessagesResult == 0) {
           System.out.println("Failed to get messages");
           
           resultCode = getMessagesResult;
           printlnResult();
           
           System.out.println();
           
           return Optional.empty();
       }
       else {
           System.out.println("Found " + messages.size() + " new messages");
           
           Optional<List<Message>> optional = Optional.of(messages);
           messages = new ArrayList<>();
           
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
        
        System.out.println("[Mock] Message sending " + (sendMessageResult == 0 ? "successful" : "failure"));
        
        resultCode = sendMessageResult;
        printlnResult();
        
        System.out.println();
        
        return sendMessageResult == 0;
    }

    @Override
    public boolean logout() {
        System.out.println("[Mock] Logging out...\n"
                         + "[Mock] Waiting for " + sendMessageTime + " milliseconds");
        pause(logoutTime);
        
        System.out.println("[Mock] Logout " + (logoutResult == 0 ? "successful" : "failure"));
        
        resultCode = logoutResult;
        printlnResult();
        
        System.out.println();
        
        return logoutResult == 0;
    }
}
