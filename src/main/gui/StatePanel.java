package main.gui;

import javax.swing.JButton;
import javax.swing.JPanel;

public abstract class StatePanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    public abstract String getTitle();
    public abstract JButton getSubmitButton();
    
}
