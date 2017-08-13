package main.data;

import java.net.ConnectException;
import java.util.Optional;

import main.networking.NetworkInterface;

/**
 * Stores data of a group chat room
 */
public class ChatRoom implements Comparable<ChatRoom> {
    
    public final int id;
    public final String name;
    
    public ChatRoom(int id, NetworkInterface network) {
        this(id, getChatNameOrThrowExceptionAnd("Unkown", network.getChatName(id), network));
    }
    
    public ChatRoom(int id, String name) {
        this.id = id;
        this.name = name;
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
    
    @Override
    public boolean equals(Object other) {
        return other instanceof ChatRoom && this.id == ((ChatRoom)other).id;
    }

    @Override
    public int compareTo(ChatRoom o) {
        return name.compareTo(o.name);
    }
}
