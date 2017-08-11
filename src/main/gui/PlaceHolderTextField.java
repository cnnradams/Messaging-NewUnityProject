package main.gui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class PlaceHolderTextField extends JTextField {

    private static final long serialVersionUID = 1L;
    
    public final String text;
    public final Color forground = Color.GRAY;
    
    public PlaceHolderTextField(String text) {
        this.text = text;
        this.addFocusListener(new PlaceHolderFocusListener(this));
        this.setForeground(forground);
        this.setText(text);
    }
    
    public boolean isPlaceHolder() {
        return (this.getText().equals(text) && this.getForeground().equals(Color.GRAY)) || this.getText().isEmpty();
    }
    
    public void reset() {
        setText(text);
        setForeground(forground);
    }
    
    class PlaceHolderFocusListener implements FocusListener {

        public final PlaceHolderTextField text;
        
        public PlaceHolderFocusListener(PlaceHolderTextField text) {
            this.text = text;
        }
        
        @Override
        public void focusGained(FocusEvent e) {
            if (text.getText().equals(text.text) && text.getForeground() == text.forground) {
                text.setText("");
                text.setForeground(Color.BLACK);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (text.getText().equals("")) {
                text.setText(text.text);
                text.setForeground(text.forground);
            }
        }
    }
}
