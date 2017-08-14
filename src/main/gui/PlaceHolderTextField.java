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
    
    public final String text;
    
    public PlaceHolderTextField(String text) {
        this.text = text;
        this.addFocusListener(new PlaceHolderFocusListener(this));
        this.setForeground(ColorConstants.UNFOCUSED_COLOR);
        this.setText(text);
    }
    
    /**
     * Checks if the text is currently a placeholder
     * @return Is the text a placeholder?
     */
    public boolean isPlaceHolder() {
        return (this.getText().equals(text) && this.getForeground().equals(ColorConstants.UNFOCUSED_COLOR)) || this.getText().isEmpty();
    }
    
    /**
     * Turns the text back into a placeholder
     */
    public void reset() {
        setText(text);
        setForeground(ColorConstants.UNFOCUSED_COLOR);
    }

    
    @Override
    public void grabFocus() {
       super.grabFocus();
       if (getText().equals(text) && getForeground() == ColorConstants.UNFOCUSED_COLOR) {
           setText("");
           setForeground(ColorConstants.FOCUSED_COLOR);
       }
    }
    
    class PlaceHolderFocusListener implements FocusListener {

        public final PlaceHolderTextField text;
        
        public PlaceHolderFocusListener(PlaceHolderTextField text) {
            this.text = text;
        }
        
        @Override
        public void focusGained(FocusEvent e) {
            if (text.getText().equals(text.text) && text.getForeground() == ColorConstants.UNFOCUSED_COLOR) {
                text.setText("");
                text.setForeground(ColorConstants.FOCUSED_COLOR);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (text.getText().equals("")) {
                text.setText(text.text);
                text.setForeground(ColorConstants.UNFOCUSED_COLOR);
            }
        }
        
    }
}