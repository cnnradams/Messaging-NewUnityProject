package main.networking;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.zeromq.ZMQ;

import main.data.ChatRoom;
import main.data.Message;
import main.data.User;

/**
 * A real network implementation that uses the JeroMQ pure Java implementation
 * of the ZeroMQ library. This was used because it does not require any separate native files.
 * And can be packaged in the final JAR.
 */
public class ZeroMQServer implements NetworkInterface {

    /**
     * The IP address of the server
     */
    public final InetAddress ip;
    
    /**
     * The port that the server is on
     */
    public final int port;
    
    private final ZMQ.Context context;
    private final ZMQ.Socket requester;
    
    private int resultCode;
    
    private String requestToken;
    
    /**
     * Constructs the server with the IP and port
     * 
     * @param ip The IP of the server
     * @param port The port the server is on
     */
    public ZeroMQServer(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
        
        context = ZMQ.context(1);
        
        requestToken = UUID.randomUUID().toString();

        //  Socket to talk to clients
        requester = context.socket(ZMQ.REQ);
        requester.connect("tcp://" + ip.getHostAddress() + ":" + port);
        
        Runtime.getRuntime().addShutdownHook(new Thread(this::disconnect));
    }
    
    // Build request with correct format, send it, and return the response
    private ServerResponse sendRequest(ZMQ.Socket requester, String requestToken, int request, String... args) {
        String requestString = requestToken + "\n"
                             + request;
        for(String arg : args) {
            requestString += "\n" + arg;
        }
         
         requester.send(requestString);
         
         return new ServerResponse(requester);
    }
    
    @Override
    public int getLastResultCode() {
        return resultCode;
    }

    @Override
    public boolean login(User user) {
        ServerResponse loginResponse = sendRequest(requester, requestToken, REQUEST_LOGIN, user.username, user.nickname);
        
        resultCode = loginResponse.resultCode;
        return resultCode == RESULT_SUCCESS;
    }

    @Override
    public Optional<Set<ChatRoom>> getAllChats() {
        
        // Gets the number of chats
        ServerResponse chatSizeResponse = sendRequest(requester, requestToken, REQUEST_CHATS_ONLINE);
        if(chatSizeResponse.resultCode != RESULT_SUCCESS) {
            resultCode = chatSizeResponse.resultCode;
            return Optional.empty();
        }
        
        int onlineChats = Integer.parseInt(chatSizeResponse.response[0]);
        Set<ChatRoom> chats = new TreeSet<>();
        // Loop through the number of onlineChats
        for(int i = 0; i < onlineChats; i++) {
            // Get the ID of each chat
            ServerResponse chatIdResponse = sendRequest(requester, requestToken, REQUEST_CHAT, String.valueOf(i));
            if(chatIdResponse.resultCode != RESULT_SUCCESS) {
                resultCode = chatIdResponse.resultCode;
                return Optional.empty();
            }
            
            int chatId = Integer.parseInt(chatIdResponse.response[0]);
            
            // Create the chat object with the ID
            chats.add(new ChatRoom(chatId, this));
        }
        
        return Optional.of(chats);
    }

    @Override
    public Optional<String> getChatName(int chatID) {
        ServerResponse chatNameResponse = sendRequest(requester, requestToken, REQUEST_CHAT_NAME, String.valueOf(chatID));
        if(chatNameResponse.resultCode != RESULT_SUCCESS) {
            resultCode = chatNameResponse.resultCode;
            return Optional.empty();
        }
        
        return Optional.of(chatNameResponse.response[0]);
    }

    @Override
    public Optional<Map<ChatRoom, List<Integer>>> getChatUpdates() {
        Map<ChatRoom, List<Integer>> updates = new HashMap<>();
        while(true) {
            // Get a new chat update
            ServerResponse chatUpdateResponse = sendRequest(requester, requestToken, REQUEST_CHAT_UPDATES);
            if(chatUpdateResponse.resultCode != RESULT_SUCCESS) {
                resultCode = chatUpdateResponse.resultCode;
                return Optional.empty();
            }
            if(chatUpdateResponse.response.length != 2) {
                // No new chat updates, return
                resultCode = chatUpdateResponse.resultCode;
                return Optional.of(updates);
            }
            
            int id = -1;
            try {
                id = Integer.parseInt(chatUpdateResponse.response[0]);
            }
            catch(NumberFormatException e) {
                e.printStackTrace();
            }
            
            // If the chat name can't be found, it's because it was remvoved so it doesn't matter
            String name = "Don't care";
            try {
                name = getChatName(id).get();
            }
            catch(NoSuchElementException e) {}
            
            // Add update to updates
            ChatRoom chat = new ChatRoom(id, name);
            List<Integer> updateValues = new ArrayList<>();
            for(String updateString : chatUpdateResponse.response[1].split(",")) {
                updateValues.add(Integer.parseInt(updateString));
            }
            updates.put(chat, updateValues);
        }
    }

