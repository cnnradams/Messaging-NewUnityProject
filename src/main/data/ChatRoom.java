package main.data;

import java.net.ConnectException;
import java.util.Optional;

import main.networking.NetworkInterface;

public class ChatRoom {
    
    public final int id;
    public final String name;
    
    public ChatRoom(int id, NetworkInterface network) {
        this.id = id;
        this.name = getChatNameOrThrowExceptionAnd("Unkown", network.getChatName(id), network);
    }
    
    private static String getChatNameOrThrowExceptionAnd(String fallbackChatName, Optional<String> chatNameOptional, NetworkInterface network) {
        try {
            return chatNameOptional.orElseThrow(network::makeException);
        }
        catch(ConnectException e) {
            e.printStackTrace();
            return fallbackChatName;
        }
    }
}
