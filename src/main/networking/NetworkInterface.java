package main.networking;

import java.awt.image.BufferedImage;
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

/**
 * All of the functions that the network should do
 * 
 * @see MockServer
 * @see ZeroMQServer
 */
public interface NetworkInterface {
    
    // All update codes for users and/or chats
    /**
     * A user joined the server
     * or a chat was added to the server
     */
    public static final int CHANGE_CONNECTED = 1;
    
    /**
     * A user left the server
     * or a chat was removed to the server
     */
    public static final int CHANGE_DISCONNECTED = 2;
    
    /**
     * A user changed their nickname
     * or a chat's name was changed (unused)
     */
    public static final int CHANGE_CHANGED_NICKNAME = 3;
    
    /**
     * A user changed their profile picture
     */
    public static final int CHANGE_CHANGED_PICTURE = 4;
    
    // All request codes received from clients so we know what they want us to do
    // These also all return the result code for the Requester
    /**
     * Requests to log in to the server
     * 
     * (Sting username, String nickname) -> void
     */
    public static final int REQUEST_LOGIN = 0;
    
    /**
     * Requests the number of chats online
     * 
     * Must be logged in to use this request
     * () -> int chats
     */
    public static final int REQUEST_CHATS_ONLINE = 1;
    
    /**
     * Requests the chat id by index
     * 
     * Must be logged in to use this request
     * (int chatIndex) -> int chatID
     */
    public static final int REQUEST_CHAT = 2;
    
    /**
     * Requests the chat name by id
     * 
     * Must be logged in to use this request
     * (int chatID) -> String chatName
     */
    public static final int REQUEST_CHAT_NAME = 3;
    
    /**
     * Gets a new chat update or nothing if there aren't any
     * 
     * Must be logged in to use this request
     * () -> Optional(Chat, int[] updates (separated by commas))
     */
    public static final int REQUEST_CHAT_UPDATES = 4;
    
    /**
     * Requests the number of users online
     * 
     * Must be logged in to use this request
     * () -> int users
     */
    public static final int REQUEST_USERS_ONLINE = 5;
    
    /**
     * Requests the username by index
     * 
     * Must be logged in to use this request
     * (int userIndex) -> String username
     */
    public static final int REQUEST_USER = 6;
    
    /**
     * Requests the user nickname by username
     * 
     * Must be logged in to use this request
     * (String username) -> String nickanme
     */
    public static final int REQUEST_USER_NICKNAME = 7;
    
    /**
     * Gets a new user update or nothing if there aren't any
     * 
     * Must be logged in to use this request
     * () -> Optional(User, int[] updates (separated by commas))
     */
    public static final int REQUEST_USER_UPDATES = 8;
    
    /**
     * Gets a new message or nothing if there aren't any
     * 
     * Must be logged in to use this request
     * () -> String fromUser, boolean chooseNextArg, (int chatID or String username), String message, String utcTime
     */
    public static final int REQUEST_NEW_MESSAGE = 9;
    
    /**
     * Sends a message to a user
     * 
     * Must be logged in to use this request
     * (boolean chooseNextArg, (int chatID or String username), String message, String utcTime) -> void
     */
    public static final int REQUEST_SEND_MESSAGE = 10;
    
    /**
     * Logs user out of the server
     * 
     * Must be logged in to use this request
     * () -> void
     */
    public static final int REQUEST_LOGOUT = 11;
    
    /**
     * Resets kick timer for requester
     * 
     * Must be logged in to use this request
     * () -> void
     */
    public static final int REQUEST_KEEP_ALIVE = 12;
    
    /**
     * Sets the nickname of the user
     * 
     * Must be logged in to use this request
     * () -> void
     */
    public static final int REQUEST_SET_NICKNAME = 13;
    
    /**
     * Gets a user's profile picture
     * 
     * Must be logged in to use this request
     * (String username) -> boolean hasPicture, Optional(Base64 image)
     */
    public static final int REQUEST_USER_PICTURE = 14;
    
    /**
     * Sets the user's profile picture
     * 
     * Must be logged in to use this request
     * (boolean hasImage, Base64 image) -> void
     */
    public static final int REQUEST_SET_USER_PICTURE = 15;
    
    /**
     * Creates a chatroom with a specified name
     * 
     * Must be logged in to use this request
     * (String name) -> int chatID
     */
    public static final int REQUEST_CREATE_CHAT_ROOM = 16;
    
    
    // Result codes tell the client what happened with the request
    /**
     * Operation was successful
     */
    public static final int RESULT_SUCCESS = 0;
    
    /**
     * Unable to connect
     */
    public static final int RESULT_COULD_NOT_CONNECT = -1;
    
    /**
     * Username taken
     */
    public static final int RESULT_USERNAME_TAKEN = -2;
    
    /**
     * Unknown username
     */
    public static final int RESULT_UNKNOWN_USERNAME = -3;
    
    /**
     * No user logged into server
     */
    public static final int RESULT_NOT_LOGGED_IN = -4;
    
    /**
     * You are already logged in
     */
    public static final int RESULT_ALREADY_LOGGED_IN = -5;
    
    /**
     * This chat is missing
     */
    public static final int RESULT_UNKNOWN_CHAT = -6;
    
    /**
     * Bad request
     */
    public static final int RESULT_BAD_REQUEST = -7;
    
    /**
     * Text was too long
     */
    public static final int RESULT_TEXT_TOO_LONG = -8;
    
