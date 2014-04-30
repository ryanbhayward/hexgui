//----------------------------------------------------------------------------
// $Id$ 
//----------------------------------------------------------------------------

package hexgui.gui;

import hexgui.hex.HexColor;
import hexgui.game.Node;

import java.util.*;
import javax.swing.*;          
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

//----------------------------------------------------------------------------

public final class GuiToolBar 
    implements ActionListener
{
    public GuiToolBar(ActionListener listener, GuiPreferences preferences)
    {
	m_preferences = preferences;
	m_listener = listener;

	m_toolBar = new JToolBar();
	createToolBar();

	setVisible(m_preferences.getBoolean("gui-toolbar-visible"));
    }

    public JToolBar getJToolBar()
    {
	return m_toolBar;
    }

    public void setVisible(boolean visible)
    {
	m_toolBar.setVisible(visible);
	m_preferences.put("gui-toolbar-visible", visible);
    }

    public void enableStopButton()
    {
	m_stop.setEnabled(true);
    }
    
    public void disableStopButton()
    {
	m_stop.setEnabled(false);
    }
   
    public void setToMove(String string)
    {
        if (string.equals("black")) {
            if (m_black_to_play != null)
                m_tomove.setIcon(m_black_to_play);
            else 
                m_tomove.setText("black");
        }
        else if (string.equals("white")) {
            if (m_white_to_play != null)
                m_tomove.setIcon(m_white_to_play);
            else 
                m_tomove.setText("white");
        }
        else
            System.out.println("Unknown value: '" + string + "'");
    }

    /** Returns the click context--what type of move does the user
     *  wish to make?  
     *  @return "black" if black setup icon is selected.
     *          "white" if white setup icon is selected.
     *          "play"  otherwise. 
     */
    public String getClickContext()
    {
        if (m_setup_black.isSelected())
            return "black";
        else if (m_setup_white.isSelected())
            return "white";
        return "play";
    }

    public void setProgramConnected(boolean f)
    {
	m_play.setEnabled(f);
        m_hint.setEnabled(f);
        m_solve.setEnabled(f);
        m_program_options.setEnabled(f);
    }
    
    public void updateButtonStates(Node node)
    {
	m_beginning.setEnabled(node.getParent() != null);
	m_back10.setEnabled(node.getParent() != null);
	m_back.setEnabled(node.getParent() != null);

	m_forward.setEnabled(node.getChild() != null);
	m_forward10.setEnabled(node.getChild() != null);
	m_end.setEnabled(node.getChild() != null);

	m_up.setEnabled(node.getNext() != null);
	m_down.setEnabled(node.getPrev() != null);

        m_swap.setEnabled(node.isSwapAllowed());
    }

    public void lockToolbar()
    {
        m_new.setEnabled(false);
        m_load.setEnabled(false);
        m_save.setEnabled(false);

        m_setup_black.setEnabled(false);
        m_setup_white.setEnabled(false);

	m_beginning.setEnabled(false);
	m_back10.setEnabled(false);
	m_back.setEnabled(false);

	m_forward.setEnabled(false);
	m_forward10.setEnabled(false);
	m_end.setEnabled(false);

	m_up.setEnabled(false);
	m_down.setEnabled(false);

        m_swap.setEnabled(false);

	m_play.setEnabled(false);

        m_tomove.setEnabled(false);

        m_hint.setEnabled(false);
        m_solve.setEnabled(false);
        m_program_options.setEnabled(false);

        enableStopButton();
    }

    public void unlockToolbar(Node node)
    {
        disableStopButton();

	m_play.setEnabled(true);

        m_tomove.setEnabled(true);

        m_setup_black.setEnabled(true);
        m_setup_white.setEnabled(true);

        m_new.setEnabled(true);
        m_load.setEnabled(true);
        m_save.setEnabled(true);

        m_hint.setEnabled(true);
        m_solve.setEnabled(true);
        m_program_options.setEnabled(true);
       
        updateButtonStates(node);
    }

    //----------------------------------------------------------------------

    private void createToolBar()
    {
	m_new = makeButton("hexgui/images/filenew.png", 
                           "newgame",
                           "New Game",
                           "New");
        m_toolBar.add(m_new);

        m_load = makeButton("hexgui/images/fileopen.png", 
                            "loadgame",
                            "Load Game",
                            "Load");
	m_toolBar.add(m_load);
        
        m_save = makeButton("hexgui/images/filesave2.png", 
                            "savegame",
                            "Save Game",
                            "Save");
        m_toolBar.add(m_save);
        
	m_toolBar.addSeparator();

        m_setup_black = makeToggleButton("hexgui/images/setup-black.png",
                                         "setup-black", 
                                         "Setup Black Stones",
                                         "Setup Black");
        m_toolBar.add(m_setup_black);

        m_setup_white = makeToggleButton("hexgui/images/setup-white.png",
                                         "setup-white", 
                                         "Setup White Stones",
                                         "Setup White");
        m_toolBar.add(m_setup_white);

	m_toolBar.addSeparator();
	
	m_beginning = makeButton("hexgui/images/beginning.png", 
				 "game_beginning",
				 "Game Start",
				 "Start");
	m_toolBar.add(m_beginning);
	
	m_back10 = makeButton("hexgui/images/backward10.png", 
			      "game_backward10",
			      "Go back ten moves",
			      "Back10");
	m_toolBar.add(m_back10);
    
	m_back = makeButton("hexgui/images/back.png", 
			    "game_back",
			    "Go back one move",
			    "Back");
	m_toolBar.add(m_back);

	m_forward = makeButton("hexgui/images/forward.png", 
			     "game_forward",
			     "Go forward one move",
			     "Forward");
	m_toolBar.add(m_forward);

	m_forward10 = makeButton("hexgui/images/forward10.png", 
				 "game_forward10",
				 "Go forward ten moves",
				 "Forward10");
	m_toolBar.add(m_forward10);

	m_end = makeButton("hexgui/images/end.png", 
			   "game_end",
			   "Go to end of game",
			   "End");
	m_toolBar.add(m_end);
    
	m_toolBar.addSeparator();

	m_up = makeButton("hexgui/images/up.png",
			  "game_up",
			  "Explore previous variation",
			  "Up");
	m_toolBar.add(m_up);
	
	m_down = makeButton("hexgui/images/down.png",
			  "game_down",
			  "Explore next variation",
			  "Down");
	m_toolBar.add(m_down);

	m_toolBar.addSeparator();

	m_swap = makeButton("hexgui/images/swap.png",
			    "game_swap",
			    "Play swap move",
			    "Swap");
	m_toolBar.add(m_swap);
	m_swap.setEnabled(false);

	m_toolBar.addSeparator();

	m_play = makeButton("hexgui/images/play.png",
			    "genmove",
			    "Generate computer move",
			    "Play");
	m_toolBar.add(m_play);
	m_play.setEnabled(false);

	m_stop = makeButton("hexgui/images/stop.png",
			    "stop",
			    "Stop Action",
			    "Stop");
	m_toolBar.add(m_stop);
	disableStopButton();

        m_toolBar.addSeparator();

	String pref = m_preferences.get("first-move-color");
	m_tomove = makeButton("hexgui/images/black-24x24.png",
                              "toggle_tomove",
                              "Color of player to move",
                              pref);
        m_tomove.setText("");
        {
            URL imageURL = getURL("hexgui/images/black-24x24.png");
            m_black_to_play = new ImageIcon(imageURL, "black");
        }
        {
            URL imageURL = getURL("hexgui/images/white-24x24.png");
            m_white_to_play = new ImageIcon(imageURL, "white");
        }
        setToMove(pref);

	m_toolBar.add(m_tomove);

        m_toolBar.addSeparator();

        m_hint = makeButton("hexgui/images/hint-24x24.png",
                            "show_consider_set",
                            "Show provably inferior cells", "Hint");
        m_toolBar.add(m_hint);
        m_hint.setEnabled(false);

        m_solve = makeButton("hexgui/images/solve.png", "solve_state",
                             "Solve State with DFPN", "Solve");
        m_toolBar.add(m_solve);
        m_solve.setEnabled(false);

        m_program_options = makeButton("hexgui/images/program-options.png",
                                       "program_options",
                                       "Program Options", "Options");
        m_toolBar.add(m_program_options);
        m_program_options.setEnabled(false);
    }

    private JButton makeButton(String imageFile, String actionCommand,
			      String tooltip, String altText)
    {
	JButton button = new JButton();
        button.setFocusable(false);
	button.addActionListener(m_listener);
	button.setActionCommand(actionCommand);
	button.setToolTipText(tooltip);
        addIconToButton(button, imageFile, altText); 
	return button;
    }

    private JToggleButton makeToggleButton(String imageFile, String actionCommand,
                                           String tooltip, String altText)
    {
	JToggleButton button = new JToggleButton();
        button.setFocusable(false);
	button.addActionListener(this);
	button.setActionCommand(actionCommand);
	button.setToolTipText(tooltip);
        addIconToButton(button, imageFile, altText); 
	return button;
    }
    
    private void addIconToButton(AbstractButton button, 
                                 String imageFile, 
                                 String altText)
    {
        URL imageURL = getURL(imageFile);
        if (imageURL != null) {
	    button.setIcon(new ImageIcon(imageURL, altText));
	} else {
	    button.setText(altText);
	    System.out.println("*** Resource not found: " + imageFile);
	}
    }

    private URL getURL(String filename)
    {
        URL url = null;
        if (filename != null) {
            ClassLoader classLoader = getClass().getClassLoader();
            url = classLoader.getResource(filename);
        }
        return url;
    }

    //----------------------------------------------------------------------

    public void actionPerformed(ActionEvent e) 
    {
        String cmd = e.getActionCommand();
        if (cmd.equals("setup-black")) {
            if (m_setup_white.isSelected())
                m_setup_white.setSelected(false);
        } else if (cmd.equals("setup-white")) {
            if (m_setup_black.isSelected())
                m_setup_black.setSelected(false);
        } else {
            System.out.println("GuiToolBar: Unknown action command '" + cmd + "'");
        }
    }

    //----------------------------------------------------------------------

    private GuiPreferences m_preferences;
    private JToolBar m_toolBar;
    private ActionListener m_listener;

    private JButton m_new, m_load, m_save;

    private JToggleButton m_setup_black, m_setup_white;

    private JButton m_beginning, m_back10, m_back;
    private JButton m_forward, m_forward10, m_end;
    private JButton m_up, m_down;
    private JButton m_play, m_stop, m_swap;

    private JButton m_tomove;
    private ImageIcon m_black_to_play, m_white_to_play;

    private JButton m_solve, m_hint, m_program_options;
}

//----------------------------------------------------------------------------
