package main.gui;

import javax.swing.JFrame;

public class Window extends JFrame {
    
    private static final long serialVersionUID = 1L;

    private StatePanel currentPanel = null;
    
    public final LoginWindow loginWindow;
    public final MessagingWindow messagingWindow;
    
    public Window() {
        loginWindow = new LoginWindow();
        messagingWindow = new MessagingWindow();
        
        loginWindow.setVisible(true);
        
        this.setTitle(loginWindow.getName());
        this.setVisible(true);
    }
    
    public void setStatePanel(StatePanel panel) {
        this.setTitle(panel.getTitle());
        this.setIconImage(panel.getImage());
        this.setSize(panel.getWidth(), panel.getHeight());
        
        this.add(panel);
        
        if(currentPanel != null)
            this.remove(currentPanel);
        
        currentPanel = panel;
        
        panel.setVisible(true);
    }
}
