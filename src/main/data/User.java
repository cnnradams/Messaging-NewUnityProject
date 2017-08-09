package main.data;

import java.util.NoSuchElementException;

import main.networking.NetworkInterface;

public class User {

    public final String username, nickname;
    
    public User(String username, NetworkInterface network) throws NoSuchElementException {
        this(username, network.getNickname(username).get());
    }
    
    public User(String username, String nickname) {
        this.username = username;
        this.nickname = nickname;
    }
    
}