    /**
     * Unknown error occurred
     */
    public static final int RESULT_FAILURE_UNKNOWN = -9;
    
    /**
     * A map with all the result codes and what they mean
     */
    public static final Map<Integer, String> ERROR_MEANINGS = initErrorCodeMap();
    
    /**
     * Initializes {@code ERROR_MEANINGS} or returns it if it was already initialized
     * 
     * @return {@code ERROR_MEANINGS}
     */
    static Map<Integer, String> initErrorCodeMap() {
        if(ERROR_MEANINGS != null) {
            return ERROR_MEANINGS;
        }
        
        HashMap<Integer, String> decodings = new HashMap<>(5);
        decodings.put(RESULT_SUCCESS,              "Operation was successful");
        decodings.put(RESULT_COULD_NOT_CONNECT,    "Unable to connect");
        decodings.put(RESULT_USERNAME_TAKEN,       "Username taken");
        decodings.put(RESULT_UNKNOWN_USERNAME,     "Unknown username");
        decodings.put(RESULT_NOT_LOGGED_IN,        "No user logged into server");
        decodings.put(RESULT_ALREADY_LOGGED_IN,    "You are already logged in");
        decodings.put(RESULT_UNKNOWN_CHAT,         "This chat is missing");
        decodings.put(RESULT_BAD_REQUEST,          "Bad request");
        decodings.put(RESULT_TEXT_TOO_LONG,        "Text was too long");
        decodings.put(RESULT_FAILURE_UNKNOWN,      "Unknown error occurred");
        
        return Collections.unmodifiableMap(decodings);
    }
    
    /**
     * Helper method that returns an exception for when something goes wrong connecting to the server
     * 
     * @return The exception to throw
     */
    default public ConnectException makeException() {
        return new ConnectException("[Network] Result: Code " + getLastResultCode() + " - " + NetworkInterface.ERROR_MEANINGS.get(getLastResultCode()));
    }
    
    /**
     * Gets the result of the last server request
     * 
     * @return The result code
     */
    public int getLastResultCode();
    
    /**
     * Logs into the the server
     * 
     * @param user The user to log in as
     * 
     * @return Whether the login was successful
     */
    public boolean login(User user);
    
    /**
     * Gets all the chatrooms on the server
     * 
     * @return If successful, all the chats on the server, otherwise {@code Optional.empty()}
     */
    public Optional<Set<ChatRoom>> getAllChats();
    
    
    /**
     * Gets the name of a chat from its ID
     * 
     * @param chatID The ID of the chat
     * 
     * @return If successful, the chat name, otherwise {@code Optional.empty()}
     */
    public Optional<String> getChatName(int chatID);
    
    /**
     * Gets all changes to chatrooms either
     * {@code CHANGE_CONNECTED} or {@code CHANGE_DISCONNECTED}
     * 
     * @return If successful, all the chats with updates and the updates to them, otherwise {@code Optional.empty()}
     */
    public Optional<Map<ChatRoom, List<Integer>>> getChatUpdates();
    
    /**
     * Gets the nickname of a user
     * 
     * @param username The unique username of the user
     * 
     * @return If successful, the nickname of the user, otherwise {@code Optional.empty()}
     */
    public Optional<String> getNickname(String username);
    
    /**
     * Gets all the users on the server
     * 
     * @return If successful, all the users on the server, otherwise {@code Optional.empty()}
     */
    public Optional<Set<User>> getAllUsers();
    
    /**
     * Gets all changes to users either
     * {@code CHANGE_CONNECTED}, {@code CHANGE_DISCONNECTED}, {@code CHANGE_CHANGED_NICKNAME}, or {@code CHANGE_CHANGED_PICTURE}
     * 
     * @return If successful, all the users with updates and the updates to them, otherwise {@code Optional.empty()}
     */
    public Optional<Map<User, List<Integer>>> getUserUpdates();
    
    /**
     * Gets all the incoming messages to this user
     * 
     * @return If successful, all the new messages, otherwise {@code Optional.empty()}
     */
    public Optional<List<Message>> getIncomingMessages();
    
    
    /**
     * Sends a message based on the user and chat addressed in the {@code Message}
     * 
     * @param message The message to send
     * 
     * @return Whether the operation was successful
     */
    public boolean sendMessage(Message message);
    
    /**
     * Sets the nickname of the user
     * 
     * @param nickname The nickname to set
     * 
     * @return Whether the operation was successful
     */
    public boolean setNickname(String nickname);
    
    /**
     * Gets the profile picture for a user
     * 
     * @param username The username of the user to get the profile picture of
     * 
     * @return If successful and the user has a profile picture, the profile picture, otherwise {@code Optional.empty()}
     */
    public Optional<BufferedImage> getProfilePicture(String username);
    
    /**
     * Sets the profile picture for the user
     * 
     * @param image The optional image to set the profile picture to (empty if it should be set to nothing)
     * 
     * @return Whether the operation was successful
     */
    public boolean setProfilePicture(Optional<BufferedImage> image);
    
    /**
     * Logs out of the server
     * 
     * @return Whether the operation was successful
     */
    public boolean logout();
    
    /**
     * Keeps the server from kicking the user
     * 
     * @return The result code of this operation
     */
    public int keepAlive();
    
    /**
     * Creates a chatroom on the server
     * 
     * @param name The name of the chatroom
     * 
     * @return If the operation was successful, the newly created chatroom, otherwise {@code Optional.empty()}
     */
    public Optional<ChatRoom> createChat(String name);
}
