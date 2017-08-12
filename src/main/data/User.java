package main.data;

import java.net.ConnectException;
import java.util.NoSuchElementException;
import java.util.Optional;

import main.networking.NetworkInterface;

public class User implements Comparable<User> {

    public final String username, nickname;
    
    public User(String username, NetworkInterface network) throws NoSuchElementException {
        this(username, getNicknameOrThrowExceptionAnd("Unknown", network.getNickname(username), network));
        
    }
    
    public User(String username, String nickname) {
        this.username = username;
        this.nickname = nickname;
    }
 
    private static String getNicknameOrThrowExceptionAnd(String fallbackNickname, Optional<String> nicknameOptional, NetworkInterface network) {
        try {
            return nicknameOptional.orElseThrow(network::makeException);
        }
        catch(ConnectException e) {
            e.printStackTrace();
            return fallbackNickname;
        }
    }
    
    @Override
    public boolean equals(Object other) {
        return other instanceof User && this.username.equals(((User)other).username);
    }

    @Override
    public int compareTo(User other) {
        return username.compareToIgnoreCase(other.username);
    }
    
    private static User me;
    public static User getMe() {
    	return me;
    }
    public static void setMe(User me) {
    	User.me = me;
    }
}
