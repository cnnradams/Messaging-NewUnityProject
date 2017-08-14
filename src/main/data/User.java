package main.data;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.imageio.ImageIO;

import main.networking.NetworkInterface;

/**
 * Holds data for each user
 */
public class User implements Comparable<User> {

    /**
     * The unique username that identifies the user
     */
    public final String username;
    
    /**
     * The changeable nickname of the user
     */
    public final String nickname;
    
    /**
     * The user's profile picture
     */
    public final BufferedImage image;
    
    /**
     * Constructs the user by getting information from the server
     * 
     * @param username The unique username that identifies the user and used to get information about user
     * @param network The server to get the user information from
     * 
     * @throws NoSuchElementException If the username does not exist
     */
    public User(String username, NetworkInterface network) throws NoSuchElementException {
        this(username, getNicknameOrThrowExceptionAnd("Unknown", network.getNickname(username), network), network.getProfilePicture(username).orElse(null));
        
    }
    
    /**
     * Constructs the user
     * 
     * @param username The unique username that identifies the user
     * @param nickname The changeable nickname of the user
     * @param image The profile picture of the user
     */
    public User(String username, String nickname, BufferedImage image) {
        this.username = username;
        this.nickname = nickname;
        this.image = image;
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
    
    /**
     * Encodes an image to a Base64 String to send over the network
     * 
     * @param image The image to encode
     * @return The Base64 encoded String
     * 
     * @throws IOException If the image couldn't be encoded properly
     */
    public static String encodeToString(BufferedImage image) throws IOException {
        Encoder encoder = Base64.getEncoder();
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bytesOut);
        byte[] bytes = bytesOut.toByteArray();
        bytesOut.close();
        
        return encoder.encodeToString(bytes);
    }
    
    /**
     * Decodes an image from a Base64 String received over the network
     * 
     * @param imageString The String to decode
     * @return The decoded image
     * 
     * @throws IOException If the image couldn't be decoded properly
     */
    public static BufferedImage decodeImage(String imageString) throws IOException {
        BufferedImage image = null;
        byte[] imageData;
        Decoder decoder = Base64.getDecoder();
        imageData = decoder.decode(imageString);
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(imageData);
        image = ImageIO.read(bytesIn);
        bytesIn.close();
        
        return image;
    }
    
    private static User me;
    
    /**
     * Gets the user that is logged in on this client
     * 
     * @return The current user
     */
    public static User getMe() {
    	return me;
    }
    
    /**
     * Sets the current logged in user
     * 
     * @param me The current user
     */
    public static void setMe(User me) {
    	User.me = me;
    }
}
