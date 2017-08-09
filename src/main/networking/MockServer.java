package main.networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import main.data.Message;
import main.data.User;

public class MockServer implements NetworkInterface {

    public boolean canLogin, canGetMessages, canSendMessage, canLogout;
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
    
    @Override
    public boolean login(User user) {
        System.out.println("[Mock] Logging in as " + user.username + " (" + user.nickname + ")...\n"
                         + "[Mock] Waiting for " + loginTime + " milliseconds");
        pause(loginTime);
        
        System.out.println("[Mock] Login " + (canLogin ? "successful" : "failure"));
        
        return canLogin;
    }

    @Override
    public Optional<String> getNickname(String username) {
        System.out.println("[Mock] Looking up nickname for " + username + "..."
                         + "[Mock] Waiting for " + getNicknameTime + " milliseconds");
       pause(getNicknameTime);
       
       Optional<String> nickname = Optional.ofNullable(nicknameMap.get(username));
       
       System.out.println("[Mock] Nickname for " + username + " is " + nickname.orElse("unknown"));
       
       return nickname;
    }
    
    @Override
    public Optional<List<Message>> getIncomingMessages() {
       System.out.println("[Mock] Getting messages..."
                        + "[Mock] Waiting for " + getMessagesTime + " milliseconds");
       pause(getMessagesTime);
       
       if(canGetMessages) {
           System.out.print("Failed to get messages");
           
           return Optional.empty();
       }
       else {
           System.out.println("Found " + messages.size() + " new messages");
           
           Optional<List<Message>> optional = Optional.of(messages);
           messages = new ArrayList<>();
           
           return optional;
       }
    }
    
    @Override
    public boolean sendMessage(Message message) {
        System.out.println("[Mock] Sending message to " + message.user.username + " (" + message.user.nickname + ")..."
                         + "[Mock] Waiting for " + sendMessageTime + " milliseconds");
        pause(sendMessageTime);
        
        System.out.println("[Mock] Message sending " + (canSendMessage ? "successful" : "failure"));
        
        return canSendMessage;
    }

    @Override
    public boolean logout() {
        System.out.println("[Mock] Logging out..."
                         + "[Mock] Waiting for " + sendMessageTime + " milliseconds");
        pause(logoutTime);
        
        System.out.println("[Mock] Logout " + (canLogout ? "successful" : "failure"));
        
        return canLogout;
    }
}
