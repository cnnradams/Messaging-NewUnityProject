package main.gui;

import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import main.data.User;

public class LoginWindow extends StatePanel {
    
    private static final long serialVersionUID = 1L;

    private User loginInfo;
    private final JButton submit;
    
    public LoginWindow() {
        this.setSize(500, 500);
        
        JTextField username = new JTextField("Username");
        username.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if(username.getText().equals("Username")) {
                    username.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(username.getText().equals("")) {
                    username.setText("Username");
                }
            }
        });
        
        JTextField nickname = new JTextField("Nickname");
        nickname.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if(nickname.getText().equals("Nickname")) {
                    nickname.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(nickname.getText().equals("")) {
                    nickname.setText("Nickname");
                }
            }
        });
        submit = new JButton("Submit");
        JProgressBar loggingIn = new JProgressBar();
        loggingIn.setVisible(false);
        loggingIn.setIndeterminate(true);
        loggingIn.setString("Logging in...");
        
        submit.addActionListener(e -> {
            loginInfo = new User(username.getText(), nickname.getText());
            loggingIn.setVisible(true);
        });
        
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
}
