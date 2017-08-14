package main.gui;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Panel that fills the {@code Window}
 */
public abstract class StatePanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Gets the title that should be set for the window
     * 
     * @return The title to set
     */
    public abstract String getTitle();
    
    /**
     * Gets the button that should be set as the default button that is submitted on enter press
     * 
     * @return The button to set
     */
    public abstract JButton getSubmitButton();
    
}
