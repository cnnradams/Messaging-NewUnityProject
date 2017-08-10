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
	private Image loginbg;
	private long startMillis = 0;

	public LoginWindow() {
		this.setLayout(null);
		this.setResizable(false);
		this.setSize(800, 439);
		logoStartup = Toolkit.getDefaultToolkit().createImage("src/resources/logo.gif");
		logoDone = Toolkit.getDefaultToolkit().createImage("src/resources/logodone.gif");
		loginbg = Toolkit.getDefaultToolkit().createImage("src/resources/loginbg.png");
		tempLogoStartup = logoStartup.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
		tempLogoDone = logoDone.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
		if (startMillis == 0) {
			startMillis = System.currentTimeMillis();
		}
		action = new JLabel();
		action.setForeground(Color.white);
		action.setVisible(false);
		PlaceHolderTextField username = new PlaceHolderTextField("  display name");
		username.setBorder(BorderFactory.createMatteBorder(2,2,2,2, new Color(105,105,105)));
		username.setHorizontalAlignment(JTextField.LEFT);
		
		PlaceHolderTextField nickname = new PlaceHolderTextField("  nickname");
		nickname.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 100, getWidth() / 4, 50);
		nickname.setBorder(BorderFactory.createMatteBorder(2,2, 2, 2, new Color(105,105,105)));
		nickname.setHorizontalAlignment(JTextField.LEFT);
		
		submit = new JButton("login");
		submit.setBounds(getWidth() / 2 - getWidth() / 8, getHeight() / 2 + 150, getWidth() / 4, 50);
		submit.setBackground( new Color(0,191,255));
		JProgressBar loggingIn = new JProgressBar();
		loggingIn.setVisible(false);
		loggingIn.setIndeterminate(true);
		loggingIn.setForeground(Color.white);
		loggingIn.setString("Logging in...");

		submit.addActionListener(e -> {
	    	
			if (username.isPlaceHolder() || nickname.isPlaceHolder()) {
				action.setText("A display name and nickname are required.");
				action.setBounds(450, 130, getWidth() / 3, 30);
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
		    	username.setBounds(450, 160, getWidth() / 3, 30);    
		    	nickname.setBounds(450, 190, getWidth() / 3, 30);
		    	loggingIn.setBounds(450, 190, getWidth() / 6, 30);
		    	submit.setBounds(450, 220, getWidth() / 6, 30);
		    	tempLogoStartup = logoStartup.getScaledInstance(getWidth() / 3, -1, Image.SCALE_DEFAULT);
				tempLogoDone = logoDone.getScaledInstance(getWidth() / 3, -1, Image.SCALE_DEFAULT);
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
	private void setResizable(boolean b) {
		// TODO Auto-generated method stub
		
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
		g.drawImage(loginbg, 0, 0, this);
		if (tempLogoStartup != null && (System.currentTimeMillis() - startMillis) < 4190)
			g.drawImage(tempLogoStartup, 74, 174, this);
		else if (tempLogoDone != null)
			g.drawImage(tempLogoDone, 74, 174, this);

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
