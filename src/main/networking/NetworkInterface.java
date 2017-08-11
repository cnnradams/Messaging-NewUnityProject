package main.networking;

import java.net.ConnectException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import main.data.ChatRoom;
import main.data.Message;
import main.data.User;

public interface NetworkInterface {
    
    // String username, string nickname -> boolean success
    public static final int REQUEST_LOGIN = 0;
    
    // () -> int chats
    public static final int REQUEST_CHATS_ONLINE = 1;
    
    // int chatIDX -> int chatID
    public static final int REQUEST_CHAT = 2;
    
    // int chatID -> String chatName
    public static final int REQUEST_CHAT_NAME = 3;
    
    // int chatID -> encoded int updates (in binary could be: 000, 001, 010, 011, 100, 101, 110, 111)
    public static final int REQUEST_CHAT_UPDATES = 4;
    
    // () -> int chats
    public static final int REQUEST_USERS_ONLINE = 5;
    
    // int userIDX -> string username
    public static final int REQUEST_USER = 6;
    
    // String username -> String nickname
    public static final int REQUEST_USER_NICKNAME = 7;
    
    // String username -> encoded int updates (in binary could be: 000, 001, 010, 011, 100, 101, 110, 111)
    public static final int REQUEST_USER_UPDATES = 8;
    
    // () -> IntString/String chatID/username, String message, String utcTime
    public static final int REQUEST_NEW_MESSAGE = 9;
    
    // IntString/String chatID/username, String message, String utcTime -> boolean success
    public static final int REQUEST_SEND_MESSAGE = 10;
    
    // () -> boolean success
    public static final int REQUEST_LOGOUT = 11;
    
    
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_COULD_NOT_CONNECT = -1;
    public static final int RESULT_USERNAME_TAKEN = -2;
    public static final int RESULT_UNKNOWN_USERNAME = -3;
    public static final int RESULT_NOT_LOGGED_IN = -4;
    public static final int RESULT_ALREADY_LOGGED_IN = -5;
    public static final int RESULT_UNKNOWN_CHAT = -6;
    public static final int RESULT_BAD_REQUEST = -7;
    
    public static final int CHANGE_CONNECTED = 1;
    public static final int CHANGE_DISCONNECTED = 2;
    public static final int CHANGE_CHANGED_NICKNAME = 3;
    
    public static final Map<Integer, String> ERROR_MEANINGS = initErrorCodeMap();
    
    static Map<Integer, String> initErrorCodeMap() {
        if(ERROR_MEANINGS != null) {
            return ERROR_MEANINGS;
        }
        
        HashMap<Integer, String> decodings = new HashMap<>(5);
        decodings.put(RESULT_SUCCESS,              "Operation was successful");
        decodings.put(RESULT_COULD_NOT_CONNECT,    "Unable to connect!");
        decodings.put(RESULT_USERNAME_TAKEN,       "Username taken!");
        decodings.put(RESULT_UNKNOWN_USERNAME,     "Unknown username!");
        decodings.put(RESULT_NOT_LOGGED_IN,        "Username undefinied.");
        decodings.put(RESULT_ALREADY_LOGGED_IN,    "You are already logged in!");
        decodings.put(RESULT_UNKNOWN_CHAT,         "This chat is missing!");
        decodings.put(RESULT_BAD_REQUEST,          "Bad request!");
        
        return Collections.unmodifiableMap(decodings);
    }
    
    default public ConnectException makeException() {
        return new ConnectException("[Network] Result: Code " + getLastResultCode() + " - " + NetworkInterface.ERROR_MEANINGS.get(getLastResultCode()));
    }
    
    public int getLastResultCode();
    
    public boolean login(User user);
    
    public Optional<Set<ChatRoom>> getAllChats();
    
    public Optional<String> getChatName(int chatID);
    
    public Optional<Map<ChatRoom, List<Integer>>> getChatUpdates();
    
    public Optional<String> getNickname(String username);
    
    public Optional<Set<User>> getAllUsers();
    
    public Optional<Map<User, List<Integer>>> getUserUpdates();
    
    public Optional<List<Message>> getIncomingMessages();
    
    public boolean sendMessage(Message message);
    
    public boolean logout();
}
