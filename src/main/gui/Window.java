package main.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Window extends JFrame {
    
    private static final long serialVersionUID = 1L;

    private StatePanel currentPanel = null;
    
    public final LoginWindow loginWindow;
    public final MessagingWindow messagingWindow;
    
    public Window() {
        loginWindow = new LoginWindow();
        messagingWindow = new MessagingWindow();
        
        
        List<BufferedImage> icons = new ArrayList<>(4);
        
        try {
            icons.add(ImageIO.read(new File("src/resources/icon16.png")));
            icons.add(ImageIO.read(new File("src/resources/icon32.png")));
            icons.add(ImageIO.read(new File("src/resources/icon64.png")));
            icons.add(ImageIO.read(new File("src/resources/icon128.png")));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        
        this.setIconImages(icons);
        
        this.setResizable(false);
        loginWindow.setVisible(true);
        
        this.setTitle(loginWindow.getName());
        this.setVisible(true);
    }
    
    public void setStatePanel(StatePanel panel) {
        this.setTitle(panel.getTitle());
        this.setSize(panel.getWidth(), panel.getHeight());
        this.getRootPane().setDefaultButton(panel.getSubmitButton());
        
        this.add(panel);
        
        if(currentPanel != null)
            this.remove(currentPanel);
        
        currentPanel = panel;
        
        panel.setVisible(true);
        
        SwingUtilities.updateComponentTreeUI(this);
    }
}