    @Override
    public Optional<String> getNickname(String username) {
        ServerResponse userNicknameResponse = sendRequest(requester, requestToken, REQUEST_USER_NICKNAME, username);
        if(userNicknameResponse.resultCode != RESULT_SUCCESS) {
            resultCode = userNicknameResponse.resultCode;
            return Optional.empty();
        }
        
        return Optional.of(userNicknameResponse.response[0]);
    }

    @Override
    public Optional<Set<User>> getAllUsers() {
        
        // Gets the number of users
        ServerResponse userSizeResponse = sendRequest(requester, requestToken, REQUEST_USERS_ONLINE);
        if(userSizeResponse.resultCode != RESULT_SUCCESS) {
            resultCode = userSizeResponse.resultCode;
            return Optional.empty();
        }
        
        // Loop through the number of onlineUsers
        int onlineUsers = Integer.parseInt(userSizeResponse.response[0]);
        Set<User> users = new TreeSet<>();
        for(int i = 0; i < onlineUsers; i++) {
            // Get the username of each user
            ServerResponse usernameResponse = sendRequest(requester, requestToken, REQUEST_USER, String.valueOf(i));
            if(usernameResponse.resultCode != RESULT_SUCCESS) {
                // No new user updates, return
                resultCode = usernameResponse.resultCode;
                return Optional.empty();
            }
            
            String username = usernameResponse.response[0];
            
            // Create the user object with the username
            users.add(new User(username, this));
        }
        
        return Optional.of(users);
    }

    @Override
    public Optional<Map<User, List<Integer>>> getUserUpdates() {
        Map<User, List<Integer>> updates = new HashMap<>();
        while(true) {
            // Get a new user update
            ServerResponse userUpdateResponse = sendRequest(requester, requestToken, REQUEST_USER_UPDATES);
            if(userUpdateResponse.resultCode != RESULT_SUCCESS) {
                resultCode = userUpdateResponse.resultCode;
                return Optional.empty();
            }
            if(userUpdateResponse.response.length != 2) {
                resultCode = userUpdateResponse.resultCode;
                return Optional.of(updates);
            }
            
            String username = userUpdateResponse.response[0];
            
            // If the user can't be found, it's because they disconnected so nickname and profile picture don't matter
            String nickname = "Don't care";
            try {
                nickname = getNickname(username).get();
            }
            catch(NoSuchElementException e) {}
            
            BufferedImage picture = null;
            try {
                picture = getProfilePicture(username).orElse(null);
            }
            catch(NoSuchElementException e) {}
            
            // Add update to updates
            User user = new User(username, nickname, picture);
            List<Integer> updateValues = new ArrayList<>();
            for(String updateString : userUpdateResponse.response[1].split(",")) {
                updateValues.add(Integer.parseInt(updateString));
            }
            updates.put(user, updateValues);
        }
    }

    @Override
    public Optional<List<Message>> getIncomingMessages() {
        List<Message> messages = new ArrayList<>();
        while(true) {
            // Get a new message
            ServerResponse newMessageResponse = sendRequest(requester, requestToken, REQUEST_NEW_MESSAGE);
            if(newMessageResponse.resultCode != RESULT_SUCCESS) {
                resultCode = newMessageResponse.resultCode;
                return Optional.empty();
            }
            // When out of new messages, return
            if(newMessageResponse.response.length != 5) {
                resultCode = newMessageResponse.resultCode;
                return Optional.of(messages);
            }
            
            // Build message information from response
            User fromUser = new User(newMessageResponse.response[0], this);
            String message = newMessageResponse.response[3];
            ZonedDateTime dateTime = ZonedDateTime.parse(newMessageResponse.response[4]);
            
            if(!Boolean.parseBoolean(newMessageResponse.response[1])) {
                messages.add(new Message(new ChatRoom(Integer.parseInt(newMessageResponse.response[2]), this), fromUser, message, dateTime));
            }
            else {
                messages.add(new Message(fromUser, message, dateTime));
            }
        }
    }

