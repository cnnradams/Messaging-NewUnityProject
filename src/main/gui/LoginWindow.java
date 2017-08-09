package main.gui;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import main.data.User;

public class LoginWindow extends StatePanel {
    
    private static final long serialVersionUID = 1L;

    private User loginInfo;
    private final JButton submit;
    private final JLabel action;
    
    
    public LoginWindow() {
        this.setSize(500, 500);
        
        action = new JLabel();
        action.setVisible(false);
        JTextField username = new JTextField("Username");
        username.setForeground(Color.GRAY);
        username.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if(username.getText().equals("Username") && username.getForeground() == Color.GRAY) {
                    username.setText("");
                    username.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(username.getText().equals("")) {
                    username.setText("Username");
                    username.setForeground(Color.GRAY);
                }
            }
        });
        
        JTextField nickname = new JTextField("Nickname");
        nickname.setForeground(Color.GRAY);
        nickname.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if(nickname.getText().equals("Nickname") && nickname.getForeground() == Color.GRAY) {
                    nickname.setText("");
                    nickname.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(nickname.getText().equals("")) {
                    nickname.setText("Nickname");
                    nickname.setForeground(Color.GRAY);
                }
            }
        });
        submit = new JButton("Submit");
        JProgressBar loggingIn = new JProgressBar();
        loggingIn.setVisible(false);
        loggingIn.setIndeterminate(true);
        loggingIn.setString("Logging in...");
        
        submit.addActionListener(e -> {
            if(username.getForeground() == Color.GRAY || nickname.getForeground() == Color.GRAY
                            || username.getText().isEmpty() || nickname.getText().isEmpty()) {
                action.setText("Please ensure username and nickname are set");
                action.setVisible(true);
            }
            else {
                action.setVisible(true);
                loginInfo = new User(username.getText(), nickname.getText());
                loggingIn.setVisible(true);
            }
        });
        
        this.add(action);
        this.add(username);
        this.add(nickname);
        this.add(submit);
        this.add(loggingIn);
    }
    
    public Optional<User> getLoginInfo() {
        return Optional.ofNullable(loginInfo);
    }
    
    @Override
    public String getTitle() {
        return "NewUnityProject - Login";
    }

    @Override
    public Image getImage() {
        return null;
    }
    
    @Override
    public JButton getSubmitButton() {
        return submit;
    }
    
    public void setActionText(String text) {
        action.setText(text);
    }
}
