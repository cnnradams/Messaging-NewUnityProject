package main.networking;

import java.util.List;
import java.util.Optional;

import main.data.Message;
import main.data.User;

public interface NetworkInterface {
    
    public boolean login(User user);
    
    public Optional<String> getNickname(String username);
    
    public Optional<List<Message>> getIncomingMessages();
    
    public boolean sendMessage(Message message);
    
    public boolean logout();
}