    @Override
    public boolean sendMessage(Message message) {
        String[] params = new String[4];
        // false indicates the next value is an ID for a chatroom
        if(message.chatRoom.isPresent()) {
            params[0] = String.valueOf(false);
            params[1] = String.valueOf(message.chatRoom.get().id);
        }
        // true indicates the next value is a username for a user
        else {
            params[0] = String.valueOf(true);
            params[1] = message.user.username;
        }
        
        // Add other request parameters
        params[2] = message.message;
        params[3] = message.dateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        
        // Send request with the parameters that were added
        ServerResponse sendMessageResponse = sendRequest(requester, requestToken, REQUEST_SEND_MESSAGE, params);
        
        resultCode = sendMessageResponse.resultCode;
        return resultCode == RESULT_SUCCESS;
    }

    @Override
    public boolean logout() {
        ServerResponse loginResponse = sendRequest(requester, requestToken, REQUEST_LOGOUT);
        
        resultCode = loginResponse.resultCode;
        return resultCode == RESULT_SUCCESS;
    }
    
    @Override
    public int keepAlive() {
        ServerResponse keepAliveResponse = sendRequest(requester, requestToken, REQUEST_KEEP_ALIVE);
        
        // Return result code so client can disconnect if not logged in or server is shutting down
        resultCode = keepAliveResponse.resultCode;
        return resultCode;
    }
    
    private void disconnect() {
        // End connection so application can exit
        requester.close();
        context.term();
    }

    @Override
    public boolean setNickname(String nickname) {
        ServerResponse setNicknameResponse = sendRequest(requester, requestToken, REQUEST_SET_NICKNAME, nickname);
        resultCode = setNicknameResponse.resultCode;
        
        return resultCode == RESULT_SUCCESS;
    }

    @Override
    public Optional<BufferedImage> getProfilePicture(String username) {
        // Request profile picture
        ServerResponse getUserPictureResponse = sendRequest(requester, requestToken, REQUEST_USER_PICTURE, username);
        resultCode = getUserPictureResponse.resultCode;
        
        // The response contains a boolean whether there is a profile picture present
        if(getUserPictureResponse.response.length > 0 && Boolean.parseBoolean(getUserPictureResponse.response[0])) {
            try {
                // Return the image decoded from a Base64 String
                return Optional.of(User.decodeImage(getUserPictureResponse.response[1]));
            } catch (IOException e) {
                // This should never happen but if it does, just return no image
                e.initCause(makeException());
                e.printStackTrace();
                return Optional.empty();
            }
        }
        else {
            // Return no image because there is no image
            return Optional.empty();
        }
    }

    @Override
    public boolean setProfilePicture(Optional<BufferedImage> image) {
        // Set boolean whether there is an image to false because no image is present
        if(!image.isPresent()) {
            ServerResponse setProfilePictureResponse = sendRequest(requester, requestToken, REQUEST_SET_USER_PICTURE, String.valueOf(false));
            resultCode = setProfilePictureResponse.resultCode;
            
            return resultCode == RESULT_SUCCESS;
        }
        // Image is present
        else {
            String imageString = null;
            
            try {
                // Encode image to a Base64 String to send
                imageString = User.encodeToString(image.get());
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            
            // Send profile picture
            ServerResponse setProfilePictureResponse = sendRequest(requester, requestToken, REQUEST_SET_USER_PICTURE, String.valueOf(true), imageString);
            resultCode = setProfilePictureResponse.resultCode;
            
            return resultCode == RESULT_SUCCESS;
        }
    }
    
    @Override
    public Optional<ChatRoom> createChat(String name) {
        ServerResponse createChatResponse = sendRequest(requester, requestToken, REQUEST_CREATE_CHAT_ROOM, name);
        resultCode = createChatResponse.resultCode;
        
        // Get the ID of the chat
        int id = -1;
        try {
            id = Integer.parseInt(createChatResponse.response[0]);
        }
        catch(NumberFormatException e) {
            return Optional.empty();
        }
        
        // Create and return the chat object
        return Optional.of(new ChatRoom(id, name));
        
    }
}
