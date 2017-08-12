package main.gui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class PlaceHolderTextField extends JTextField {

    private static final long serialVersionUID = 1L;
    
    public final String text;
    
    private Color unfocusedColor = Color.GRAY;
    private Color focusedColor = Color.WHITE;
    
    public PlaceHolderTextField(String text) {
        this.text = text;
        this.addFocusListener(new PlaceHolderFocusListener(this));
        this.setForeground(unfocusedColor);
        this.setText(text);
    }
    
    public boolean isPlaceHolder() {
        return (this.getText().equals(text) && this.getForeground().equals(unfocusedColor)) || this.getText().isEmpty();
    }
    
    public void reset() {
        setText(text);
        setForeground(unfocusedColor);
    }
    
    public void setFocusedColor(Color c) {
        focusedColor = c;
    }
    
    public Color getFocusedColor() {
        return focusedColor;
    }
    
    public void setUnfocusedColor(Color c) {
        unfocusedColor = c;
    }
    
    public Color getUnfocusedColor() {
        return unfocusedColor;
    }
    
    @Override
    public void grabFocus() {
       super.grabFocus();
       if (getText().equals(text) && getForeground() == unfocusedColor) {
           setText("");
           setForeground(focusedColor);
       }
    }
    
    class PlaceHolderFocusListener implements FocusListener {

        public final PlaceHolderTextField text;
        
        public PlaceHolderFocusListener(PlaceHolderTextField text) {
            this.text = text;
        }
        
        @Override
        public void focusGained(FocusEvent e) {
            if (text.getText().equals(text.text) && text.getForeground() == text.unfocusedColor) {
                text.setText("");
                text.setForeground(focusedColor);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (text.getText().equals("")) {
                text.setText(text.text);
                text.setForeground(unfocusedColor);
            }
        }
        
    }
}