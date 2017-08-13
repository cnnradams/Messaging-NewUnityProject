package main.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
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
	private final PlaceHolderTextField username;
	private final PlaceHolderTextField nickname;
	private final JProgressBar loggingIn;

	public LoginWindow() {
		Font font = null;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResource("/resources/font/RobotoMono-Medium.ttf").openStream());
		} catch (FontFormatException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}   

		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		genv.registerFont(font);
		font = font.deriveFont(15f);
		this.setLayout(null);
		this.setSize(800, 439);																//set all the settings for the window,
		logoStartup = Toolkit.getDefaultToolkit().createImage(this.getClass().getResource("/resources/logo.gif"));	//like size, logos, etc.
		logoDone = Toolkit.getDefaultToolkit().createImage(this.getClass().getResource("/resources/logodone.gif"));
		loginbg = Toolkit.getDefaultToolkit().createImage(this.getClass().getResource("/resources/loginbg.png"));
		tempLogoStartup = logoStartup.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
		tempLogoDone = logoDone.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
		if (startMillis == 0) {
			startMillis = System.currentTimeMillis();										//This is used for changing the logo animations. First it plays the typing animation logo, then it
		}																					//switches to the looping logo with the blinking cursor after 4190ms.
		action = new JLabel();
		action.setForeground(Color.white);
		action.setVisible(false);
		
		username = new PlaceHolderTextField("username");									//Username input field
		username.setBorder(BorderFactory.createMatteBorder(0,0,0,0, new Color(105,105,105)));
		username.setFocusedColor(new Color(160,160,160));
		username.setBackground(new Color(60,60,60));
		username.setHorizontalAlignment(JTextField.LEFT);
		username.setFont(font);
		nickname = new PlaceHolderTextField("nickname (shown in chat)");					//Nickname input field
		nickname.setFocusedColor(new Color(160,160,160));
		nickname.setBackground(new Color(60,60,60));
		nickname.setFont(font);
		nickname.setBorder(BorderFactory.createMatteBorder(0,0,0,0, new Color(105,105,105)));
		nickname.setHorizontalAlignment(JTextField.LEFT);
		
		submit = new JButton("login");														//login button
		submit.setHorizontalAlignment(JTextField.LEFT);
		submit.setForeground(new Color(0, 255, 255));
		submit.setBackground(new Color(60, 60, 60));
		submit.setFont(font);
        submit.setBorderPainted(false); 
        submit.setContentAreaFilled(false); 
        submit.setFocusPainted(false); 
        submit.setOpaque(false);
        
		loggingIn = new JProgressBar();														//progress bar for logging in
		loggingIn.setVisible(false);
		loggingIn.setIndeterminate(true);
		loggingIn.setForeground(Color.white);
		action.setFont(font);
		loggingIn.setBackground(new Color(60,60,60));
		loggingIn.setBorderPainted(false);
		loggingIn.setString("Logging in...");

		submit.addActionListener(e -> {
	    	
			if (username.isPlaceHolder() || nickname.isPlaceHolder()) {
				action.setText("Fill in all fields, please!");						//if username/nickname was left empty
				action.setBounds(450, 130, getWidth() / 3, 30);
				action.setVisible(true);
			} else {
				hidden = true;
				action.setVisible(true);
				action.setBounds(450, 130, getWidth() / 3, 30);						//Start the visual login process
				username.setVisible(false);    
		    	nickname.setVisible(false);
		    	submit.setVisible(false);
				loginInfo = new User(username.getText(), nickname.getText(), null);
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
		    	action.setBounds(450, 130, getWidth() / 3, 30);
		    	username.setBounds(456, 158, getWidth() / 3 - 8, 24);    
		    	nickname.setBounds(456, 193, getWidth() / 3 - 8, 24);
		    	loggingIn.setBounds(450, 188, getWidth() / 3, 24);					//Update the bounds on window resize
		    	submit.setBounds(450, 230, getWidth() / 8, 30);
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
	@SuppressWarnings("unused")
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
			g.drawImage(tempLogoStartup, 74, 174, this);							//Do all the logo stuff mentioned earlier
		else if (tempLogoDone != null)
			g.drawImage(tempLogoDone, 74, 174, this);
		//g.setColor(new Color(78,78,78));
		//g.drawRoundRect(450, 160, getWidth() / 3, 30, 20, 20);//paint border
		

	}
	@Override
	public void paint(Graphics g) {
			super.paint(g);
			g.setColor(new Color(78,78,78));
			((Graphics2D) g).setStroke(new BasicStroke(2));
		if (!hidden) {
			g.drawRoundRect(450, 155, getWidth() / 3, 30, 10, 10);//paint border
			g.drawRoundRect(450, 190, getWidth() / 3, 30, 10, 10);//paint border
			g.setColor(new Color(0, 255, 255));										//Make rounded edges for the textboxes / buttons
			g.drawRoundRect(450, 230, getWidth() / 10, 30, 10, 10);//paint border
		}
		if (hidden) {
			g.drawRoundRect(450, 188, getWidth() / 3, 24, 10, 10);
		}
	}
	public Optional<User> getLoginInfo() {
		return Optional.ofNullable(loginInfo);
	}

	public void resetLoginInfo() {
        /*
         * Reset all the settings because the user
         * went back to the login screen, either
         * cause they were kicked, server stopped, etc.
         */
        username.setVisible(true);    
        nickname.setVisible(true);
        submit.setVisible(true);
        
        hidden = false;
        action.setBounds(450, 130, getWidth() / 3, 30);
        username.setBorder(BorderFactory.createMatteBorder(0,0,0,0, new Color(105,105,105)));
        username.setFocusedColor(new Color(160,160,160));
        username.setBackground(new Color(60,60,60));
        username.setHorizontalAlignment(JTextField.LEFT);
        nickname.setFocusedColor(new Color(160,160,160));
        nickname.setBackground(new Color(60,60,60));
        nickname.setBorder(BorderFactory.createMatteBorder(0,0,0,0, new Color(105,105,105)));
        nickname.setHorizontalAlignment(JTextField.LEFT);
        loggingIn.setVisible(false);
        loginInfo = null;
        action.setLayout(null);
        action.setBounds(450, 130, getWidth() / 3, 30);
        action.setVisible(true);
	}

	
	@Override
	public String getTitle() {
		return "jmessage - Login | Property of NewUnityProject Team!";
	}

	@Override
	public JButton getSubmitButton() {
		return submit;
	}

	public void setActionText(String text) {
		action.setText(text);								//used for telling the user stuff on the login screen
	}
	
}
