package main.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;


/**
 * Text that deletes itself when clicked, and changes color when focused.
 * Used for login screen
 */
public class PlaceHolderTextField extends JTextField {

    private static final long serialVersionUID = 1L;
    
    /**
     * The placeholder text to display when the text field is not selected
     */
    public final String text;
    
    /**
     * Constructs the placeholder text field with a placeholder
     * 
     * @param text The placeholder text
     */
    public PlaceHolderTextField(String text) {
        this.text = text;
        this.addFocusListener(new PlaceHolderFocusListener(this));
        this.setForeground(ColourConstants.UNFOCUSED_COLOR);
        this.setText(text);
    }
    
    /**
     * Checks if the text is currently a placeholder
     * @return Is the text a placeholder?
     */
    public boolean isPlaceHolder() {
        return (this.getText().equals(text) && this.getForeground().equals(ColourConstants.UNFOCUSED_COLOR)) || this.getText().isEmpty();
    }
    
    /**
     * Turns the text back into a placeholder
     */
    public void reset() {
        setText(text);
        setForeground(ColourConstants.UNFOCUSED_COLOR);
    }

    
    @Override
    public void grabFocus() {
       super.grabFocus();
       if (getText().equals(text) && getForeground() == ColourConstants.UNFOCUSED_COLOR) {
           setText("");
           setForeground(ColourConstants.FOCUSED_COLOR);
       }
    }
    
    private class PlaceHolderFocusListener implements FocusListener {

        private final PlaceHolderTextField text;
        
        private PlaceHolderFocusListener(PlaceHolderTextField text) {
            this.text = text;
        }
        
        @Override
        public void focusGained(FocusEvent e) {
            if (text.getText().equals(text.text) && text.getForeground() == ColourConstants.UNFOCUSED_COLOR) {
                text.setText("");
                text.setForeground(ColourConstants.FOCUSED_COLOR);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (text.getText().equals("")) {
                text.setText(text.text);
                text.setForeground(ColourConstants.UNFOCUSED_COLOR);
            }
        }
        
    }
}