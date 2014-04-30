//----------------------------------------------------------------------------
// $Id: HexGui.java 30 2006-10-27 05:09:12Z broderic $
//----------------------------------------------------------------------------

package hexgui.gui;

import hexgui.hex.*;
import javax.swing.*;          
import java.awt.*;
import java.awt.event.*;

/** Dialog for entering a board size. */
public final class BoardSizeDialog
{
    /** Run dialog.
        @return Board dimensions as string in format "w x h"; returns "-1 x -1" 
	if aborted. */
    public static String show(Component parent, Dimension current)
    {
        String ret;
	String value = "" + current.width + " x " + current.height;
        ret = JOptionPane.showInputDialog(parent, "Board size", value);
        return ret;
    }

    /** Make constructor unavailable; class is for namespace only. */
    private BoardSizeDialog()
    {
    }
}

