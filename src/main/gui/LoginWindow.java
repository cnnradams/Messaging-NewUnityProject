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

/**
 * Login screen frontend
 */
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

	/**
	 * Loads resources and adds all the {@code JComponent}s for this panel
	 */
	public LoginWindow() {
		Font font = null;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResource("/resources/font/RobotoMono-Medium.ttf").openStream());
		} catch (FontFormatException | IOException e1) {
			e1.printStackTrace();
		}   

		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		genv.registerFont(font);
		font = font.deriveFont(15f);
		
		this.setLayout(null);	
		this.setSize(800, 439);
		
		// Set all the settings for the window, like size, logos, etc.
		logoStartup = Toolkit.getDefaultToolkit().createImage(this.getClass().getResource("/resources/logo.gif"));	
		logoDone = Toolkit.getDefaultToolkit().createImage(this.getClass().getResource("/resources/logodone.gif"));
		loginbg = Toolkit.getDefaultToolkit().createImage(this.getClass().getResource("/resources/loginbg.png"));
		tempLogoStartup = logoStartup.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
		tempLogoDone = logoDone.getScaledInstance(getWidth(), -1, Image.SCALE_DEFAULT);
		
		// This is used for changing the logo animations. First it plays the typing animation logo, then it
		// switches to the looping logo with the blinking cursor after 4190ms.
		if (startMillis == 0) 
			startMillis = System.currentTimeMillis();
		
		// JLabel to tell you status of login
		action = new JLabel();
		action.setForeground(Color.white);
		action.setVisible(false);
		action.setFont(font);
		
		// Username field
		username = new PlaceHolderTextField("username");
		username.setBorder(BorderFactory.createMatteBorder(0,0,0,0, ColourConstants.BACKGROUND_COLOR));
		username.setBackground(ColourConstants.BACKGROUND_COLOR);
		username.setHorizontalAlignment(JTextField.LEFT);
		username.setFont(font);
		
		// Nickname field
		nickname = new PlaceHolderTextField("nickname (shown in chat)");
		nickname.setBackground(ColourConstants.BACKGROUND_COLOR);
		nickname.setFont(font);
		nickname.setBorder(BorderFactory.createMatteBorder(0,0,0,0, ColourConstants.BACKGROUND_COLOR));
		nickname.setHorizontalAlignment(JTextField.LEFT);
		
		// Submit button
		submit = new JButton("login");
		submit.setHorizontalAlignment(JTextField.LEFT);
		submit.setForeground(ColourConstants.SUBMIT_FOREGROUND_COLOR);
		submit.setBackground(ColourConstants.BACKGROUND_COLOR);
		submit.setFont(font);
        submit.setBorderPainted(false); 
        submit.setContentAreaFilled(false); 
        submit.setFocusPainted(false); 
        submit.setOpaque(false);
        
        // Logging in loading bar
		loggingIn = new JProgressBar();
		loggingIn.setVisible(false);
		loggingIn.setIndeterminate(true);
		loggingIn.setForeground(Color.white);
		loggingIn.setBackground(ColourConstants.BACKGROUND_COLOR);
		loggingIn.setBorderPainted(false);
		loggingIn.setString("Logging in...");

		submit.addActionListener(e -> {

			if (username.isPlaceHolder() || nickname.isPlaceHolder()) {
				// If username/nickname was left empty
				action.setText("Fill in all fields, please!");						
				action.setBounds(450, 130, getWidth() / 3, 30);
				action.setVisible(true);
			} else {
				// Start the visual login process
				hidden = true;
				action.setVisible(true);
				action.setBounds(450, 130, getWidth() / 3, 30);						
				username.setVisible(false);    
		    	nickname.setVisible(false);
		    	submit.setVisible(false);
				loginInfo = new User(username.getText(), nickname.getText(), null);
				loggingIn.setVisible(true);
			}
		});
		
		// Adding everything to the login JPanel
		this.setBackground(ColourConstants.BACKGROUND_COLOR);
		this.add(action);
		this.add(username);
		this.add(nickname);
		this.add(submit);
		this.add(loggingIn);
		
		
		this.addComponentListener(new ComponentListener() {
			
			// Whenever the window is resized reset the location of stuff
		    public void componentResized(ComponentEvent e) {
		    	action.setBounds(450, 130, getWidth() / 3, 30);
		    	username.setBounds(456, 158, getWidth() / 3 - 8, 24);
		    	nickname.setBounds(456, 193, getWidth() / 3 - 8, 24);
		    	loggingIn.setBounds(450, 188, getWidth() / 3, 24);
		    	submit.setBounds(450, 230, getWidth() / 8, 30);
		    	
		    	// Rescale jmessage image
		    	tempLogoStartup = logoStartup.getScaledInstance(getWidth() / 3, -1, Image.SCALE_DEFAULT);
				tempLogoDone = logoDone.getScaledInstance(getWidth() / 3, -1, Image.SCALE_DEFAULT);
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

	private Image tempLogoStartup;
	private Image tempLogoDone;
	private boolean hidden = false;
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.drawImage(loginbg, 0, 0, this);
		
		// Draw the launch gif or the idle gif
		if (tempLogoStartup != null && (System.currentTimeMillis() - startMillis) < 4190)
			g.drawImage(tempLogoStartup, 74, 174, this);							
		else if (tempLogoDone != null)
			g.drawImage(tempLogoDone, 74, 174, this);
	}
	
	@Override
	public void paint(Graphics g) {
			super.paint(g);
			
			g.setColor(ColourConstants.LOGIN_RECTANGLE_COLOR);
			((Graphics2D) g).setStroke(new BasicStroke(2));
			
			// Draw the rectangles OVER the other components
		if (!hidden) {
			g.drawRoundRect(450, 155, getWidth() / 3, 30, 10, 10);//paint border
			g.drawRoundRect(450, 190, getWidth() / 3, 30, 10, 10);//paint border
			g.setColor(ColourConstants.SUBMIT_FOREGROUND_COLOR);										//Make rounded edges for the textboxes / buttons
			g.drawRoundRect(450, 230, getWidth() / 10, 30, 10, 10);//paint border
		}
		if (hidden) {
			g.drawRoundRect(450, 188, getWidth() / 3, 24, 10, 10);
		}
	}
	
	public Optional<User> getLoginInfo() {
		return Optional.ofNullable(loginInfo);
	}

    /**
     * Reset all the settings because the user
     * went back to the login screen, either
     * cause they were kicked, server stopped, etc.
     */
	public void resetLoginInfo() {
        username.setVisible(true);    
        nickname.setVisible(true);
        submit.setVisible(true);
        
        hidden = false;
        
        username.setBorder(BorderFactory.createMatteBorder(0,0,0,0, ColourConstants.BACKGROUND_COLOR));
        username.setBackground(ColourConstants.BACKGROUND_COLOR);
        username.setHorizontalAlignment(JTextField.LEFT);
        
        nickname.setBackground(ColourConstants.BACKGROUND_COLOR);
        nickname.setBorder(BorderFactory.createMatteBorder(0,0,0,0, ColourConstants.BACKGROUND_COLOR));
        nickname.setHorizontalAlignment(JTextField.LEFT);
        
        loggingIn.setVisible(false);
        loginInfo = null;
        
        action.setLayout(null);
        action.setBounds(450, 130, getWidth() / 3, 30);
        action.setVisible(true);
        action.setBounds(450, 130, getWidth() / 3, 30);
	}

	
	@Override
	public String getTitle() {
		return "jmessage - Login | Property of NewUnityProject Team!";
	}

	@Override
	public JButton getSubmitButton() {
		return submit;
	}

	/**
	 * Sets the message to display when attempting to log in
	 * 
	 * @param text The message to display
	 */
	public void setActionText(String text) {
		action.setText(text);
	}
	
}
