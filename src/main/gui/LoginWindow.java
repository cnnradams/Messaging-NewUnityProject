package main.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Optional;

import javax.swing.BorderFactory;
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
		PlaceHolderTextField username = new PlaceHolderTextField("Username");
		username.setBorder(BorderFactory.createMatteBorder(2,2,2,2, new Color(105,105,105)));
		username.setHorizontalAlignment(JTextField.CENTER);
		
		PlaceHolderTextField nickname = new PlaceHolderTextField("Nickname");
		nickname.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 100, getWidth() / 4, 50);
		nickname.setBorder(BorderFactory.createMatteBorder(2,2, 2, 2, new Color(105,105,105)));
		nickname.setHorizontalAlignment(JTextField.CENTER);
		
		submit = new JButton("Submit");
		submit.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 150, getWidth() / 4, 50);
		submit.setBackground( new Color(0,191,255));
		JProgressBar loggingIn = new JProgressBar();
		loggingIn.setVisible(false);
		loggingIn.setIndeterminate(true);
		loggingIn.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 150, getWidth() / 4, 50);
		loggingIn.setBackground( new Color(0,191,255));
		loggingIn.setString("Logging in...");

		submit.addActionListener(e -> {
	    	
			if (username.isPlaceHolder() || nickname.isPlaceHolder()) {
				action.setText("Please ensure username and nickname are set");
				action.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 5, getWidth() / 4, 50);
				action.setVisible(true);
			} else {
				hidden = true;
				action.setVisible(true);
				action.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 90, getWidth() / 4, 50);
				username.setVisible(false);    
		    	nickname.setVisible(false);
		    	submit.setVisible(false);
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
		    	action.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 5, getWidth() / 4, 50);
		    	username.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 40, getWidth() / 4, 50);    
		    	nickname.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 100, getWidth() / 4, 50);
		    	loggingIn.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 160, getWidth() / 4, 50);
		    	submit.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 160, getWidth() / 4, 50);
		    	tempLogoStartup = logoStartup.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
				tempLogoDone = logoDone.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
				rectX = getWidth() / 2 - getWidth() / 8 - 25;
				rectY = getHeight() / 2 + 5;
				rectW = getWidth() / 4 + 50;
				rectH = 240;
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
	int rectX = 0;
	int rectY = 150;
	int rectW = 100;
	int rectH = 100;
	boolean hidden = false;
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(!hidden) {
			g.setColor(Color.WHITE);
			g.fillRoundRect(rectX, rectY, rectW, rectH, 20, 20);//paint background
			g.setColor(new Color(105,105,105));
			g.drawRoundRect(rectX + 10, rectY + 10, rectW - 20, rectH - 20, 20, 20);//paint border
		}
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
