package main.gui;

import java.awt.Image;

import javax.swing.JButton;
import javax.swing.JPanel;

public abstract class StatePanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    public abstract String getTitle();
    public abstract Image getImage();
    public abstract JButton getSubmitButton();
    
}
