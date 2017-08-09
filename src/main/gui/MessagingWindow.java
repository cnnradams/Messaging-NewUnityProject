package main.gui;

import java.awt.Image;

import javax.swing.JButton;

public class MessagingWindow extends StatePanel {
    
    private static final long serialVersionUID = 1L;
    
    public MessagingWindow() {
        this.setSize(848, 477);
    }
    
    @Override
    public String getTitle() {
        return "NewUnityProject - Messaging";
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public JButton getSubmitButton() {
        return null;
    }
}
