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

public class ZeroMQServer implements NetworkInterface {

    public final InetAddress ip;
    public final int port;
    
    private final ZMQ.Context context;
    private final ZMQ.Socket requester;
    
    private int resultCode;
    
    private String requestToken;
    
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
        ServerResponse chatSizeResponse = sendRequest(requester, requestToken, REQUEST_CHATS_ONLINE);
        if(chatSizeResponse.resultCode != RESULT_SUCCESS) {
            resultCode = chatSizeResponse.resultCode;
            return Optional.empty();
        }
        
        int onlineChats = Integer.parseInt(chatSizeResponse.response[0]);
        Set<ChatRoom> chats = new TreeSet<>();
        for(int i = 0; i < onlineChats; i++) {
            ServerResponse chatIdResponse = sendRequest(requester, requestToken, REQUEST_CHAT, String.valueOf(i));
            if(chatIdResponse.resultCode != RESULT_SUCCESS) {
                resultCode = chatIdResponse.resultCode;
                return Optional.empty();
            }
            
            int chatId = Integer.parseInt(chatIdResponse.response[0]);
            
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
            ServerResponse chatUpdateResponse = sendRequest(requester, requestToken, REQUEST_CHAT_UPDATES);
            if(chatUpdateResponse.resultCode != RESULT_SUCCESS) {
                resultCode = chatUpdateResponse.resultCode;
                return Optional.empty();
            }
            if(chatUpdateResponse.response.length != 2) {
                resultCode = chatUpdateResponse.resultCode;
                return Optional.of(updates);
            }
            ChatRoom chat = new ChatRoom(Integer.parseInt(chatUpdateResponse.response[0]), this);
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
        ServerResponse userSizeResponse = sendRequest(requester, requestToken, REQUEST_USERS_ONLINE);
        if(userSizeResponse.resultCode != RESULT_SUCCESS) {
            resultCode = userSizeResponse.resultCode;
            return Optional.empty();
        }
        
        int onlineUsers = Integer.parseInt(userSizeResponse.response[0]);
        Set<User> users = new TreeSet<>();
        for(int i = 0; i < onlineUsers; i++) {
            ServerResponse usernameResponse = sendRequest(requester, requestToken, REQUEST_USER, String.valueOf(i));
            if(usernameResponse.resultCode != RESULT_SUCCESS) {
                resultCode = usernameResponse.resultCode;
                return Optional.empty();
            }
            
            String username = usernameResponse.response[0];
            
            users.add(new User(username, this));
        }
        
        return Optional.of(users);
    }

    @Override
    public Optional<Map<User, List<Integer>>> getUserUpdates() {
        Map<User, List<Integer>> updates = new HashMap<>();
        while(true) {
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
            String nickname = "Don't care";
            try {
                nickname = getNickname(username).get();
            }
            catch(NoSuchElementException e) {}
            
            User user = new User(username, nickname);
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
            ServerResponse newMessageResponse = sendRequest(requester, requestToken, REQUEST_NEW_MESSAGE);
            if(newMessageResponse.resultCode != RESULT_SUCCESS) {
                resultCode = newMessageResponse.resultCode;
                return Optional.empty();
            }
            if(newMessageResponse.response.length != 5) {
                resultCode = newMessageResponse.resultCode;
                return Optional.of(messages);
            }
            
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
        if(message.chatRoom.isPresent()) {
            params[0] = String.valueOf(false);
            params[1] = String.valueOf(message.chatRoom.get().id);
        }
        else {
            params[0] = String.valueOf(true);
            params[1] = message.user.username;
        }
        
        params[2] = message.message;
        params[3] = message.dateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        
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
    public void keepAlive() {
        sendRequest(requester, requestToken, REQUEST_KEEP_ALIVE);
        
    }
    
    public void disconnect() {
        requester.close();
        context.term();
    }

    @Override
    public boolean setNickname(String nickname) {
        ServerResponse setNicknameResponse = sendRequest(requester, requestToken, REQUEST_SET_NICKNAME);
        resultCode = setNicknameResponse.resultCode;
        
        return resultCode == RESULT_SUCCESS;
    }

    @Override
    public BufferedImage getProfilePicture(User user) {
        ServerResponse getUserPictureResponse = sendRequest(requester, requestToken, REQUEST_USER_PICTURE, user.username);
        resultCode = getUserPictureResponse.resultCode;
        
        if(Boolean.parseBoolean(getUserPictureResponse.response[0])) {
            try {
                return User.decodeImage(getUserPictureResponse.response[1]);
            } catch (IOException e) {
                e.initCause(makeException());
                e.printStackTrace();
                return null;
            }
        }
        else {
            return null;
        }
    }

    @Override
    public boolean setProfilePicture(BufferedImage image) {
        if(image == null) {
            ServerResponse setProfilePictureResponse = sendRequest(requester, requestToken, REQUEST_SET_USER_PICTURE, String.valueOf(false));
            resultCode = setProfilePictureResponse.resultCode;
            
            return resultCode == RESULT_SUCCESS;
        }
        else {
            String imageString = null;
            
            try {
                imageString = User.encodeToString(image);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            
            ServerResponse setProfilePictureResponse = sendRequest(requester, requestToken, REQUEST_SET_USER_PICTURE, String.valueOf(true), imageString);
            resultCode = setProfilePictureResponse.resultCode;
            
            return resultCode == RESULT_SUCCESS;
        }
    }
}
