package main.networking;

import java.net.ConnectException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import main.data.Message;
import main.data.User;

public interface NetworkInterface {
    
    public static final int SUCCESS = 0;
    public static final int COULD_NOT_CONNECT = -1;
    public static final int USERNAME_TAKEN = -2;
    public static final int UNKNOWN_USERNAME = -3;
    public static final int NOT_LOGGED_IN = -4;
    public static final int ALREADY_LOGGED_IN = -5;
    
    public static final int CONNECTED = 1;
    public static final int DISCONNECTED = 2;
    public static final int CHANGED_NICKNAME = 3;
    
    public static final Map<Integer, String> ERROR_MEANINGS = initErrorCodeMap();
    
    static Map<Integer, String> initErrorCodeMap() {
        if(ERROR_MEANINGS != null) {
            return ERROR_MEANINGS;
        }
        
        HashMap<Integer, String> decodings = new HashMap<>(5);
        decodings.put(SUCCESS,              "Operation was successful");
        decodings.put(COULD_NOT_CONNECT,    "Could not connect to the server");
        decodings.put(USERNAME_TAKEN,       "The specified username has already been taken on the server");
        decodings.put(UNKNOWN_USERNAME,     "Could not find the specified username on the server");
        decodings.put(NOT_LOGGED_IN,        "No username has been specified to login to the server with");
        decodings.put(ALREADY_LOGGED_IN,    "Cannot login more than once without logging out");
        
        return Collections.unmodifiableMap(decodings);
    }
    
    default public ConnectException makeException() {
        return new ConnectException("[Network] Result: Code " + getLastResultCode() + " - " + NetworkInterface.ERROR_MEANINGS.get(getLastResultCode()));
    }
    
    public int getLastResultCode();
    
    public boolean login(User user);
    
    public Optional<String> getNickname(String username);
    
    public Optional<Set<User>> getAllUsers();
    
    public Optional<Map<User, List<Integer>>> getUserUpdates();
    
    public Optional<List<Message>> getIncomingMessages();
    
    public boolean sendMessage(Message message);
    
    public boolean logout();
}
