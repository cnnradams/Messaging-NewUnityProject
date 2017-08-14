package main.gui;

import java.awt.Color;

/**
 * All the different colours used in the window
 */
public class ColourConstants {
	/**
	 * Background colour for all windows (a dark gray)
	 */
	public static final Color BACKGROUND_COLOR = new Color(60, 60, 60);
	/**
	 * Colour for focused text on text fields
	 */
	public static final Color FOCUSED_COLOR = new Color(160, 160, 160);
	/**
	 * Colour for unfocused / placeholder text on text fields
	 */
	public static final Color UNFOCUSED_COLOR = Color.GRAY;
	/**
	 * Colour for submit button, also an accent colour (cyan)
	 */
	public static final Color SUBMIT_FOREGROUND_COLOR = new Color(0, 255, 255);
	/**
	 * Colour for username and nickname text boxes on login screen
	 */
	public static final Color LOGIN_RECTANGLE_COLOR = new Color(78, 78, 78);
	/**
	 * Background colour for the messaging pane in chats
	 */
	public static final Color MESSAGING_PANE_BACKGROUND_COLOR = new Color(40, 40, 40);
	/**
	 * Background colour for new messages in current chat
	 */
	public static final Color NEW_MESSAGES_BACKGROUND_COLOR = new Color(90, 90, 90);
	/**
	 * Colour for messages (pure white)
	 */
	public static final Color MESSAGE_COLOR = new Color(255, 255, 255);
	/**
	 * Colour for message info (who was it sent by, what time was it sent)
	 */
	public static final Color MESSAGE_INFO_COLOR = new Color(120, 120, 120);

}
