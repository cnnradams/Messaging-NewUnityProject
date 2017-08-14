package main.data;

import java.net.ConnectException;
import java.util.Optional;

import main.networking.NetworkInterface;

/**
 * A global group chat that all users are a part of
 */
public class ChatRoom implements Comparable<ChatRoom> {
    
    /**
     * Identifies the server, may be different than the index in {@code Main.chats}.
     * Each id is unique to the ChatRoom
     */
    public final int id;
    
    /**
     * The name of the chat. Multiple chats can have the same name.
     */
    public final String name;
    
    /**
     * Constructs the group chat getting information from the server
     * 
     * @param id The unique ID of the chatroom
     * @param network The NetworkInterface to get the chat name
     */
    public ChatRoom(int id, NetworkInterface network) {
        this(id, getChatNameOrThrowExceptionAnd("Unkown", network.getChatName(id), network));
    }
    
    /**
     * Constructs the group chat
     * 
     * @param id The unique ID of the chatroom
     * @param name The name of the chatroom
     */
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
