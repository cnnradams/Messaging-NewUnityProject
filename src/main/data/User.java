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
    
    public static String encodeToString(BufferedImage image) throws IOException {
        Encoder encoder = Base64.getEncoder();
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bytesOut);
        byte[] bytes = bytesOut.toByteArray();
        bytesOut.close();
        
        return encoder.encodeToString(bytes);
    }
    
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
}
