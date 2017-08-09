package main;

import java.util.Optional;

import main.data.User;
import main.gui.Window;
import main.networking.MockServer;
import main.networking.NetworkInterface;

public class Main {
    
    public static void main(String[] args) {
        Window window = new Window();
        window.setStatePanel(window.loginWindow);
        window.setVisible(true);
        window.toFront();
        
        NetworkInterface network = new MockServer();
        
        ((MockServer)network).loginTime = 5000;
        
        Optional<User> loginInfo = null;
        while(!(loginInfo = window.loginWindow.getLoginInfo()).isPresent()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        network.login(loginInfo.get());
        
        window.setStatePanel(window.messagingWindow);
        
        while(window.isShowing()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.exit(0);
    }
    
}
