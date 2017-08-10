package main.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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
	private Image logoStartup;
	private Image logoDone;
	private long startMillis = 0;

	public LoginWindow() {
		this.setLayout(null);
		
		System.out.println("b");
		this.setSize(500, 500);
		logoStartup = Toolkit.getDefaultToolkit().createImage("src/resources/logo.gif");
		logoDone = Toolkit.getDefaultToolkit().createImage("src/resources/logodone.gif");
		tempLogoStartup = logoStartup.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
		tempLogoDone = logoDone.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
		if (startMillis == 0) {
			startMillis = System.currentTimeMillis();
		}
		action = new JLabel();
		action.setVisible(false);
		JTextField username = new JTextField("Username");
		username.setForeground(Color.GRAY);
		username.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 50, getWidth() / 4, 50);
		username.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (username.getText().equals("Username") && username.getForeground() == Color.GRAY) {
					username.setText("");
					username.setForeground(Color.BLACK);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (username.getText().equals("")) {
					username.setText("Username");
					username.setForeground(Color.GRAY);
				}
			}
		});

		JTextField nickname = new JTextField("Nickname");
		nickname.setForeground(Color.GRAY);
		nickname.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 100, getWidth() / 4, 50);
		nickname.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (nickname.getText().equals("Nickname") && nickname.getForeground() == Color.GRAY) {
					nickname.setText("");
					nickname.setForeground(Color.BLACK);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (nickname.getText().equals("")) {
					nickname.setText("Nickname");
					nickname.setForeground(Color.GRAY);
				}
			}
		});
		submit = new JButton("Submit");
		submit.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 150, getWidth() / 4, 50);
		JProgressBar loggingIn = new JProgressBar();
		loggingIn.setVisible(false);
		loggingIn.setIndeterminate(true);
		loggingIn.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 150, getWidth() / 4, 50);
		loggingIn.setString("Logging in...");

		submit.addActionListener(e -> {
			username.setVisible(false);    
	    	nickname.setVisible(false);
	    	submit.setVisible(false);
			if (username.getForeground() == Color.GRAY || nickname.getForeground() == Color.GRAY
					|| username.getText().isEmpty() || nickname.getText().isEmpty()) {
				action.setText("Please ensure username and nickname are set");
				action.setVisible(true);
			} else {
				action.setVisible(true);
				loginInfo = new User(username.getText(), nickname.getText());
				loggingIn.setVisible(true);
			}
		});
		this.setBackground(new Color(60, 60, 60));
		this.add(action);
		this.add(username);
		this.add(nickname);
		this.add(submit);
		this.add(loggingIn);
		this.addComponentListener(new ComponentListener() {
		    public void componentResized(ComponentEvent e) {
		    	username.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 50, getWidth() / 4, 50);    
		    	nickname.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 100, getWidth() / 4, 50);
		    	loggingIn.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 150, getWidth() / 4, 50);
		    	submit.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 150, getWidth() / 4, 50);
		    	tempLogoStartup = logoStartup.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
				tempLogoDone = logoDone.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
		    }

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	Image tempLogoStartup;
	Image tempLogoDone;

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int x = getWidth() / 2 - tempLogoStartup.getWidth(null) / 2;
		if (tempLogoStartup != null && (System.currentTimeMillis() - startMillis) < 4190)
			g.drawImage(tempLogoStartup, x, 0, this);
		else if (tempLogoDone != null)
			g.drawImage(tempLogoDone, x, 0, this);

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
